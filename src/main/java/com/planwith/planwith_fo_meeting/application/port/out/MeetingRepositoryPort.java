package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingScope;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

public interface MeetingRepositoryPort {

	Meeting save(Meeting meeting);

	Optional<Meeting> findByMeetingUuid(UUID meetingUuid);

	MeetingPage searchPublic(MeetingStatus status, int page, int size);

	MeetingPage searchMine(UUID memberUuid, MeetingScope scope, MeetingStatus status, int page, int size);
}
