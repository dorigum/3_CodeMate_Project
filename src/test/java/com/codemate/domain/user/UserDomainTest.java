package com.codemate.domain.user;

import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDomainTest {

    @Test
    void profileCanBeUpdated() {
        User user = createUser();

        user.updateProfile("new-nickname", "Spring Boot");

        assertThat(user.getNickname()).isEqualTo("new-nickname");
        assertThat(user.getMainTechStack()).isEqualTo("Spring Boot");
    }

    @Test
    void passwordChangeReplacesEncodedPassword() {
        User user = createUser();

        user.changePassword("new-encoded-password");

        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
    }

    @Test
    void tokenInvalidationIncrementsVersion() {
        User user = createUser();

        user.invalidateTokens();
        user.invalidateTokens();

        assertThat(user.getTokenVersion()).isEqualTo(2);
    }

    private User createUser() {
        return User.builder()
                .email("user@example.com")
                .password("encoded-password")
                .nickname("nickname")
                .mainTechStack("Java")
                .role(UserRole.ROLE_USER)
                .build();
    }
}
