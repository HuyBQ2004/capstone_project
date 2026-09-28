package com.sfms.mf09.repository;

import com.sfms.mf09.domain.ServingDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ServingDishRepository extends JpaRepository<ServingDish, UUID> {

    List<ServingDish> findByDistributionId(UUID distributionId);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    /**
     * Trục truy xuất MF11 — "học sinh nào đã ăn món X?" (ADR-M09-1). Chỉ nối tới phiên ĐÃ
     * {@code SERVED} qua {@code distribution_id}, dùng index {@code ix_sdish_dish}.
     */
    @Query("""
            select sd from ServingDish sd
            join sd.distribution d
            where sd.dishId = :dishId and d.date between :from and :to
              and d.status = com.sfms.mf09.domain.DistributionStatus.SERVED
            """)
    List<ServingDish> findServedByDishId(@Param("dishId") UUID dishId,
                                          @Param("from") LocalDate from,
                                          @Param("to") LocalDate to);
}
