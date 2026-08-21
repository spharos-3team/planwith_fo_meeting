package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.application.port.out.MemberProfileQueryPort;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface GetMeetingMemberUseCase {

	Result get(UUID meetingUuid, UUID targetMemberUuid, UUID viewerMemberUuid);

	record Result(MeetingMember member, MemberProfileQueryPort.MemberProfile profile) {
	}
}
