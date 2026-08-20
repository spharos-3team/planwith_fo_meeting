package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.List;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

public record MeetingPage(
		List<Meeting> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
