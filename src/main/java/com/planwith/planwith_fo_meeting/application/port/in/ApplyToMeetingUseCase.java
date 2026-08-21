package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface ApplyToMeetingUseCase {

	MeetingMember apply(Command command);

	record Command(
			UUID meetingUuid,
			UUID memberUuid,
			String message
	) {
	}
}
