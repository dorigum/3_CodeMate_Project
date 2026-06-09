package com.codemate.domain.user;

import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserProfileIntegrationTest extends IntegrationTestSupport {

    private static final String NEW_PASSWORD = "newPassword456";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void authenticatedUserCanUpdateNicknameAndMainTechStack() throws Exception {
        String token = signupAndLogin("profile-update@example.com", "profile-before");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequest("profile-after", "Java, Spring Boot")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("profile-update@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("profile-after"))
                .andExpect(jsonPath("$.data.mainTechStack").value("Java, Spring Boot"));

        User updatedUser = userRepository.findByEmail("profile-update@example.com").orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo("profile-after");
        assertThat(updatedUser.getMainTechStack()).isEqualTo("Java, Spring Boot");
    }

    @Test
    void userCanKeepCurrentNicknameWhenOnlyTechStackChanges() throws Exception {
        String token = signupAndLogin("profile-same-nickname@example.com", "same-nickname");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequest("same-nickname", "Kotlin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("same-nickname"))
                .andExpect(jsonPath("$.data.mainTechStack").value("Kotlin"));
    }

    @Test
    void profileUpdateRejectsAnotherUsersNickname() throws Exception {
        signup("profile-owner@example.com", "nickname-owner");
        String token = signupAndLogin("profile-duplicate@example.com", "nickname-before");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequest("nickname-owner", "Java")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    void profileUpdateRequiresAuthenticationAndValidNickname() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequest("valid-nickname", "Java")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));

        String token = signupAndLogin("profile-validation@example.com", "validation-user");

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequest(" ", "Java")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nickname").value("닉네임은 필수입니다."));
    }

    @Test
    void passwordChangeRejectsWrongCurrentPasswordAndSameNewPassword() throws Exception {
        String token = signupAndLogin("password-reject@example.com", "password-reject");

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordChangeRequest("wrongPassword", NEW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 올바르지 않습니다."));

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordChangeRequest(TEST_PASSWORD, TEST_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("새 비밀번호는 현재 비밀번호와 달라야 합니다."));
    }

    @Test
    void changedPasswordIsEncodedAndUsedForNextLogin() throws Exception {
        String email = "password-change@example.com";
        String token = signupAndLogin(email, "password-change");

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordChangeRequest(TEST_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호를 변경했습니다."));

        User updatedUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(updatedUser.getPassword()).isNotEqualTo(NEW_PASSWORD);
        assertThat(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword())).isTrue();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(email, TEST_PASSWORD)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(email, NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
    }

    private String userUpdateRequest(String nickname, String mainTechStack) {
        return """
                {
                  "nickname": "%s",
                  "mainTechStack": "%s"
                }
                """.formatted(nickname, mainTechStack);
    }

    private String passwordChangeRequest(String currentPassword, String newPassword) {
        return """
                {
                  "currentPassword": "%s",
                  "newPassword": "%s"
                }
                """.formatted(currentPassword, newPassword);
    }
}
