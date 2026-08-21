package com.planwith.planwith_fo_meeting.adapter.out.kafka.dto;

import java.util.UUID;

public record MeetingCreatedPayload(
		UUID meetingUuid,
		UUID hostMemberUuid,
		String title
) {
}
