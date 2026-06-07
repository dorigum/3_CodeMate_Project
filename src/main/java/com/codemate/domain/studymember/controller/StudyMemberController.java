package com.codemate.domain.studymember.controller;

import com.codemate.domain.studymember.dto.StudyMemberResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies/{studyId}/members")
@Tag(name = "Study Members", description = "스터디 참여 신청, 신청 목록, 승인·거절 API")
@SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    @GetMapping
    @Operation(summary = "참여 신청 목록 조회", description = "방장만 전체 또는 상태별 참여 신청 목록을 조회할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "방장 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "스터디 없음")
    })
    public ResponseEntity<ApiResponse<List<StudyMemberResponse>>> getStudyMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @RequestParam(required = false) StudyMemberStatus status
    ) {
        List<StudyMemberResponse> response = studyMemberService.getStudyMembers(userDetails.getId(), studyId, status);
        return ResponseEntity.ok(ApiResponse.success("스터디 참여 신청 목록을 조회했습니다.", response));
    }

    @PostMapping
    @Operation(summary = "스터디 참여 신청", description = "로그인 사용자가 다른 사용자의 모집 중인 스터디에 참여 신청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "신청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "본인 스터디, 모집 마감 또는 정원 초과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "스터디 또는 사용자 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 신청")
    })
    public ResponseEntity<ApiResponse<StudyMemberResponse>> apply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId
    ) {
        StudyMemberResponse response = studyMemberService.apply(userDetails.getId(), studyId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("스터디 참여 신청이 완료되었습니다.", response));
    }

    @PatchMapping("/{memberId}/approve")
    @Operation(summary = "참여 신청 승인", description = "방장이 대기 중인 신청을 승인합니다. 동시 승인 시에도 정원을 초과하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "승인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "대기 상태가 아니거나 모집 정원 마감"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "방장 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "스터디 또는 신청 내역 없음")
    })
    public ResponseEntity<ApiResponse<StudyMemberResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long memberId
    ) {
        StudyMemberResponse response = studyMemberService.approve(userDetails.getId(), studyId, memberId);
        return ResponseEntity.ok(ApiResponse.success("스터디 참여 신청을 승인했습니다.", response));
    }

    @PatchMapping("/{memberId}/reject")
    @Operation(summary = "참여 신청 거절", description = "방장이 대기 중인 신청을 거절합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "거절 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "대기 상태가 아닌 신청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "방장 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "스터디 또는 신청 내역 없음")
    })
    public ResponseEntity<ApiResponse<StudyMemberResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long memberId
    ) {
        StudyMemberResponse response = studyMemberService.reject(userDetails.getId(), studyId, memberId);
        return ResponseEntity.ok(ApiResponse.success("스터디 참여 신청을 거절했습니다.", response));
    }
}
