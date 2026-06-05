package com.codemate.domain.studymember.dto;

import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import java.time.LocalDateTime;

public record StudyMemberResponse(
        Long id,
        Long studyId,
        Long userId,
        String userNickname,
        StudyMemberStatus status,
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
