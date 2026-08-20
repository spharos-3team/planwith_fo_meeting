package com.planwith.planwith_fo_meeting.application.meeting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingsUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingPage;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

@Service
public class ListMeetingsService implements ListMeetingsUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;

	public ListMeetingsService(MeetingRepositoryPort meetingRepositoryPort) {
		this.meetingRepositoryPort = meetingRepositoryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result list(MeetingStatus status, int page, int size) {
		MeetingPage meetingPage = meetingRepositoryPort.searchPublic(status, page, size);
		return new Result(
				meetingPage.content(),
				meetingPage.page(),
				meetingPage.size(),
				meetingPage.totalElements(),
				meetingPage.totalPages()
		);
	}
}
