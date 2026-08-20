package com.planwith.planwith_fo_meeting.adapter.in.web.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest(properties = {
		"app.gateway.trust-check-enabled=true",
		"app.gateway.internal-token=test-gateway-token"
})
@AutoConfigureMockMvc
class GatewayTrustInterceptorIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deployCheckIsForbiddenWithoutGatewayToken() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-meeting/deploy-check"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
	}

	@Test
	void deployCheckSucceedsWithGatewayToken() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-meeting/deploy-check")
						.header("X-Gateway-Internal-Token", "test-gateway-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("planwith-fo-meeting"));
	}

	@Test
	void actuatorHealthIsExcludedFromTrustCheck() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}
}
