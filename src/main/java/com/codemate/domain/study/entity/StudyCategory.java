package com.codemate.domain.study.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집 유형: STUDY(함께 학습), MOGAKKO(모여서 각자 코딩)")
public enum StudyCategory {
    STUDY,
    MOGAKKO
}
