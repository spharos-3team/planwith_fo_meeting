package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.CreateMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingCreatedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.ScheduleQueryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

@Service
public class CreateMeetingService implements CreateMeetingUseCase {

	private final ScheduleQueryPort scheduleQueryPort;
	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingCreatedEventPort meetingCreatedEventPort;

	public CreateMeetingService(
			ScheduleQueryPort scheduleQueryPort,
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort,
			MeetingCreatedEventPort meetingCreatedEventPort
	) {
		this.scheduleQueryPort = scheduleQueryPort;
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
		this.meetingCreatedEventPort = meetingCreatedEventPort;
	}

	@Override
	@Transactional
	public Meeting create(Command command) {
		if (command.scheduleUuid() == null) {
			throw new BusinessException(ErrorCode.SCHEDULE_REQUIRED);
		}
		ScheduleSnapshot lookedUp = scheduleQueryPort.requireSchedule(command.scheduleUuid());
		ScheduleSnapshot snapshot = mergeSnapshot(lookedUp, command.scheduleSnapshot());
		Instant now = Instant.now();
		Meeting meeting = Meeting.create(
				command.hostMemberUuid(),
				command.scheduleUuid(),
				command.title().trim(),
				command.intro().trim(),
				command.maxMemberCount(),
				blankToNull(command.coverImage()),
				snapshot,
				now
		);
		Meeting saved = meetingRepositoryPort.save(meeting);
		meetingMemberRepositoryPort.save(MeetingMember.host(saved.getMeetingId(), saved.getHostMemberUuid(), now));
		meetingCreatedEventPort.publish(new MeetingCreatedEventPort.MeetingCreatedEvent(
				saved.getMeetingUuid(),
				saved.getHostMemberUuid(),
				saved.getScheduleUuid(),
				saved.getTitle(),
				now
		));
		return saved;
	}

	private ScheduleSnapshot mergeSnapshot(ScheduleSnapshot lookedUp, ScheduleSnapshot requested) {
		if (requested == null) {
			return lookedUp;
		}
		return new ScheduleSnapshot(
				lookedUp.scheduleUuid(),
				firstText(requested.destination(), lookedUp.destination()),
				requested.startAt() != null ? requested.startAt() : lookedUp.startAt(),
				requested.endAt() != null ? requested.endAt() : lookedUp.endAt(),
				firstText(requested.cost(), lookedUp.cost()),
				firstText(requested.transport(), lookedUp.transport())
		);
	}

	private String firstText(String preferred, String fallback) {
		return StringUtils.hasText(preferred) ? preferred : fallback;
	}

	private String blankToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
