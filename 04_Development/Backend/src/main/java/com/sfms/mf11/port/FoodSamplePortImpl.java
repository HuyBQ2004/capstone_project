package com.sfms.mf11.port;

import com.sfms.common.code.CodeGenerator;
import com.sfms.mf08.domain.RetentionPolicy;
import com.sfms.mf11.error.IemExceptions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@link FoodSamplePort} — native query trên {@code food_samples}/{@code sample_lab_requests}
 * của MF08 (V14, đã merge). Cùng kiểu tiếp cận với {@link InventoryBatchPortImpl} (Javadoc ở đó
 * giải thích lý do dùng {@code EntityManager} thay vì Port do MF08 expose).
 *
 * <p>⚠️ Đơn giản hoá MVP: {@code requestRetrieval} luôn mở phiếu kiểu {@code LAB} (gửi xét
 * nghiệm), KHÔNG hỗ trợ {@code AUTHORITY} (thu giữ bởi cơ quan chức năng — cần
 * {@code authority_id} tra theo tên, chưa có bảng ánh xạ ổn định). {@code authorityName} dùng
 * làm {@code lab_destination}. Ghi nợ tương tự {@code M07-D08}/{@code M08-D08}.
 */
@Component
public class FoodSamplePortImpl implements FoodSamplePort {

    private static final String SCOPE_PREFIX = "mf08_lab_request:";

    @PersistenceContext
    private EntityManager em;

    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public FoodSamplePortImpl(CodeGenerator codeGenerator, Clock clock) {
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<SampleBrief> findByIncident(UUID incidentId) {
        if (incidentId == null) {
            return List.of();
        }
        List<Object[]> rows = em.createNativeQuery("""
                select s.code, s.dish_name, s.collected_at, s.status, lr.code
                from food_samples s
                left join sample_lab_requests lr on lr.incident_id = s.incident_id
                where s.incident_id = :incidentId
                order by s.collected_at
                """)
                .setParameter("incidentId", incidentId)
                .getResultList();
        List<SampleBrief> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new SampleBrief((String) r[0], (String) r[1], toInstant(r[2]), (String) r[3], (String) r[4]));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<LabRequestBrief> labRequestsByIncident(UUID incidentId) {
        if (incidentId == null) {
            return List.of();
        }
        List<Object[]> rows = em.createNativeQuery("""
                select code, lab_destination, test_criteria, status, sent_to_lab_at
                from sample_lab_requests where incident_id = :incidentId order by sent_to_lab_at desc
                """)
                .setParameter("incidentId", incidentId).getResultList();
        List<LabRequestBrief> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new LabRequestBrief((String) r[0], (String) r[1], (String) r[2], (String) r[3], toInstant(r[4])));
        }
        return out;
    }

    @Override
    @Transactional
    public void requestRetrieval(UUID incidentId, List<String> sampleCodes, String authorityName,
                                  String testCriteria, UUID actorId, UUID schoolId) {
        int year = Year.now(clock).getValue();
        String code = codeGenerator.next(SCOPE_PREFIX + year, "LABREQ-" + year, 3);
        String destination = authorityName == null || authorityName.isBlank() ? "Trung tâm kiểm nghiệm" : authorityName;
        String criteria = testCriteria == null || testCriteria.isBlank() ? "Xét nghiệm vi sinh tổng quát" : testCriteria;

        Instant now = Instant.now(clock);
        em.createNativeQuery("""
                insert into sample_lab_requests
                  (code, school_id, incident_id, lab_destination, handover_type, test_criteria,
                   requested_by, requested_at, delivered_by, sent_to_lab_at, status)
                values (:code, :schoolId, :incidentId, :destination, 'LAB', :criteria,
                        :actorLabel, :now, :actorLabel, :now, 'SENT_TO_LAB')
                """)
                .setParameter("code", code)
                .setParameter("schoolId", schoolId)
                .setParameter("incidentId", incidentId)
                .setParameter("destination", destination)
                .setParameter("criteria", criteria)
                .setParameter("actorLabel", actorLabel(actorId))
                .setParameter("now", now)
                .executeUpdate();

        if (!sampleCodes.isEmpty()) {
            // BUG tìm thấy 08/08/2026 (backlog B2): bản trước set `status = 'SENT_TO_LAB'` mà
            // KHÔNG set `lab_request_id`/`sent_to_lab_at` ⇒ vi phạm `ck_smp_sent_has_request`
            // (V14:114) ⇒ 500 + rollback TOÀN BỘ, phiếu LABREQ vừa insert mất theo. Tức là
            // "giữ mẫu để điều tra" chưa từng chạy được lần nào.
            //
            // `sent_to_lab_at` phải >= `collected_at` (`ck_smp_sent_after_collect`) — mẫu thu
            // trong tương lai là dữ liệu bẩn, dùng greatest() để không vỡ CHECK thứ hai.
            //
            // Gia hạn `retention_deadline` (Nam chốt, kế hoạch §2.3): BR-111 lưu tối thiểu 24h
            // TRÙNG ĐÚNG cửa sổ điều tra BR-149 24h, nên mẫu đủ điều kiện hủy ngay lúc điều tra
            // bắt đầu. Nạp lại đồng hồ 24h kể từ thời điểm y tế giữ mẫu — mốc gắn với BR-111,
            // KHÔNG phải một hằng số tuỳ tiện. Chốt chặn thật vẫn là `hasOpenIncidentLinked`
            // trong `FoodSampleServiceImpl.dispose()`; đây là lớp thứ hai để CHECK ở DB nói
            // cùng một câu chuyện.
            int updated = em.createNativeQuery("""
                    update food_samples
                    set incident_id = :incidentId,
                        status = 'SENT_TO_LAB',
                        lab_request_id = (select id from sample_lab_requests where code = :labCode),
                        sent_to_lab_at = greatest(:now, collected_at),
                        retrieved_at = :now,
                        retention_deadline = greatest(retention_deadline, :holdUntil)
                    where code in :codes and school_id = :schoolId and status <> 'DISPOSED'
                    """)
                    .setParameter("incidentId", incidentId)
                    .setParameter("labCode", code)
                    .setParameter("now", now)
                    .setParameter("holdUntil", now.plus(RetentionPolicy.MIN_RETENTION_HOURS, ChronoUnit.HOURS))
                    .setParameter("codes", sampleCodes)
                    .setParameter("schoolId", schoolId)
                    .executeUpdate();
            if (updated == 0) {
                throw IemExceptions.validationFailed("sampleCodes",
                        "no retainable sample matched: " + String.join(", ", sampleCodes));
            }
        }
    }

    @Override
    @Transactional
    public int holdForIncident(UUID incidentId, List<String> sampleCodes, UUID schoolId) {
        if (sampleCodes == null || sampleCodes.isEmpty()) {
            throw IemExceptions.validationFailed("sampleCodes", "required");
        }
        Instant now = Instant.now(clock);
        int updated = em.createNativeQuery("""
                update food_samples
                set incident_id = :incidentId,
                    status = 'HOLD',
                    retention_deadline = greatest(retention_deadline, :holdUntil)
                where code in :codes and school_id = :schoolId and status in ('RETAINED', 'HOLD')
                """)
                .setParameter("incidentId", incidentId)
                .setParameter("holdUntil", now.plus(RetentionPolicy.MIN_RETENTION_HOURS, ChronoUnit.HOURS))
                .setParameter("codes", sampleCodes)
                .setParameter("schoolId", schoolId)
                .executeUpdate();
        if (updated == 0) {
            // Mẫu đã DISPOSED/SENT_TO_LAB, hoặc mã sai — nói rõ thay vì im lặng báo thành công.
            throw IemExceptions.validationFailed("sampleCodes",
                    "no retainable sample matched: " + String.join(", ", sampleCodes));
        }
        return updated;
    }

    private String actorLabel(UUID actorId) {
        if (actorId == null) {
            return "Medical Staff";
        }
        var results = em.createNativeQuery("select name from user_accounts where id = :id")
                .setParameter("id", actorId).getResultList();
        return results.isEmpty() ? "Medical Staff" : (String) results.get(0);
    }

    private static Instant toInstant(Object raw) {
        return raw instanceof Instant instant ? instant : ((java.sql.Timestamp) raw).toInstant();
    }
}
