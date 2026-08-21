package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.DecideMeetingApplicationUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingParticipationChangedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@Service
public class DecideMeetingApplicationService implements DecideMeetingApplicationUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingParticipationChangedEventPort participationChangedEventPort;

	public DecideMeetingApplicationService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort,
			MeetingParticipationChangedEventPort participationChangedEventPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
		this.participationChangedEventPort = participationChangedEventPort;
	}

	@Override
	@Transactional
	public MeetingMember approve(UUID meetingUuid, UUID memberUuid, UUID hostMemberUuid) {
		Meeting meeting = requireHostMeeting(meetingUuid, hostMemberUuid);
		MeetingMember application = requirePending(meeting, memberUuid);
		if (!meeting.canAcceptJoin()) {
			throw new BusinessException(ErrorCode.MEETING_FULL);
		}
		Instant now = Instant.now();
		MeetingMember saved = meetingMemberRepositoryPort.save(application.approve(now));
		meetingRepositoryPort.save(meeting.approveJoin(now));
		publish(meeting, saved, now);
		return saved;
	}

	@Override
	@Transactional
	public MeetingMember reject(UUID meetingUuid, UUID memberUuid, UUID hostMemberUuid) {
		Meeting meeting = requireHostMeeting(meetingUuid, hostMemberUuid);
		MeetingMember application = requirePending(meeting, memberUuid);
		Instant now = Instant.now();
		MeetingMember saved = meetingMemberRepositoryPort.save(application.reject());
		publish(meeting, saved, now);
		return saved;
	}

	private Meeting requireHostMeeting(UUID meetingUuid, UUID hostMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> found.getStatus() != MeetingStatus.DISBANDED)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		return meeting;
	}

	private MeetingMember requirePending(Meeting meeting, UUID memberUuid) {
		MeetingMember application = meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
		if (application.getStatus() != ParticipationStatus.PENDING) {
			throw new BusinessException(ErrorCode.APPLICATION_NOT_PENDING);
		}
		return application;
	}

	private void publish(Meeting meeting, MeetingMember member, Instant now) {
		participationChangedEventPort.publish(new MeetingParticipationChangedEventPort.MeetingParticipationChangedEvent(
				meeting.getMeetingUuid(),
				member.getMemberUuid(),
				member.getStatus(),
				now
		));
	}
}
