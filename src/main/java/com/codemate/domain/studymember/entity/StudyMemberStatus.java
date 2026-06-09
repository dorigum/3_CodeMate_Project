package com.codemate.domain.studymember.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참여 신청 상태: PENDING(승인 대기), APPROVED(승인), REJECTED(거절)")
public enum StudyMemberStatus {
    PENDING,
    APPROVED,
    REJECTED
}
