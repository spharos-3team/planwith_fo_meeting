package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.ClearViceHostUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingViceHostChangedEventPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;

@Service
public class ClearViceHostService implements ClearViceHostUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingViceHostChangedEventPort viceHostChangedEventPort;

	public ClearViceHostService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort,
			MeetingViceHostChangedEventPort viceHostChangedEventPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
		this.viceHostChangedEventPort = viceHostChangedEventPort;
	}

	@Override
	@Transactional
	public MeetingMember clear(UUID meetingUuid, UUID hostMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		if (!meeting.isEditable()) {
			throw new BusinessException(ErrorCode.MEETING_NOT_EDITABLE);
		}
		MeetingMember viceHost = meetingMemberRepositoryPort
				.findByMeetingIdAndRole(meeting.getMeetingId(), MeetingRole.VICE_HOST)
				.filter(MeetingMember::isApproved)
				.orElseThrow(() -> new BusinessException(ErrorCode.VICE_HOST_NOT_FOUND));
		MeetingMember saved = meetingMemberRepositoryPort.save(viceHost.withRole(MeetingRole.MEMBER));
		viceHostChangedEventPort.publish(new MeetingViceHostChangedEventPort.MeetingViceHostChangedEvent(
				meeting.getMeetingUuid(),
				null,
				Instant.now()
		));
		return saved;
	}
}
