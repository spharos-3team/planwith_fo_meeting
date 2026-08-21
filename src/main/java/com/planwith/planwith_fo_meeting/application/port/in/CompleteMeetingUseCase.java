package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

public interface CompleteMeetingUseCase {

	Meeting complete(UUID meetingUuid, UUID hostMemberUuid);
}
