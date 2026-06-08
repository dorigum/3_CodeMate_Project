package com.codemate.domain.studymember.controller;

import com.codemate.domain.studymember.dto.MyStudyApplicationResponse;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import com.codemate.domain.studymember.service.StudyMemberService;
import com.codemate.global.config.OpenApiConfig;
import com.codemate.global.response.ApiResponse;
import com.codemate.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/study-applications")
@Tag(name = "Study Members", description = "스터디 참여 신청, 신청 목록, 승인·거절 API")
@SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
public class MyStudyApplicationController {

    private final StudyMemberService studyMemberService;

    @GetMapping
    @Operation(
            summary = "내 스터디 신청 내역 조회",
            description = "로그인 사용자가 자신이 신청한 스터디와 신청 처리 상태를 최신 신청순으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<List<MyStudyApplicationResponse>>> getMyStudyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) StudyMemberStatus status
    ) {
        List<MyStudyApplicationResponse> response =
                studyMemberService.getMyStudyApplications(userDetails.getId(), status);

        return ResponseEntity.ok(ApiResponse.success("내 스터디 신청 내역을 조회했습니다.", response));
    }
}
