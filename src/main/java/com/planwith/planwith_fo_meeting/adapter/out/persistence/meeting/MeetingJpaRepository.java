package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

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
			         coalesce(m.bumpAt, m.createdAt) desc,
			         m.createdAt desc
			""")
	Page<MeetingJpaEntity> searchPublic(@Param("status") MeetingStatus status, Pageable pageable);

	@Query("""
			select m from MeetingJpaEntity m
			where m.memberUuid = :memberUuid
			  and m.meetingStatus <> com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus.DISBANDED
			  and (:status is null or m.meetingStatus = :status)
			order by case when m.meetingStatus = com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus.FULL then 1 else 0 end,
			         coalesce(m.bumpAt, m.createdAt) desc,
			         m.createdAt desc
			""")
	Page<MeetingJpaEntity> searchHosted(
			@Param("memberUuid") String memberUuid,
			@Param("status") MeetingStatus status,
			Pageable pageable
	);

	@Query("""
			select m from MeetingJpaEntity m
			join com.planwith.planwith_fo_meeting.adapter.out.persistence.participation.MeetingMemberJpaEntity mm
			  on mm.meeting = m
			where mm.memberUuid = :memberUuid
			  and mm.status = :participation
			  and (:excludeHost = false or mm.role <> com.planwith.planwith_fo_meeting.domain.participation.MeetingRole.HOST)
			  and m.meetingStatus <> com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus.DISBANDED
			  and (:status is null or m.meetingStatus = :status)
			order by case when m.meetingStatus = com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus.FULL then 1 else 0 end,
			         coalesce(m.bumpAt, m.createdAt) desc,
			         m.createdAt desc
			""")
	Page<MeetingJpaEntity> searchByParticipation(
			@Param("memberUuid") String memberUuid,
			@Param("participation") ParticipationStatus participation,
			@Param("excludeHost") boolean excludeHost,
			@Param("status") MeetingStatus status,
			Pageable pageable
	);
}
