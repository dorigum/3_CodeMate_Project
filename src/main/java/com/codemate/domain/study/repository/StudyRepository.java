package com.codemate.domain.study.repository;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {

    Page<Study> findAllByStatus(StudyStatus status, Pageable pageable);

    Page<Study> findAllByCategory(StudyCategory category, Pageable pageable);

    Page<Study> findAllByCategoryAndStatus(StudyCategory category, StudyStatus status, Pageable pageable);
}
