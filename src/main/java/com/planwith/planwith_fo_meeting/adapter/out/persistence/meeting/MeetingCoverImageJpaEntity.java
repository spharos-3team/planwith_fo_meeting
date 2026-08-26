package com.planwith.planwith_fo_meeting.adapter.out.persistence.meeting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "meeting_cover_images")
public class MeetingCoverImageJpaEntity {

	@Id
	@Column(name = "meeting_uuid", nullable = false, length = 36)
	private String meetingUuid;

	@Column(name = "content_type", nullable = false, length = 64)
	private String contentType;

	@Lob
	@Column(name = "image_bytes", nullable = false, columnDefinition = "longblob")
	private byte[] imageBytes;

	public String getMeetingUuid() {
		return meetingUuid;
	}

	public void setMeetingUuid(String meetingUuid) {
		this.meetingUuid = meetingUuid;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}
}
