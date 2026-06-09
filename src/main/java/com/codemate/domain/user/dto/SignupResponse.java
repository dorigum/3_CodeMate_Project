package com.codemate.domain.user.dto;

import com.codemate.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 성공 응답 데이터")
public record SignupResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "회원 이메일", example = "user@example.com")
        String email,

        @Schema(description = "회원 닉네임", example = "코드메이트")
        String nickname,

        @Schema(description = "주요 기술 스택", example = "Spring Boot")
        String mainTechStack
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getMainTechStack()
        );
    }
}
