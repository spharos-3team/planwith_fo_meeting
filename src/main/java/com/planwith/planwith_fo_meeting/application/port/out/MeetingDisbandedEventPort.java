package com.planwith.planwith_fo_meeting.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface MeetingDisbandedEventPort {

	void publish(MeetingDisbandedEvent event);

	record MeetingDisbandedEvent(
			UUID meetingUuid,
			UUID hostMemberUuid,
			Instant occurredAt
	) {
	}
}
