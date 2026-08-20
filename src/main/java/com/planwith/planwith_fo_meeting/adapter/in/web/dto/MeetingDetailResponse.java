package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingDetailUseCase;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public record MeetingDetailResponse(
		UUID meetingUuid,
		UUID memberUuid,
		UUID scheduleUuid,
		String title,
		String intro,
		String coverImage,
		int maxMemberCount,
		int currentMemberCount,
		String status,
		Instant createdAt,
		String myParticipation,
		String myRole,
		boolean canApply,
		boolean canEnterChat,
		boolean canViewMembers
) {

	public static MeetingDetailResponse from(GetMeetingDetailUseCase.Result result) {
		Meeting meeting = result.meeting();
		MeetingMember mine = result.myParticipation();
		return new MeetingDetailResponse(
				meeting.getMeetingUuid(),
				meeting.getHostMemberUuid(),
				meeting.getScheduleUuid(),
				meeting.getTitle(),
				meeting.getDescription(),
				meeting.getThumbnailUrl(),
				meeting.getMaxMemberCount(),
				meeting.getCurrentMemberCount(),
				meeting.getStatus().name(),
				meeting.getCreatedAt(),
				mine == null ? null : mine.getStatus().name(),
				mine == null ? null : mine.getRole().name(),
				result.canApply(),
				result.canEnterChat(),
				result.canViewMembers()
		);
	}
}
