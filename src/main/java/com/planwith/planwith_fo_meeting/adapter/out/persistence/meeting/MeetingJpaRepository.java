package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

public interface MeetingJpaRepository extends JpaRepository<MeetingJpaEntity, Long> {

	Optional<MeetingJpaEntity> findByMeetingUuid(String meetingUuid);

	@Query("""
			select m from MeetingJpaEntity m
			where m.meetingStatus <> com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus.DISBANDED
			  and (
			        :status is not null and m.meetingStatus = :status
			        or :status is null and m.meetingStatus <> com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus.COMPLETED
			      )
			order by case when m.meetingStatus = com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus.FULL then 1 else 0 end,
			         m.bumpAt desc nulls last,
			         m.createdAt desc
			""")
	Page<MeetingJpaEntity> searchPublic(@Param("status") MeetingStatus status, Pageable pageable);
}
