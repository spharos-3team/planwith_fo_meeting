package com.planwith.planwith_fo_meeting.adapter.in.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_meeting.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_meeting.config.DeployProperties;
import com.planwith.planwith_fo_meeting.dto.LoginRequest;
import com.planwith.planwith_fo_meeting.dto.LoginResponse;
import com.planwith.planwith_fo_meeting.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-meeting")
@Tag(name = "planwith-fo-meeting", description = "Server notebook deploy verification API")
public class DeployController {

	private final AuthService authService;
	private final DeployProperties deployProperties;

	public DeployController(AuthService authService, DeployProperties deployProperties) {
		this.authService = authService;
		this.deployProperties = deployProperties;
	}

	@GetMapping("/deploy-check")
	@Operation(summary = "Deploy check")
	public ResponseEntity<Map<String, String>> deployCheck() {
		return ResponseEntity.ok(Map.of(
				"service", "planwith-fo-meeting",
				"marker", deployProperties.marker(),
				"message", "planwith-fo-meeting deploy pipeline ok"
		));
	}

	@PostMapping("/login")
	@Operation(summary = "Login (deploy scaffold)")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
	}
}
