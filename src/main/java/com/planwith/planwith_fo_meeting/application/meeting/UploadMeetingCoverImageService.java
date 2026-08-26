package com.planwith.planwith_fo_meeting.application.meeting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.in.UploadMeetingCoverImageUseCase;
import com.planwith.planwith_fo_meeting.application.port.out.CoverImageStoragePort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

@Service
public class UploadMeetingCoverImageService implements UploadMeetingCoverImageUseCase {

	private static final long MAX_BYTES = 2 * 1024 * 1024;
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg",
			"image/jpg",
			"image/png",
			"image/webp"
	);

	private final MeetingRepositoryPort meetingRepositoryPort;
	private final CoverImageStoragePort coverImageStoragePort;

	public UploadMeetingCoverImageService(
			MeetingRepositoryPort meetingRepositoryPort,
			CoverImageStoragePort coverImageStoragePort
	) {
		this.meetingRepositoryPort = meetingRepositoryPort;
		this.coverImageStoragePort = coverImageStoragePort;
	}

	@Override
	@Transactional
	public Meeting upload(UUID meetingUuid, UUID actorMemberUuid, MultipartFile file) {
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(meetingUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
		if (!meeting.isHost(actorMemberUuid)) {
			throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
		}
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_COVER_IMAGE, "이미지 파일이 필요합니다.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BusinessException(ErrorCode.INVALID_COVER_IMAGE, "이미지 용량은 2MB 이하여야 합니다.");
		}
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new BusinessException(ErrorCode.INVALID_COVER_IMAGE, "jpg/jpeg/png/webp만 업로드할 수 있습니다.");
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		}
		catch (IOException exception) {
			throw new BusinessException(ErrorCode.INVALID_COVER_IMAGE);
		}
		if (!"image/webp".equals(contentType)) {
			try {
				if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
					throw new BusinessException(ErrorCode.INVALID_COVER_IMAGE);
				}
			}
			catch (BusinessException exception) {
				throw exception;
			}
			catch (Exception exception) {
				throw new BusinessException(ErrorCode.INVALID_COVER_IMAGE);
			}
		}
		String storedType = storedContentType(contentType);
		coverImageStoragePort.save(meetingUuid, storedType, bytes);
		String publicUrl = "/api/v1/meetings/" + meetingUuid + "/cover-image";
		return meetingRepositoryPort.save(meeting.withThumbnailUrl(publicUrl, Instant.now()));
	}

	private String storedContentType(String contentType) {
		return switch (contentType) {
			case "image/png" -> "image/png";
			case "image/webp" -> "image/webp";
			default -> "image/jpeg";
		};
	}
}
