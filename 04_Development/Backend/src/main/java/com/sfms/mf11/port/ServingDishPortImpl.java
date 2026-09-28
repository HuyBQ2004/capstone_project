package com.sfms.mf11.port;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@link ServingDishPort} — native query trên {@code serving_dishes}/{@code student_distributions}
 * của MF09 (V16, đã merge). Cùng kiểu tiếp cận với {@link InventoryBatchPortImpl}.
 */
@Component
public class ServingDishPortImpl implements ServingDishPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ServedStudent> studentsWhoAte(UUID dishId, LocalDate date, UUID schoolId) {
        if (dishId == null || date == null || schoolId == null) {
            return List.of();
        }
        List<Object[]> rows = em.createNativeQuery("""
                select d.student_id, d.student_name, d.class_name, d.served_at
                from serving_dishes sd
                join student_distributions d on d.id = sd.distribution_id
                where sd.dish_id = :dishId and d.date = :date and d.school_id = :schoolId
                  and d.status = 'SERVED'
                """)
                .setParameter("dishId", dishId).setParameter("date", date).setParameter("schoolId", schoolId)
                .getResultList();
        List<ServedStudent> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new ServedStudent((UUID) r[0], (String) r[1], (String) r[2], toInstant(r[3])));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ServedDishBrief> dishesServedBetween(UUID schoolId, Instant from, Instant to) {
        if (schoolId == null || from == null || to == null) {
            return List.of();
        }
        List<Object[]> rows = em.createNativeQuery("""
                select distinct sd.dish_id, sd.dish_name, d.meal_type, d.served_at
                from serving_dishes sd
                join student_distributions d on d.id = sd.distribution_id
                where d.school_id = :schoolId and d.status = 'SERVED'
                  and d.served_at between :from and :to
                """)
                .setParameter("schoolId", schoolId).setParameter("from", from).setParameter("to", to)
                .getResultList();
        List<ServedDishBrief> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new ServedDishBrief((UUID) r[0], (String) r[1], (String) r[2], toInstant(r[3])));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ServedDishByAffected> dishesEatenByStudentsBetween(UUID schoolId, List<UUID> studentIds,
                                                                    Instant from, Instant to) {
        if (schoolId == null || studentIds == null || studentIds.isEmpty() || from == null || to == null) {
            return List.of();
        }
        // `ix_sdish_dish` (V16) được tạo đúng cho câu này — comment trong migration ghi thẳng
        // "em nào đã ăn món X? (MF11)". Sắp theo SỐ EM bị ảnh hưởng cùng ăn: món đứng đầu là
        // món cần nghi ngờ trước.
        List<Object[]> rows = em.createNativeQuery("""
                select sd.dish_id, min(sd.dish_name), min(d.meal_type), max(d.served_at),
                       count(distinct d.student_id)
                from serving_dishes sd
                join student_distributions d on d.id = sd.distribution_id
                where d.school_id = :schoolId and d.status = 'SERVED'
                  and d.served_at between :from and :to
                  and d.student_id in :studentIds
                group by sd.dish_id
                order by count(distinct d.student_id) desc, max(d.served_at) desc
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("studentIds", studentIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        List<ServedDishByAffected> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new ServedDishByAffected((UUID) r[0], (String) r[1], (String) r[2], toInstant(r[3]),
                    ((Number) r[4]).longValue()));
        }
        return out;
    }

    private static Instant toInstant(Object raw) {
        return raw instanceof Instant instant ? instant : ((java.sql.Timestamp) raw).toInstant();
    }
}
