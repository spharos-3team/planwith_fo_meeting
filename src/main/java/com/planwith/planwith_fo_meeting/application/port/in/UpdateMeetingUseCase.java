package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

public interface UpdateMeetingUseCase {

	Meeting update(Command command);

	record Command(
			UUID meetingUuid,
			UUID hostMemberUuid,
			UUID scheduleUuid,
			String title,
			String intro,
			Integer maxMemberCount
	) {
	}
}
