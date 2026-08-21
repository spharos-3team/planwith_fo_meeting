package com.planwith.planwith_fo_meeting.application.port.out;

import java.util.UUID;

public interface GradeQueryPort {

	boolean canBump(UUID memberUuid);
}
