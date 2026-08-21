package com.planwith.planwith_fo_meeting.adapter.out.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_meeting.application.port.out.MeetingViceHostChangedEventPort;

@Component
public class NoOpMeetingViceHostChangedEventAdapter implements MeetingViceHostChangedEventPort {

	private static final Logger log = LoggerFactory.getLogger(NoOpMeetingViceHostChangedEventAdapter.class);

	@Override
	public void publish(MeetingViceHostChangedEvent event) {
		log.info(
				"meeting.vice-host.changed queued for Kafka (#11): meetingUuid={} viceHostMemberUuid={}",
				event.meetingUuid(),
				event.viceHostMemberUuid()
		);
	}
}
