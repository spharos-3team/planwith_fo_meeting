package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

public interface MeetingRepositoryPort {

	Meeting save(Meeting meeting);

	Optional<Meeting> findByMeetingUuid(UUID meetingUuid);
}
