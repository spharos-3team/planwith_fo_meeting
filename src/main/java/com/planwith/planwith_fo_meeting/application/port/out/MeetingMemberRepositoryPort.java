package com.planwith.planwith_fo_meeting.application.port.out;

import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;

public interface MeetingMemberRepositoryPort {

	MeetingMember save(MeetingMember member);
}
