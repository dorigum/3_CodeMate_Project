package com.codemate.domain.study.controller;

import com.codemate.domain.study.dto.StudyCreateRequest;
import com.codemate.domain.study.dto.StudyResponse;
import com.codemate.domain.study.dto.StudySummaryResponse;
import com.codemate.domain.study.dto.StudyUpdateRequest;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.study.service.StudyService;
import com.codemate.global.response.ApiResponse;
import com.codemate.global.response.PageResponse;
import com.codemate.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies")
public class StudyController {

    private final StudyService studyService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudyResponse>> createStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StudyCreateRequest request
    ) {
        StudyResponse response = studyService.createStudy(userDetails.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("스터디 모집 글이 생성되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudySummaryResponse>>> getStudies(
            @RequestParam(required = false) StudyCategory category,
            @RequestParam(required = false) StudyStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<StudySummaryResponse> response = studyService.getStudies(category, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글 목록을 조회했습니다.", PageResponse.from(response)));
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<ApiResponse<StudyResponse>> getStudy(@PathVariable Long studyId) {
        StudyResponse response = studyService.getStudy(studyId);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글을 조회했습니다.", response));
    }

    @PatchMapping("/{studyId}")
    public ResponseEntity<ApiResponse<StudyResponse>> updateStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @Valid @RequestBody StudyUpdateRequest request
    ) {
        StudyResponse response = studyService.updateStudy(userDetails.getId(), studyId, request);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글이 수정되었습니다.", response));
    }

    @DeleteMapping("/{studyId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId
    ) {
        studyService.deleteStudy(userDetails.getId(), studyId);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글이 삭제되었습니다."));
    }
}
