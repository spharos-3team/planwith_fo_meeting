package com.planwith.planwith_fo_meeting.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface MeetingViceHostChangedEventPort {

	void publish(MeetingViceHostChangedEvent event);

	record MeetingViceHostChangedEvent(
			UUID meetingUuid,
			UUID viceHostMemberUuid,
			Instant occurredAt
	) {
	}
}
