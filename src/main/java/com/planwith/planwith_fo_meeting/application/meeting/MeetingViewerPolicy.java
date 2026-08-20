package com.planwith.planwith_fo_meeting.application.meeting;

import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

public final class MeetingViewerPolicy {

	private MeetingViewerPolicy() {
	}

	public static boolean accessible(MeetingMember member) {
		return member == null || member.getStatus() != ParticipationStatus.KICKED;
	}

	public static boolean canApply(Meeting meeting, MeetingMember member) {
		if (meeting.getStatus() != MeetingStatus.RECRUITING) {
			return false;
		}
		if (member == null) {
			return true;
		}
		return member.getStatus() == ParticipationStatus.REJECTED
				|| member.getStatus() == ParticipationStatus.LEFT;
	}

	public static boolean canEnterChat(MeetingMember member) {
		return member != null && member.getStatus() == ParticipationStatus.APPROVED;
	}

	public static boolean canViewMembers(MeetingMember member) {
		return canEnterChat(member);
	}
}
