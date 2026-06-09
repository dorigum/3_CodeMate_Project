package com.codemate.domain.user.service;

import com.codemate.domain.user.dto.LoginRequest;
import com.codemate.domain.user.dto.LoginResponse;
import com.codemate.domain.user.dto.PasswordChangeRequest;
import com.codemate.domain.user.dto.SignupRequest;
import com.codemate.domain.user.dto.SignupResponse;
import com.codemate.domain.user.dto.UserInfoResponse;
import com.codemate.domain.user.dto.UserUpdateRequest;
import com.codemate.domain.user.dto.TokenRefreshRequest;
import com.codemate.domain.user.entity.RefreshToken;
import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.entity.UserRole;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.domain.user.repository.RefreshTokenRepository;
import com.codemate.global.exception.BusinessException;
import com.codemate.global.exception.ErrorCode;
import com.codemate.global.security.CustomUserDetails;
import com.codemate.global.security.JwtTokenProvider;
import com.codemate.global.security.RefreshTokenHasher;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicateUser(request);

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .mainTechStack(request.mainTechStack())
                .role(UserRole.ROLE_USER)
                .build();

        return SignupResponse.from(userRepository.save(user));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse refresh(TokenRefreshRequest request) {
        Claims claims = parseRefreshToken(request.refreshToken());
        String tokenHash = refreshTokenHasher.hash(request.refreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (storedToken.isExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        User user = storedToken.getUser();
        if (!user.getEmail().equals(jwtTokenProvider.getEmail(claims))
                || user.getTokenVersion() != jwtTokenProvider.getTokenVersion(claims)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return issueTokens(user);
    }

    @Transactional
    public void logout(Long userId) {
        User user = getUser(userId);
        user.invalidateTokens();
        refreshTokenRepository.deleteByUserId(userId);
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User user = getUser(userId);
        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = getUser(userId);

        if (userRepository.existsByNicknameAndIdNot(request.nickname(), userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateProfile(request.nickname(), request.mainTechStack());
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = getUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        user.invalidateTokens();
        refreshTokenRepository.deleteByUserId(userId);
    }

    private void validateDuplicateUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private LoginResponse issueTokens(User user) {
        CustomUserDetails userDetails = CustomUserDetails.from(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);
        LocalDateTime expiresAt = jwtTokenProvider.getExpiresAt(
                jwtTokenProvider.parseRefreshToken(refreshToken)
        );

        RefreshToken storedToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> RefreshToken.create(user, refreshTokenHash, expiresAt));
        storedToken.rotate(refreshTokenHash, expiresAt);
        refreshTokenRepository.save(storedToken);

        return LoginResponse.bearer(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds(),
                jwtTokenProvider.getRefreshTokenExpiresInSeconds()
        );
    }

    private Claims parseRefreshToken(String refreshToken) {
        try {
            return jwtTokenProvider.parseRefreshToken(refreshToken);
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
