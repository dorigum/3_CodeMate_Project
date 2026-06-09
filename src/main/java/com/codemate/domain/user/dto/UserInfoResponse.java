package com.codemate.domain.user.dto;

import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 사용자 정보")
public record UserInfoResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,
        @Schema(description = "회원 이메일", example = "user@example.com")
        String email,
        @Schema(description = "회원 닉네임", example = "코드메이트")
        String nickname,
        @Schema(description = "주요 기술 스택", example = "Spring Boot")
        String mainTechStack,
        @Schema(description = "회원 권한", example = "ROLE_USER")
        UserRole role
) {

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getMainTechStack(),
                user.getRole()
        );
    }
}
