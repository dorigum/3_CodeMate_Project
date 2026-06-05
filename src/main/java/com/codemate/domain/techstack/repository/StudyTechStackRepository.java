package com.codemate.domain.techstack.repository;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.techstack.entity.StudyTechStack;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTechStackRepository extends JpaRepository<StudyTechStack, Long> {

    List<StudyTechStack> findAllByStudy(Study study);

    void deleteAllByStudy(Study study);
}
