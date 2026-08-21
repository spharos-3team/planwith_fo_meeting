package com.planwith.planwith_fo_meeting.adapter.out.kafka.dto;

import java.util.UUID;

public record MeetingParticipationChangedPayload(
		UUID meetingUuid,
		UUID memberUuid,
		String status
) {
}
