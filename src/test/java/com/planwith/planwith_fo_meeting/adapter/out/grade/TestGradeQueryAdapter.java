package com.planwith.planwith_fo_meeting.adapter.out.grade;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.GradeQueryPort;

@Component
@Profile("test")
public class TestGradeQueryAdapter implements GradeQueryPort {

	public static final UUID INELIGIBLE_MEMBER_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");

	@Override
	public boolean canBump(UUID memberUuid) {
		return memberUuid != null && !INELIGIBLE_MEMBER_UUID.equals(memberUuid);
	}
}
