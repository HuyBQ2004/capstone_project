package com.sfms.mf07;

import com.sfms.common.dto.PagedResponse;
import com.sfms.mf07.dto.InspectionDtos.InspectionSessionResponse;
import com.sfms.mf07.dto.KitchenInspectionDtos.KitchenInspectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF07 — các TC tài liệu kiểm ở tầng SCHEMA (DDL Flyway) và tầng RANH GIỚI MODULE, không cần
 * Docker/Testcontainers: đọc thẳng file migration và mã nguồn trong repo.
 *
 * <p>Cách làm này là bản tương đương tĩnh của kịch bản trong tài liệu (tài liệu mô tả truy vấn
 * {@code pg_constraint}/{@code information_schema}); DDL là nguồn duy nhất sinh ra hai bảng hệ
 * thống đó, nên kiểm trên DDL cho cùng kết luận mà chạy được ở mọi máy. Chỗ nào tài liệu lệch
 * với hiện thực thật thì assert hiện thực thật + ghi chú một dòng.
 */
class Mf07SchemaAndBoundaryTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final Path V12 = MIGRATIONS.resolve("V12__mf07_inspection.sql");
    private static final Path MF09_SOURCES = Path.of("src/main/java/com/sfms/mf09");
    private static final Path MF07_SOURCES = Path.of("src/main/java/com/sfms/mf07");

    // ═════════════════════════ CHECK constraints (tuyến phòng thủ thứ hai) ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-012 — CHECK constraint tồn tại ở tầng DB cho ngưỡng nhiệt độ, ảnh bằng chứng và giá trị enum")
    void criticalCheckConstraintsExistAtTheDatabaseLayer() {
        String ddl = read(V12);

        // Ảnh bằng chứng bắt buộc khi "Không đạt" (BR-083/BR-207) — cả 4 bảng dòng + checklist.
        assertThat(ddl).contains("CONSTRAINT ck_s1f_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)");
        assertThat(ddl).contains("CONSTRAINT ck_s1d_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)");
        assertThat(ddl).contains("CONSTRAINT ck_s2_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)");
        assertThat(ddl).contains("CONSTRAINT ck_s3_evidence CHECK (sensory_pass = TRUE OR photo_count >= 1)");
        assertThat(ddl).contains("CONSTRAINT ck_cir_evidence");
        assertThat(ddl).contains("passed = TRUE OR (photo_count >= 1 AND reason IS NOT NULL)");

        // BR-090 — Bước 2 đã chốt PASSED thì bắt buộc core_temp >= 75 (chặn cả khi bỏ qua service).
        assertThat(ddl).contains("CONSTRAINT ck_insp_core_temp");
        assertThat(ddl).contains("step2 <> 'PASSED' OR (core_temp IS NOT NULL AND core_temp >= 75)");

        // BR-098 — overall PASSED chỉ khi cả 3 bước PASSED.
        // ⚠️ Lệch tài liệu: tài liệu gọi ràng buộc này là `ck_insp_overall`; tên THẬT trong V12 là
        // `ck_insp_overall_passed`, còn tập giá trị hợp lệ của cột nằm ở CHECK inline không đặt tên.
        assertThat(ddl).contains("CONSTRAINT ck_insp_overall_passed");
        assertThat(ddl).contains("overall <> 'PASSED' OR (step1='PASSED' AND step2='PASSED' AND step3='PASSED')");

        // Enum: `overall` đúng 4 giá trị (CF-23 thêm IN_PROGRESS), 3 cột bước đúng 3 giá trị.
        assertThat(ddl).contains("CHECK (overall IN ('PENDING','IN_PROGRESS','PASSED','FAILED'))");
        for (String step : List.of("step1", "step2", "step3")) {
            assertThat(ddl).contains("CHECK (" + step + " IN ('PENDING','PASSED','FAILED'))");
        }
        assertThat(ddl).contains("CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK'))");
    }

    @Test
    @DisplayName("MF07-TC-INT-013 — ck_insp_holding_temp CỐ Ý chưa bật, phiên có holding_temp = NULL không bị chặn (L19/CF-24)")
    void holdingTempConstraintIsDeliberatelyStillDisabled() {
        String ddl = read(V12);

        // Ràng buộc chỉ tồn tại dưới dạng COMMENT — không có CHECK sống nào mang tên đó.
        assertThat(ddl).contains("-- CONSTRAINT ck_insp_holding_temp");
        assertThat(ddl).doesNotContain("CONSTRAINT ck_insp_holding_temp\n      CHECK");
        assertThat(ddl.lines().filter(l -> l.contains("ck_insp_holding_temp"))
                .allMatch(l -> l.trim().startsWith("--"))).isTrue();

        // Lý do được ghi ngay tại chỗ (CF-24) — chống việc bật sớm khi FE chưa có ô nhập.
        assertThat(ddl).contains("CF-24");
        assertThat(ddl).contains("holding_temp");
        // Cột vẫn NULLABLE ⇒ INSERT với holding_temp = NULL hợp lệ.
        assertThat(ddl).contains("holding_temp          NUMERIC(5,2),");

        // Không migration nào về sau bật ràng buộc đó lên.
        assertThat(allMigrationSql()).doesNotContain("ADD CONSTRAINT ck_insp_holding_temp");
    }

    @Test
    @DisplayName("MF07-TC-INT-015 — fridge_temp_logs/pest_control_logs KHÔNG BAO GIỜ được tạo, pest_control_valid vẫn còn trên kitchen_inspections")
    void deprecatedTemperatureAndPestTablesAreNeverCreated() {
        String allSql = allMigrationSql();

        // L8/CF-4 — BR-089 đã bỏ; hai bảng này không được xuất hiện ở BẤT KỲ migration nào.
        assertThat(allSql).doesNotContain("fridge_temp_logs");
        assertThat(allSql).doesNotContain("pest_control_logs");

        // L9/CF-5 — cột vẫn phải còn (màn School Admin đọc).
        assertThat(read(V12)).contains("pest_control_valid    BOOLEAN      NOT NULL DEFAULT TRUE");
    }

    @Test
    @DisplayName("MF07-TC-SEC-013 — không có đường XOÁ phiên kiểm thực cho bất kỳ vai trò nào, kể cả System Admin (QĐ 1246)")
    void inspectionSessionsCanBeNeitherDeletedNorHidden() {
        // (1) Tầng API: không controller MF07 nào khai DELETE cho `inspections`
        // ⇒ Spring trả 405 Method Not Allowed cho MỌI vai trò (không có handler để phân quyền).
        String inspectionController = read(MF07_SOURCES.resolve("web/InspectionController.java"));
        assertThat(inspectionController).doesNotContain("@DeleteMapping");
        assertThat(inspectionController).doesNotContain("DeleteMapping");
        // Cũng không có PUT để sửa thẳng `overall` (dẫn xuất, ADR-M07-02).
        assertThat(inspectionController).doesNotContain("@PutMapping");

        // (2) Tầng service: không có phương thức xoá nào.
        assertThat(read(MF07_SOURCES.resolve("service/InspectionService.java")))
                .doesNotContain("void delete", "deleteById");

        // (3) Tầng DB: bảng KHÔNG có cờ xoá mềm nào (khác checklist_groups vốn có is_active) —
        // hồ sơ kiểm thực là hồ sơ pháp lý bắt buộc, không xoá cũng không ẩn.
        String sessionsDdl = block(read(V12), "CREATE TABLE inspection_sessions");
        assertThat(sessionsDdl).doesNotContain("is_active");
        assertThat(sessionsDdl).doesNotContain("deleted_at");
        assertThat(sessionsDdl).doesNotContain("is_deleted");
        assertThat(allMigrationSql()).doesNotContain("ALTER TABLE inspection_sessions DROP");
    }

    // ═════════════════════════ Ranh giới module & quyền riêng tư ═════════════════════════

    @Test
    @DisplayName("MF07-TC-SEC-015 — MF09 KHÔNG có đường vòng qua serving-gate: không import lớp MF07, không truy vấn bảng MF07")
    void mf09HasNoBypassAroundTheServingGate() {
        List<Path> mf09Files = javaFilesUnder(MF09_SOURCES);
        assertThat(mf09Files).isNotEmpty();   // đảm bảo test thật sự quét được mã nguồn

        for (Path file : mf09Files) {
            String source = read(file);
            assertThat(source)
                    .as("MF09 không được import lớp của MF07 (00_ §4b — tham chiếu bằng ID, không import entity module khác): %s", file)
                    .doesNotContain("com.sfms.mf07");
            assertThat(source.toLowerCase())
                    .as("MF09 không được truy vấn thẳng bảng của MF07: %s", file)
                    .doesNotContain("inspection_sessions");
        }
    }

    @Test
    @DisplayName("MF07-TC-SEC-011 — log của MF07 không bao giờ ghi định danh cá nhân của nhân viên/người đại diện (PDPD, BR-183)")
    void mf07LogsNeverCarryIndividualStaffIdentifiers() {
        List<String> forbidden = List.of("staffName", "staffPosition", "caterersRepresentativeId");

        for (Path file : javaFilesUnder(MF07_SOURCES)) {
            String source = read(file);
            for (String line : source.split("\n")) {
                if (!line.contains("log.")) {
                    continue;
                }
                for (String secret : forbidden) {
                    assertThat(line)
                            .as("Dòng log của %s không được chứa định danh cá nhân %s", file, secret)
                            .doesNotContain(secret);
                }
                // Cũng không ghi tên người kiểm/người đồng ký ra log — chỉ mã phiên/mã CAPA.
                assertThat(line).doesNotContain("getInspectorUserId()");
                assertThat(line).doesNotContain("coSigner");
            }
        }
        // Mặt còn lại của TC: API VẪN trả các nhãn người dùng cho bản ghi lịch sử (L11) —
        // chỉ log là nơi không được có.
        assertThat(componentNames(InspectionSessionResponse.class)).contains("inspector", "coSigner");
    }

    // ═════════════════════════ Hợp đồng FE↔BE ═════════════════════════

    @Test
    @DisplayName("MF07-TC-E2E-003 — shape response của MF07 giữ nguyên tên field mock, phân trang đúng 5 khoá")
    void responseShapeMatchesTheFrontendMockContract() {
        Set<String> session = componentNames(InspectionSessionResponse.class);
        // C12 — các field hợp đồng do MF07 sở hữu.
        assertThat(session).contains("meal", "step1", "step2", "step3", "overall", "coreTemp", "dish",
                "evidencePhotos", "staffNotes", "formHeader", "step1Fresh", "step1Dry", "step2Rows", "step3Rows");

        Set<String> kitchen = componentNames(KitchenInspectionResponse.class);
        assertThat(kitchen).contains("itemFindings", "itemResults", "confirmStatus");
        // ⚠️ Lệch tài liệu: `signedRecordFiles` thuộc biên bản vi phạm của MF11 (M07-D07), không
        // phải resource của MF07 ⇒ không có trong DTO nào ở đây.
        assertThat(kitchen).doesNotContain("signedRecordFiles");

        // Phân trang: ĐÚNG 5 khoá, không wrapper data/result (00_ §2b-1).
        assertThat(componentNames(PagedResponse.class))
                .containsExactlyInAnyOrder("items", "total", "page", "pageSize", "pageCount");

        // `overall` chấp nhận IN_PROGRESS ngoài các trạng thái nền (CF-23).
        assertThat(Arrays.stream(com.sfms.mf07.domain.StepStatus.values()).map(Enum::name).toList())
                .contains("IN_PROGRESS", "PENDING", "PASSED", "FAILED");
        // `meal` ∈ {BREAKFAST, LUNCH, SNACK} (D6).
        assertThat(Arrays.stream(com.sfms.common.enums.MealType.values()).map(Enum::name).toList())
                .containsExactlyInAnyOrder("BREAKFAST", "LUNCH", "SNACK");
    }

    // ═════════════════════════ Tiện ích ═════════════════════════

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được " + path.toAbsolutePath(), e);
        }
    }

    private static String allMigrationSql() {
        try (Stream<Path> files = Files.list(MIGRATIONS)) {
            return files.filter(p -> p.toString().endsWith(".sql"))
                    .map(Mf07SchemaAndBoundaryTest::read)
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Path> javaFilesUnder(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Cắt đoạn DDL của một bảng: từ {@code CREATE TABLE x} tới dấu {@code );} đầu tiên. */
    private static String block(String sql, String marker) {
        int start = sql.indexOf(marker);
        assertThat(start).as("Không tìm thấy '%s' trong V12", marker).isGreaterThanOrEqualTo(0);
        int end = sql.indexOf("\n);", start);
        return sql.substring(start, end < 0 ? sql.length() : end);
    }

    private static Set<String> componentNames(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet());
    }
}
