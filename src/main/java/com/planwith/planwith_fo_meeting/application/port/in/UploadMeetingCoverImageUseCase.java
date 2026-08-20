package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

public interface UploadMeetingCoverImageUseCase {

	Meeting upload(UUID meetingUuid, UUID actorMemberUuid, MultipartFile file);
}
