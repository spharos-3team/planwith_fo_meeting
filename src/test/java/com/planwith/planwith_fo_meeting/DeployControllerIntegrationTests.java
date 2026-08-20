package com.planwith.planwith_fo_meeting;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DeployControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deployCheckReturnsMarker() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-meeting/deploy-check"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("planwith-fo-meeting"))
				.andExpect(jsonPath("$.marker").value("planwith-fo-meeting-deploy-v1"))
				.andExpect(jsonPath("$.message").value("planwith-fo-meeting deploy pipeline ok"));
	}

	@Test
	void loginSucceedsWithConfiguredCredentials() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-meeting/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "id": "test-001",
								  "pw": "1234"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value("test-001"))
				.andExpect(jsonPath("$.data.message").value("로그인에 성공했습니다."));
	}

	@Test
	void loginFailsWithInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-meeting/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "id": "test-001",
								  "pw": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void loginFailsWhenRequiredValueIsBlank() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-meeting/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "id": "",
								  "pw": "1234"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.error.fieldErrors.id").value("아이디는 필수입니다."));
	}
}
