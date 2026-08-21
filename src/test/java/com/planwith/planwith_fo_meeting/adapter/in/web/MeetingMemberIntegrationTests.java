package com.planwith.planwith_fo_meeting.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeetingMemberIntegrationTests {

	private static final String HOST_UUID = "11111111-1111-1111-1111-111111111111";
	private static final String OTHER_UUID = "22222222-2222-2222-2222-222222222222";
	private static final String THIRD_UUID = "33333333-3333-3333-3333-333333333333";
	private static final String FOURTH_UUID = "44444444-4444-4444-4444-444444444444";
	private static final String SCHEDULE_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void membersRequireAuth() throws Exception {
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/members", "33333333-3333-3333-3333-333333333333"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void outsiderCannotListMembers() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/members", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_MEETING_PARTICIPANT"));
	}

	@Test
	void approvedMemberCanListAndReadProfile() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		approve(meetingUuid, apply(meetingUuid, OTHER_UUID));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/members", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(2))
				.andExpect(jsonPath("$.data[0].role").value("HOST"))
				.andExpect(jsonPath("$.data[0].nickname").exists())
				.andExpect(jsonPath("$.data[1].memberUuid").value(OTHER_UUID))
				.andExpect(jsonPath("$.data[1].role").value("MEMBER"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/members/{memberUuid}", meetingUuid, HOST_UUID)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.memberUuid").value(HOST_UUID))
				.andExpect(jsonPath("$.data.role").value("HOST"))
				.andExpect(jsonPath("$.data.nickname").exists());
	}

	@Test
	void hostAssignsAndClearsViceHost() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		approve(meetingUuid, apply(meetingUuid, OTHER_UUID));
		approve(meetingUuid, apply(meetingUuid, THIRD_UUID));
		mockMvc.perform(put("/api/v1/meetings/{meetingUuid}/vice-host", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"memberUuid\":\"%s\"}".formatted(OTHER_UUID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.role").value("VICE_HOST"));
		mockMvc.perform(put("/api/v1/meetings/{meetingUuid}/vice-host", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"memberUuid\":\"%s\"}".formatted(THIRD_UUID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.memberUuid").value(THIRD_UUID))
				.andExpect(jsonPath("$.data.role").value("VICE_HOST"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/members", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[1].memberUuid").value(THIRD_UUID))
				.andExpect(jsonPath("$.data[1].role").value("VICE_HOST"))
				.andExpect(jsonPath("$.data[2].memberUuid").value(OTHER_UUID))
				.andExpect(jsonPath("$.data[2].role").value("MEMBER"));
		mockMvc.perform(delete("/api/v1/meetings/{meetingUuid}/vice-host", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.role").value("MEMBER"));
	}

	@Test
	void hostCannotBeAssignedAsViceHost() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		mockMvc.perform(put("/api/v1/meetings/{meetingUuid}/vice-host", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"memberUuid\":\"%s\"}".formatted(HOST_UUID)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("CANNOT_ASSIGN_HOST_AS_VICE_HOST"));
	}

	@Test
	void hostCannotLeave() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		mockMvc.perform(delete("/api/v1/meetings/{meetingUuid}/members/me", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("HOST_CANNOT_LEAVE"));
	}

	@Test
	void memberLeaveDecrementsCountAndReopensRecruiting() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 2);
		approve(meetingUuid, apply(meetingUuid, OTHER_UUID));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("FULL"))
				.andExpect(jsonPath("$.data.currentMemberCount").value(2));
		mockMvc.perform(delete("/api/v1/meetings/{meetingUuid}/members/me", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("LEFT"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("RECRUITING"))
				.andExpect(jsonPath("$.data.currentMemberCount").value(1));
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	@Test
	void hostCanKickAndKickedCannotReapply() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		approve(meetingUuid, apply(meetingUuid, OTHER_UUID));
		mockMvc.perform(delete("/api/v1/meetings/{meetingUuid}/members/{memberUuid}", meetingUuid, OTHER_UUID)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("KICKED"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("KICKED_MEMBER"));
	}

	@Test
	void viceHostCanKickMemberButNotHost() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		approve(meetingUuid, apply(meetingUuid, OTHER_UUID));
		approve(meetingUuid, apply(meetingUuid, THIRD_UUID));
		mockMvc.perform(put("/api/v1/meetings/{meetingUuid}/vice-host", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"memberUuid\":\"%s\"}".formatted(OTHER_UUID)))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/api/v1/meetings/{meetingUuid}/members/{memberUuid}", meetingUuid, HOST_UUID)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("CANNOT_KICK_HOST"));
		mockMvc.perform(delete("/api/v1/meetings/{meetingUuid}/members/{memberUuid}", meetingUuid, THIRD_UUID)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("KICKED"));
	}

	@Test
	void regularMemberCannotKick() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		approve(meetingUuid, apply(meetingUuid, OTHER_UUID));
		approve(meetingUuid, apply(meetingUuid, FOURTH_UUID));
		mockMvc.perform(delete("/api/v1/meetings/{meetingUuid}/members/{memberUuid}", meetingUuid, FOURTH_UUID)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_MEETING_MANAGER"));
	}

	private String apply(String meetingUuid, String memberUuid) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.data.memberUuid");
	}

	private void approve(String meetingUuid, String memberUuid) throws Exception {
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/approve", meetingUuid, memberUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk());
	}

	private String createMeeting(String hostUuid, int maxMemberCount) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/meetings")
						.header("X-Auth-User-Id", hostUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "scheduleUuid": "%s",
								  "title": "주말 부산 여행",
								  "intro": "함께 가요",
								  "maxMemberCount": %d
								}
								""".formatted(SCHEDULE_UUID, maxMemberCount)))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.data.meetingUuid");
	}
}
