package com.planwith.planwith_fo_meeting.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface MeetingCompletedEventPort {

	void publish(MeetingCompletedEvent event);

	record MeetingCompletedEvent(
			UUID meetingUuid,
			UUID hostMemberUuid,
			Instant occurredAt
	) {
	}
}
