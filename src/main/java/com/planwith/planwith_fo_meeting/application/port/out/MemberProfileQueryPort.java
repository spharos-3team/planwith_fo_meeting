package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.UUID;

public interface MemberProfileQueryPort {

	MemberProfile requireProfile(UUID memberUuid);

	record MemberProfile(UUID memberUuid, String nickname, String profileImageUrl) {
	}
}
