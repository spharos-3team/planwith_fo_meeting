package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.port.out.MeetingPage;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
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
		ScheduleSnapshot snapshot = meeting.getScheduleSnapshot();
		entity.setDestination(snapshot == null ? null : snapshot.destination());
		entity.setStartDate(snapshot == null ? null : snapshot.startDate());
		entity.setEndDate(snapshot == null ? null : snapshot.endDate());
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

	@Override
	@Transactional(readOnly = true)
	public MeetingPage searchPublic(MeetingStatus status, int page, int size) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 50);
		Page<MeetingJpaEntity> result = meetingJpaRepository.searchPublic(status, PageRequest.of(safePage, safeSize));
		return new MeetingPage(
				result.getContent().stream().map(this::toDomain).toList(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages()
		);
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
				new ScheduleSnapshot(
						scheduleUuid,
						entity.getDestination(),
						entity.getStartDate(),
						entity.getEndDate()
				),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
