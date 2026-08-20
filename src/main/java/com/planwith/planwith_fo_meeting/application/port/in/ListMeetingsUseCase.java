package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.List;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

public interface ListMeetingsUseCase {

	Result list(MeetingStatus status, int page, int size);

	record Result(
			List<Meeting> content,
			int page,
			int size,
			long totalElements,
			int totalPages
	) {
	}
}
