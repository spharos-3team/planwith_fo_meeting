package com.planwith.planwith_fo_meeting.adapter.in.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.adapter.in.web.dto.MeetingListItemResponse;
import com.planwith.planwith_fo_meeting.application.port.out.MemberProfileQueryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

@Component
public class MeetingListItemAssembler {

	private final MemberProfileQueryPort memberProfileQueryPort;

	public MeetingListItemAssembler(MemberProfileQueryPort memberProfileQueryPort) {
		this.memberProfileQueryPort = memberProfileQueryPort;
	}

	public List<MeetingListItemResponse> assemble(List<Meeting> meetings) {
		Map<UUID, MemberProfileQueryPort.MemberProfile> hosts = new HashMap<>();
		return meetings.stream()
				.map(meeting -> assemble(meeting, hosts))
				.toList();
	}

	private MeetingListItemResponse assemble(
			Meeting meeting,
			Map<UUID, MemberProfileQueryPort.MemberProfile> hosts
	) {
		MemberProfileQueryPort.MemberProfile host = hosts.computeIfAbsent(
				meeting.getHostMemberUuid(),
				memberProfileQueryPort::requireProfile
		);
		return MeetingListItemResponse.from(meeting, host);
	}
}
