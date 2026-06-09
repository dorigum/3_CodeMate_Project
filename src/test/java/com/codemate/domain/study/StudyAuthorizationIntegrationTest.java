package com.codemate.domain.study;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.repository.StudyRepository;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class StudyAuthorizationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void studyListAndDetailArePublic() throws Exception {
        String hostToken = signupAndLogin(
                "study-public-host@example.com",
                "study-public-host"
        );
        Long studyId = createStudy(hostToken, "공개 조회 스터디", 4);

        mockMvc.perform(get("/api/studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/studies/{studyId}", studyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(studyId))
                .andExpect(jsonPath("$.data.title").value("공개 조회 스터디"));
    }

    @Test
    void anonymousUserCannotCreateStudy() throws Exception {
        mockMvc.perform(post("/api/studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("인증 없는 생성 요청")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    void authenticatedUserCanCreateStudyAsHost() throws Exception {
        String hostToken = signupAndLogin(
                "study-create-host@example.com",
                "study-create-host"
        );

        MvcResult result = mockMvc.perform(post("/api/studies")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest("방장 생성 스터디")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.hostNickname").value("study-create-host"))
                .andExpect(jsonPath("$.data.currentMemberCount").value(1))
                .andReturn();

        Long studyId = jsonLong(result, "$.data.id");
        Study savedStudy = studyRepository.findById(studyId).orElseThrow();
        Long hostId = userRepository.findByEmail("study-create-host@example.com")
                .orElseThrow()
                .getId();

        assertThat(savedStudy.getHost().getId()).isEqualTo(hostId);
    }

    @Test
    void nonHostCannotUpdateStudyAndOriginalDataRemains() throws Exception {
        String hostToken = signupAndLogin(
                "study-update-host@example.com",
                "study-update-host"
        );
        String otherUserToken = signupAndLogin(
                "study-update-other@example.com",
                "study-update-other"
        );
        Long studyId = createStudy(hostToken, "수정 전 제목", 4);

        mockMvc.perform(patch("/api/studies/{studyId}", studyId)
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest("권한 없는 수정 제목")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("스터디 방장만 처리할 수 있습니다."));

        Study unchangedStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(unchangedStudy.getTitle()).isEqualTo("수정 전 제목");
    }

    @Test
    void hostCanUpdateStudy() throws Exception {
        String hostToken = signupAndLogin(
                "study-update-success@example.com",
                "study-update-success"
        );
        Long studyId = createStudy(hostToken, "방장 수정 전 제목", 4);

        mockMvc.perform(patch("/api/studies/{studyId}", studyId)
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest("방장이 수정한 제목")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("방장이 수정한 제목"))
                .andExpect(jsonPath("$.data.location").value("서울 강남"));
    }

    @Test
    void nonHostCannotDeleteStudyAndHostCanDeleteIt() throws Exception {
        String hostToken = signupAndLogin(
                "study-delete-host@example.com",
                "study-delete-host"
        );
        String otherUserToken = signupAndLogin(
                "study-delete-other@example.com",
                "study-delete-other"
        );
        Long studyId = createStudy(hostToken, "삭제 권한 스터디", 4);

        mockMvc.perform(delete("/api/studies/{studyId}", studyId)
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("스터디 방장만 처리할 수 있습니다."));

        assertThat(studyRepository.existsById(studyId)).isTrue();

        mockMvc.perform(delete("/api/studies/{studyId}", studyId)
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(studyRepository.existsById(studyId)).isFalse();
    }

    private String createRequest(String title) {
        return createStudyRequest(title, 4);
    }

    private String updateRequest(String title) {
        return """
                {
                  "title": "%s",
                  "content": "방장 권한으로 수정한 모집 글입니다.",
                  "category": "STUDY",
                  "meetingType": "OFFLINE",
                  "location": "서울 강남",
                  "maxMemberCount": 5,
                  "status": "RECRUITING",
                  "techStackNames": ["Java", "Spring Security"]
                }
                """.formatted(title);
    }
}
