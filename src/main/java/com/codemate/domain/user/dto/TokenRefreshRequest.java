package com.codemate.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "JWT 재발급 요청")
public record TokenRefreshRequest(

        @Schema(description = "로그인 또는 직전 재발급에서 받은 refresh token")
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
}
