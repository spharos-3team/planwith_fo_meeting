package com.planwith.planwith_fo_meeting.application.meeting;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingCoverImageUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.CoverImageStoragePort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

@Service
public class GetMeetingCoverImageService implements GetMeetingCoverImageUseCase {

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final CoverImageStoragePort coverImageStoragePort;

	public GetMeetingCoverImageService(
			MeetingRepositoryPort meetingRepositoryPort,
			CoverImageStoragePort coverImageStoragePort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.coverImageStoragePort = coverImageStoragePort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result get(UUID meetingUuid) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.filter(found -> !found.isDisbanded())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		CoverImageStoragePort.StoredCoverImage stored = coverImageStoragePort.find(meeting.getMeetingUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.COVER_IMAGE_NOT_FOUND));
		return new Result(stored.contentType(), stored.bytes());
	}
}
