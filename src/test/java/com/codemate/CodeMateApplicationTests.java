package com.codemate;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class CodeMateApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void signup() throws Exception {
        String requestBody = """
                {
                  "email": "user@example.com",
                  "password": "password123",
                  "nickname": "codemate",
                  "mainTechStack": "Spring Boot"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("codemate"));
    }

    @Test
    void loginAndGetMe() throws Exception {
        signup("login-user@example.com", "login-user", "Spring Security");

        String accessToken = login("login-user@example.com");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("login-user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("login-user"));
    }

    @Test
    void studyCrud() throws Exception {
        signup("study-host@example.com", "study-host", "Java");
        String accessToken = login("study-host@example.com");

        String createRequestBody = """
                {
                  "title": "Spring Boot 스터디",
                  "content": "매주 프로젝트 코드를 리뷰합니다.",
                  "category": "STUDY",
                  "meetingType": "ONLINE",
                  "location": "Discord",
                  "maxMemberCount": 4,
                  "techStackNames": ["Java", "Spring Boot", "JPA"]
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/studies")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title").value("Spring Boot 스터디"))
                .andExpect(jsonPath("$.data.currentMemberCount").value(1))
                .andExpect(jsonPath("$.data.techStackNames[0]").value("Java"))
                .andExpect(jsonPath("$.data.techStackNames[1]").value("Spring Boot"))
                .andExpect(jsonPath("$.data.techStackNames[2]").value("JPA"))
                .andReturn();

        Integer studyId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].title").value("Spring Boot 스터디"))
                .andExpect(jsonPath("$.data.items[0].techStackNames[0]").value("Java"));

        mockMvc.perform(get("/api/studies/" + studyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spring Boot 스터디"))
                .andExpect(jsonPath("$.data.techStackNames[1]").value("Spring Boot"));

        String updateRequestBody = """
                {
                  "title": "Spring Boot 심화 스터디",
                  "content": "JPA와 Security까지 함께 다룹니다.",
                  "category": "STUDY",
                  "meetingType": "OFFLINE",
                  "location": "강남",
                  "maxMemberCount": 5,
                  "status": "RECRUITING",
                  "techStackNames": ["Java", "Spring Security"]
                }
                """;

        mockMvc.perform(patch("/api/studies/" + studyId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spring Boot 심화 스터디"))
                .andExpect(jsonPath("$.data.location").value("강남"))
                .andExpect(jsonPath("$.data.techStackNames[0]").value("Java"))
                .andExpect(jsonPath("$.data.techStackNames[1]").value("Spring Security"));

        mockMvc.perform(delete("/api/studies/" + studyId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void applyStudy() throws Exception {
        signup("apply-host@example.com", "apply-host", "Spring Boot");
        signup("applicant@example.com", "applicant", "JPA");

        String hostToken = login("apply-host@example.com");
        String applicantToken = login("applicant@example.com");

        Integer studyId = createStudy(hostToken);

        mockMvc.perform(post("/api/studies/" + studyId + "/members")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studyId").value(studyId))
                .andExpect(jsonPath("$.data.userNickname").value("applicant"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/api/studies/" + studyId + "/members")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getStudyMembers() throws Exception {
        signup("list-host@example.com", "list-host", "Spring Boot");
        signup("list-applicant@example.com", "list-applicant", "JPA");

        String hostToken = login("list-host@example.com");
        String applicantToken = login("list-applicant@example.com");

        Integer studyId = createStudy(hostToken);
        Integer memberId = applyStudy(studyId, applicantToken);

        mockMvc.perform(get("/api/studies/" + studyId + "/members")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(memberId))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        mockMvc.perform(get("/api/studies/" + studyId + "/members")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userNickname").value("list-applicant"));
    }

    @Test
    void securityErrorResponse() throws Exception {
        String createRequestBody = """
                {
                  "title": "인증 실패 테스트",
                  "content": "토큰 없이 생성 요청을 보냅니다.",
                  "category": "STUDY",
                  "meetingType": "ONLINE",
                  "location": "Discord",
                  "maxMemberCount": 4
                }
                """;

        mockMvc.perform(post("/api/studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        signup("security-host@example.com", "security-host", "Spring Boot");
        signup("security-applicant@example.com", "security-applicant", "JPA");

        String hostToken = login("security-host@example.com");
        String applicantToken = login("security-applicant@example.com");

        Integer studyId = createStudy(hostToken);
        applyStudy(studyId, applicantToken);

        mockMvc.perform(get("/api/studies/" + studyId + "/members")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("스터디 방장만 처리할 수 있습니다."));
    }

    @Test
    void approveStudyMemberAndCloseStudy() throws Exception {
        signup("approve-host@example.com", "approve-host", "Spring Boot");
        signup("approve-applicant@example.com", "approve-applicant", "JPA");

        String hostToken = login("approve-host@example.com");
        String applicantToken = login("approve-applicant@example.com");

        Integer studyId = createStudy(hostToken, 2);
        Integer memberId = applyStudy(studyId, applicantToken);

        mockMvc.perform(patch("/api/studies/" + studyId + "/members/" + memberId + "/approve")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(get("/api/studies/" + studyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentMemberCount").value(2))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    void rejectStudyMember() throws Exception {
        signup("reject-host@example.com", "reject-host", "Spring Boot");
        signup("reject-applicant@example.com", "reject-applicant", "JPA");

        String hostToken = login("reject-host@example.com");
        String applicantToken = login("reject-applicant@example.com");

        Integer studyId = createStudy(hostToken);
        Integer memberId = applyStudy(studyId, applicantToken);

        mockMvc.perform(patch("/api/studies/" + studyId + "/members/" + memberId + "/reject")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        mockMvc.perform(get("/api/studies/" + studyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentMemberCount").value(1))
                .andExpect(jsonPath("$.data.status").value("RECRUITING"));
    }

    private void signup(String email, String nickname, String mainTechStack) throws Exception {
        String requestBody = """
                {
                  "email": "%s",
                  "password": "password123",
                  "nickname": "%s",
                  "mainTechStack": "%s"
                }
                """.formatted(email, nickname, mainTechStack);

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        String requestBody = """
                {
                  "email": "%s",
                  "password": "password123"
                }
                """.formatted(email);

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andReturn();

        return JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private Integer createStudy(String accessToken) throws Exception {
        return createStudy(accessToken, 4);
    }

    private Integer createStudy(String accessToken, int maxMemberCount) throws Exception {
        String requestBody = """
                {
                  "title": "알고리즘 스터디",
                  "content": "매주 문제 풀이를 공유합니다.",
                  "category": "STUDY",
                  "meetingType": "ONLINE",
                  "location": "Discord",
                  "maxMemberCount": %d,
                  "techStackNames": ["Java", "Spring Boot"]
                }
                """.formatted(maxMemberCount);

        MvcResult createResult = mockMvc.perform(post("/api/studies")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");
    }

    private Integer applyStudy(Integer studyId, String accessToken) throws Exception {
        MvcResult applyResult = mockMvc.perform(post("/api/studies/" + studyId + "/members")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(applyResult.getResponse().getContentAsString(), "$.data.id");
    }
}
