package com.codemate.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "CodeMate 공통 오류 응답")
public record ErrorResponse(
        @Schema(description = "요청 성공 여부", example = "false")
        boolean success,

        @Schema(description = "오류 메시지", example = "입력값이 올바르지 않습니다.")
        String message,

        @Schema(
                description = "필드별 검증 오류. 요청값 검증 실패 시에만 포함됩니다.",
                example = "{\"email\":\"이메일 형식이 올바르지 않습니다.\",\"password\":\"비밀번호는 필수입니다.\"}"
        )
        Map<String, String> errors
) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message, Map.of());
    }

    public static ErrorResponse validation(String message, Map<String, String> errors) {
        return new ErrorResponse(false, message, errors);
    }
}
