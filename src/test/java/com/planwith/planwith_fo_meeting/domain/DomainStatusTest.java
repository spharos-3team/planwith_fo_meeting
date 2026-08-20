package com.planwith.planwith_fo_meeting.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

class DomainStatusTest {

	@Test
	void meetingStatusesMatchApiContract() {
		assertThat(MeetingStatus.values()).containsExactly(
				MeetingStatus.RECRUITING,
				MeetingStatus.FULL,
				MeetingStatus.COMPLETED,
				MeetingStatus.DISBANDED
		);
	}

	@Test
	void participationStatusesMatchApiContract() {
		assertThat(ParticipationStatus.values()).containsExactly(
				ParticipationStatus.PENDING,
				ParticipationStatus.APPROVED,
				ParticipationStatus.REJECTED,
				ParticipationStatus.LEFT,
				ParticipationStatus.KICKED
		);
	}

	@Test
	void meetingRolesMatchApiContract() {
		assertThat(MeetingRole.values()).containsExactly(
				MeetingRole.HOST,
				MeetingRole.VICE_HOST,
				MeetingRole.MEMBER
		);
	}
}
