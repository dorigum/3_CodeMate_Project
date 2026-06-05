package com.codemate.domain.user.service;

import com.codemate.domain.user.dto.LoginRequest;
import com.codemate.domain.user.dto.LoginResponse;
import com.codemate.domain.user.dto.SignupRequest;
import com.codemate.domain.user.dto.SignupResponse;
import com.codemate.domain.user.dto.UserInfoResponse;
import com.codemate.domain.user.entity.User;
import com.codemate.domain.user.entity.UserRole;
import com.codemate.domain.user.repository.UserRepository;
import com.codemate.global.exception.BusinessException;
import com.codemate.global.exception.ErrorCode;
import com.codemate.global.security.CustomUserDetails;
import com.codemate.global.security.JwtTokenProvider;
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

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        CustomUserDetails userDetails = CustomUserDetails.from(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        return LoginResponse.bearer(jwtTokenProvider.createAccessToken(authentication));
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResponse.from(user);
    }

    private void validateDuplicateUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
