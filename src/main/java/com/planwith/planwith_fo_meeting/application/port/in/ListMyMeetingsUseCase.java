package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingScope;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

public interface ListMyMeetingsUseCase {

	Result list(UUID memberUuid, MeetingScope scope, MeetingStatus status, int page, int size);

	static MeetingScope parseScope(String value) {
		if (value == null || value.isBlank()) {
			return MeetingScope.HOSTED;
		}
		try {
			return MeetingScope.valueOf(value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "scope는 hosted, joined, pending 중 하나여야 합니다.");
		}
	}

	record Result(
			List<Meeting> content,
			int page,
			int size,
			long totalElements,
			int totalPages,
			boolean canCreate
	) {
	}
}
