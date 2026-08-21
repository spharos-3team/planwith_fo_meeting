package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

public interface BumpMeetingUseCase {

	Meeting bump(UUID meetingUuid, UUID hostMemberUuid);
}
