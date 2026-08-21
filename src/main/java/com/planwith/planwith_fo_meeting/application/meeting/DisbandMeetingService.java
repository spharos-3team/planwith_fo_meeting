package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.DisbandMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingDisbandedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

@Service
public class DisbandMeetingService implements DisbandMeetingUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingDisbandedEventPort meetingDisbandedEventPort;

	public DisbandMeetingService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort,
			MeetingDisbandedEventPort meetingDisbandedEventPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
		this.meetingDisbandedEventPort = meetingDisbandedEventPort;
	}

	@Override
	@Transactional
	public Meeting disband(UUID meetingUuid, UUID hostMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		Instant now = Instant.now();
		for (MeetingMember member : meetingMemberRepositoryPort.findByMeetingId(meeting.getMeetingId())) {
			if (!member.isKicked()) {
				meetingMemberRepositoryPort.save(member.leave());
			}
		}
		Meeting saved = meetingRepositoryPort.save(meeting.disband(now));
		meetingDisbandedEventPort.publish(new MeetingDisbandedEventPort.MeetingDisbandedEvent(
				saved.getMeetingUuid(),
				saved.getHostMemberUuid(),
				now
		));
		return saved;
	}
}
