package com.codemate.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class IntegrationTestSupport {

    protected static final String TEST_PASSWORD = "password123";

    @Autowired
    protected MockMvc mockMvc;

    protected String signupAndLogin(String email, String nickname) throws Exception {
        signup(email, nickname);

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(email, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andReturn();

        return jsonString(loginResult, "$.data.accessToken");
    }

    protected void signup(String email, String nickname) throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequest(email, nickname)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    protected Long createStudy(
            String accessToken,
            String title,
            int maxMemberCount
    ) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/studies")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createStudyRequest(title, maxMemberCount)))
                .andExpect(status().isCreated())
                .andReturn();

        return jsonLong(createResult, "$.data.id");
    }

    protected String signupRequest(String email, String nickname) {
        return """
                {
                  "email": "%s",
                  "password": "%s",
                  "nickname": "%s",
                  "mainTechStack": "Spring Boot"
                }
                """.formatted(email, TEST_PASSWORD, nickname);
    }

    protected String loginRequest(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    protected String createStudyRequest(String title, int maxMemberCount) {
        return """
                {
                  "title": "%s",
                  "content": "통합 테스트용 스터디 모집 글입니다.",
                  "category": "STUDY",
                  "meetingType": "ONLINE",
                  "location": "Discord",
                  "maxMemberCount": %d,
                  "techStackNames": ["Java", "Spring Boot"]
                }
                """.formatted(title, maxMemberCount);
    }

    protected Long jsonLong(MvcResult result, String path) throws Exception {
        Number value = JsonPath.read(result.getResponse().getContentAsString(), path);
        return value.longValue();
    }

    protected String jsonString(MvcResult result, String path) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), path);
    }
}
