package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

public interface ChangeMeetingRecruitmentStatusUseCase {

	Meeting change(UUID meetingUuid, UUID hostMemberUuid, MeetingStatus status);
}
