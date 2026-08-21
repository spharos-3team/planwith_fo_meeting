package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

import jakarta.validation.constraints.NotNull;

public record RecruitmentStatusRequest(
		@NotNull(message = "모집상태는 필수입니다.")
		MeetingStatus status
) {
}
