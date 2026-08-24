package com.planwith.planwith_fo_meeting.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

public interface ListMeetingsUseCase {

	Result list(Command command);

	record Command(
			MeetingStatus status,
			String destination,
			LocalDate from,
			LocalDate to,
			int page,
			int size
	) {
	}

	record Result(
			List<Meeting> content,
			int page,
			int size,
			long totalElements,
			int totalPages
	) {
	}
}
