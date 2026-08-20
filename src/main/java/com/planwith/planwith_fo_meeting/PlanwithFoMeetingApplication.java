package com.planwith.planwith_fo_meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_meeting.config.AuthProperties;
import com.planwith.planwith_fo_meeting.config.DeployProperties;
import com.planwith.planwith_fo_meeting.config.GatewayTrustProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		GatewayTrustProperties.class
})
public class PlanwithFoMeetingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoMeetingApplication.class, args);
	}
}
