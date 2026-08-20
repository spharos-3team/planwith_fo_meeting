package com.planwith.planwith_fo_meeting.adapter.out.persistence.participation;

import java.time.Instant;

import com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting.MeetingJpaEntity;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "meeting_members",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_meeting_members_member",
				columnNames = {"meeting_id", "member_uuid"}
		)
)
public class MeetingMemberJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "meeting_member_id", columnDefinition = "bigint unsigned")
	private Long meetingMemberId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "meeting_id", nullable = false, columnDefinition = "bigint unsigned")
	private MeetingJpaEntity meeting;

	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 32)
	private MeetingRole role;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private ParticipationStatus status;

	@Column(name = "join_message", length = 300)
	private String joinMessage;

	@Column(name = "join_at", columnDefinition = "datetime")
	private Instant joinAt;

	@Column(name = "joined_at", columnDefinition = "datetime")
	private Instant joinedAt;

	public Long getMeetingMemberId() {
		return meetingMemberId;
	}

	public MeetingJpaEntity getMeeting() {
		return meeting;
	}

	public void setMeeting(MeetingJpaEntity meeting) {
		this.meeting = meeting;
	}

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public MeetingRole getRole() {
		return role;
	}

	public void setRole(MeetingRole role) {
		this.role = role;
	}

	public ParticipationStatus getStatus() {
		return status;
	}

	public void setStatus(ParticipationStatus status) {
		this.status = status;
	}

	public String getJoinMessage() {
		return joinMessage;
	}

	public void setJoinMessage(String joinMessage) {
		this.joinMessage = joinMessage;
	}

	public Instant getJoinAt() {
		return joinAt;
	}

	public void setJoinAt(Instant joinAt) {
		this.joinAt = joinAt;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(Instant joinedAt) {
		this.joinedAt = joinedAt;
	}
}
