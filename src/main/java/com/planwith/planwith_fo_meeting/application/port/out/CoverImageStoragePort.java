package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface CoverImageStoragePort {

	void save(UUID meetingUuid, String contentType, byte[] bytes);

	Optional<StoredCoverImage> find(UUID meetingUuid);

	record StoredCoverImage(String contentType, byte[] bytes) {
	}
}
