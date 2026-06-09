package com.codemate.domain.user;

import com.codemate.domain.user.entity.RefreshToken;
import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.repository.RefreshTokenRepository;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.global.security.RefreshTokenHasher;
import com.codemate.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class JwtTokenLifecycleIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenHasher refreshTokenHasher;

    @Test
    void loginReturnsTokenPairAndStoresOnlyRefreshTokenHash() throws Exception {
        String email = "jwt-pair@example.com";
        signup(email, "jwt-pair");

        TokenPair tokens = login(email, TEST_PASSWORD);
        User user = userRepository.findByEmail(email).orElseThrow();
        RefreshToken storedToken = refreshTokenRepository.findByUserId(user.getId()).orElseThrow();

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(storedToken.getTokenHash()).isNotEqualTo(tokens.refreshToken());
        assertThat(storedToken.getTokenHash()).isEqualTo(refreshTokenHasher.hash(tokens.refreshToken()));
    }

    @Test
    void refreshRotatesTokenPairAndRejectsPreviousRefreshToken() throws Exception {
        String email = "jwt-rotate@example.com";
        signup(email, "jwt-rotate");
        TokenPair firstTokens = login(email, TEST_PASSWORD);

        MvcResult refreshResult = mockMvc.perform(post("/api/users/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest(firstTokens.refreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andReturn();

        TokenPair secondTokens = tokenPair(refreshResult);
        assertThat(secondTokens.accessToken()).isNotEqualTo(firstTokens.accessToken());
        assertThat(secondTokens.refreshToken()).isNotEqualTo(firstTokens.refreshToken());

        mockMvc.perform(post("/api/users/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest(firstTokens.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + secondTokens.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void accessAndRefreshTokensCannotBeUsedForEachOthersPurpose() throws Exception {
        String email = "jwt-purpose@example.com";
        signup(email, "jwt-purpose");
        TokenPair tokens = login(email, TEST_PASSWORD);

        mockMvc.perform(post("/api/users/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest(tokens.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tokens.refreshToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }

    @Test
    void logoutInvalidatesExistingAccessAndRefreshTokens() throws Exception {
        String email = "jwt-logout@example.com";
        signup(email, "jwt-logout");
        TokenPair tokens = login(email, TEST_PASSWORD);

        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃했습니다."));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        mockMvc.perform(post("/api/users/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest(tokens.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."));
    }

    @Test
    void passwordChangeInvalidatesExistingTokenPair() throws Exception {
        String email = "jwt-password@example.com";
        signup(email, "jwt-password");
        TokenPair tokens = login(email, TEST_PASSWORD);

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "%s",
                                  "newPassword": "newPassword456"
                                }
                                """.formatted(TEST_PASSWORD)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/users/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest(tokens.refreshToken())))
                .andExpect(status().isUnauthorized());

        login(email, "newPassword456");
    }

    private TokenPair login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn", greaterThan(0)))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn", greaterThan(0)))
                .andReturn();

        return tokenPair(result);
    }

    private TokenPair tokenPair(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return new TokenPair(
                JsonPath.read(response, "$.data.accessToken"),
                JsonPath.read(response, "$.data.refreshToken")
        );
    }

    private String refreshRequest(String refreshToken) {
        return """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }
}
