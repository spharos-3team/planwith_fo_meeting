package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingMemberUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingMembersUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MemberProfileQueryPort;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public record MeetingMemberResponse(
		UUID memberUuid,
		String role,
		String status,
		Instant joinedAt,
		String nickname,
		String profileImageUrl
) {

	public static MeetingMemberResponse from(ListMeetingMembersUseCase.Result result) {
		return from(result.member(), result.profile());
	}

	public static MeetingMemberResponse from(GetMeetingMemberUseCase.Result result) {
		return from(result.member(), result.profile());
	}

	public static MeetingMemberResponse from(MeetingMember member, MemberProfileQueryPort.MemberProfile profile) {
		return new MeetingMemberResponse(
				member.getMemberUuid(),
				member.getRole().name(),
				member.getStatus().name(),
				member.getJoinedAt(),
				profile.nickname(),
				profile.profileImageUrl()
		);
	}

	public static MeetingMemberResponse from(MeetingMember member) {
		return new MeetingMemberResponse(
				member.getMemberUuid(),
				member.getRole().name(),
				member.getStatus().name(),
				member.getJoinedAt(),
				null,
				null
		);
	}
}
