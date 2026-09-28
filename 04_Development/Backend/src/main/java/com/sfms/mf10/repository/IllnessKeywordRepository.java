package com.sfms.mf10.repository;

import com.sfms.mf10.domain.IllnessKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IllnessKeywordRepository extends JpaRepository<IllnessKeyword, UUID> {

    List<IllnessKeyword> findByActiveTrue();   // BR-142 (cache 10 phút — MVP: đọc thẳng)
}
