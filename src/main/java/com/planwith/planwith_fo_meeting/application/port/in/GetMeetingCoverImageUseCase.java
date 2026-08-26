package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

public interface GetMeetingCoverImageUseCase {

	Result get(UUID meetingUuid);

	record Result(String contentType, byte[] bytes) {
	}
}
