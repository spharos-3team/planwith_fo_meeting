package com.planwith.planwith_fo_meeting.domain.participation;

import java.time.Instant;
import java.util.UUID;

public class MeetingMember {

	private final Long meetingId;
	private final UUID memberUuid;
	private final MeetingRole role;
	private final ParticipationStatus status;
	private final String joinMessage;
	private final Instant joinAt;
	private final Instant joinedAt;

	public MeetingMember(
			Long meetingId,
			UUID memberUuid,
			MeetingRole role,
			ParticipationStatus status,
			String joinMessage,
			Instant joinAt,
			Instant joinedAt
	) {
		this.meetingId = meetingId;
		this.memberUuid = memberUuid;
		this.role = role;
		this.status = status;
		this.joinMessage = joinMessage;
		this.joinAt = joinAt;
		this.joinedAt = joinedAt;
	}

	public static MeetingMember host(Long meetingId, UUID memberUuid, Instant now) {
		return new MeetingMember(
				meetingId,
				memberUuid,
				MeetingRole.HOST,
				ParticipationStatus.APPROVED,
				null,
				now,
				now
		);
	}

	public static MeetingMember apply(Long meetingId, UUID memberUuid, String message, Instant now) {
		return new MeetingMember(
				meetingId,
				memberUuid,
				MeetingRole.MEMBER,
				ParticipationStatus.PENDING,
				message,
				now,
				null
		);
	}

	public MeetingMember reapply(String message, Instant now) {
		return new MeetingMember(
				meetingId,
				memberUuid,
				MeetingRole.MEMBER,
				ParticipationStatus.PENDING,
				message,
				now,
				null
		);
	}

	public MeetingMember approve(Instant now) {
		return new MeetingMember(
				meetingId,
				memberUuid,
				role,
				ParticipationStatus.APPROVED,
				joinMessage,
				joinAt,
				now
		);
	}

	public MeetingMember reject() {
		return new MeetingMember(
				meetingId,
				memberUuid,
				role,
				ParticipationStatus.REJECTED,
				joinMessage,
				joinAt,
				null
		);
	}

	public boolean canReapply() {
		return status == ParticipationStatus.REJECTED || status == ParticipationStatus.LEFT;
	}

	public Long getMeetingId() {
		return meetingId;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public MeetingRole getRole() {
		return role;
	}

	public ParticipationStatus getStatus() {
		return status;
	}

	public String getJoinMessage() {
		return joinMessage;
	}

	public Instant getJoinAt() {
		return joinAt;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}
}
