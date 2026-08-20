package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

public record MeetingResponse(
		UUID meetingUuid,
		UUID memberUuid,
		UUID scheduleUuid,
		String title,
		String intro,
		int maxMemberCount,
		int currentMemberCount,
		String status,
		String coverImage,
		Instant createdAt
) {

	public static MeetingResponse from(Meeting meeting) {
		return new MeetingResponse(
				meeting.getMeetingUuid(),
				meeting.getHostMemberUuid(),
				meeting.getScheduleUuid(),
				meeting.getTitle(),
				meeting.getDescription(),
				meeting.getMaxMemberCount(),
				meeting.getCurrentMemberCount(),
				meeting.getStatus().name(),
				meeting.getThumbnailUrl(),
				meeting.getCreatedAt()
		);
	}
}
