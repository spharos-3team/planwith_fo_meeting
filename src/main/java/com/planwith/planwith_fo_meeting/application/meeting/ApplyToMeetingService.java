package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.ApplyToMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingParticipationChangedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@Service
public class ApplyToMeetingService implements ApplyToMeetingUseCase {

	private static final Logger log = LoggerFactory.getLogger(ApplyToMeetingService.class);

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingParticipationChangedEventPort participationChangedEventPort;

	public ApplyToMeetingService(
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
	public MeetingMember apply(Command command) {
		long started = System.nanoTime();
		try {
			return doApply(command);
		}
		finally {
			log.info(
					"apply elapsedMs={} meetingUuid={}",
					(System.nanoTime() - started) / 1_000_000,
					command.meetingUuid()
			);
		}
	}

	private MeetingMember doApply(Command command) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(command.meetingUuid())
				.filter(found -> found.getStatus() != MeetingStatus.DISBANDED)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (meeting.isHost(command.memberUuid())) {
			throw new BusinessException(ErrorCode.CANNOT_APPLY_OWN_MEETING);
		}
		if (!meeting.isRecruiting()) {
			throw new BusinessException(ErrorCode.MEETING_NOT_RECRUITING);
		}
		Instant now = Instant.now();
		String message = StringUtils.hasText(command.message()) ? command.message().trim() : null;
		MeetingMember existing = meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), command.memberUuid())
				.orElse(null);
		MeetingMember saved;
		if (existing == null) {
			saved = meetingMemberRepositoryPort.save(
					MeetingMember.apply(meeting.getMeetingId(), command.memberUuid(), message, now)
			);
		}
		else if (existing.getStatus() == ParticipationStatus.KICKED) {
			throw new BusinessException(ErrorCode.KICKED_MEMBER);
		}
		else if (existing.getStatus() == ParticipationStatus.PENDING) {
			throw new BusinessException(ErrorCode.ALREADY_APPLIED);
		}
		else if (existing.getStatus() == ParticipationStatus.APPROVED) {
			throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING);
		}
		else if (existing.canReapply()) {
			saved = meetingMemberRepositoryPort.save(existing.reapply(message, now));
		}
		else {
			throw new BusinessException(ErrorCode.ALREADY_APPLIED);
		}
		participationChangedEventPort.publish(new MeetingParticipationChangedEventPort.MeetingParticipationChangedEvent(
				meeting.getMeetingUuid(),
				saved.getMemberUuid(),
				saved.getStatus(),
				now
		));
		return saved;
	}
}
