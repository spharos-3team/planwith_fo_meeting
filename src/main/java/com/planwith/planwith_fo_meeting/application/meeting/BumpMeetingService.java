package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.BumpMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.GradeQueryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

@Service
public class BumpMeetingService implements BumpMeetingUseCase {

	static final Duration BUMP_INTERVAL = Duration.ofHours(6);

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final GradeQueryPort gradeQueryPort;

	public BumpMeetingService(MeetingRepositoryPort meetingRepositoryPort, GradeQueryPort gradeQueryPort) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.gradeQueryPort = gradeQueryPort;
	}

	@Override
	@Transactional
	public Meeting bump(UUID meetingUuid, UUID hostMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> found.getStatus() != MeetingStatus.DISBANDED)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		if (!meeting.isEditable()) {
			throw new BusinessException(ErrorCode.MEETING_NOT_EDITABLE);
		}
		if (!gradeQueryPort.canBump(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.BUMP_NOT_ALLOWED);
		}
		Instant now = Instant.now();
		if (!meeting.canBumpAt(now, BUMP_INTERVAL)) {
			throw new BusinessException(ErrorCode.BUMP_TOO_SOON);
		}
		return meetingRepositoryPort.save(meeting.bump(now));
	}
}
