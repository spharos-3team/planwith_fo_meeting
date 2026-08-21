package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.KickMeetingMemberUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingParticipationChangedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingViceHostChangedEventPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@Service
public class KickMeetingMemberService implements KickMeetingMemberUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MeetingParticipationChangedEventPort participationChangedEventPort;
	private final MeetingViceHostChangedEventPort viceHostChangedEventPort;

	public KickMeetingMemberService(
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
	public MeetingMember kick(UUID meetingUuid, UUID actorMemberUuid, UUID targetMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isEditable()) {
			throw new BusinessException(ErrorCode.MEETING_NOT_EDITABLE);
		}
		if (actorMemberUuid.equals(targetMemberUuid)) {
			throw new BusinessException(ErrorCode.CANNOT_KICK_SELF);
		}
		MeetingMember actor = meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), actorMemberUuid)
				.filter(MeetingMember::canManageMembers)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_MEETING_MANAGER));
		MeetingMember target = meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), targetMemberUuid)
				.filter(MeetingMember::isApproved)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_APPROVED));
		if (target.isHost()) {
			throw new BusinessException(ErrorCode.CANNOT_KICK_HOST);
		}
		if (actor.isViceHost() && target.isViceHost()) {
			throw new BusinessException(ErrorCode.NOT_MEETING_MANAGER);
		}
		Instant now = Instant.now();
		boolean wasViceHost = target.isViceHost();
		MeetingMember saved = meetingMemberRepositoryPort.save(target.kick());
		meetingRepositoryPort.save(meeting.removeParticipant(now));
		participationChangedEventPort.publish(new MeetingParticipationChangedEventPort.MeetingParticipationChangedEvent(
				meeting.getMeetingUuid(),
				saved.getMemberUuid(),
				ParticipationStatus.KICKED,
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
