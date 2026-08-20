package com.planwith.planwith_fo_meeting.adapter.in.web;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
				.andExpect(jsonPath("$.data.hostMemberUuid").value(HOST_UUID))
				.andExpect(jsonPath("$.data.scheduleUuid").value(SCHEDULE_UUID))
				.andExpect(jsonPath("$.data.status").value("RECRUITING"))
				.andExpect(jsonPath("$.data.currentMemberCount").value(1))
				.andExpect(jsonPath("$.data.maxMemberCount").value(4))
				.andExpect(jsonPath("$.data.title").value("주말 부산 여행"))
				.andExpect(jsonPath("$.data.destination").value("부산"));
	}

	@Test
	void uploadCoverImageStoresStubUrl() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"cover.webp",
				"image/webp",
				new byte[] {1, 2, 3, 4}
		);
		mockMvc.perform(multipart("/api/v1/meetings/{meetingUuid}/cover-image", meetingUuid)
						.file(file)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.coverImage").value("stub://meetings/" + meetingUuid + ".webp"));
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
	void guestCanListAndSeeApplyFlag() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(meetingUuid))
				.andExpect(jsonPath("$.data.content[0].canApply").value(true))
				.andExpect(jsonPath("$.data.content[0].canEnterChat").value(false))
				.andExpect(jsonPath("$.data.content[0].accessible").value(true))
				.andExpect(jsonPath("$.data.content[0].myParticipation").value(nullValue()));
	}

	@Test
	void hostDetailIncludesParticipation() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.myParticipation").value("APPROVED"))
				.andExpect(jsonPath("$.data.myRole").value("HOST"))
				.andExpect(jsonPath("$.data.canApply").value(false))
				.andExpect(jsonPath("$.data.canEnterChat").value(true))
				.andExpect(jsonPath("$.data.canViewMembers").value(true));
	}

	@Test
	void unknownMeetingDetailIsNotFound() throws Exception {
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", "33333333-3333-3333-3333-333333333333"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MEETING_NOT_FOUND"));
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
				  "maxMemberCount": 4,
				  "destination": "부산"
				}
				""".formatted(SCHEDULE_UUID);
	}
}
