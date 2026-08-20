package com.planwith.planwith_fo_meeting.application.meeting;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.port.in.ListMyMeetingsUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingPage;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingScope;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

@Service
public class ListMyMeetingsService implements ListMyMeetingsUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;

	public ListMyMeetingsService(MeetingRepositoryPort meetingRepositoryPort) {
		this.meetingRepositoryPort = meetingRepositoryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result list(UUID memberUuid, MeetingScope scope, MeetingStatus status, int page, int size) {
		MeetingPage meetingPage = meetingRepositoryPort.searchMine(memberUuid, scope, status, page, size);
		return new Result(
				meetingPage.content(),
				meetingPage.page(),
				meetingPage.size(),
				meetingPage.totalElements(),
				meetingPage.totalPages(),
				scope == MeetingScope.HOSTED
		);
	}
}
