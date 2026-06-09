package com.codemate.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 정보 수정 요청")
public record UserUpdateRequest(

        @Schema(description = "변경할 닉네임", example = "코드메이트2")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "변경할 주요 기술 스택", example = "Java, Spring Boot")
        @Size(max = 50, message = "주요 기술 스택은 50자 이하로 입력해주세요.")
        String mainTechStack
) {
}
