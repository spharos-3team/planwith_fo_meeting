package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface DecideMeetingApplicationUseCase {

	MeetingMember approve(UUID meetingUuid, UUID memberUuid, UUID hostMemberUuid);

	MeetingMember reject(UUID meetingUuid, UUID memberUuid, UUID hostMemberUuid);
}
