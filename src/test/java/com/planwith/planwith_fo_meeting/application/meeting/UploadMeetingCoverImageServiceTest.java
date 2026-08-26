package com.planwith.planwith_fo_meeting.application.meeting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.out.CoverImageStoragePort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

@ExtendWith(MockitoExtension.class)
class UploadMeetingCoverImageServiceTest {

	@Mock
	private MeetingRepositoryPort meetingRepositoryPort;

	@Mock
	private CoverImageStoragePort coverImageStoragePort;

	private UploadMeetingCoverImageService service;
	private Meeting meeting;

	@BeforeEach
	void setUp() {
		service = new UploadMeetingCoverImageService(meetingRepositoryPort, coverImageStoragePort);
		UUID host = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID scheduleUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		meeting = Meeting.create(
				host,
				scheduleUuid,
				"주말 부산 여행",
				"함께 가요",
				4,
				null,
				new ScheduleSnapshot(scheduleUuid, "부산", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3)),
				Instant.parse("2026-08-26T01:00:00Z")
		);
		when(meetingRepositoryPort.findByMeetingUuid(meeting.getMeetingUuid())).thenReturn(Optional.of(meeting));
	}

	@Test
	void imageIoFailureIsInvalidCoverImageNotInternalError() {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"cover.jpg",
				"image/jpeg",
				new byte[] {(byte) 0xFF, (byte) 0xD8, 0x00, 0x01}
		) {
			@Override
			public java.io.InputStream getInputStream() throws IOException {
				throw new IllegalArgumentException("Numbers of source Raster bands and source color space components do not match");
			}
		};

		assertThatThrownBy(() -> service.upload(meeting.getMeetingUuid(), meeting.getHostMemberUuid(), file))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_COVER_IMAGE);
	}
}
