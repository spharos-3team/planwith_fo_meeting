package com.planwith.planwith_fo_meeting.application.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeTest {

	@Test
	void commonCodesMatchHttpStatus() {
		assertThat(ErrorCode.INVALID_REQUEST.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.INVALID_CREDENTIALS.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(ErrorCode.UNAUTHORIZED.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(ErrorCode.FORBIDDEN.status()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(ErrorCode.MEETING_NOT_FOUND.status()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(ErrorCode.SCHEDULE_REQUIRED.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.NOT_MEETING_HOST.status()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(ErrorCode.ALREADY_APPLIED.status()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(ErrorCode.MEETING_FULL.status()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(ErrorCode.APPLICATION_NOT_FOUND.status()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(ErrorCode.MAX_MEMBER_TOO_SMALL.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.BUMP_NOT_ALLOWED.status()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(ErrorCode.BUMP_TOO_SOON.status()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(ErrorCode.MEETING_ALREADY_COMPLETED.status()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(ErrorCode.INTERNAL_SERVER_ERROR.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
