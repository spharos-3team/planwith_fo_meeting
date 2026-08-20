package com.planwith.planwith_fo_meeting.adapter.in.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.config.GatewayTrustProperties;

class AuthenticatedUserContextTest {

	private final GatewayAuthenticationContextResolver resolver =
			new GatewayAuthenticationContextResolver(new GatewayTrustProperties("token", false));

	@AfterEach
	void clearContext() {
		AuthenticatedUserContext.clear();
	}

	@Test
	void requireUserThrowsWhenContextIsEmpty() {
		assertThatThrownBy(resolver::requireUser)
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	@Test
	void requireUserReturnsAuthenticatedUser() {
		AuthenticatedUser user = new AuthenticatedUser(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				List.of("USER"),
				List.of(),
				"session-1",
				"req-1"
		);
		AuthenticatedUserContext.set(user);

		assertThat(resolver.requireUser()).isEqualTo(user);
	}
}
