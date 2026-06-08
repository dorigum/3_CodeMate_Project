package com.codemate.domain.studymember.dto;

import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import java.time.LocalDateTime;

public record MyStudyApplicationResponse(
        Long applicationId,
        Long studyId,
        String studyTitle,
        String hostNickname,
        StudyStatus studyStatus,
        StudyMemberStatus applicationStatus,
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
