package com.planwith.planwith_fo_meeting.adapter.out.member;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.MemberProfileQueryPort;

@Component
@Profile("test")
public class TestMemberProfileQueryAdapter implements MemberProfileQueryPort {

	@Override
	public MemberProfile requireProfile(UUID memberUuid) {
		return new MemberProfile(memberUuid, "닉네임-" + memberUuid.toString().substring(0, 8), "stub://profile.png");
	}
}
