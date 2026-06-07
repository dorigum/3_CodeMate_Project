package com.codemate.domain.study.repository;

import com.codemate.domain.study.dto.StudySearchCondition;
import com.codemate.domain.study.entity.Study;
import com.codemate.domain.techstack.entity.StudyTechStack;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class StudySpecifications {

    private StudySpecifications() {
    }

    public static Specification<Study> withCondition(StudySearchCondition condition) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(condition.keyword())) {
                String pattern = containsPattern(condition.keyword());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(root.get("content"), rawContainsPattern(condition.keyword()))
                ));
            }

            if (condition.category() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), condition.category()));
            }

            if (condition.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), condition.status()));
            }

            if (condition.meetingType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("meetingType"), condition.meetingType()));
            }

            if (hasText(condition.location())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        containsPattern(condition.location())
                ));
            }

            if (hasText(condition.techStack())) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<StudyTechStack> studyTechStack = subquery.from(StudyTechStack.class);
                subquery.select(studyTechStack.get("id"))
                        .where(
                                criteriaBuilder.equal(studyTechStack.get("study"), root),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(studyTechStack.get("techStack").get("name")),
                                        containsPattern(condition.techStack())
                                )
                        );
                predicates.add(criteriaBuilder.exists(subquery));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String containsPattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private static String rawContainsPattern(String value) {
        return "%" + value.trim() + "%";
    }
}
