package com.planwith.planwith_fo_meeting.adapter.out.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.MeetingParticipationChangedEventPort;

@Component
public class NoOpMeetingParticipationChangedEventAdapter implements MeetingParticipationChangedEventPort {

	private static final Logger log = LoggerFactory.getLogger(NoOpMeetingParticipationChangedEventAdapter.class);

	@Override
	public void publish(MeetingParticipationChangedEvent event) {
		log.info(
				"meeting.participation.changed queued for Kafka (#11): meetingUuid={} memberUuid={} status={}",
				event.meetingUuid(),
				event.memberUuid(),
				event.status()
		);
	}
}
