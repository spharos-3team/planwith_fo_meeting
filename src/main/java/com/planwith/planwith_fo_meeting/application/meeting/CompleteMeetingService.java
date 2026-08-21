package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.CompleteMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingCompletedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

@Service
public class CompleteMeetingService implements CompleteMeetingUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingCompletedEventPort meetingCompletedEventPort;

	public CompleteMeetingService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingCompletedEventPort meetingCompletedEventPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingCompletedEventPort = meetingCompletedEventPort;
	}

	@Override
	@Transactional
	public Meeting complete(UUID meetingUuid, UUID hostMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		if (meeting.isCompleted()) {
			throw new BusinessException(ErrorCode.MEETING_ALREADY_COMPLETED);
		}
		Instant now = Instant.now();
		Meeting saved = meetingRepositoryPort.save(meeting.complete(now));
		meetingCompletedEventPort.publish(new MeetingCompletedEventPort.MeetingCompletedEvent(
				saved.getMeetingUuid(),
				saved.getHostMemberUuid(),
				now
		));
		return saved;
	}
}
