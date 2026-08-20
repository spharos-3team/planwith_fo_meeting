package com.planwith.planwith_fo_meeting.application.meeting;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingDetailUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

@Service
public class GetMeetingDetailService implements GetMeetingDetailUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;

	public GetMeetingDetailService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result get(UUID meetingUuid, UUID viewerMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> found.getStatus() != MeetingStatus.DISBANDED)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		MeetingMember member = viewerMemberUuid == null
				? null
				: meetingMemberRepositoryPort.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), viewerMemberUuid)
						.orElse(null);
		if (!MeetingViewerPolicy.accessible(member)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "강퇴된 모임은 조회할 수 없습니다.");
		}
		return new Result(
				meeting,
				member,
				MeetingViewerPolicy.canApply(meeting, member),
				MeetingViewerPolicy.canEnterChat(member),
				MeetingViewerPolicy.canViewMembers(member)
		);
	}
}
