package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignViceHostRequest(
		@NotNull(message = "memberUuid는 필수입니다.")
		UUID memberUuid
) {
}
