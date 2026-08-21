package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface AssignViceHostUseCase {

	MeetingMember assign(UUID meetingUuid, UUID hostMemberUuid, UUID targetMemberUuid);
}
