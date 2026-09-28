package com.sfms.mf07.repository;

import com.sfms.mf07.domain.ChecklistItemResult;
import com.sfms.mf07.domain.ChecklistItemResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ChecklistItemResultRepository extends JpaRepository<ChecklistItemResult, ChecklistItemResultId> {

    /** CF-15 — BR-210: chặn xoá cứng nhóm/hạng mục đã được phiên đã lưu tham chiếu. */
    @Query("select count(r) > 0 from ChecklistItemResult r where r.item.group.id = :groupId")
    boolean existsByGroupId(UUID groupId);
}
