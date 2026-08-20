package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

@Component
@Transactional
public class MeetingPersistenceAdapter implements MeetingRepositoryPort {

	private final MeetingJpaRepository meetingJpaRepository;

	public MeetingPersistenceAdapter(MeetingJpaRepository meetingJpaRepository) {
		this.meetingJpaRepository = meetingJpaRepository;
	}

	@Override
	public Meeting save(Meeting meeting) {
		MeetingJpaEntity entity = meeting.getMeetingUuid() == null
				? new MeetingJpaEntity()
				: meetingJpaRepository.findByMeetingUuid(meeting.getMeetingUuid().toString())
						.orElseGet(MeetingJpaEntity::new);
		entity.setMeetingUuid(meeting.getMeetingUuid().toString());
		entity.setMemberUuid(meeting.getHostMemberUuid().toString());
		entity.setScheduleUuid(meeting.getScheduleUuid() == null ? null : meeting.getScheduleUuid().toString());
		entity.setTitle(meeting.getTitle());
		entity.setDescription(meeting.getDescription());
		entity.setThumbnailUrl(meeting.getThumbnailUrl());
		entity.setDestination(meeting.getScheduleSnapshot() == null ? null : meeting.getScheduleSnapshot().destination());
		entity.setMaxMember(meeting.getMaxMemberCount());
		entity.setCurrentMember(meeting.getCurrentMemberCount());
		entity.setMeetingStatus(meeting.getStatus());
		entity.setBumpAt(meeting.getBumpAt());
		entity.setCreatedAt(meeting.getCreatedAt());
		entity.setUpdatedAt(meeting.getUpdatedAt());
		return toDomain(meetingJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Meeting> findByMeetingUuid(UUID meetingUuid) {
		return meetingJpaRepository.findByMeetingUuid(meetingUuid.toString()).map(this::toDomain);
	}

	private Meeting toDomain(MeetingJpaEntity entity) {
		UUID scheduleUuid = entity.getScheduleUuid() == null ? null : UUID.fromString(entity.getScheduleUuid());
		return new Meeting(
				entity.getMeetingId(),
				UUID.fromString(entity.getMeetingUuid()),
				UUID.fromString(entity.getMemberUuid()),
				scheduleUuid,
				entity.getTitle(),
				entity.getDescription(),
				entity.getMaxMember(),
				entity.getCurrentMember(),
				entity.getMeetingStatus(),
				entity.getThumbnailUrl(),
				entity.getBumpAt(),
				new ScheduleSnapshot(scheduleUuid, entity.getDestination(), null, null, null, null),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
