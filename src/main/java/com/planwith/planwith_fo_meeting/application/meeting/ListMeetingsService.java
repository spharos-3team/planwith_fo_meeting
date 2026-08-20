package com.planwith.planwith_fo_meeting.application.meeting;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingsUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingPage;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

@Service
public class ListMeetingsService implements ListMeetingsUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final MeetingMemberRepositoryPort meetingMemberRepositoryPort;

	public ListMeetingsService(
			MeetingRepositoryPort meetingRepositoryPort,
			MeetingMemberRepositoryPort meetingMemberRepositoryPort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.meetingMemberRepositoryPort = meetingMemberRepositoryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result list(MeetingStatus status, int page, int size, UUID viewerMemberUuid) {
		MeetingPage meetingPage = meetingRepositoryPort.searchPublic(status, page, size);
		Map<Long, MeetingMember> mineByMeetingId = Map.of();
		if (viewerMemberUuid != null && !meetingPage.content().isEmpty()) {
			List<Long> ids = meetingPage.content().stream().map(Meeting::getMeetingId).toList();
			mineByMeetingId = meetingMemberRepositoryPort.findByMeetingIdsAndMemberUuid(ids, viewerMemberUuid)
					.stream()
					.collect(Collectors.toMap(MeetingMember::getMeetingId, Function.identity()));
		}
		Map<Long, MeetingMember> participation = mineByMeetingId;
		List<Item> items = meetingPage.content().stream()
				.map(meeting -> {
					MeetingMember member = participation.get(meeting.getMeetingId());
					return new Item(
							meeting,
							member,
							MeetingViewerPolicy.accessible(member),
							MeetingViewerPolicy.canApply(meeting, member),
							MeetingViewerPolicy.canEnterChat(member)
					);
				})
				.toList();
		return new Result(items, meetingPage.page(), meetingPage.size(), meetingPage.totalElements(), meetingPage.totalPages());
	}
}
