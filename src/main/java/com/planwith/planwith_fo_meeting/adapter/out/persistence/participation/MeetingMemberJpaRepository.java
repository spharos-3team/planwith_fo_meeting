package com.planwith.planwith_fo_meeting.adapter.out.persistence.participation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

public interface MeetingMemberJpaRepository extends JpaRepository<MeetingMemberJpaEntity, Long> {

	Optional<MeetingMemberJpaEntity> findByMeeting_MeetingIdAndMemberUuid(Long meetingId, String memberUuid);

	List<MeetingMemberJpaEntity> findByMeeting_MeetingIdInAndMemberUuid(Collection<Long> meetingIds, String memberUuid);

	List<MeetingMemberJpaEntity> findByMeeting_MeetingIdAndStatusOrderByJoinAtAsc(
			Long meetingId,
			ParticipationStatus status
	);

	List<MeetingMemberJpaEntity> findByMeeting_MeetingId(Long meetingId);

	Optional<MeetingMemberJpaEntity> findByMeeting_MeetingIdAndRole(Long meetingId, MeetingRole role);
}
