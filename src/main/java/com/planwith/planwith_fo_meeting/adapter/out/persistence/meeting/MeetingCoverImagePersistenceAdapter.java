package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.port.out.CoverImageStoragePort;

@Component
@Transactional
public class MeetingCoverImagePersistenceAdapter implements CoverImageStoragePort {

	private final MeetingCoverImageJpaRepository meetingCoverImageJpaRepository;

	public MeetingCoverImagePersistenceAdapter(MeetingCoverImageJpaRepository meetingCoverImageJpaRepository) {
		this.meetingCoverImageJpaRepository = meetingCoverImageJpaRepository;
	}

	@Override
	public void save(UUID meetingUuid, String contentType, byte[] bytes) {
		MeetingCoverImageJpaEntity entity = meetingCoverImageJpaRepository.findById(meetingUuid.toString())
				.orElseGet(MeetingCoverImageJpaEntity::new);
		entity.setMeetingUuid(meetingUuid.toString());
		entity.setContentType(contentType);
		entity.setImageBytes(bytes);
		meetingCoverImageJpaRepository.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoredCoverImage> find(UUID meetingUuid) {
		return meetingCoverImageJpaRepository.findById(meetingUuid.toString())
				.map(entity -> new StoredCoverImage(entity.getContentType(), entity.getImageBytes()));
	}
}
