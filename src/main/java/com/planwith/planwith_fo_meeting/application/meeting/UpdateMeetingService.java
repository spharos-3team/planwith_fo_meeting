package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.UpdateMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingUpdatedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.ScheduleQueryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

@Service
public class UpdateMeetingService implements UpdateMeetingUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final ScheduleQueryPort scheduleQueryPort;
	private final MeetingUpdatedEventPort meetingUpdatedEventPort;

	public UpdateMeetingService(
			MeetingRepositoryPort meetingRepositoryPort,
			ScheduleQueryPort scheduleQueryPort,
			MeetingUpdatedEventPort meetingUpdatedEventPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.scheduleQueryPort = scheduleQueryPort;
		this.meetingUpdatedEventPort = meetingUpdatedEventPort;
	}

	@Override
	@Transactional
	public Meeting update(Command command) {
		if (command.scheduleUuid() == null
				&& !StringUtils.hasText(command.title())
				&& !StringUtils.hasText(command.intro())
				&& command.maxMemberCount() == null) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "수정할 값이 없습니다.");
		}
		Meeting meeting = requireEditableHost(command.meetingUuid(), command.hostMemberUuid());
		UUID scheduleUuid = command.scheduleUuid() == null ? meeting.getScheduleUuid() : command.scheduleUuid();
		ScheduleSnapshot snapshot = scheduleQueryPort.requireSchedule(scheduleUuid);
		String title = StringUtils.hasText(command.title()) ? command.title().trim() : meeting.getTitle();
		String intro = StringUtils.hasText(command.intro()) ? command.intro().trim() : meeting.getDescription();
		int maxMemberCount = command.maxMemberCount() == null
				? meeting.getMaxMemberCount()
				: command.maxMemberCount();
		if (maxMemberCount < meeting.getCurrentMemberCount()) {
			throw new BusinessException(ErrorCode.MAX_MEMBER_TOO_SMALL);
		}
		MeetingStatus status = meeting.getStatus();
		if (status == MeetingStatus.RECRUITING && meeting.getCurrentMemberCount() >= maxMemberCount) {
			status = MeetingStatus.FULL;
		}
		Instant now = Instant.now();
		Meeting saved = meetingRepositoryPort.save(meeting.withDetails(
				scheduleUuid,
				title,
				intro,
				maxMemberCount,
				status,
				snapshot,
				now
		));
		meetingUpdatedEventPort.publish(new MeetingUpdatedEventPort.MeetingUpdatedEvent(
				saved.getMeetingUuid(),
				saved.getHostMemberUuid(),
				now
		));
		return saved;
	}

	private Meeting requireEditableHost(UUID meetingUuid, UUID hostMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> found.getStatus() != MeetingStatus.DISBANDED)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		if (!meeting.isEditable()) {
			throw new BusinessException(ErrorCode.MEETING_NOT_EDITABLE);
		}
		return meeting;
	}
}
