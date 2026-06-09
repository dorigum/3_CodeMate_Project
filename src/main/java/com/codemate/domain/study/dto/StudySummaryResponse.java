package com.codemate.domain.study.dto;

import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 모집 글 목록 항목")
public record StudySummaryResponse(
        @Schema(description = "스터디 ID", example = "1")
        Long id,
        @Schema(description = "방장 회원 ID", example = "1")
        Long hostId,
        @Schema(description = "방장 닉네임", example = "스터디장")
        String hostNickname,
        @Schema(description = "모집 글 제목", example = "Spring Boot 사이드 프로젝트 팀원 모집")
        String title,
        @Schema(description = "모집 유형", example = "STUDY")
        StudyCategory category,
        @Schema(description = "진행 방식", example = "ONLINE")
        MeetingType meetingType,
        @Schema(description = "오프라인 진행 장소", example = "서울 강남")
        String location,
        @Schema(description = "최대 인원", example = "5")
        int maxMemberCount,
        @Schema(description = "현재 승인 인원", example = "2")
        int currentMemberCount,
        @Schema(description = "스터디 상태", example = "RECRUITING")
        StudyStatus status,
        @Schema(description = "기술 스택 목록", example = "[\"Java\",\"Spring Boot\"]")
        List<String> techStackNames,
        @Schema(description = "생성 일시", example = "2026-06-09T10:30:00")
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
