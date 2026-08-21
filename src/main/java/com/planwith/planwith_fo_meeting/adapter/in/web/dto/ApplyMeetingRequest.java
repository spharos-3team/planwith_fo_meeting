package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

public record ApplyMeetingRequest(
		@Size(max = 300, message = "신청 메시지는 300자 이하여야 합니다.")
		String message
) {
}
