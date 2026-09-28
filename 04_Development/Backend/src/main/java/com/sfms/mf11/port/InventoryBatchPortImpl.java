package com.sfms.mf11.port;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link InventoryBatchPort} — native query trên bảng {@code inventory_batches}/
 * {@code receiving_sessions}/{@code partners} của MF06 (V10, đã merge).
 *
 * <p>Dùng {@code EntityManager} thay vì import entity MF06, cùng kiểu với
 * {@code common/reference/ReferenceResolver} (ADR-M02-5/AP-AI-005) — MF06 chưa expose port
 * hành vi nào cho việc này, và bản thân {@code ReferenceResolver} đã có tiền lệ đọc thuần
 * bảng module khác theo cách này. Phần GHI ({@code markQuarantined}) là ngoại lệ có ghi chú:
 * MF06/V10 đã chừa sẵn 3 cột {@code quarantine_at}/{@code quarantine_by_user_id}/
 * {@code quarantine_reason}/{@code quarantine_incident_id} đúng cho MF11 dùng (thấy khi rà
 * Bước 0), nên đây là điền vào chỗ đã dành sẵn, không phải ghi tuỳ tiện vào bảng người khác.
 */
@Component
public class InventoryBatchPortImpl implements InventoryBatchPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Optional<BatchBrief> findByCode(String batchCode, UUID schoolId) {
        if (batchCode == null || batchCode.isBlank() || schoolId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select b.id, b.code, b.ingredient_label, b.unit, b.received_date, b.supplier_id, p.name
                from inventory_batches b join partners p on p.id = b.supplier_id
                where b.code = :code and b.school_id = :schoolId
                """)
                .setParameter("code", batchCode)
                .setParameter("schoolId", schoolId)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new BatchBrief((UUID) cols[0], (String) cols[1], (String) cols[2], (String) cols[3],
                toLocalDate(cols[4]), (UUID) cols[5], (String) cols[6]));
    }

    @Override
    @Transactional
    public void markQuarantined(UUID batchId, String incidentCode, String reason, String cause, UUID actorId) {
        // BUG tìm thấy 05/08/2026: bản trước LUÔN set `quarantine_incident_id = null`, bỏ
        // qua tham số `incidentCode` — cột đã được V10 chừa sẵn đúng cho việc này (xem
        // doc-comment class) nhưng chưa từng ghi. Hệ quả: `InventoryBatchResponse.quarantine
        // .incidentId` luôn null, FE (`service-caterers/inventory`) không link ngược được
        // sang sự cố đã khoá lô. Sửa: resolve UUID từ mã sự cố rồi ghi thật.
        em.createNativeQuery("""
                update inventory_batches
                set quarantine_at = now(), quarantine_by_user_id = :actorId, quarantine_reason = :reason,
                    quarantine_cause = :cause,
                    quarantine_incident_id = (select id from incidents where code = :incidentCode)
                where id = :batchId
                """)
                .setParameter("actorId", actorId)
                .setParameter("reason", reason)
                .setParameter("cause", cause)
                .setParameter("incidentCode", incidentCode)
                .setParameter("batchId", batchId)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAlreadyQuarantined(UUID batchId) {
        if (batchId == null) {
            return false;
        }
        var results = em.createNativeQuery("select 1 from inventory_batches where id = :id and quarantine_at is not null")
                .setParameter("id", batchId).getResultList();
        return !results.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAtTerminalState(UUID batchId) {
        if (batchId == null) {
            return false;
        }
        var results = em.createNativeQuery(
                        "select 1 from inventory_batches where id = :id and quarantine_disposition is not null")
                .setParameter("id", batchId).getResultList();
        return !results.isEmpty();
    }

    @Override
    @Transactional
    public boolean applyDisposition(UUID batchId, Disposition disposition) {
        if (batchId == null || disposition == null) {
            return false;
        }
        // Chỉ ghi cho lô ĐÃ cách ly và CHƯA có trạng thái cuối — một chiều, cùng tinh thần
        // BR-217/`RULE iqb_no_delete`: đã trả NCC rồi thì không "đổi ý" thành huỷ bỏ được.
        // `quarantine_by_user_id` giữ nguyên người CÁCH LY; người XỬ LÝ ghi ở dòng sự kiện
        // (`incident_events`) — V20 không chừa cột cho actor xử lý.
        return em.createNativeQuery("""
                update inventory_batches
                set quarantine_disposition = :disposition, quarantine_disposition_at = now()
                where id = :batchId and quarantine_at is not null and quarantine_disposition is null
                """)
                .setParameter("disposition", disposition.name())
                .setParameter("batchId", batchId)
                .executeUpdate() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DispositionBrief> dispositionOf(UUID batchId) {
        if (batchId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select quarantine_disposition, quarantine_disposition_at
                from inventory_batches where id = :id and quarantine_disposition is not null
                """)
                .setParameter("id", batchId).getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new DispositionBrief((String) cols[0], toInstant(cols[1])));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReceivingBrief> receivingSessionOf(UUID batchId) {
        if (batchId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select rs.code, rs.received_at, rs.supplier_id, p.name
                from inventory_batches b
                join receiving_sessions rs on rs.id = b.receiving_session_id
                join partners p on p.id = rs.supplier_id
                where b.id = :id
                """)
                .setParameter("id", batchId).getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new ReceivingBrief((String) cols[0], toLocalDate(cols[1]), (UUID) cols[2], (String) cols[3]));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> ingredientNameOf(UUID batchId) {
        if (batchId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select ci.name from inventory_batches b
                join catalog_ingredients ci on ci.id = b.ingredient_id
                where b.id = :id
                """)
                .setParameter("id", batchId).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((String) results.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> dishNamesUsingBatch(UUID batchId, UUID schoolId) {
        if (batchId == null) {
            return List.of();
        }
        // `dish_id` có thể NULL ở giao dịch không gắn món (điều chỉnh kho, huỷ) — bỏ qua.
        @SuppressWarnings("unchecked")
        List<String> names = em.createNativeQuery("""
                select distinct d.name
                from inventory_transactions t
                join dishes d on d.id = t.dish_id
                where t.batch_id = :batchId and t.dish_id is not null
                  and (cast(:schoolId as uuid) is null or t.school_id = cast(:schoolId as uuid))
                order by d.name
                """)
                .setParameter("batchId", batchId)
                .setParameter("schoolId", schoolId)
                .getResultList();
        return names;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> batchIdsSuppliedBy(UUID partnerId, UUID schoolId) {
        if (partnerId == null || schoolId == null) {
            return List.of();
        }
        var results = em.createNativeQuery(
                        "select id from inventory_batches where supplier_id = :partnerId and school_id = :schoolId")
                .setParameter("partnerId", partnerId).setParameter("schoolId", schoolId).getResultList();
        List<UUID> out = new ArrayList<>();
        for (Object r : results) {
            out.add((UUID) r);
        }
        return out;
    }

    private static Instant toInstant(Object raw) {
        if (raw == null) {
            return null;
        }
        return raw instanceof Instant instant ? instant : ((java.sql.Timestamp) raw).toInstant();
    }

    private static LocalDate toLocalDate(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof LocalDate d) {
            return d;
        }
        if (raw instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        return ((Instant) raw).atZone(java.time.ZoneOffset.UTC).toLocalDate();
    }
}
