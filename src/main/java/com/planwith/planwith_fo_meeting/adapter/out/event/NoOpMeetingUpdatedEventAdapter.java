package com.planwith.planwith_fo_meeting.adapter.out.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.MeetingUpdatedEventPort;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpMeetingUpdatedEventAdapter implements MeetingUpdatedEventPort {

	private static final Logger log = LoggerFactory.getLogger(NoOpMeetingUpdatedEventAdapter.class);

	@Override
	public void publish(MeetingUpdatedEvent event) {
		log.info(
				"meeting.updated queued for Kafka (#11): meetingUuid={} hostMemberUuid={}",
				event.meetingUuid(),
				event.hostMemberUuid()
		);
	}
}
