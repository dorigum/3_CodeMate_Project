package com.codemate.domain.studymember.entity;

import com.codemate.domain.study.entity.Study;
import com.codemate.domain.user.entity.User;
import com.codemate.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "study_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"study_id", "user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyMemberStatus status;

    @Builder
    private StudyMember(Study study, User user, StudyMemberStatus status) {
        this.study = study;
        this.user = user;
        this.status = status;
    }

    public boolean isPending() {
        return status == StudyMemberStatus.PENDING;
    }

    public boolean isRejected() {
        return status == StudyMemberStatus.REJECTED;
    }

    public boolean isApproved() {
        return status == StudyMemberStatus.APPROVED;
    }

    public void reapply() {
        this.status = StudyMemberStatus.PENDING;
    }

    public void approve() {
        this.status = StudyMemberStatus.APPROVED;
    }

    public void reject() {
        this.status = StudyMemberStatus.REJECTED;
    }
}
