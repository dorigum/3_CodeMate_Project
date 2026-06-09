package com.codemate.domain.study.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 상태: RECRUITING(모집 중), CLOSED(모집 마감), IN_PROGRESS(진행 중), FINISHED(종료)")
public enum StudyStatus {
    RECRUITING,
    CLOSED,
    IN_PROGRESS,
    FINISHED
}
