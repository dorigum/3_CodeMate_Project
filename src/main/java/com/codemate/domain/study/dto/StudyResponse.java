package com.codemate.domain.study.dto;

import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import java.time.LocalDateTime;
import java.util.List;

public record StudyResponse(
        Long id,
        Long hostId,
        String hostNickname,
        String title,
        String content,
        StudyCategory category,
        MeetingType meetingType,
        String location,
        int maxMemberCount,
        int currentMemberCount,
        StudyStatus status,
        List<String> techStackNames,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static StudyResponse from(Study study) {
        return from(study, List.of());
    }

    public static StudyResponse from(Study study, List<String> techStackNames) {
        return new StudyResponse(
                study.getId(),
                study.getHost().getId(),
                study.getHost().getNickname(),
                study.getTitle(),
                study.getContent(),
                study.getCategory(),
                study.getMeetingType(),
                study.getLocation(),
                study.getMaxMemberCount(),
                study.getCurrentMemberCount(),
                study.getStatus(),
                techStackNames,
                study.getCreatedAt(),
                study.getUpdatedAt()
        );
    }
}
