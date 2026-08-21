package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface LeaveMeetingUseCase {

	MeetingMember leave(UUID meetingUuid, UUID memberUuid);
}
