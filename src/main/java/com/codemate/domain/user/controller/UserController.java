package com.codemate.domain.user.controller;

import com.codemate.domain.user.dto.LoginRequest;
import com.codemate.domain.user.dto.LoginResponse;
import com.codemate.domain.user.dto.SignupRequest;
import com.codemate.domain.user.dto.SignupResponse;
import com.codemate.domain.user.dto.UserInfoResponse;
import com.codemate.domain.user.service.UserService;
import com.codemate.global.config.OpenApiConfig;
import com.codemate.global.response.ApiResponse;
import com.codemate.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "회원가입, 로그인, 로그인 사용자 정보 API")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 신규 회원을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
    })
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 JWT access token을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 정보 불일치")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
    }

    @GetMapping("/me")
    @Operation(
            summary = "내 정보 조회",
            description = "JWT로 인증된 현재 사용자의 정보를 조회합니다.",
            security = @SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않은 토큰")
    })
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse response = userService.getUserInfo(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("내 정보를 조회했습니다.", response));
    }
}
