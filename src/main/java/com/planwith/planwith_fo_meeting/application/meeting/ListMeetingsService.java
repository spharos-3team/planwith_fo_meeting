package com.planwith.planwith_fo_meeting.application.meeting;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingsUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingPage;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;

@Service
public class ListMeetingsService implements ListMeetingsUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;

	public ListMeetingsService(MeetingRepositoryPort meetingRepositoryPort) {
		this.meetingRepositoryPort = meetingRepositoryPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result list(Command command) {
		Objects.requireNonNull(command, "List meetings command is required.");
		LocalDate from = command.from();
		LocalDate to = command.to();
		if (from != null && to != null && from.isAfter(to)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "from은 to보다 이후일 수 없습니다.");
		}
		String destination = command.destination() == null || command.destination().isBlank()
				? null
				: command.destination().trim();
		MeetingPage meetingPage = meetingRepositoryPort.searchPublic(
				command.status(),
				destination,
				from,
				to,
				command.page(),
				command.size()
		);
		return new Result(
				meetingPage.content(),
				meetingPage.page(),
				meetingPage.size(),
				meetingPage.totalElements(),
				meetingPage.totalPages()
		);
	}
}
