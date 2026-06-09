package com.codemate.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청")
public record PasswordChangeRequest(

        @Schema(description = "현재 비밀번호", example = "strongPassword1!")
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newStrongPassword2!")
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "새 비밀번호는 8자 이상 30자 이하로 입력해주세요.")
        String newPassword
) {
}
