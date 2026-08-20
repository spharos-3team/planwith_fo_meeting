package com.planwith.planwith_fo_meeting.adapter.out.persistence.participation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting.MeetingJpaEntity;
import com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting.MeetingJpaRepository;
import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

@Component
@Transactional
public class MeetingMemberPersistenceAdapter implements MeetingMemberRepositoryPort {

	private final MeetingJpaRepository meetingJpaRepository;
	private final MeetingMemberJpaRepository meetingMemberJpaRepository;

	public MeetingMemberPersistenceAdapter(
			MeetingJpaRepository meetingJpaRepository,
			MeetingMemberJpaRepository meetingMemberJpaRepository
	) {
		this.meetingJpaRepository = meetingJpaRepository;
		this.meetingMemberJpaRepository = meetingMemberJpaRepository;
	}

	@Override
	public MeetingMember save(MeetingMember member) {
		MeetingJpaEntity meeting = meetingJpaRepository.findById(member.getMeetingId())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		MeetingMemberJpaEntity entity = new MeetingMemberJpaEntity();
		entity.setMeeting(meeting);
		entity.setMemberUuid(member.getMemberUuid().toString());
		entity.setRole(member.getRole());
		entity.setStatus(member.getStatus());
		entity.setJoinMessage(member.getJoinMessage());
		entity.setJoinAt(member.getJoinAt());
		entity.setJoinedAt(member.getJoinedAt());
		MeetingMemberJpaEntity saved = meetingMemberJpaRepository.save(entity);
		return toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MeetingMember> findByMeetingIdAndMemberUuid(Long meetingId, UUID memberUuid) {
		return meetingMemberJpaRepository
				.findByMeeting_MeetingIdAndMemberUuid(meetingId, memberUuid.toString())
				.map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MeetingMember> findByMeetingIdsAndMemberUuid(Collection<Long> meetingIds, UUID memberUuid) {
		if (meetingIds == null || meetingIds.isEmpty()) {
			return List.of();
		}
		return meetingMemberJpaRepository
				.findByMeeting_MeetingIdInAndMemberUuid(meetingIds, memberUuid.toString())
				.stream()
				.map(this::toDomain)
				.toList();
	}

	private MeetingMember toDomain(MeetingMemberJpaEntity saved) {
		return new MeetingMember(
				saved.getMeeting().getMeetingId(),
				UUID.fromString(saved.getMemberUuid()),
				saved.getRole(),
				saved.getStatus(),
				saved.getJoinMessage(),
				saved.getJoinAt(),
				saved.getJoinedAt()
		);
	}
}
