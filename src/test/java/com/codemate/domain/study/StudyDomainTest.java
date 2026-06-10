package com.codemate.domain.study;

import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.Study;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyDomainTest {

    @Test
    void reachingCapacityClosesRecruitmentAutomatically() {
        Study study = createStudy(2);

        study.increaseCurrentMemberCount();

        assertThat(study.getCurrentMemberCount()).isEqualTo(2);
        assertThat(study.getStatus()).isEqualTo(StudyStatus.CLOSED);
        assertThat(study.isRecruitmentClosedManually()).isFalse();
    }

    @Test
    void withdrawalReopensAutomaticallyClosedRecruitment() {
        Study study = createStudy(2);
        study.increaseCurrentMemberCount();

        study.decreaseCurrentMemberCount();

        assertThat(study.getCurrentMemberCount()).isEqualTo(1);
        assertThat(study.getStatus()).isEqualTo(StudyStatus.RECRUITING);
    }

    @Test
    void withdrawalKeepsManuallyClosedRecruitmentClosed() {
        Study study = createStudy(3);
        study.increaseCurrentMemberCount();
        study.closeRecruitment();

        study.decreaseCurrentMemberCount();

        assertThat(study.getStatus()).isEqualTo(StudyStatus.CLOSED);
        assertThat(study.isRecruitmentClosedManually()).isTrue();
    }

    @Test
    void memberCountCannotDropBelowHost() {
        Study study = createStudy(3);

        assertThatThrownBy(study::decreaseCurrentMemberCount)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Study member count cannot be less than the host count");
    }

    private Study createStudy(int maxMemberCount) {
        User host = User.builder()
                .email("host@example.com")
                .password("encoded-password")
                .nickname("host")
                .mainTechStack("Java")
                .role(UserRole.ROLE_USER)
                .build();

        return Study.builder()
                .host(host)
                .title("스터디")
                .content("스터디 내용")
                .category(StudyCategory.STUDY)
                .meetingType(MeetingType.ONLINE)
                .maxMemberCount(maxMemberCount)
                .currentMemberCount(1)
                .status(StudyStatus.RECRUITING)
                .build();
    }
}
