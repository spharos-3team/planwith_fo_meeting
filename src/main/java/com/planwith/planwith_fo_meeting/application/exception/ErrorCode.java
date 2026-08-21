package com.planwith.planwith_fo_meeting.application.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값이 올바르지 않습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
	MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_NOT_FOUND", "모임을 찾을 수 없습니다."),
	SCHEDULE_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE_REQUIRED", "일정이 없으면 모임을 만들 수 없습니다."),
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "일정을 찾을 수 없습니다."),
	INVALID_COVER_IMAGE(HttpStatus.BAD_REQUEST, "INVALID_COVER_IMAGE", "대표 이미지 형식이 올바르지 않습니다."),
	NOT_MEETING_HOST(HttpStatus.FORBIDDEN, "NOT_MEETING_HOST", "방장만 수행할 수 있습니다."),
	ALREADY_APPLIED(HttpStatus.CONFLICT, "ALREADY_APPLIED", "이미 신청한 모임입니다."),
	ALREADY_PARTICIPATING(HttpStatus.CONFLICT, "ALREADY_PARTICIPATING", "이미 참여 중인 모임입니다."),
	KICKED_MEMBER(HttpStatus.FORBIDDEN, "KICKED_MEMBER", "강퇴된 모임에는 다시 신청할 수 없습니다."),
	CANNOT_APPLY_OWN_MEETING(HttpStatus.FORBIDDEN, "CANNOT_APPLY_OWN_MEETING", "내가 만든 모임에는 신청할 수 없습니다."),
	MEETING_NOT_RECRUITING(HttpStatus.CONFLICT, "MEETING_NOT_RECRUITING", "모집중인 모임만 신청할 수 있습니다."),
	MEETING_FULL(HttpStatus.CONFLICT, "MEETING_FULL", "정원이 가득 찼습니다."),
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", "신청을 찾을 수 없습니다."),
	APPLICATION_NOT_PENDING(HttpStatus.CONFLICT, "APPLICATION_NOT_PENDING", "대기 중인 신청만 처리할 수 있습니다."),
	MEETING_ALREADY_COMPLETED(HttpStatus.CONFLICT, "MEETING_ALREADY_COMPLETED", "이미 완료된 모임입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public String message() {
		return message;
	}
}
