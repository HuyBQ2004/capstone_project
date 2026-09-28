package com.sfms.mf12.repository;

import com.sfms.mf12.domain.MetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MetricSnapshotRepository extends JpaRepository<MetricSnapshot, UUID> {

    List<MetricSnapshot> findByCapturedAtAfterOrderByCapturedAtAsc(Instant after);

    long deleteByCapturedAtBefore(Instant before);
}
