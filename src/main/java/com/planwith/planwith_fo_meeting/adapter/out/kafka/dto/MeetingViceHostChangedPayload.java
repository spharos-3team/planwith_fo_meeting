package com.planwith.planwith_fo_meeting.adapter.out.kafka.dto;

import java.util.UUID;

public record MeetingViceHostChangedPayload(
		UUID meetingUuid,
		UUID viceHostMemberUuid
) {
}
