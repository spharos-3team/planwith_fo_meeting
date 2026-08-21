package com.planwith.planwith_fo_meeting.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_meeting.adapter.in.web.auth.GatewayAuthenticationContextResolver;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.ApplicationResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.ApplyMeetingRequest;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.ParticipationResponse;
import com.planwith.planwith_fo_meeting.application.port.in.ApplyToMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.DecideMeetingApplicationUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.GetMyParticipationUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingApplicationsUseCase;
import com.planwith.planwith_fo_meeting.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/meetings/{meetingUuid}")
@Tag(name = "meeting-applications", description = "모임 신청·승인·거절")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MeetingApplicationController {

	private final GatewayAuthenticationContextResolver authContextResolver;
	private final ApplyToMeetingUseCase applyToMeetingUseCase;
	private final ListMeetingApplicationsUseCase listMeetingApplicationsUseCase;
	private final DecideMeetingApplicationUseCase decideMeetingApplicationUseCase;
	private final GetMyParticipationUseCase getMyParticipationUseCase;

	public MeetingApplicationController(
			GatewayAuthenticationContextResolver authContextResolver,
			ApplyToMeetingUseCase applyToMeetingUseCase,
			ListMeetingApplicationsUseCase listMeetingApplicationsUseCase,
			DecideMeetingApplicationUseCase decideMeetingApplicationUseCase,
			GetMyParticipationUseCase getMyParticipationUseCase
	) {
		this.authContextResolver = authContextResolver;
		this.applyToMeetingUseCase = applyToMeetingUseCase;
		this.listMeetingApplicationsUseCase = listMeetingApplicationsUseCase;
		this.decideMeetingApplicationUseCase = decideMeetingApplicationUseCase;
		this.getMyParticipationUseCase = getMyParticipationUseCase;
	}

	@PostMapping("/applications")
	@Operation(summary = "모임 신청")
	public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
			@PathVariable UUID meetingUuid,
			@Valid @RequestBody(required = false) ApplyMeetingRequest request
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		String message = request == null ? null : request.message();
		return ResponseEntity.ok(ApiResponse.success(ApplicationResponse.from(
				applyToMeetingUseCase.apply(new ApplyToMeetingUseCase.Command(meetingUuid, memberUuid, message))
		)));
	}

	@GetMapping("/applications")
	@Operation(summary = "승인 대기 목록 (호스트)")
	public ResponseEntity<ApiResponse<List<ApplicationResponse>>> list(@PathVariable UUID meetingUuid) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		List<ApplicationResponse> body = listMeetingApplicationsUseCase.listPending(meetingUuid, hostMemberUuid)
				.stream()
				.map(ApplicationResponse::from)
				.toList();
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@PostMapping("/applications/{memberUuid}/approve")
	@Operation(summary = "신청 승인")
	public ResponseEntity<ApiResponse<ApplicationResponse>> approve(
			@PathVariable UUID meetingUuid,
			@PathVariable UUID memberUuid
	) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(ApplicationResponse.from(
				decideMeetingApplicationUseCase.approve(meetingUuid, memberUuid, hostMemberUuid)
		)));
	}

	@PostMapping("/applications/{memberUuid}/reject")
	@Operation(summary = "신청 거절")
	public ResponseEntity<ApiResponse<ApplicationResponse>> reject(
			@PathVariable UUID meetingUuid,
			@PathVariable UUID memberUuid
	) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(ApplicationResponse.from(
				decideMeetingApplicationUseCase.reject(meetingUuid, memberUuid, hostMemberUuid)
		)));
	}

	@GetMapping("/participation")
	@Operation(summary = "내 참여 상태")
	public ResponseEntity<ApiResponse<ParticipationResponse>> participation(@PathVariable UUID meetingUuid) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(ParticipationResponse.from(
				meetingUuid,
				getMyParticipationUseCase.get(meetingUuid, memberUuid).orElse(null)
		)));
	}
}
