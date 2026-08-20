package com.planwith.planwith_fo_meeting.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface MeetingCreatedEventPort {

	void publish(MeetingCreatedEvent event);

	record MeetingCreatedEvent(
			UUID meetingUuid,
			UUID hostMemberUuid,
			UUID scheduleUuid,
			String title,
			Instant occurredAt
	) {
	}
}
