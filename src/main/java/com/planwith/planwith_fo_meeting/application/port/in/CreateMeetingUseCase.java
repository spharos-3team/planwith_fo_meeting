package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

public interface CreateMeetingUseCase {

	Meeting create(Command command);

	record Command(
			UUID hostMemberUuid,
			UUID scheduleUuid,
			String title,
			String intro,
			int maxMemberCount,
			String coverImage,
			ScheduleSnapshot scheduleSnapshot
	) {
	}
}
