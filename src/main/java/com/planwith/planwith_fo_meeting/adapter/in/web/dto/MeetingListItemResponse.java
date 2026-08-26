package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.application.port.out.MemberProfileQueryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

public record MeetingListItemResponse(
		UUID meetingUuid,
		UUID hostMemberUuid,
		String hostNickname,
		String title,
		String coverImage,
		String intro,
		int maxMemberCount,
		int currentMemberCount,
		String destination,
		LocalDate startDate,
		LocalDate endDate,
		String status
) {

	public static MeetingListItemResponse from(
			Meeting meeting,
			MemberProfileQueryPort.MemberProfile host
	) {
		ScheduleSnapshot snapshot = meeting.getScheduleSnapshot();
		return new MeetingListItemResponse(
				meeting.getMeetingUuid(),
				meeting.getHostMemberUuid(),
				host == null ? null : host.nickname(),
				meeting.getTitle(),
				meeting.getThumbnailUrl(),
				meeting.getDescription(),
				meeting.getMaxMemberCount(),
				meeting.getCurrentMemberCount(),
				snapshot == null ? null : snapshot.destination(),
				snapshot == null ? null : snapshot.startDate(),
				snapshot == null ? null : snapshot.endDate(),
				meeting.getStatus().name()
		);
	}
}
