package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface ListMeetingsUseCase {

	Result list(MeetingStatus status, int page, int size, UUID viewerMemberUuid);

	record Item(
			Meeting meeting,
			MeetingMember myParticipation,
			boolean accessible,
			boolean canApply,
			boolean canEnterChat
	) {
	}

	record Result(
			java.util.List<Item> content,
			int page,
			int size,
			long totalElements,
			int totalPages
	) {
	}
}
