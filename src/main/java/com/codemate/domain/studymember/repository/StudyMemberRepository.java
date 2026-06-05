package com.codemate.domain.studymember.repository;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import com.codemate.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    boolean existsByStudyAndUser(Study study, User user);

    Optional<StudyMember> findByStudyAndUser(Study study, User user);

    List<StudyMember> findAllByStudy(Study study);

    List<StudyMember> findAllByStudyAndStatus(Study study, StudyMemberStatus status);
}
