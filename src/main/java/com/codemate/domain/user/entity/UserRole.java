package com.codemate.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 권한: ROLE_USER(일반 사용자), ROLE_ADMIN(관리자)")
public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN
}
