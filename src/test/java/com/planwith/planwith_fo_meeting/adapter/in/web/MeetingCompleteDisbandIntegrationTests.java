package com.planwith.planwith_fo_meeting.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeetingCompleteDisbandIntegrationTests {

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
	void completeRequiresAuth() throws Exception {
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/complete", "33333333-3333-3333-3333-333333333333"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void hostCompletesMeetingAndKeepsChatEntry() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/complete", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("COMPLETED"));
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalElements").value(0));
		mockMvc.perform(get("/api/v1/meetings").param("status", "COMPLETED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalElements").value(1))
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(meetingUuid));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("COMPLETED"))
				.andExpect(jsonPath("$.data.canApply").value(false))
				.andExpect(jsonPath("$.data.canEnterChat").value(true));
	}

	@Test
	void completeTwiceIsRejected() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/complete", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/complete", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("MEETING_ALREADY_COMPLETED"));
	}

	@Test
	void nonHostCannotComplete() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/complete", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_MEETING_HOST"));
	}

	@Test
	void hostDisbandsMeetingAndMembersLeave() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/disband", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("DISBANDED"));
		mockMvc.perform(get("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalElements").value(0));
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(UUID.fromString(meetingUuid)).orElseThrow();
		assertThat(meetingMemberRepositoryPort.findByMeetingId(meeting.getMeetingId()))
				.allMatch(member -> member.getStatus() == ParticipationStatus.LEFT);
	}

	@Test
	void nonHostCannotDisband() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/disband", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_MEETING_HOST"));
	}

	private String createMeeting(String hostUuid) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/meetings")
						.header("X-Auth-User-Id", hostUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "scheduleUuid": "%s",
								  "title": "주말 부산 여행",
								  "intro": "함께 가요",
								  "maxMemberCount": 4
								}
								""".formatted(SCHEDULE_UUID)))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.data.meetingUuid");
	}
}
