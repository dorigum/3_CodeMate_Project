package com.codemate.domain.study.dto;

import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;

public record StudySearchCondition(
        String keyword,
        StudyCategory category,
        StudyStatus status,
        MeetingType meetingType,
        String location,
        String techStack
) {
}
