package com.planwith.planwith_fo_meeting.domain.meeting;

import java.time.Instant;
import java.util.UUID;

public class Meeting {

	private final Long meetingId;
	private final UUID meetingUuid;
	private final UUID hostMemberUuid;
	private final UUID scheduleUuid;
	private final String title;
	private final String description;
	private final int maxMemberCount;
	private final int currentMemberCount;
	private final MeetingStatus status;
	private final String thumbnailUrl;
	private final Instant bumpAt;
	private final ScheduleSnapshot scheduleSnapshot;
	private final Instant createdAt;
	private final Instant updatedAt;

	public Meeting(
			Long meetingId,
			UUID meetingUuid,
			UUID hostMemberUuid,
			UUID scheduleUuid,
			String title,
			String description,
			int maxMemberCount,
			int currentMemberCount,
			MeetingStatus status,
			String thumbnailUrl,
			Instant bumpAt,
			ScheduleSnapshot scheduleSnapshot,
			Instant createdAt,
			Instant updatedAt
	) {
		this.meetingId = meetingId;
		this.meetingUuid = meetingUuid;
		this.hostMemberUuid = hostMemberUuid;
		this.scheduleUuid = scheduleUuid;
		this.title = title;
		this.description = description;
		this.maxMemberCount = maxMemberCount;
		this.currentMemberCount = currentMemberCount;
		this.status = status;
		this.thumbnailUrl = thumbnailUrl;
		this.bumpAt = bumpAt;
		this.scheduleSnapshot = scheduleSnapshot;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Meeting create(
			UUID hostMemberUuid,
			UUID scheduleUuid,
			String title,
			String description,
			int maxMemberCount,
			String thumbnailUrl,
			ScheduleSnapshot scheduleSnapshot,
			Instant now
	) {
		return new Meeting(
				null,
				UUID.randomUUID(),
				hostMemberUuid,
				scheduleUuid,
				title,
				description,
				maxMemberCount,
				1,
				MeetingStatus.RECRUITING,
				thumbnailUrl,
				null,
				scheduleSnapshot,
				now,
				now
		);
	}

	public Meeting withThumbnailUrl(String thumbnailUrl, Instant now) {
		return new Meeting(
				meetingId,
				meetingUuid,
				hostMemberUuid,
				scheduleUuid,
				title,
				description,
				maxMemberCount,
				currentMemberCount,
				status,
				thumbnailUrl,
				bumpAt,
				scheduleSnapshot,
				createdAt,
				now
		);
	}

	public boolean isHost(UUID memberUuid) {
		return hostMemberUuid.equals(memberUuid);
	}

	public Long getMeetingId() {
		return meetingId;
	}

	public UUID getMeetingUuid() {
		return meetingUuid;
	}

	public UUID getHostMemberUuid() {
		return hostMemberUuid;
	}

	public UUID getScheduleUuid() {
		return scheduleUuid;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getMaxMemberCount() {
		return maxMemberCount;
	}

	public int getCurrentMemberCount() {
		return currentMemberCount;
	}

	public MeetingStatus getStatus() {
		return status;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public Instant getBumpAt() {
		return bumpAt;
	}

	public ScheduleSnapshot getScheduleSnapshot() {
		return scheduleSnapshot;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
