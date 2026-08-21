package com.planwith.planwith_fo_meeting.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingCompletedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingCreatedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingDisbandedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingParticipationChangedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingUpdatedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingViceHostChangedEventPort;
import com.planwith.planwith_fo_meeting.config.MeetingKafkaProperties;
import com.planwith.planwith_fo_meeting.domain.participation.ParticipationStatus;

@ExtendWith(MockitoExtension.class)
class KafkaMeetingEventAdapterTest {

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	private ObjectMapper objectMapper;
	private KafkaMeetingEventAdapter adapter;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		adapter = new KafkaMeetingEventAdapter(
				new KafkaMeetingEventPublisher(kafkaTemplate, objectMapper),
				new MeetingKafkaProperties(
						true,
						"planwith.meeting.created",
						"planwith.meeting.completed",
						"planwith.meeting.disbanded",
						"planwith.meeting.participation.changed",
						"planwith.meeting.updated",
						"planwith.meeting.vice-host.changed"
				)
		);
	}

	@Test
	void createdEventUsesEnvelopeContract() throws Exception {
		UUID meetingUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		UUID hostUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
		Instant now = Instant.parse("2026-08-21T07:00:00Z");
		stubSend("planwith.meeting.created", meetingUuid);

		adapter.publish(new MeetingCreatedEventPort.MeetingCreatedEvent(
				meetingUuid,
				hostUuid,
				UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
				"주말 부산",
				now
		));

		JsonNode envelope = capture("planwith.meeting.created", meetingUuid);
		assertEnvelope(envelope, "meeting.created", meetingUuid, now);
		assertThat(envelope.get("payload").get("meetingUuid").asText()).isEqualTo(meetingUuid.toString());
		assertThat(envelope.get("payload").get("hostMemberUuid").asText()).isEqualTo(hostUuid.toString());
		assertThat(envelope.get("payload").get("title").asText()).isEqualTo("주말 부산");
		assertThat(envelope.toString()).doesNotContain("accessToken", "refreshToken", "password");
	}

	@Test
	void completedAndDisbandedCarryMeetingUuidOnly() throws Exception {
		UUID meetingUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		UUID hostUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
		Instant now = Instant.parse("2026-08-21T07:00:00Z");
		stubSend("planwith.meeting.completed", meetingUuid);
		adapter.publish(new MeetingCompletedEventPort.MeetingCompletedEvent(meetingUuid, hostUuid, now));
		JsonNode completed = capture("planwith.meeting.completed", meetingUuid);
		assertEnvelope(completed, "meeting.completed", meetingUuid, now);
		assertThat(completed.get("payload").get("meetingUuid").asText()).isEqualTo(meetingUuid.toString());

		stubSend("planwith.meeting.disbanded", meetingUuid);
		adapter.publish(new MeetingDisbandedEventPort.MeetingDisbandedEvent(meetingUuid, hostUuid, now));
		JsonNode disbanded = capture("planwith.meeting.disbanded", meetingUuid);
		assertEnvelope(disbanded, "meeting.disbanded", meetingUuid, now);
	}

	@Test
	void participationChangedUsesApprovedStatusName() throws Exception {
		UUID meetingUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		UUID memberUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
		Instant now = Instant.parse("2026-08-21T07:00:00Z");
		stubSend("planwith.meeting.participation.changed", meetingUuid);

		adapter.publish(new MeetingParticipationChangedEventPort.MeetingParticipationChangedEvent(
				meetingUuid,
				memberUuid,
				ParticipationStatus.APPROVED,
				now
		));

		JsonNode envelope = capture("planwith.meeting.participation.changed", meetingUuid);
		assertEnvelope(envelope, "meeting.participation.changed", meetingUuid, now);
		assertThat(envelope.get("payload").get("memberUuid").asText()).isEqualTo(memberUuid.toString());
		assertThat(envelope.get("payload").get("status").asText()).isEqualTo("APPROVED");
	}

	@Test
	void updatedAndViceHostChangedArePublished() throws Exception {
		UUID meetingUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		UUID hostUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
		Instant now = Instant.parse("2026-08-21T07:00:00Z");
		stubSend("planwith.meeting.updated", meetingUuid);
		adapter.publish(new MeetingUpdatedEventPort.MeetingUpdatedEvent(meetingUuid, hostUuid, now));
		assertEnvelope(capture("planwith.meeting.updated", meetingUuid), "meeting.updated", meetingUuid, now);

		stubSend("planwith.meeting.vice-host.changed", meetingUuid);
		adapter.publish(new MeetingViceHostChangedEventPort.MeetingViceHostChangedEvent(meetingUuid, hostUuid, now));
		JsonNode viceHost = capture("planwith.meeting.vice-host.changed", meetingUuid);
		assertEnvelope(viceHost, "meeting.vice-host.changed", meetingUuid, now);
		assertThat(viceHost.get("payload").get("viceHostMemberUuid").asText()).isEqualTo(hostUuid.toString());
	}

	private void stubSend(String topic, UUID meetingUuid) {
		when(kafkaTemplate.send(eq(topic), eq(meetingUuid.toString()), org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(CompletableFuture.completedFuture(null));
	}

	private JsonNode capture(String topic, UUID meetingUuid) throws Exception {
		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(kafkaTemplate).send(eq(topic), eq(meetingUuid.toString()), payloadCaptor.capture());
		return objectMapper.readTree(payloadCaptor.getValue());
	}

	private void assertEnvelope(JsonNode envelope, String eventType, UUID meetingUuid, Instant occurredAt) {
		assertThat(envelope.get("eventId").asText()).isNotBlank();
		assertThat(envelope.get("eventType").asText()).isEqualTo(eventType);
		assertThat(Instant.parse(envelope.get("occurredAt").asText())).isEqualTo(occurredAt);
		assertThat(envelope.get("aggregateId").asText()).isEqualTo(meetingUuid.toString());
		assertThat(envelope.get("version").asInt()).isEqualTo(1);
		assertThat(envelope.has("payload")).isTrue();
	}
}
