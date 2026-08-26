package com.planwith.planwith_fo_meeting.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingMemberRepositoryPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingMember;
import com.planwith.planwith_fo_meeting.domain.participation.MeetingRole;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeetingControllerIntegrationTests {

	private static final String HOST_UUID = "11111111-1111-1111-1111-111111111111";
	private static final String OTHER_UUID = "22222222-2222-2222-2222-222222222222";
	private static final String SCHEDULE_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MeetingRepositoryPort meetingRepositoryPort;

	@Autowired
	private MeetingMemberRepositoryPort meetingMemberRepositoryPort;

	@Test
	void createMeetingRequiresAuth() throws Exception {
		mockMvc.perform(post("/api/v1/meetings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(createBody()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	void createMeetingRequiresSchedule() throws Exception {
		mockMvc.perform(post("/api/v1/meetings")
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "주말 부산 여행",
								  "intro": "함께 가요",
								  "maxMemberCount": 4
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	@Test
	void createMeetingReturnsRecruitingHost() throws Exception {
		mockMvc.perform(post("/api/v1/meetings")
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createBody()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.memberUuid").value(HOST_UUID))
				.andExpect(jsonPath("$.data.scheduleUuid").value(SCHEDULE_UUID))
				.andExpect(jsonPath("$.data.status").value("RECRUITING"))
				.andExpect(jsonPath("$.data.currentMemberCount").value(1))
				.andExpect(jsonPath("$.data.maxMemberCount").value(4))
				.andExpect(jsonPath("$.data.title").value("주말 부산 여행"))
				.andExpect(jsonPath("$.data.hostMemberUuid").doesNotExist())
				.andExpect(jsonPath("$.data.startAt").doesNotExist())
				.andExpect(jsonPath("$.data.destination").doesNotExist());
	}

	@Test
	void uploadCoverImageStoresAndServesBytes() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		byte[] webp = {1, 2, 3, 4};
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"cover.webp",
				"image/webp",
				webp
		);
		mockMvc.perform(multipart("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid)
						.file(file)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.coverImage").value("/api/v1/meetings/" + meetingUuid + "/cover-image"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Type", "image/webp"))
				.andExpect(content().bytes(webp));
	}

	@Test
	void uploadCoverJpegStoresAndServesBytes() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		byte[] jpeg = jpegBytes();
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"cover.jpg",
				"image/jpeg",
				jpeg
		);
		mockMvc.perform(multipart("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid)
						.file(file)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.coverImage").value("/api/v1/meetings/" + meetingUuid + "/cover-image"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Type", "image/jpeg"))
				.andExpect(content().bytes(jpeg));
	}

	@Test
	void missingCoverImageIsNotFound() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("COVER_IMAGE_NOT_FOUND"));
	}

	@Test
	void uploadCoverCorruptJpegIsBadRequest() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"cover.jpg",
				"image/jpeg",
				new byte[] {(byte) 0xFF, (byte) 0xD8, 0x00, 0x01}
		);
		mockMvc.perform(multipart("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid)
						.file(file)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_COVER_IMAGE"));
	}

	@Test
	void uploadCoverMissingFileIsBadRequest() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(multipart("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_COVER_IMAGE"));
	}

	@Test
	void uploadCoverImageRejectedForNonHost() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"cover.webp",
				"image/webp",
				new byte[] {1, 2, 3, 4}
		);
		mockMvc.perform(multipart("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid)
						.file(file)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_MEETING_HOST"));
	}

	@Test
	void guestCanListPagedCardsFromMeetingSnapshot() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.page").value(0))
				.andExpect(jsonPath("$.data.size").value(20))
				.andExpect(jsonPath("$.data.totalElements").value(1))
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(meetingUuid))
				.andExpect(jsonPath("$.data.content[0].title").value("주말 부산 여행"))
				.andExpect(jsonPath("$.data.content[0].intro").value("함께 가요"))
				.andExpect(jsonPath("$.data.content[0].maxMemberCount").value(4))
				.andExpect(jsonPath("$.data.content[0].currentMemberCount").value(1))
				.andExpect(jsonPath("$.data.content[0].destination").value("부산"))
				.andExpect(jsonPath("$.data.content[0].startDate").value("2026-09-01"))
				.andExpect(jsonPath("$.data.content[0].endDate").value("2026-09-03"))
				.andExpect(jsonPath("$.data.content[0].scheduleUuid").doesNotExist())
				.andExpect(jsonPath("$.data.content[0].cost").doesNotExist())
				.andExpect(jsonPath("$.data.content[0].canApply").doesNotExist());
	}

	@Test
	void listRespectsPageSize() throws Exception {
		createMeeting(HOST_UUID);
		createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings").param("page", "0").param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.size").value(1))
				.andExpect(jsonPath("$.data.totalElements").value(2))
				.andExpect(jsonPath("$.data.totalPages").value(2))
				.andExpect(jsonPath("$.data.content.length()").value(1));
	}

	@Test
	void hostDetailIncludesParticipationAndScheduleUuidOnly() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.scheduleUuid").value(SCHEDULE_UUID))
				.andExpect(jsonPath("$.data.intro").value("함께 가요"))
				.andExpect(jsonPath("$.data.myParticipation").value("APPROVED"))
				.andExpect(jsonPath("$.data.myRole").value("HOST"))
				.andExpect(jsonPath("$.data.canApply").value(false))
				.andExpect(jsonPath("$.data.canEnterChat").value(true))
				.andExpect(jsonPath("$.data.canViewMembers").value(true))
				.andExpect(jsonPath("$.data.destination").value("부산"))
				.andExpect(jsonPath("$.data.startDate").value("2026-09-01"))
				.andExpect(jsonPath("$.data.endDate").value("2026-09-03"))
				.andExpect(jsonPath("$.data.startAt").doesNotExist())
				.andExpect(jsonPath("$.data.endAt").doesNotExist())
				.andExpect(jsonPath("$.data.cost").doesNotExist())
				.andExpect(jsonPath("$.data.transport").doesNotExist());
	}

	@Test
	void unknownMeetingDetailIsNotFound() throws Exception {
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", "33333333-3333-3333-3333-333333333333"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MEETING_NOT_FOUND"));
	}

	@Test
	void myMeetingsRequiresAuth() throws Exception {
		mockMvc.perform(get("/api/v1/meetings/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	void hostedEmptyListAllowsCreate() throws Exception {
		mockMvc.perform(get("/api/v1/meetings/me").header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").isEmpty())
				.andExpect(jsonPath("$.data.canCreate").value(true));
	}

	@Test
	void hostedContainsCreatedMeetingSeparateFromJoined() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings/me").param("scope", "hosted").header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalElements").value(1))
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(meetingUuid))
				.andExpect(jsonPath("$.data.canCreate").value(true));
		mockMvc.perform(get("/api/v1/meetings/me").param("scope", "joined").header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").isEmpty())
				.andExpect(jsonPath("$.data.canCreate").value(false));
	}

	@Test
	void pendingScopeReturnsOnlyPendingMembership() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(java.util.UUID.fromString(meetingUuid)).orElseThrow();
		meetingMemberRepositoryPort.save(new MeetingMember(
				meeting.getMeetingId(),
				java.util.UUID.fromString(OTHER_UUID),
				MeetingRole.MEMBER,
				ParticipationStatus.PENDING,
				"함께 가고 싶어요",
				java.time.Instant.parse("2026-08-20T00:00:00Z"),
				null
		));
		mockMvc.perform(get("/api/v1/meetings/me").param("scope", "pending").header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalElements").value(1))
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(meetingUuid))
				.andExpect(jsonPath("$.data.canCreate").value(false));
		mockMvc.perform(get("/api/v1/meetings/me").param("scope", "joined").header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").isEmpty());
		mockMvc.perform(get("/api/v1/meetings/me").param("scope", "hosted").header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").isEmpty())
				.andExpect(jsonPath("$.data.canCreate").value(true));
	}

	@Test
	void invalidMyMeetingScopeIsBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/meetings/me").param("scope", "all").header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	private byte[] jpegBytes() throws Exception {
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		var graphics = image.createGraphics();
		graphics.setColor(Color.BLUE);
		graphics.fillRect(0, 0, 8, 8);
		graphics.dispose();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "jpeg", output);
		return output.toByteArray();
	}

	private String createMeeting(String hostUuid) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/meetings")
						.header("X-Auth-User-Id", hostUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createBody()))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.data.meetingUuid");
	}

	private String createBody() {
		return """
				{
				  "scheduleUuid": "%s",
				  "title": "주말 부산 여행",
				  "intro": "함께 가요",
				  "maxMemberCount": 4
				}
				""".formatted(SCHEDULE_UUID);
	}
}
