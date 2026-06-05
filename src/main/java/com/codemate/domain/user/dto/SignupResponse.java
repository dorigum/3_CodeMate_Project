package com.codemate.domain.user.dto;

import com.codemate.domain.user.entity.User;

public record SignupResponse(
        Long id,
        String email,
        String nickname,
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
