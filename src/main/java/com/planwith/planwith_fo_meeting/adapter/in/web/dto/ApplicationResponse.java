package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public record ApplicationResponse(
		UUID memberUuid,
		String message,
		String status,
		String role,
		Instant joinAt,
		Instant joinedAt
) {

	public static ApplicationResponse from(MeetingMember member) {
		return new ApplicationResponse(
				member.getMemberUuid(),
				member.getJoinMessage(),
				member.getStatus().name(),
				member.getRole().name(),
				member.getJoinAt(),
				member.getJoinedAt()
		);
	}
}
