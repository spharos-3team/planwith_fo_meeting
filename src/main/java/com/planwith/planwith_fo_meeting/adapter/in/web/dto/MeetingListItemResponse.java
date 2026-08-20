package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingsUseCase;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public record MeetingListItemResponse(
		UUID meetingUuid,
		UUID memberUuid,
		String title,
		String coverImage,
		String destination,
		int maxMemberCount,
		int currentMemberCount,
		String status,
		Instant bumpAt,
		Instant createdAt,
		String myParticipation,
		boolean accessible,
		boolean canApply,
		boolean canEnterChat
) {

	public static MeetingListItemResponse from(ListMeetingsUseCase.Item item) {
		Meeting meeting = item.meeting();
		MeetingMember mine = item.myParticipation();
		return new MeetingListItemResponse(
				meeting.getMeetingUuid(),
				meeting.getHostMemberUuid(),
				meeting.getTitle(),
				meeting.getThumbnailUrl(),
				meeting.getScheduleSnapshot() == null ? null : meeting.getScheduleSnapshot().destination(),
				meeting.getMaxMemberCount(),
				meeting.getCurrentMemberCount(),
				meeting.getStatus().name(),
				meeting.getBumpAt(),
				meeting.getCreatedAt(),
				mine == null ? null : mine.getStatus().name(),
				item.accessible(),
				item.canApply(),
				item.canEnterChat()
		);
	}
}
