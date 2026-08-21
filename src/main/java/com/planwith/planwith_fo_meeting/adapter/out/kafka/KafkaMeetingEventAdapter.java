package com.planwith.planwith_fo_meeting.adapter.out.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.adapter.out.kafka.dto.MeetingCreatedPayload;
import com.planwith.planwith_fo_meeting.adapter.out.kafka.dto.MeetingParticipationChangedPayload;
import com.planwith.planwith_fo_meeting.adapter.out.kafka.dto.MeetingUpdatedPayload;
import com.planwith.planwith_fo_meeting.adapter.out.kafka.dto.MeetingUuidPayload;
import com.planwith.planwith_fo_meeting.adapter.out.kafka.dto.MeetingViceHostChangedPayload;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingCompletedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingCreatedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingDisbandedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingParticipationChangedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingUpdatedEventPort;
import com.planwith.planwith_fo_meeting.application.port.out.MeetingViceHostChangedEventPort;
import com.planwith.planwith_fo_meeting.config.MeetingKafkaProperties;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaMeetingEventAdapter implements
		MeetingCreatedEventPort,
		MeetingCompletedEventPort,
		MeetingDisbandedEventPort,
		MeetingParticipationChangedEventPort,
		MeetingUpdatedEventPort,
		MeetingViceHostChangedEventPort {

	public static final String CREATED = "meeting.created";
	public static final String COMPLETED = "meeting.completed";
	public static final String DISBANDED = "meeting.disbanded";
	public static final String PARTICIPATION_CHANGED = "meeting.participation.changed";
	public static final String UPDATED = "meeting.updated";
	public static final String VICE_HOST_CHANGED = "meeting.vice-host.changed";

	private final KafkaMeetingEventPublisher publisher;
	private final MeetingKafkaProperties properties;

	public KafkaMeetingEventAdapter(KafkaMeetingEventPublisher publisher, MeetingKafkaProperties properties) {
		this.publisher = publisher;
		this.properties = properties;
	}

	@Override
	public void publish(MeetingCreatedEvent event) {
		publisher.publish(
				properties.createdTopic(),
				event.meetingUuid(),
				CREATED,
				event.occurredAt(),
				new MeetingCreatedPayload(event.meetingUuid(), event.hostMemberUuid(), event.title())
		);
	}

	@Override
	public void publish(MeetingCompletedEvent event) {
		publisher.publish(
				properties.completedTopic(),
				event.meetingUuid(),
				COMPLETED,
				event.occurredAt(),
				new MeetingUuidPayload(event.meetingUuid())
		);
	}

	@Override
	public void publish(MeetingDisbandedEvent event) {
		publisher.publish(
				properties.disbandedTopic(),
				event.meetingUuid(),
				DISBANDED,
				event.occurredAt(),
				new MeetingUuidPayload(event.meetingUuid())
		);
	}

	@Override
	public void publish(MeetingParticipationChangedEvent event) {
		publisher.publish(
				properties.participationChangedTopic(),
				event.meetingUuid(),
				PARTICIPATION_CHANGED,
				event.occurredAt(),
				new MeetingParticipationChangedPayload(
						event.meetingUuid(),
						event.memberUuid(),
						event.status().name()
				)
		);
	}

	@Override
	public void publish(MeetingUpdatedEvent event) {
		publisher.publish(
				properties.updatedTopic(),
				event.meetingUuid(),
				UPDATED,
				event.occurredAt(),
				new MeetingUpdatedPayload(event.meetingUuid(), event.hostMemberUuid())
		);
	}

	@Override
	public void publish(MeetingViceHostChangedEvent event) {
		publisher.publish(
				properties.viceHostChangedTopic(),
				event.meetingUuid(),
				VICE_HOST_CHANGED,
				event.occurredAt(),
				new MeetingViceHostChangedPayload(event.meetingUuid(), event.viceHostMemberUuid())
		);
	}
}
