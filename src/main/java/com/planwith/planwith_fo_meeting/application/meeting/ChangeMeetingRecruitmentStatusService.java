package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.ChangeMeetingRecruitmentStatusUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingUpdatedEventPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

@Service
public class ChangeMeetingRecruitmentStatusService implements ChangeMeetingRecruitmentStatusUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingUpdatedEventPort meetingUpdatedEventPort;

	public ChangeMeetingRecruitmentStatusService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingUpdatedEventPort meetingUpdatedEventPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingUpdatedEventPort = meetingUpdatedEventPort;
	}

	@Override
	@Transactional
	public Meeting change(UUID meetingUuid, UUID hostMemberUuid, MeetingStatus status) {
		if (status != MeetingStatus.RECRUITING && status != MeetingStatus.FULL) {
			throw new BusinessException(ErrorCode.INVALID_RECRUITMENT_STATUS);
		}
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> found.getStatus() != MeetingStatus.DISBANDED)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		if (!meeting.isEditable()) {
			throw new BusinessException(ErrorCode.MEETING_NOT_EDITABLE);
		}
		if (status == MeetingStatus.RECRUITING && meeting.getCurrentMemberCount() >= meeting.getMaxMemberCount()) {
			throw new BusinessException(ErrorCode.MEETING_FULL);
		}
		Instant now = Instant.now();
		Meeting saved = meetingRepositoryPort.save(meeting.withRecruitmentStatus(status, now));
		meetingUpdatedEventPort.publish(new MeetingUpdatedEventPort.MeetingUpdatedEvent(
				saved.getMeetingUuid(),
				saved.getHostMemberUuid(),
				now
		));
		return saved;
	}
}
