package com.codemate.domain.user.entity;

import com.codemate.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(length = 50)
    private String mainTechStack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private long tokenVersion;

    @Builder
    private User(String email, String password, String nickname, String mainTechStack, UserRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.mainTechStack = mainTechStack;
        this.role = role;
    }

    public void updateProfile(String nickname, String mainTechStack) {
        this.nickname = nickname;
        this.mainTechStack = mainTechStack;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void invalidateTokens() {
        this.tokenVersion++;
    }
}
