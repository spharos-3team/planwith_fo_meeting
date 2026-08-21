package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.AssignViceHostUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingViceHostChangedEventPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;

@Service
public class AssignViceHostService implements AssignViceHostUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingViceHostChangedEventPort viceHostChangedEventPort;

	public AssignViceHostService(
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
	public MeetingMember assign(UUID meetingUuid, UUID hostMemberUuid, UUID targetMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(hostMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		if (!meeting.isEditable()) {
			throw new BusinessException(ErrorCode.MEETING_NOT_EDITABLE);
		}
		if (meeting.isHost(targetMemberUuid)) {
			throw new BusinessException(ErrorCode.CANNOT_ASSIGN_HOST_AS_VICE_HOST);
		}
		MeetingMember target = meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), targetMemberUuid)
				.filter(MeetingMember::isApproved)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_APPROVED));
		meetingMemberRepositoryPort.findByMeetingIdAndRole(meeting.getMeetingId(), MeetingRole.VICE_HOST)
				.filter(existing -> !existing.getMemberUuid().equals(targetMemberUuid))
				.ifPresent(existing -> meetingMemberRepositoryPort.save(existing.withRole(MeetingRole.MEMBER)));
		MeetingMember saved = target.isViceHost() ? target : meetingMemberRepositoryPort.save(target.withRole(MeetingRole.VICE_HOST));
		viceHostChangedEventPort.publish(new MeetingViceHostChangedEventPort.MeetingViceHostChangedEvent(
				meeting.getMeetingUuid(),
				saved.getMemberUuid(),
				Instant.now()
		));
		return saved;
	}
}
