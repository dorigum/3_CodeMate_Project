package com.codemate.domain.studymember;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.study.repository.StudyRepository;
import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import com.codemate.domain.studymember.repository.StudyMemberRepository;
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
class StudyMemberStatusTransitionIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Test
    void applicationStartsAsPending() throws Exception {
        TestUsers users = createUsers("transition-pending");
        Long studyId = createStudy(users.hostToken(), "신청 대기 상태", 4);

        Long memberId = apply(studyId, users.applicantToken());

        StudyMember application = studyMemberRepository.findById(memberId).orElseThrow();
        assertThat(application.getStatus()).isEqualTo(StudyMemberStatus.PENDING);
    }

    @Test
    void pendingApplicationCanBeApprovedAndCannotBeProcessedAgain() throws Exception {
        TestUsers users = createUsers("transition-approve");
        Long studyId = createStudy(users.hostToken(), "신청 승인 상태", 4);
        Long memberId = apply(studyId, users.applicantToken());

        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/approve", studyId, memberId)
                        .header("Authorization", "Bearer " + users.hostToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        StudyMember approvedApplication = studyMemberRepository.findById(memberId).orElseThrow();
        Study study = studyRepository.findById(studyId).orElseThrow();
        assertThat(approvedApplication.getStatus()).isEqualTo(StudyMemberStatus.APPROVED);
        assertThat(study.getCurrentMemberCount()).isEqualTo(2);

        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/reject", studyId, memberId)
                        .header("Authorization", "Bearer " + users.hostToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("대기 중인 신청만 처리할 수 있습니다."));
    }

    @Test
    void approvalClosesStudyWhenCapacityIsReached() throws Exception {
        TestUsers users = createUsers("transition-capacity");
        Long studyId = createStudy(users.hostToken(), "정원 마감 상태", 2);
        Long memberId = apply(studyId, users.applicantToken());

        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/approve", studyId, memberId)
                        .header("Authorization", "Bearer " + users.hostToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        Study closedStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(closedStudy.getCurrentMemberCount()).isEqualTo(2);
        assertThat(closedStudy.getStatus()).isEqualTo(StudyStatus.CLOSED);
    }

    @Test
    void rejectedApplicationCanReapplyWithSameMemberId() throws Exception {
        TestUsers users = createUsers("transition-reapply");
        Long studyId = createStudy(users.hostToken(), "거절 후 재신청", 4);
        Long memberId = apply(studyId, users.applicantToken());

        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/reject", studyId, memberId)
                        .header("Authorization", "Bearer " + users.hostToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        Study rejectedStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(rejectedStudy.getCurrentMemberCount()).isEqualTo(1);

        MvcResult reapplyResult = mockMvc.perform(post("/api/studies/{studyId}/members", studyId)
                        .header("Authorization", "Bearer " + users.applicantToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(memberId))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        assertThat(jsonLong(reapplyResult, "$.data.id")).isEqualTo(memberId);
        assertThat(studyMemberRepository.findById(memberId).orElseThrow().getStatus())
                .isEqualTo(StudyMemberStatus.PENDING);
    }

    @Test
    void pendingOrApprovedApplicationCannotBeSubmittedAgain() throws Exception {
        TestUsers users = createUsers("transition-duplicate");
        Long studyId = createStudy(users.hostToken(), "중복 신청 차단", 4);
        Long memberId = apply(studyId, users.applicantToken());

        expectDuplicateApplication(studyId, users.applicantToken());

        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/approve", studyId, memberId)
                        .header("Authorization", "Bearer " + users.hostToken()))
                .andExpect(status().isOk());

        expectDuplicateApplication(studyId, users.applicantToken());
    }

    @Test
    void onlyHostCanApproveOrRejectApplication() throws Exception {
        TestUsers users = createUsers("transition-authority");
        String outsiderToken = signupAndLogin(
                "transition-authority-outsider@example.com",
                "transition-authority-outsider"
        );
        Long studyId = createStudy(users.hostToken(), "방장 처리 권한", 4);
        Long memberId = apply(studyId, users.applicantToken());

        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/approve", studyId, memberId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("스터디 방장만 처리할 수 있습니다."));

        assertThat(studyMemberRepository.findById(memberId).orElseThrow().getStatus())
                .isEqualTo(StudyMemberStatus.PENDING);
    }

    @Test
    void applicantCanQueryApplicationStatusAfterEachTransition() throws Exception {
        TestUsers users = createUsers("transition-query");
        Long studyId = createStudy(users.hostToken(), "신청 상태 조회", 4);
        Long memberId = apply(studyId, users.applicantToken());

        expectMyApplicationStatus(users.applicantToken(), studyId, "PENDING");

        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/reject", studyId, memberId)
                        .header("Authorization", "Bearer " + users.hostToken()))
                .andExpect(status().isOk());

        expectMyApplicationStatus(users.applicantToken(), studyId, "REJECTED");

        apply(studyId, users.applicantToken());
        expectMyApplicationStatus(users.applicantToken(), studyId, "PENDING");
    }

    @Test
    void pendingApplicantCanCancelAndApplyAgain() throws Exception {
        TestUsers users = createUsers("transition-cancel");
        Long studyId = createStudy(users.hostToken(), "신청 취소 후 재신청", 4);
        Long cancelledMemberId = apply(studyId, users.applicantToken());

        mockMvc.perform(delete("/api/studies/{studyId}/members/me/application", studyId)
                        .header("Authorization", "Bearer " + users.applicantToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스터디 참여 신청을 취소했습니다."));

        assertThat(studyMemberRepository.findById(cancelledMemberId)).isEmpty();

        Long reappliedMemberId = apply(studyId, users.applicantToken());
        assertThat(reappliedMemberId).isNotEqualTo(cancelledMemberId);
    }

    @Test
    void approvedMemberCanWithdrawAndMemberCountDecreases() throws Exception {
        TestUsers users = createUsers("transition-withdraw");
        Long studyId = createStudy(users.hostToken(), "승인 후 탈퇴", 4);
        Long memberId = apply(studyId, users.applicantToken());
        approve(studyId, memberId, users.hostToken());

        mockMvc.perform(delete("/api/studies/{studyId}/members/me/membership", studyId)
                        .header("Authorization", "Bearer " + users.applicantToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스터디 참여를 탈퇴했습니다."));

        Study study = studyRepository.findById(studyId).orElseThrow();
        assertThat(study.getCurrentMemberCount()).isEqualTo(1);
        assertThat(studyMemberRepository.findById(memberId)).isEmpty();
    }

    @Test
    void withdrawalReopensStudyClosedAutomaticallyByCapacity() throws Exception {
        TestUsers users = createUsers("auto-reopen");
        Long studyId = createStudy(users.hostToken(), "자동 마감 후 탈퇴", 2);
        Long memberId = apply(studyId, users.applicantToken());
        approve(studyId, memberId, users.hostToken());

        assertThat(studyRepository.findById(studyId).orElseThrow().getStatus())
                .isEqualTo(StudyStatus.CLOSED);

        mockMvc.perform(delete("/api/studies/{studyId}/members/me/membership", studyId)
                        .header("Authorization", "Bearer " + users.applicantToken()))
                .andExpect(status().isOk());

        Study reopenedStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(reopenedStudy.getCurrentMemberCount()).isEqualTo(1);
        assertThat(reopenedStudy.getStatus()).isEqualTo(StudyStatus.RECRUITING);
        assertThat(reopenedStudy.isRecruitmentClosedManually()).isFalse();
    }

    @Test
    void withdrawalKeepsManuallyClosedStudyClosed() throws Exception {
        TestUsers users = createUsers("manual-close");
        Long studyId = createStudy(users.hostToken(), "수동 마감 후 탈퇴", 4);
        Long memberId = apply(studyId, users.applicantToken());
        approve(studyId, memberId, users.hostToken());

        mockMvc.perform(patch("/api/studies/{studyId}/close", studyId)
                        .header("Authorization", "Bearer " + users.hostToken()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/studies/{studyId}/members/me/membership", studyId)
                        .header("Authorization", "Bearer " + users.applicantToken()))
                .andExpect(status().isOk());

        Study closedStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(closedStudy.getCurrentMemberCount()).isEqualTo(1);
        assertThat(closedStudy.getStatus()).isEqualTo(StudyStatus.CLOSED);
        assertThat(closedStudy.isRecruitmentClosedManually()).isTrue();
    }

    @Test
    void cancellationAndWithdrawalRequireMatchingStatus() throws Exception {
        TestUsers users = createUsers("invalid-exit");
        Long studyId = createStudy(users.hostToken(), "잘못된 취소 탈퇴 차단", 4);
        Long memberId = apply(studyId, users.applicantToken());

        mockMvc.perform(delete("/api/studies/{studyId}/members/me/membership", studyId)
                        .header("Authorization", "Bearer " + users.applicantToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("승인된 스터디 참여만 탈퇴할 수 있습니다."));

        approve(studyId, memberId, users.hostToken());

        mockMvc.perform(delete("/api/studies/{studyId}/members/me/application", studyId)
                        .header("Authorization", "Bearer " + users.applicantToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("대기 중인 참여 신청만 취소할 수 있습니다."));
    }

    private TestUsers createUsers(String prefix) throws Exception {
        String hostToken = signupAndLogin(prefix + "-host@example.com", prefix + "-host");
        String applicantToken = signupAndLogin(prefix + "-applicant@example.com", prefix + "-applicant");
        return new TestUsers(hostToken, applicantToken);
    }

    private Long apply(Long studyId, String applicantToken) throws Exception {
        MvcResult applyResult = mockMvc.perform(post("/api/studies/{studyId}/members", studyId)
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        return jsonLong(applyResult, "$.data.id");
    }

    private void approve(Long studyId, Long memberId, String hostToken) throws Exception {
        mockMvc.perform(patch("/api/studies/{studyId}/members/{memberId}/approve", studyId, memberId)
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    private void expectDuplicateApplication(Long studyId, String applicantToken) throws Exception {
        mockMvc.perform(post("/api/studies/{studyId}/members", studyId)
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 참여 신청한 스터디입니다."));
    }

    private void expectMyApplicationStatus(
            String applicantToken,
            Long studyId,
            String expectedStatus
    ) throws Exception {
        mockMvc.perform(get("/api/users/me/study-applications")
                        .param("status", expectedStatus)
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].studyId").value(studyId))
                .andExpect(jsonPath("$.data[0].applicationStatus").value(expectedStatus));
    }

    private record TestUsers(String hostToken, String applicantToken) {
    }
}
