package com.planwith.planwith_fo_meeting.adapter.out.persistence.participation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingMemberJpaRepository extends JpaRepository<MeetingMemberJpaEntity, Long> {

	Optional<MeetingMemberJpaEntity> findByMeeting_MeetingIdAndMemberUuid(Long meetingId, String memberUuid);

	List<MeetingMemberJpaEntity> findByMeeting_MeetingIdInAndMemberUuid(Collection<Long> meetingIds, String memberUuid);
}
