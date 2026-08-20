package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import java.time.Instant;

import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "meetings")
public class MeetingJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "meeting_id", columnDefinition = "bigint unsigned")
	private Long meetingId;

	@Column(name = "meeting_uuid", nullable = false, unique = true, length = 36)
	private String meetingUuid;

	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Column(name = "schedule_uuid", length = 36)
	private String scheduleUuid;

	@Column(name = "title", nullable = false, length = 100)
	private String title;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "thumbnail_url", length = 1000)
	private String thumbnailUrl;

	@Column(name = "destination", length = 100)
	private String destination;

	@Column(name = "max_member", nullable = false, columnDefinition = "int unsigned")
	private int maxMember;

	@Column(name = "current_member", nullable = false, columnDefinition = "int unsigned")
	private int currentMember;

	@Enumerated(EnumType.STRING)
	@Column(name = "metting_status", nullable = false, length = 32)
	private MeetingStatus meetingStatus;

	@Column(name = "bump_at", columnDefinition = "datetime")
	private Instant bumpAt;

	@Column(name = "created_at", nullable = false, columnDefinition = "datetime")
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false, columnDefinition = "datetime")
	private Instant updatedAt;

	public Long getMeetingId() {
		return meetingId;
	}

	public String getMeetingUuid() {
		return meetingUuid;
	}

	public void setMeetingUuid(String meetingUuid) {
		this.meetingUuid = meetingUuid;
	}

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public String getScheduleUuid() {
		return scheduleUuid;
	}

	public void setScheduleUuid(String scheduleUuid) {
		this.scheduleUuid = scheduleUuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getMaxMember() {
		return maxMember;
	}

	public void setMaxMember(int maxMember) {
		this.maxMember = maxMember;
	}

	public int getCurrentMember() {
		return currentMember;
	}

	public void setCurrentMember(int currentMember) {
		this.currentMember = currentMember;
	}

	public MeetingStatus getMeetingStatus() {
		return meetingStatus;
	}

	public void setMeetingStatus(MeetingStatus meetingStatus) {
		this.meetingStatus = meetingStatus;
	}

	public Instant getBumpAt() {
		return bumpAt;
	}

	public void setBumpAt(Instant bumpAt) {
		this.bumpAt = bumpAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
