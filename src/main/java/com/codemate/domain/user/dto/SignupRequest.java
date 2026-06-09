package com.codemate.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청")
public record SignupRequest(

        @Schema(description = "로그인에 사용할 이메일", example = "user@example.com")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "8자 이상 30자 이하의 비밀번호", example = "strongPassword1!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하로 입력해주세요.")
        String password,

        @Schema(description = "서비스에서 표시할 닉네임", example = "코드메이트")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "사용자의 주요 기술 스택", example = "Spring Boot")
        @Size(max = 50, message = "주요 기술 스택은 50자 이하로 입력해주세요.")
        String mainTechStack
) {
}
