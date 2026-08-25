package com.planwith.planwith_fo_meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_meeting.config.AuthProperties;
import com.planwith.planwith_fo_meeting.config.DeployProperties;
import com.planwith.planwith_fo_meeting.config.GatewayTrustProperties;
import com.planwith.planwith_fo_meeting.config.LocalDotenvLoader;
import com.planwith.planwith_fo_meeting.config.MeetingKafkaProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		GatewayTrustProperties.class,
		MeetingKafkaProperties.class
})
public class PlanwithFoMeetingApplication {

	public static void main(String[] args) {
		LocalDotenvLoader.load("planwith_fo_meeting");
		SpringApplication.run(PlanwithFoMeetingApplication.class, args);
	}
}
