package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingDetailUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.ScheduleQueryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

@Service
public class GetMeetingDetailService implements GetMeetingDetailUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;
	private final ScheduleQueryPort scheduleQueryPort;

	public GetMeetingDetailService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort,
			ScheduleQueryPort scheduleQueryPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
		this.scheduleQueryPort = scheduleQueryPort;
	}

	@Override
	@Transactional
	public Result get(UUID meetingUuid, UUID viewerMemberUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> found.getStatus() != MeetingStatus.DISBANDED)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		meeting = refreshSnapshotIfBlank(meeting);
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

	private Meeting refreshSnapshotIfBlank(Meeting meeting) {
		if (meeting.getScheduleUuid() == null || hasSnapshot(meeting.getScheduleSnapshot())) {
			return meeting;
		}
		try {
			ScheduleSnapshot snapshot = scheduleQueryPort.requireSchedule(meeting.getScheduleUuid());
			if (!hasSnapshot(snapshot)) {
				return meeting;
			}
			return meetingRepositoryPort.save(meeting.withDetails(
					meeting.getScheduleUuid(),
					meeting.getTitle(),
					meeting.getDescription(),
					meeting.getMaxMemberCount(),
					meeting.getStatus(),
					snapshot,
					Instant.now()
			));
		}
		catch (RuntimeException exception) {
			return meeting;
		}
	}

	private boolean hasSnapshot(ScheduleSnapshot snapshot) {
		return snapshot != null
				&& (StringUtils.hasText(snapshot.destination()) || snapshot.startDate() != null);
	}
}
