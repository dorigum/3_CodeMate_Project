package com.codemate.domain.studymember.dto;

import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 스터디 참여 신청 내역")
public record MyStudyApplicationResponse(
        @Schema(description = "참여 신청 ID", example = "3")
        Long applicationId,
        @Schema(description = "스터디 ID", example = "1")
        Long studyId,
        @Schema(description = "스터디 제목", example = "Spring Boot 사이드 프로젝트 팀원 모집")
        String studyTitle,
        @Schema(description = "방장 닉네임", example = "스터디장")
        String hostNickname,
        @Schema(description = "스터디 진행 상태", example = "RECRUITING")
        StudyStatus studyStatus,
        @Schema(description = "내 신청 처리 상태", example = "APPROVED")
        StudyMemberStatus applicationStatus,
        @Schema(description = "최근 신청 또는 재신청 일시", example = "2026-06-09T10:30:00")
        LocalDateTime appliedAt
) {

    public static MyStudyApplicationResponse from(StudyMember studyMember) {
        return new MyStudyApplicationResponse(
                studyMember.getId(),
                studyMember.getStudy().getId(),
                studyMember.getStudy().getTitle(),
                studyMember.getStudy().getHost().getNickname(),
                studyMember.getStudy().getStatus(),
                studyMember.getStatus(),
                studyMember.getUpdatedAt()
        );
    }
}
