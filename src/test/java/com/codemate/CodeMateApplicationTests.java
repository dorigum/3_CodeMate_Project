package com.codemate;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.study.repository.StudyRepository;
import com.codemate.domain.studymember.dto.StudyMemberResponse;
import com.codemate.domain.studymember.service.StudyMemberService;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.global.exception.BusinessException;
import com.codemate.global.exception.ErrorCode;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;
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

    @Autowired
    private StudyMemberService studyMemberService;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Flyway flyway;

    @Test
    void contextLoads() {
    }

    @Test
    void flywayMigrationIsApplied() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    void healthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void openApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("CodeMate API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/users/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/users/token/refresh'].post").exists())
                .andExpect(jsonPath("$.paths['/api/users/logout'].post").exists())
                .andExpect(jsonPath("$.paths['/api/users/me'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/users/me/password'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/studies'].get").exists())
                .andExpect(jsonPath("$.paths['/api/studies/{studyId}/close'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/studies'].get.parameters[*].name")
                        .value(hasItems("page", "size", "sort")))
                .andExpect(jsonPath("$.paths['/api/studies'].get.parameters[*].name")
                        .value(not(hasItems("pageable"))))
                .andExpect(jsonPath("$.paths['/api/studies/{studyId}/members/{memberId}/approve'].patch").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.errors").exists())
                .andExpect(jsonPath("$.components.schemas.SignupRequest.properties.email.description")
                        .value("로그인에 사용할 이메일"))
                .andExpect(jsonPath("$.components.schemas.StudyCreateRequest.properties.category.example")
                        .value("STUDY"))
                .andExpect(jsonPath("$.paths['/api/users/signup'].post.responses['400'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"))
                .andExpect(jsonPath("$.paths['/api/users/signup'].post.responses['500'].content['application/json']")
                        .exists());
    }

    @Test
    void getStudiesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/studies")
                        .param("keyword", "Java")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1));
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
    void validationErrorResponseContainsFieldErrors() throws Exception {
        String requestBody = """
                {
                  "email": "invalid-email",
                  "password": "",
                  "nickname": "",
                  "mainTechStack": "Spring Boot"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors.email").value("이메일 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors.password").value("비밀번호는 필수입니다."))
                .andExpect(jsonPath("$.errors.nickname").value("닉네임은 필수입니다."));
    }

    @Test
    void invalidEnumUsesCommonErrorResponse() throws Exception {
        mockMvc.perform(get("/api/studies")
                        .param("category", "INVALID_CATEGORY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."));
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

        mockMvc.perform(get("/api/studies")
                        .param("keyword", "Spring Boot 스터디"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[*].id").value(hasItems(studyId)));

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
    void searchStudiesWithCombinedFilters() throws Exception {
        signup("search-host@example.com", "search-host", "Kotlin");
        String accessToken = login("search-host@example.com");

        createStudy(
                accessToken,
                "검색전용 Kotlin 스터디",
                "코루틴과 Spring을 함께 학습합니다.",
                "STUDY",
                "OFFLINE",
                "판교 테크노밸리",
                "[\"Kotlin\", \"Spring Boot\"]"
        );
        createStudy(
                accessToken,
                "검색전용 Java 스터디",
                "JPA 성능 최적화를 학습합니다.",
                "STUDY",
                "OFFLINE",
                "판교 테크노밸리",
                "[\"Java\", \"JPA\"]"
        );
        createStudy(
                accessToken,
                "검색전용 Kotlin 모각코",
                "코루틴 코드를 각자 작성합니다.",
                "MOGAKKO",
                "ONLINE",
                "Discord",
                "[\"Kotlin\"]"
        );

        mockMvc.perform(get("/api/studies")
                        .param("keyword", "코루틴")
                        .param("category", "STUDY")
                        .param("status", "RECRUITING")
                        .param("meetingType", "OFFLINE")
                        .param("location", "판교")
                        .param("techStack", "kot")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("검색전용 Kotlin 스터디"))
                .andExpect(jsonPath("$.data.items[0].meetingType").value("OFFLINE"))
                .andExpect(jsonPath("$.data.items[0].location").value("판교 테크노밸리"))
                .andExpect(jsonPath("$.data.items[0].techStackNames[0]").value("Kotlin"));
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
    void deleteStudyWithMemberApplication() throws Exception {
        signup("delete-host@example.com", "delete-host", "Spring Boot");
        signup("delete-applicant@example.com", "delete-applicant", "JPA");

        String hostToken = login("delete-host@example.com");
        String applicantToken = login("delete-applicant@example.com");
        Integer studyId = createStudy(hostToken);
        applyStudy(studyId, applicantToken);

        mockMvc.perform(delete("/api/studies/" + studyId)
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/studies/" + studyId))
                .andExpect(status().isNotFound())
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
    void getMyStudyApplicationsByStatus() throws Exception {
        signup("my-app-host@example.com", "my-app-host", "Spring Boot");
        signup("my-app-user@example.com", "my-app-user", "JPA");

        String hostToken = login("my-app-host@example.com");
        String applicantToken = login("my-app-user@example.com");

        Integer pendingStudyId = createStudy(hostToken);
        Integer approvedStudyId = createStudy(hostToken);
        Integer rejectedStudyId = createStudy(hostToken);

        applyStudy(pendingStudyId, applicantToken);
        Integer approvedMemberId = applyStudy(approvedStudyId, applicantToken);
        Integer rejectedMemberId = applyStudy(rejectedStudyId, applicantToken);

        mockMvc.perform(patch("/api/studies/" + approvedStudyId + "/members/" + approvedMemberId + "/approve")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/studies/" + rejectedStudyId + "/members/" + rejectedMemberId + "/reject")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me/study-applications")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].applicationStatus")
                        .value(hasItems("PENDING", "APPROVED", "REJECTED")));

        mockMvc.perform(get("/api/users/me/study-applications")
                        .param("status", "APPROVED")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studyId").value(approvedStudyId))
                .andExpect(jsonPath("$.data[0].studyTitle").isNotEmpty())
                .andExpect(jsonPath("$.data[0].applicationStatus").value("APPROVED"));
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
    void concurrentApprovalsDoNotExceedStudyCapacity() throws Exception {
        signup("concurrent-host@example.com", "concurrent-host", "Spring Boot");
        signup("concurrent-applicant-1@example.com", "concurrent-applicant-1", "JPA");
        signup("concurrent-applicant-2@example.com", "concurrent-applicant-2", "Kotlin");

        String hostToken = login("concurrent-host@example.com");
        String applicantToken1 = login("concurrent-applicant-1@example.com");
        String applicantToken2 = login("concurrent-applicant-2@example.com");

        Integer studyId = createStudy(hostToken, 2);
        Integer memberId1 = applyStudy(studyId, applicantToken1);
        Integer memberId2 = applyStudy(studyId, applicantToken2);
        Long hostId = userRepository.findByEmail("concurrent-host@example.com")
                .orElseThrow()
                .getId();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            List<Future<ApprovalResult>> futures = List.of(
                    executor.submit(approveAtSameTime(ready, start, hostId, studyId.longValue(), memberId1.longValue())),
                    executor.submit(approveAtSameTime(ready, start, hostId, studyId.longValue(), memberId2.longValue()))
            );

            ready.await();
            start.countDown();

            List<ApprovalResult> results = futures.stream()
                    .map(this::getFutureResult)
                    .toList();

            assertThat(results).filteredOn(ApprovalResult::approved).hasSize(1);
            assertThat(results)
                    .filteredOn(result -> result.errorCode() == ErrorCode.STUDY_CAPACITY_FULL)
                    .hasSize(1);

            Study study = studyRepository.findById(studyId.longValue()).orElseThrow();
            assertThat(study.getCurrentMemberCount()).isEqualTo(2);
            assertThat(study.isFull()).isTrue();
            assertThat(study.getStatus()).isEqualTo(StudyStatus.CLOSED);
        } finally {
            executor.shutdownNow();
        }
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

    @Test
    void rejectedStudyMemberCanReapply() throws Exception {
        signup("reapply-host@example.com", "reapply-host", "Spring Boot");
        signup("reapply-applicant@example.com", "reapply-applicant", "JPA");

        String hostToken = login("reapply-host@example.com");
        String applicantToken = login("reapply-applicant@example.com");

        Integer studyId = createStudy(hostToken);
        Integer memberId = applyStudy(studyId, applicantToken);

        mockMvc.perform(patch("/api/studies/" + studyId + "/members/" + memberId + "/reject")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        mockMvc.perform(post("/api/studies/" + studyId + "/members")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(memberId))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/api/studies/" + studyId + "/members")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 참여 신청한 스터디입니다."));
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

    private Integer createStudy(
            String accessToken,
            String title,
            String content,
            String category,
            String meetingType,
            String location,
            String techStackNames
    ) throws Exception {
        String requestBody = """
                {
                  "title": "%s",
                  "content": "%s",
                  "category": "%s",
                  "meetingType": "%s",
                  "location": "%s",
                  "maxMemberCount": 4,
                  "techStackNames": %s
                }
                """.formatted(title, content, category, meetingType, location, techStackNames);

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

    private Callable<ApprovalResult> approveAtSameTime(
            CountDownLatch ready,
            CountDownLatch start,
            Long hostId,
            Long studyId,
            Long memberId
    ) {
        return () -> {
            ready.countDown();
            start.await();

            try {
                StudyMemberResponse response = studyMemberService.approve(hostId, studyId, memberId);
                return new ApprovalResult(response.status().name().equals("APPROVED"), null);
            } catch (BusinessException exception) {
                return new ApprovalResult(false, exception.getErrorCode());
            }
        };
    }

    private ApprovalResult getFutureResult(Future<ApprovalResult> future) {
        try {
            return future.get();
        } catch (Exception exception) {
            throw new AssertionError("동시 승인 작업 실행에 실패했습니다.", exception);
        }
    }

    private record ApprovalResult(boolean approved, ErrorCode errorCode) {
    }
}
