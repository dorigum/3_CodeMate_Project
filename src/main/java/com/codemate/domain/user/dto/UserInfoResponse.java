package com.codemate.domain.user.dto;

import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.entity.UserRole;

public record UserInfoResponse(
        Long id,
        String email,
        String nickname,
        String mainTechStack,
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
