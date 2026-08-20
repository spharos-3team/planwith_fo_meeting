package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.UUID;

import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

public interface ScheduleQueryPort {

	ScheduleSnapshot requireSchedule(UUID scheduleUuid);
}
