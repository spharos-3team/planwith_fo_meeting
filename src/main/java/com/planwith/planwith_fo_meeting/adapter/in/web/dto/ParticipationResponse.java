package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public record ParticipationResponse(
		UUID meetingUuid,
		UUID memberUuid,
		String status,
		String role
) {

	public static ParticipationResponse from(UUID meetingUuid, MeetingMember member) {
		if (member == null) {
			return new ParticipationResponse(meetingUuid, null, null, null);
		}
		return new ParticipationResponse(
				meetingUuid,
				member.getMemberUuid(),
				member.getStatus().name(),
				member.getRole().name()
		);
	}
}
