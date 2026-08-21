package com.planwith.planwith_fo_meeting.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class MeetingApplicationIntegrationTests {

	private static final String HOST_UUID = "11111111-1111-1111-1111-111111111111";
	private static final String OTHER_UUID = "22222222-2222-2222-2222-222222222222";
	private static final String THIRD_UUID = "33333333-3333-3333-3333-333333333333";
	private static final String SCHEDULE_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MeetingRepositoryPort meetingRepositoryPort;

	@Autowired
	private MeetingMemberRepositoryPort meetingMemberRepositoryPort;

	@Test
	void hostCanReadOwnParticipation() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/participation", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.memberUuid").value(HOST_UUID))
				.andExpect(jsonPath("$.data.status").value("APPROVED"))
				.andExpect(jsonPath("$.data.role").value("HOST"));
	}

	@Test
	void applyRequiresAuth() throws Exception {
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", "33333333-3333-3333-3333-333333333333")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void hostCannotApplyToOwnMeeting() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"저요\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("CANNOT_APPLY_OWN_MEETING"));
	}

	@Test
	void applyThenHostApproves() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		MvcResult applied = mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"함께 가요\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("PENDING"))
				.andExpect(jsonPath("$.data.message").value("함께 가요"))
				.andReturn();
		String memberUuid = JsonPath.read(applied.getResponse().getContentAsString(), "$.data.memberUuid");

		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].memberUuid").value(memberUuid));

		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/approve", meetingUuid, memberUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("APPROVED"));

		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/participation", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("APPROVED"))
				.andExpect(jsonPath("$.data.role").value("MEMBER"));

		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.currentMemberCount").value(2));
	}

	@Test
	void hostRejectsApplication() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		String memberUuid = apply(meetingUuid, OTHER_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/reject", meetingUuid, memberUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("REJECTED"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	void rejectedMemberCanReapply() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		String memberUuid = apply(meetingUuid, OTHER_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/reject", meetingUuid, memberUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"다시 신청\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("PENDING"))
				.andExpect(jsonPath("$.data.message").value("다시 신청"));
	}

	@Test
	void kickedMemberCannotApply() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(java.util.UUID.fromString(meetingUuid)).orElseThrow();
		meetingMemberRepositoryPort.save(new MeetingMember(
				meeting.getMeetingId(),
				java.util.UUID.fromString(OTHER_UUID),
				MeetingRole.MEMBER,
				ParticipationStatus.KICKED,
				null,
				java.time.Instant.parse("2026-08-20T00:00:00Z"),
				null
		));
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("KICKED_MEMBER"));
	}

	@Test
	void approveRejectedWhenFull() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 2);
		String first = apply(meetingUuid, OTHER_UUID);
		String second = apply(meetingUuid, THIRD_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/approve", meetingUuid, first)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("APPROVED"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("FULL"))
				.andExpect(jsonPath("$.data.currentMemberCount").value(2));
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/approve", meetingUuid, second)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("MEETING_FULL"));
	}

	@Test
	void nonHostCannotListOrDecide() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID, 4);
		String memberUuid = apply(meetingUuid, OTHER_UUID);
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}/applications", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_MEETING_HOST"));
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/approve", meetingUuid, memberUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden());
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
