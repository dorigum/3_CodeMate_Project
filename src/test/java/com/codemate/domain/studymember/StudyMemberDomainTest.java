package com.codemate.domain.studymember;

import com.codemate.domain.studymember.entity.StudyMember;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudyMemberDomainTest {

    @Test
    void pendingApplicationCanBeApproved() {
        StudyMember studyMember = createMember(StudyMemberStatus.PENDING);

        studyMember.approve();

        assertThat(studyMember.isApproved()).isTrue();
        assertThat(studyMember.isPending()).isFalse();
    }

    @Test
    void pendingApplicationCanBeRejected() {
        StudyMember studyMember = createMember(StudyMemberStatus.PENDING);

        studyMember.reject();

        assertThat(studyMember.isRejected()).isTrue();
    }

    @Test
    void rejectedApplicationCanReturnToPending() {
        StudyMember studyMember = createMember(StudyMemberStatus.REJECTED);

        studyMember.reapply();

        assertThat(studyMember.isPending()).isTrue();
    }

    private StudyMember createMember(StudyMemberStatus status) {
        return StudyMember.builder()
                .status(status)
                .build();
    }
}
