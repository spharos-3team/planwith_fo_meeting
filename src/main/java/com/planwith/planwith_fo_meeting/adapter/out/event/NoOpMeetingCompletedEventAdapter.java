package com.planwith.planwith_fo_meeting.adapter.out.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.MeetingCompletedEventPort;

@Component
public class NoOpMeetingCompletedEventAdapter implements MeetingCompletedEventPort {

	private static final Logger log = LoggerFactory.getLogger(NoOpMeetingCompletedEventAdapter.class);

	@Override
	public void publish(MeetingCompletedEvent event) {
		log.info(
				"meeting.completed queued for Kafka (#11): meetingUuid={} hostMemberUuid={} chatStatus=ENDED",
				event.meetingUuid(),
				event.hostMemberUuid()
		);
	}
}
