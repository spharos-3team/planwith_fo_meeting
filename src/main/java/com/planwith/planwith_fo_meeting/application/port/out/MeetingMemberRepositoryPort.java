package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

public interface MeetingMemberRepositoryPort {

	MeetingMember save(MeetingMember member);

	Optional<MeetingMember> findByMeetingIdAndMemberUuid(Long meetingId, UUID memberUuid);

	List<MeetingMember> findByMeetingIdsAndMemberUuid(Collection<Long> meetingIds, UUID memberUuid);

	List<MeetingMember> findByMeetingIdAndStatus(Long meetingId, ParticipationStatus status);

	List<MeetingMember> findByMeetingId(Long meetingId);
}
