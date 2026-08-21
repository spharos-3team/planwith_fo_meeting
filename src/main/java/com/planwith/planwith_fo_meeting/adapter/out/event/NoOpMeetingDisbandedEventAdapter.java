package com.planwith.planwith_fo_meeting.adapter.out.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.MeetingDisbandedEventPort;

@Component
public class NoOpMeetingDisbandedEventAdapter implements MeetingDisbandedEventPort {

	private static final Logger log = LoggerFactory.getLogger(NoOpMeetingDisbandedEventAdapter.class);

	@Override
	public void publish(MeetingDisbandedEvent event) {
		log.info(
				"meeting.disbanded queued for Kafka (#11): meetingUuid={} hostMemberUuid={}",
				event.meetingUuid(),
				event.hostMemberUuid()
		);
	}
}
