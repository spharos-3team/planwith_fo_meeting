package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.LeaveMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingParticipationChangedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingViceHostChangedEventPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@Service
public class LeaveMeetingService implements LeaveMeetingUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingParticipationChangedEventPort participationChangedEventPort;
	private final MeetingViceHostChangedEventPort viceHostChangedEventPort;

	public LeaveMeetingService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort,
			MeetingParticipationChangedEventPort participationChangedEventPort,
			MeetingViceHostChangedEventPort viceHostChangedEventPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
		this.participationChangedEventPort = participationChangedEventPort;
		this.viceHostChangedEventPort = viceHostChangedEventPort;
	}

	@Override
	@Transactional
	public MeetingMember leave(UUID meetingUuid, UUID memberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		MeetingMember member = meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), memberUuid)
				.filter(MeetingMember::isApproved)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_APPROVED));
		if (member.isHost()) {
			throw new BusinessException(ErrorCode.HOST_CANNOT_LEAVE);
		}
		Instant now = Instant.now();
		boolean wasViceHost = member.isViceHost();
		MeetingMember saved = meetingMemberRepositoryPort.save(member.withRole(MeetingRole.MEMBER).leave());
		meetingRepositoryPort.save(meeting.removeParticipant(now));
		participationChangedEventPort.publish(new MeetingParticipationChangedEventPort.MeetingParticipationChangedEvent(
				meeting.getMeetingUuid(),
				saved.getMemberUuid(),
				ParticipationStatus.LEFT,
				now
		));
		if (wasViceHost) {
			viceHostChangedEventPort.publish(new MeetingViceHostChangedEventPort.MeetingViceHostChangedEvent(
					meeting.getMeetingUuid(),
					null,
					now
			));
		}
		return saved;
	}
}
