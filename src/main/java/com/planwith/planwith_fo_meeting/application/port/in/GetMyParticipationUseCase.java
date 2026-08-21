package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface GetMyParticipationUseCase {

	Optional<MeetingMember> get(UUID meetingUuid, UUID memberUuid);
}
