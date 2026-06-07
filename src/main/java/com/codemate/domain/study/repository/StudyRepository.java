package com.codemate.domain.study.repository;

import com.codemate.domain.study.entity.Study;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRepository extends JpaRepository<Study, Long>, JpaSpecificationExecutor<Study> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select study from Study study where study.id = :studyId")
    Optional<Study> findByIdForUpdate(@Param("studyId") Long studyId);
}
