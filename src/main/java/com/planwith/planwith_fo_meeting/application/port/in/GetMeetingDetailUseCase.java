package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface GetMeetingDetailUseCase {

	Result get(UUID meetingUuid, UUID viewerMemberUuid);

	record Result(
			Meeting meeting,
			MeetingMember myParticipation,
			boolean canApply,
			boolean canEnterChat,
			boolean canViewMembers
	) {
	}
}
