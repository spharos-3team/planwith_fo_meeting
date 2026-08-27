package com.planwith.planwith_fo_meeting.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
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
import com.planwith.planwith_fo_meeting.adapter.out.grade.TestGradeQueryAdapter;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingRepositoryPort;
import com.planwith.planwith_fo_meeting.domain.meeting.Meeting;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeetingUpdateIntegrationTests {

	private static final String HOST_UUID = "11111111-1111-1111-1111-111111111111";
	private static final String OTHER_UUID = "22222222-2222-2222-2222-222222222222";
	private static final String SCHEDULE_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
	private static final String OTHER_SCHEDULE_UUID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MeetingRepositoryPort meetingRepositoryPort;

	@Test
	void updateRequiresAuth() throws Exception {
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}", "33333333-3333-3333-3333-333333333333")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"intro\":\"수정\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void hostUpdatesIntroAndSchedule() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "intro": "일정을 바꿨어요",
								  "scheduleUuid": "%s"
								}
								""".formatted(OTHER_SCHEDULE_UUID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.intro").value("일정을 바꿨어요"))
				.andExpect(jsonPath("$.data.scheduleUuid").value(OTHER_SCHEDULE_UUID))
				.andExpect(jsonPath("$.data.title").value("주말 부산 여행"));
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].destination").value("제주"))
				.andExpect(jsonPath("$.data.content[0].startDate").value("2026-10-01"))
				.andExpect(jsonPath("$.data.content[0].endDate").value("2026-10-05"));
	}

	@Test
	void introOnlyUpdateRefreshesScheduleSnapshot() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"intro\":\"소개만 수정\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].destination").value("부산"))
				.andExpect(jsonPath("$.data.content[0].startDate").value("2026-09-01"))
				.andExpect(jsonPath("$.data.content[0].endDate").value("2026-09-03"));
	}

	@Test
	void emptyUpdateIsRejected() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
	}

	@Test
	void nonHostCannotUpdate() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", OTHER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"intro\":\"가로채기\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("NOT_MEETING_HOST"));
	}

	@Test
	void maxMemberCannotBeSmallerThanCurrent() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		Meeting meeting = meetingRepositoryPort.findByMeetingUuid(UUID.fromString(meetingUuid)).orElseThrow();
		meetingRepositoryPort.save(new Meeting(
				meeting.getMeetingId(),
				meeting.getMeetingUuid(),
				meeting.getHostMemberUuid(),
				meeting.getScheduleUuid(),
				meeting.getTitle(),
				meeting.getDescription(),
				meeting.getMaxMemberCount(),
				3,
				meeting.getStatus(),
				meeting.getThumbnailUrl(),
				meeting.getBumpAt(),
				meeting.getScheduleSnapshot(),
				meeting.getCreatedAt(),
				meeting.getUpdatedAt()
		));
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"maxMemberCount\":2}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("MAX_MEMBER_TOO_SMALL"));
	}

	@Test
	void hostCanToggleRecruitmentStatus() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}/recruitment-status", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"FULL\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("FULL"));
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}/recruitment-status", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"RECRUITING\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("RECRUITING"));
	}

	@Test
	void completedIsNotARecruitmentStatus() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(patch("/api/v1/meetings/{meetingUuid}/recruitment-status", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"COMPLETED\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("INVALID_RECRUITMENT_STATUS"));
	}

	@Test
	void hostCanBumpOnceThenMustWait() throws Exception {
		String meetingUuid = createMeeting(HOST_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/bump", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.bumpAt").isNotEmpty());
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/bump", meetingUuid)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("BUMP_TOO_SOON"));
	}

	@Test
	void bumpRejectedWithoutEligibleGrade() throws Exception {
		String ineligible = TestGradeQueryAdapter.INELIGIBLE_MEMBER_UUID.toString();
		String meetingUuid = createMeeting(ineligible);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/bump", meetingUuid)
						.header("X-Auth-User-Id", ineligible))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("BUMP_NOT_ALLOWED"));
	}

	@Test
	void bumpedMeetingAppearsFirstInList() throws Exception {
		String older = createMeeting(HOST_UUID);
		String newer = createMeeting(HOST_UUID);
		mockMvc.perform(post("/api/v1/meetings/{meetingUuid}/bump", older)
						.header("X-Auth-User-Id", HOST_UUID))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(older))
				.andExpect(jsonPath("$.data.content[1].meetingUuid").value(newer));
	}

	@Test
	void newestCreatedMeetingAppearsFirstWhenNoneBumped() throws Exception {
		String older = createMeeting(HOST_UUID);
		Meeting found = meetingRepositoryPort.findByMeetingUuid(UUID.fromString(older)).orElseThrow();
		meetingRepositoryPort.save(new Meeting(
				found.getMeetingId(),
				found.getMeetingUuid(),
				found.getHostMemberUuid(),
				found.getScheduleUuid(),
				found.getTitle(),
				found.getDescription(),
				found.getMaxMemberCount(),
				found.getCurrentMemberCount(),
				found.getStatus(),
				found.getThumbnailUrl(),
				found.getBumpAt(),
				found.getScheduleSnapshot(),
				found.getCreatedAt().minusSeconds(2),
				found.getUpdatedAt()
		));
		String newer = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(newer))
				.andExpect(jsonPath("$.data.content[1].meetingUuid").value(older));
	}

	@Test
	void newlyCreatedMeetingAppearsBeforeOlderBumpedMeeting() throws Exception {
		String older = createMeeting(HOST_UUID);
		Meeting bumped = meetingRepositoryPort.findByMeetingUuid(UUID.fromString(older)).orElseThrow();
		meetingRepositoryPort.save(bumped.bump(Instant.now().minus(Duration.ofDays(7))));
		String newer = createMeeting(HOST_UUID);
		mockMvc.perform(get("/api/v1/meetings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(newer))
				.andExpect(jsonPath("$.data.content[1].meetingUuid").value(older));
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
