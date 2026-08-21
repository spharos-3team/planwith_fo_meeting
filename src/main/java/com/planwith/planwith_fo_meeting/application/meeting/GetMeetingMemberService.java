package com.planwith.planwith_fo_meeting.application.meeting;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingMemberUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MemberProfileQueryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

@Service
public class GetMeetingMemberService implements GetMeetingMemberUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MemberProfileQueryPort memberProfileQueryPort;

	public GetMeetingMemberService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort,
			MemberProfileQueryPort memberProfileQueryPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
		this.memberProfileQueryPort = memberProfileQueryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result get(UUID meetingUuid, UUID targetMemberUuid, UUID viewerMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		meetingMemberRepositoryPort.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), viewerMemberUuid)
				.filter(MeetingMember::isApproved)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_MEETING_PARTICIPANT));
		MeetingMember target = meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), targetMemberUuid)
				.filter(MeetingMember::isApproved)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		return new Result(target, memberProfileQueryPort.requireProfile(target.getMemberUuid()));
	}
}
