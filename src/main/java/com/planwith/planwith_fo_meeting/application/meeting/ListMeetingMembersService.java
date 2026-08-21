package com.planwith.planwith_fo_meeting.application.meeting;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingMembersUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MemberProfileQueryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@Service
public class ListMeetingMembersService implements ListMeetingMembersUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final MemberProfileQueryPort memberProfileQueryPort;

	public ListMeetingMembersService(
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
	public List<Result> list(UUID meetingUuid, UUID viewerMemberUuid) {
		Meeting meeting = requireMeeting(meetingUuid);
		requireApprovedViewer(meeting, viewerMemberUuid);
		return meetingMemberRepositoryPort.findByMeetingIdAndStatus(meeting.getMeetingId(), ParticipationStatus.APPROVED)
				.stream()
				.sorted(Comparator
						.comparingInt((MeetingMember member) -> roleOrder(member.getRole()))
						.thenComparing(MeetingMember::getJoinedAt, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(member -> new Result(member, memberProfileQueryPort.requireProfile(member.getMemberUuid())))
				.toList();
	}

	private Meeting requireMeeting(UUID meetingUuid) {
		return meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
	}

	private void requireApprovedViewer(Meeting meeting, UUID viewerMemberUuid) {
		meetingMemberRepositoryPort
				.findByMeetingIdAndMemberUuid(meeting.getMeetingId(), viewerMemberUuid)
				.filter(MeetingMember::isApproved)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_MEETING_PARTICIPANT));
	}

	private int roleOrder(MeetingRole role) {
		return switch (role) {
			case HOST -> 0;
			case VICE_HOST -> 1;
			case MEMBER -> 2;
		};
	}
}
