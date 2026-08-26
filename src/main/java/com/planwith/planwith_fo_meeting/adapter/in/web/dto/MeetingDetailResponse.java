package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingDetailUseCase;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;
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
		boolean canViewMembers,
		String destination,
		LocalDate startDate,
		LocalDate endDate
) {

	public static MeetingDetailResponse from(GetMeetingDetailUseCase.Result result) {
		Meeting meeting = result.meeting();
		MeetingMember mine = result.myParticipation();
		ScheduleSnapshot snapshot = meeting.getScheduleSnapshot();
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
				result.canViewMembers(),
				snapshot == null ? null : snapshot.destination(),
				snapshot == null ? null : snapshot.startDate(),
				snapshot == null ? null : snapshot.endDate()
		);
	}
}
