package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingJpaRepository extends JpaRepository<MeetingJpaEntity, Long> {

	Optional<MeetingJpaEntity> findByMeetingUuid(String meetingUuid);
}
