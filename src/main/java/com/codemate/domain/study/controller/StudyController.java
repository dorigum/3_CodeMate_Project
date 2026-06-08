package com.codemate.domain.study.controller;

import com.codemate.domain.study.dto.StudyCreateRequest;
import com.codemate.domain.study.dto.StudyResponse;
import com.codemate.domain.study.dto.StudySearchCondition;
import com.codemate.domain.study.dto.StudySummaryResponse;
import com.codemate.domain.study.dto.StudyUpdateRequest;
import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import com.codemate.domain.study.service.StudyService;
import com.codemate.global.config.OpenApiConfig;
import com.codemate.global.response.ApiResponse;
import com.codemate.global.response.PageResponse;
import com.codemate.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Studies", description = "스터디·모각코 모집 글 CRUD 및 복합 검색 API")
public class StudyController {

    private final StudyService studyService;

    @PostMapping
    @Operation(
            summary = "모집 글 생성",
            description = "로그인 사용자를 방장으로 하는 스터디 또는 모각코 모집 글을 생성합니다.",
            security = @SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(
            summary = "모집 글 목록 및 검색",
            description = "키워드, 카테고리, 상태, 진행 방식, 지역, 기술 스택 조건을 조합해 페이징 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목록 조회 성공")
    public ResponseEntity<ApiResponse<PageResponse<StudySummaryResponse>>> getStudies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StudyCategory category,
            @RequestParam(required = false) StudyStatus status,
            @RequestParam(required = false) MeetingType meetingType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String techStack,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        StudySearchCondition condition = new StudySearchCondition(
                keyword,
                category,
                status,
                meetingType,
                location,
                techStack
        );
        Page<StudySummaryResponse> response = studyService.getStudies(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글 목록을 조회했습니다.", PageResponse.from(response)));
    }

    @GetMapping("/{studyId}")
    @Operation(summary = "모집 글 상세 조회", description = "모집 글과 연결된 기술 스택을 함께 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모집 글 없음")
    })
    public ResponseEntity<ApiResponse<StudyResponse>> getStudy(@PathVariable Long studyId) {
        StudyResponse response = studyService.getStudy(studyId);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글을 조회했습니다.", response));
    }

    @PatchMapping("/{studyId}")
    @Operation(
            summary = "모집 글 수정",
            description = "방장만 모집 글과 기술 스택을 수정할 수 있습니다.",
            security = @SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 또는 모집 인원 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "방장 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모집 글 없음")
    })
    public ResponseEntity<ApiResponse<StudyResponse>> updateStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @Valid @RequestBody StudyUpdateRequest request
    ) {
        StudyResponse response = studyService.updateStudy(userDetails.getId(), studyId, request);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글이 수정되었습니다.", response));
    }

    @DeleteMapping("/{studyId}")
    @Operation(
            summary = "모집 글 삭제",
            description = "방장만 모집 글을 삭제할 수 있습니다.",
            security = @SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "방장 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모집 글 없음")
    })
    public ResponseEntity<ApiResponse<Void>> deleteStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId
    ) {
        studyService.deleteStudy(userDetails.getId(), studyId);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집 글이 삭제되었습니다."));
    }
}
