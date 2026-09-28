package com.sfms.mf08.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF08 — luồng end-to-end thật (Bước 2, chứng minh migration + entity + service wiring chạy
 * đúng trên Postgres 16 thật, KHÔNG mock): đăng ký mẫu (UC-069) → hủy trước hạn bị chặn (BR-111)
 * → hủy hợp lệ sau 24h (UC-030) → bàn giao lô mẫu bị chặn vì {@code IncidentStatusPort} còn là
 * stub (M08-D08, §2.5 mục 2 TDS-MF08 — MF11 chưa merge).
 *
 * <p>⚠️ Phạm vi: đây KHÔNG phải 47 TC của {@code TestSpec-MF08_Food_Sample_Retention.md} — đúng
 * tinh thần MF06/MF07 đã làm ở Bước 2 (dừng có chủ đích, ghi rõ, không claim xong). Viết đủ bộ
 * TestSpec là việc tiếp theo.
 */
class FoodSampleFlowIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";       // service-caterers, SCH-HN-0042 (V900)
    private static final String MEDICAL_STAFF = "U-0002";  // medical-staff, SCH-HN-0042 (V900)
    private static final String SCHOOL_CODE = "SCH-HN-0042";
    private static final String INSPECTION_CODE = "INSP-MF08-IT-L";

    @BeforeEach
    void seedPassedInspectionSession() {
        // Phiên kiểm thực PASSED 3/3 — cổng BR-105. Dùng lại dish 'com-trang' (đã seed ở
        // V9__seed_mf05.sql) + partner CAT-001 (V900) để không phải dựng thêm dữ liệu module khác.
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                                                  step1, step2, step3, overall, core_temp, inspector_user_id)
                SELECT gen_random_uuid(), ?, s.id, p.id, DATE '2026-07-30', 'LUNCH',
                       'PASSED', 'PASSED', 'PASSED', 'PASSED', 76.00, u.id
                FROM schools s, partners p, user_accounts u
                WHERE s.code = ? AND p.code = 'CAT-001' AND u.code = ? AND u.school_id = s.id
                ON CONFLICT (school_id, code) DO NOTHING
                """, INSPECTION_CODE, SCHOOL_CODE, CATERER);
    }

    @Test
    void registerDisposeAndHandOverFlow() throws Exception {
        String caterer = authHeaderFor(CATERER);
        String medicalStaff = authHeaderFor(MEDICAL_STAFF);

        // 1) Đăng ký mẫu (UC-069) — multipart payload + evidencePhotos[].
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "com-trang");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa — 10:15");
        payload.put("servings", 1850);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox có nắp kín");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("storageLocation", "Tủ lưu mẫu số 1 — ngăn 1");

        MockMultipartFile payloadPart = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3});

        MvcResult registerResult = mvc.perform(multipart("/api/v1/samples")
                        .file(payloadPart).file(photo)
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(registerResult.getResponse().getStatus()).isEqualTo(201);
        JsonNode registered = parse(registerResult);
        String code = registered.get("id").asText();
        assertThat(code).startsWith("SMP-20260730");
        assertThat(registered.get("status").asText()).isEqualTo("RETAINED");
        assertThat(registered.get("hoursRemaining").asLong()).isEqualTo(24L);
        assertThat(registered.get("evidencePhotos")).hasSize(1);
        // D1 — id công khai là mã nghiệp vụ, KHÔNG phải UUID.
        assertThat(code).doesNotContain("-0000-0000-");

        // Điều 7.1 — cùng (inspection, dish) không đăng ký được lần hai.
        MvcResult dupResult = mvc.perform(multipart("/api/v1/samples")
                        .file(payloadPart).file(photo)
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(dupResult.getResponse().getStatus()).isEqualTo(409);

        // BR-203 — khối lượng dưới ngưỡng 100g bị chặn.
        Map<String, Object> lowAmount = new LinkedHashMap<>(payload);
        lowAmount.put("dishId", "su-su-xao");
        lowAmount.put("amount", 50);
        MockMultipartFile lowAmountPart = new MockMultipartFile("payload", "", "application/json",
                toJson(lowAmount).getBytes(StandardCharsets.UTF_8));
        MvcResult lowAmountResult = mvc.perform(multipart("/api/v1/samples")
                        .file(lowAmountPart).file(photo)
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(lowAmountResult.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(lowAmountResult).get("error").get("code").asText()).isEqualTo("SMP-019");

        // 2) GET chi tiết + danh sách (hợp đồng phân trang §2b).
        MvcResult detail = mvc.perform(get("/api/v1/samples/" + code).header("Authorization", caterer)).andReturn();
        assertThat(detail.getResponse().getStatus()).isEqualTo(200);

        MvcResult list = mvc.perform(get("/api/v1/samples?page=1&pageSize=8").header("Authorization", caterer))
                .andReturn();
        assertThat(list.getResponse().getStatus()).isEqualTo(200);
        JsonNode listBody = parse(list);
        assertThat(listBody.get("items")).isNotEmpty();
        assertThat(listBody.has("total")).isTrue();
        assertThat(listBody.has("pageCount")).isTrue();

        // 3) BR-111 — hủy trước hạn lưu 24h bị chặn.
        Map<String, Object> disposeBody = Map.of("witness", "Ngô Thị Hà", "note", "Mẫu đạt, không phát sinh sự cố");
        MvcResult tooEarly = mvc.perform(post("/api/v1/samples/" + code + "/dispose")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(disposeBody)))
                .andReturn();
        assertThat(tooEarly.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(tooEarly).get("error").get("code").asText()).isEqualTo("SMP-111");

        // Y tế không được đăng ký/hủy mẫu (BR-007/BR-113 — bếp và y tế chia đôi nghiệp vụ).
        MvcResult wrongRole = mvc.perform(post("/api/v1/samples/" + code + "/dispose")
                        .header("Authorization", medicalStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(disposeBody)))
                .andReturn();
        assertThat(wrongRole.getResponse().getStatus()).isEqualTo(403);

        // 4) Trôi qua 25 giờ — hủy hợp lệ. Cấp lại token: access token cũ (TTL 30') đã hết hạn
        // theo đồng hồ đã trôi (BR-004), không liên quan gì tới BR-111 đang kiểm ở đây.
        clock.advance(Duration.ofHours(25));
        caterer = authHeaderFor(CATERER);
        MvcResult disposed = mvc.perform(post("/api/v1/samples/" + code + "/dispose")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(disposeBody)))
                .andReturn();
        assertThat(disposed.getResponse().getStatus()).isEqualTo(200);
        JsonNode disposedBody = parse(disposed);
        assertThat(disposedBody.get("status").asText()).isEqualTo("DISPOSED");
        assertThat(disposedBody.get("disposalWitness").asText()).isEqualTo("Ngô Thị Hà");

        // Mẫu số 5 cột 9/10/12 — thiếu witness/note phải 422 (dùng mẫu KHÁC vì mẫu này đã DISPOSED).
        Map<String, Object> secondPayload = new LinkedHashMap<>(payload);
        secondPayload.put("dishId", "su-su-xao");
        secondPayload.put("amount", 110);
        MockMultipartFile secondPart = new MockMultipartFile("payload", "", "application/json",
                toJson(secondPayload).getBytes(StandardCharsets.UTF_8));
        MvcResult secondRegister = mvc.perform(multipart("/api/v1/samples")
                        .file(secondPart).file(photo)
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(secondRegister.getResponse().getStatus()).isEqualTo(201);
        String secondCode = parse(secondRegister).get("id").asText();
        clock.advance(Duration.ofHours(25));
        caterer = authHeaderFor(CATERER);
        MvcResult incompleteDispose = mvc.perform(post("/api/v1/samples/" + secondCode + "/dispose")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("witness", "", "note", ""))))
                .andReturn();
        assertThat(incompleteDispose.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(incompleteDispose).get("error").get("code").asText()).isEqualTo("SMP-030");

        // 5) UC-076 AF1 — liên kết mẫu ĐÃ HỦY với sự cố: chặn vì IncidentStatusPort là STUB
        // (M08-D08 — MF11 chưa merge, findOpen() luôn rỗng). Hàng rào đúng như thiết kế.
        medicalStaff = authHeaderFor(MEDICAL_STAFF);   // đồng hồ đã trôi 50h — cấp lại token (BR-004)
        MvcResult linkResult = mvc.perform(post("/api/v1/samples/" + code + "/incident-link")
                        .header("Authorization", medicalStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-2026-014"))))
                .andReturn();
        assertThat(linkResult.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(linkResult).get("error").get("code").asText()).isEqualTo("SMP-023");

        // 6) UC-076 — bàn giao mẫu cho lab CŨNG chặn ở cùng cổng (sự cố không tồn tại — stub).
        // Đúng lúc này chứng minh CF-M08-2: bếp (Service Caterers) KHÔNG được gọi endpoint này.
        Map<String, Object> handOverPayload = Map.of(
                "incidentId", "INC-2026-014", "sampleIds", List.of(secondCode),
                "handoverType", "LAB", "testCriteria", "Salmonella");
        MockMultipartFile handOverPart = new MockMultipartFile("payload", "", "application/json",
                toJson(handOverPayload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile handoverRecord = new MockMultipartFile("handoverRecords", "bb.pdf",
                "application/pdf", new byte[]{'%', 'P', 'D', 'F'});

        MvcResult catererBlocked = mvc.perform(multipart("/api/v1/sample-lab-requests")
                        .file(handOverPart).file(handoverRecord)
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(catererBlocked.getResponse().getStatus()).isEqualTo(403);

        MvcResult handOverResult = mvc.perform(multipart("/api/v1/sample-lab-requests")
                        .file(handOverPart).file(handoverRecord)
                        .header("Authorization", medicalStaff))
                .andReturn();
        assertThat(handOverResult.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(handOverResult).get("error").get("code").asText()).isEqualTo("SMP-023");

        // 7) Danh mục nơi tiếp nhận mẫu (MF08 sở hữu).
        MvcResult authorities = mvc.perform(get("/api/v1/receiving-authorities").header("Authorization", medicalStaff))
                .andReturn();
        assertThat(authorities.getResponse().getStatus()).isEqualTo(200);
    }
}
