package com.planwith.planwith_fo_meeting.application.port.out;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

public interface MeetingParticipationChangedEventPort {

	void publish(MeetingParticipationChangedEvent event);

	record MeetingParticipationChangedEvent(
			UUID meetingUuid,
			UUID memberUuid,
			ParticipationStatus status,
			Instant occurredAt
	) {
	}
}
