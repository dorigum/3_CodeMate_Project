package com.codemate.domain.studymember.controller;

import com.codemate.domain.studymember.dto.StudyMemberResponse;
import com.codemate.domain.studymember.entity.StudyMemberStatus;
import com.codemate.domain.studymember.service.StudyMemberService;
import com.codemate.global.response.ApiResponse;
import com.codemate.global.security.CustomUserDetails;
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
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudyMemberResponse>>> getStudyMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @RequestParam(required = false) StudyMemberStatus status
    ) {
        List<StudyMemberResponse> response = studyMemberService.getStudyMembers(userDetails.getId(), studyId, status);
        return ResponseEntity.ok(ApiResponse.success("스터디 참여 신청 목록을 조회했습니다.", response));
    }

    @PostMapping
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
    public ResponseEntity<ApiResponse<StudyMemberResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long memberId
    ) {
        StudyMemberResponse response = studyMemberService.approve(userDetails.getId(), studyId, memberId);
        return ResponseEntity.ok(ApiResponse.success("스터디 참여 신청을 승인했습니다.", response));
    }

    @PatchMapping("/{memberId}/reject")
    public ResponseEntity<ApiResponse<StudyMemberResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long memberId
    ) {
        StudyMemberResponse response = studyMemberService.reject(userDetails.getId(), studyId, memberId);
        return ResponseEntity.ok(ApiResponse.success("스터디 참여 신청을 거절했습니다.", response));
    }
}
