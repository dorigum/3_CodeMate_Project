package com.codemate.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record LoginRequest(

        @Schema(description = "가입한 이메일", example = "user@example.com")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "가입 시 설정한 비밀번호", example = "strongPassword1!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
