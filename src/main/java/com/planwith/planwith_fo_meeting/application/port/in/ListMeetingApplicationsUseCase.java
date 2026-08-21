package com.planwith.planwith_fo_meeting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface ListMeetingApplicationsUseCase {

	List<MeetingMember> listPending(UUID meetingUuid, UUID hostMemberUuid);
}
