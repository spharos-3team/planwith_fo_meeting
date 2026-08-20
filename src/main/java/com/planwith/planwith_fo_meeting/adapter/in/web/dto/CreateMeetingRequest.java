package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMeetingRequest(
		@NotNull(message = "일정은 필수입니다.")
		UUID scheduleUuid,
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 100, message = "제목은 100자 이하여야 합니다.")
		String title,
		@NotBlank(message = "소개는 필수입니다.")
		@Size(max = 2000, message = "소개는 2000자 이하여야 합니다.")
		String intro,
		@NotNull(message = "최대 인원은 필수입니다.")
		@Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
		@Max(value = 50, message = "최대 인원은 50명 이하여야 합니다.")
		Integer maxMemberCount,
		String coverImage
) {
}
