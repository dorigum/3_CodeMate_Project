package com.codemate.domain.study.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Length;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = Length.LONG32)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeetingType meetingType;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private int maxMemberCount;

    @Column(nullable = false)
    private int currentMemberCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus status;

    @Builder
    private Study(
            User host,
            String title,
            String content,
            StudyCategory category,
            MeetingType meetingType,
            String location,
            int maxMemberCount,
            int currentMemberCount,
            StudyStatus status
    ) {
        this.host = host;
        this.title = title;
        this.content = content;
        this.category = category;
        this.meetingType = meetingType;
        this.location = location;
        this.maxMemberCount = maxMemberCount;
        this.currentMemberCount = currentMemberCount;
        this.status = status;
    }

    public void update(
            String title,
            String content,
            StudyCategory category,
            MeetingType meetingType,
            String location,
            int maxMemberCount,
            StudyStatus status
    ) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.meetingType = meetingType;
        this.location = location;
        this.maxMemberCount = maxMemberCount;
        this.status = status;
    }

    public boolean isHostedBy(Long userId) {
        return host.getId().equals(userId);
    }

    public boolean isRecruiting() {
        return status == StudyStatus.RECRUITING;
    }

    public boolean isFull() {
        return currentMemberCount >= maxMemberCount;
    }

    public void increaseCurrentMemberCount() {
        this.currentMemberCount++;

        if (isFull()) {
            this.status = StudyStatus.CLOSED;
        }
    }
}
