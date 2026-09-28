package com.sfms.mf06.service;

import com.sfms.common.enums.MealType;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.dto.TraceabilityDtos.BatchTrace;
import com.sfms.mf06.dto.TraceabilityDtos.DishBatchesResponse;
import com.sfms.mf06.error.IngExceptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * BR-076 — bản đồ truy xuất "món này nấu bằng lô nào, lô nào của NCC nào" (MF11 dùng).
 * Nối HOÀN TOÀN bằng KHOÁ (C6/ADR-M06-3): dish_id -> inventory_transactions(ISSUE) -> batch_id.
 */
@Service
public class TraceabilityService {

    private final JdbcTemplate jdbc;
    private final ReferenceResolver referenceResolver;
    private final Clock clock;

    public TraceabilityService(JdbcTemplate jdbc, ReferenceResolver referenceResolver, Clock clock) {
        this.jdbc = jdbc;
        this.referenceResolver = referenceResolver;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DishBatchesResponse dishBatches(String dishCode, LocalDate serviceDate, MealType mealType, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        UUID dishId = referenceResolver.dishIdByCode(dishCode).orElseThrow(IngExceptions::notFound);

        String sql = """
                select distinct b.code, b.ingredient_id, b.supplier_id, b.expiry_date, t.menu_id
                from inventory_transactions t join inventory_batches b on b.id = t.batch_id
                where t.dish_id = ? and t.service_date = ? and t.meal_type = ?
                  and (? is null or t.school_id = ?)
                """;
        List<Object[]> rows = jdbc.query(sql, (rs, i) -> new Object[]{
                rs.getString(1), (Object) rs.getObject(2), (Object) rs.getObject(3), rs.getDate(4), rs.getObject(5)
        }, dishId, serviceDate, mealType.name(), schoolId, schoolId);

        LocalDate today = LocalDate.now(clock);
        List<BatchTrace> batches = rows.stream().map(r -> {
            UUID ingredientId = (UUID) r[1];
            UUID supplierId = (UUID) r[2];
            LocalDate expiry = ((java.sql.Date) r[3]).toLocalDate();
            boolean locked = expiry.isBefore(today);
            return new BatchTrace((String) r[0], referenceResolver.ingredientCode(ingredientId),
                    referenceResolver.ingredientName(ingredientId), referenceResolver.partnerCode(supplierId),
                    referenceResolver.partnerName(supplierId), expiry, locked);
        }).toList();

        UUID menuId = rows.isEmpty() ? null : (UUID) rows.get(0)[4];
        return new DishBatchesResponse(dishCode, referenceResolver.dishName(dishId),
                menuId == null ? null : referenceResolver.menuCode(menuId), serviceDate, mealType.name(), batches);
    }
}
