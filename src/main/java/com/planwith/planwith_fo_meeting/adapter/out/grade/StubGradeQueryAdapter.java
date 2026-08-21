package com.planwith.planwith_fo_meeting.adapter.out.grade;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.GradeQueryPort;

@Component
@Profile("!test")
public class StubGradeQueryAdapter implements GradeQueryPort {

	@Override
	public boolean canBump(UUID memberUuid) {
		return memberUuid != null;
	}
}
