package com.planwith.planwith_fo_meeting.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface MeetingUpdatedEventPort {

	void publish(MeetingUpdatedEvent event);

	record MeetingUpdatedEvent(
			UUID meetingUuid,
			UUID hostMemberUuid,
			Instant occurredAt
	) {
	}
}
