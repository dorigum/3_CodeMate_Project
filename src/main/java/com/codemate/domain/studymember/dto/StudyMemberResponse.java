package com.codemate.domain.studymember.dto;

import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 참여 신청 정보")
public record StudyMemberResponse(
        @Schema(description = "참여 신청 ID. 승인·거절 API의 memberId로 사용합니다.", example = "3")
        Long id,
        @Schema(description = "스터디 ID", example = "1")
        Long studyId,
        @Schema(description = "신청자 회원 ID", example = "2")
        Long userId,
        @Schema(description = "신청자 닉네임", example = "참여희망자")
        String userNickname,
        @Schema(description = "신청 상태", example = "PENDING")
        StudyMemberStatus status,
        @Schema(description = "최초 신청 일시", example = "2026-06-09T10:30:00")
        LocalDateTime createdAt
) {

    public static StudyMemberResponse from(StudyMember studyMember) {
        return new StudyMemberResponse(
                studyMember.getId(),
                studyMember.getStudy().getId(),
                studyMember.getUser().getId(),
                studyMember.getUser().getNickname(),
                studyMember.getStatus(),
                studyMember.getCreatedAt()
        );
    }
}
