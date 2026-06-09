package com.codemate.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 성공 응답 데이터")
public record LoginResponse(
        @Schema(description = "Authorization 헤더의 인증 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.example.signature")
        String accessToken,

        @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiJ9.example.signature")
        String refreshToken,

        @Schema(description = "access token 만료 시간(초)", example = "3600")
        long accessTokenExpiresIn,

        @Schema(description = "refresh token 만료 시간(초)", example = "1209600")
        long refreshTokenExpiresIn
) {

    public static LoginResponse bearer(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn
    ) {
        return new LoginResponse(
                "Bearer",
                accessToken,
                refreshToken,
                accessTokenExpiresIn,
                refreshTokenExpiresIn
        );
    }
}
