package com.codemate.domain.study.dto;

import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.StudyCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 모집 글 생성 요청")
public record StudyCreateRequest(

        @Schema(description = "모집 글 제목", example = "Spring Boot 사이드 프로젝트 팀원 모집")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
        String title,

        @Schema(description = "모집 글 본문", example = "주 1회 온라인으로 코드 리뷰를 진행합니다.")
        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @Schema(description = "모집 유형. STUDY: 스터디, MOGAKKO: 모여서 각자 코딩", example = "STUDY")
        @NotNull(message = "카테고리는 필수입니다.")
        StudyCategory category,

        @Schema(description = "진행 방식. ONLINE: 온라인, OFFLINE: 오프라인", example = "ONLINE")
        @NotNull(message = "진행 방식은 필수입니다.")
        MeetingType meetingType,

        @Schema(description = "오프라인 진행 장소. 온라인이면 생략할 수 있습니다.", example = "서울 강남")
        @Size(max = 100, message = "장소는 100자 이하로 입력해주세요.")
        String location,

        @Schema(description = "방장을 포함한 최대 인원", example = "5", minimum = "2")
        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
        int maxMemberCount,

        @Schema(description = "사용 기술 스택 목록", example = "[\"Java\",\"Spring Boot\",\"MySQL\"]")
        @Size(max = 10, message = "기술 스택은 최대 10개까지 선택할 수 있습니다.")
        List<@NotBlank(message = "기술 스택 이름은 비어 있을 수 없습니다.")
        @Size(max = 50, message = "기술 스택 이름은 50자 이하로 입력해주세요.") String> techStackNames
) {
}
