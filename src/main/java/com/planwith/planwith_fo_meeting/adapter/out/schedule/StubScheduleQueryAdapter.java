package com.planwith.planwith_fo_meeting.adapter.out.schedule;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.out.ScheduleQueryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

@Component
public class StubScheduleQueryAdapter implements ScheduleQueryPort {

	@Override
	public ScheduleSnapshot requireSchedule(UUID scheduleUuid) {
		if (scheduleUuid == null) {
			throw new BusinessException(ErrorCode.SCHEDULE_REQUIRED);
		}
		return new ScheduleSnapshot(scheduleUuid, null, null, null, null, null);
	}
}
