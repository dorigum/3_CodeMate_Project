package com.codemate.domain.study.dto;

import com.codemate.domain.study.entity.MeetingType;
import com.codemate.domain.study.entity.StudyCategory;
import com.codemate.domain.study.entity.StudyStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record StudyUpdateRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "카테고리는 필수입니다.")
        StudyCategory category,

        @NotNull(message = "진행 방식은 필수입니다.")
        MeetingType meetingType,

        @Size(max = 100, message = "장소는 100자 이하로 입력해주세요.")
        String location,

        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
        int maxMemberCount,

        @NotNull(message = "모집 상태는 필수입니다.")
        StudyStatus status,

        @Size(max = 10, message = "기술 스택은 최대 10개까지 선택할 수 있습니다.")
        List<@NotBlank(message = "기술 스택 이름은 비어 있을 수 없습니다.")
        @Size(max = 50, message = "기술 스택 이름은 50자 이하로 입력해주세요.") String> techStackNames
) {
}
