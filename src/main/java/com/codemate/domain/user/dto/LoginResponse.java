package com.codemate.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 성공 응답 데이터")
public record LoginResponse(
        @Schema(description = "Authorization 헤더의 인증 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.example.signature")
        String accessToken
) {

    public static LoginResponse bearer(String accessToken) {
        return new LoginResponse("Bearer", accessToken);
    }
}
