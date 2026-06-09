package com.codemate.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_STUDY_CAPACITY(HttpStatus.BAD_REQUEST, "모집 인원은 현재 인원보다 작을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    DUPLICATE_STUDY_APPLICATION(HttpStatus.CONFLICT, "이미 참여 신청한 스터디입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."),
    FORBIDDEN_STUDY_HOST(HttpStatus.FORBIDDEN, "스터디 방장만 처리할 수 있습니다."),
    CANNOT_APPLY_OWN_STUDY(HttpStatus.BAD_REQUEST, "본인이 만든 스터디에는 참여 신청할 수 없습니다."),
    STUDY_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "모집 중인 스터디에만 참여 신청할 수 있습니다."),
    STUDY_CAPACITY_FULL(HttpStatus.BAD_REQUEST, "모집 인원이 마감되었습니다."),
    STUDY_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "참여 신청 내역을 찾을 수 없습니다."),
    INVALID_STUDY_MEMBER_STATUS(HttpStatus.BAD_REQUEST, "대기 중인 신청만 처리할 수 있습니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "데이터 제약조건을 위반했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
