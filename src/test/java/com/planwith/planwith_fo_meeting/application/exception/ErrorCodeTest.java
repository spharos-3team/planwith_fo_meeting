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
		assertThat(ErrorCode.INTERNAL_SERVER_ERROR.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
