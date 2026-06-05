package com.codemate.domain.study.dto;

import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import java.time.LocalDateTime;
import java.util.List;

public record StudySummaryResponse(
        Long id,
        Long hostId,
        String hostNickname,
        String title,
        StudyCategory category,
        MeetingType meetingType,
        String location,
        int maxMemberCount,
        int currentMemberCount,
        StudyStatus status,
        List<String> techStackNames,
        LocalDateTime createdAt
) {

    public static StudySummaryResponse from(Study study) {
        return from(study, List.of());
    }

    public static StudySummaryResponse from(Study study, List<String> techStackNames) {
        return new StudySummaryResponse(
                study.getId(),
                study.getHost().getId(),
                study.getHost().getNickname(),
                study.getTitle(),
                study.getCategory(),
                study.getMeetingType(),
                study.getLocation(),
                study.getMaxMemberCount(),
                study.getCurrentMemberCount(),
                study.getStatus(),
                techStackNames,
                study.getCreatedAt()
        );
    }
}
