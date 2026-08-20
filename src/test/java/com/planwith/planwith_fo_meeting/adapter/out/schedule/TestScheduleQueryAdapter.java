package com.planwith.planwith_fo_meeting.adapter.out.schedule;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.out.ScheduleQueryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

@Component
@Profile("test")
public class TestScheduleQueryAdapter implements ScheduleQueryPort {

	static final String DESTINATION = "부산";
	static final LocalDate START_DATE = LocalDate.of(2026, 9, 1);
	static final LocalDate END_DATE = LocalDate.of(2026, 9, 3);

	@Override
	public ScheduleSnapshot requireSchedule(UUID scheduleUuid) {
		if (scheduleUuid == null) {
			throw new BusinessException(ErrorCode.SCHEDULE_REQUIRED);
		}
		return new ScheduleSnapshot(scheduleUuid, DESTINATION, START_DATE, END_DATE);
	}
}
