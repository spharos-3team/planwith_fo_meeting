package com.planwith.planwith_fo_meeting.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_meeting.adapter.in.web.auth.GatewayAuthenticationContextResolver;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.AssignViceHostRequest;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.MeetingMemberResponse;
import com.planwith.planwith_fo_meeting.application.port.in.AssignViceHostUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ClearViceHostUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingMemberUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.KickMeetingMemberUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.LeaveMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingMembersUseCase;
import com.planwith.planwith_fo_meeting.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/meetings/{meetingUuid}")
@Tag(name = "meeting-members", description = "구성원·부방장·강퇴·탈퇴")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MeetingMemberController {

	private final GatewayAuthenticationContextResolver authContextResolver;
	private final ListMeetingMembersUseCase listMeetingMembersUseCase;
	private final GetMeetingMemberUseCase getMeetingMemberUseCase;
	private final AssignViceHostUseCase assignViceHostUseCase;
	private final ClearViceHostUseCase clearViceHostUseCase;
	private final LeaveMeetingUseCase leaveMeetingUseCase;
	private final KickMeetingMemberUseCase kickMeetingMemberUseCase;

	public MeetingMemberController(
			GatewayAuthenticationContextResolver authContextResolver,
			ListMeetingMembersUseCase listMeetingMembersUseCase,
			GetMeetingMemberUseCase getMeetingMemberUseCase,
			AssignViceHostUseCase assignViceHostUseCase,
			ClearViceHostUseCase clearViceHostUseCase,
			LeaveMeetingUseCase leaveMeetingUseCase,
			KickMeetingMemberUseCase kickMeetingMemberUseCase
	) {
		this.authContextResolver = authContextResolver;
		this.listMeetingMembersUseCase = listMeetingMembersUseCase;
		this.getMeetingMemberUseCase = getMeetingMemberUseCase;
		this.assignViceHostUseCase = assignViceHostUseCase;
		this.clearViceHostUseCase = clearViceHostUseCase;
		this.leaveMeetingUseCase = leaveMeetingUseCase;
		this.kickMeetingMemberUseCase = kickMeetingMemberUseCase;
	}

	@GetMapping("/members")
	@Operation(summary = "구성원 목록")
	public ResponseEntity<ApiResponse<List<MeetingMemberResponse>>> list(@PathVariable UUID meetingUuid) {
		UUID viewerMemberUuid = authContextResolver.requireUser().userId();
		List<MeetingMemberResponse> body = listMeetingMembersUseCase.list(meetingUuid, viewerMemberUuid)
				.stream()
				.map(MeetingMemberResponse::from)
				.toList();
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@GetMapping("/members/{memberUuid}")
	@Operation(summary = "구성원 프로필")
	public ResponseEntity<ApiResponse<MeetingMemberResponse>> detail(
			@PathVariable UUID meetingUuid,
			@PathVariable UUID memberUuid
	) {
		UUID viewerMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingMemberResponse.from(
				getMeetingMemberUseCase.get(meetingUuid, memberUuid, viewerMemberUuid)
		)));
	}

	@PutMapping("/vice-host")
	@Operation(summary = "부방장 지정/변경")
	public ResponseEntity<ApiResponse<MeetingMemberResponse>> assignViceHost(
			@PathVariable UUID meetingUuid,
			@Valid @RequestBody AssignViceHostRequest request
	) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingMemberResponse.from(
				assignViceHostUseCase.assign(meetingUuid, hostMemberUuid, request.memberUuid())
		)));
	}

	@DeleteMapping("/vice-host")
	@Operation(summary = "부방장 해제")
	public ResponseEntity<ApiResponse<MeetingMemberResponse>> clearViceHost(@PathVariable UUID meetingUuid) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingMemberResponse.from(
				clearViceHostUseCase.clear(meetingUuid, hostMemberUuid)
		)));
	}

	@DeleteMapping("/members/me")
	@Operation(summary = "모임 탈퇴")
	public ResponseEntity<ApiResponse<MeetingMemberResponse>> leave(@PathVariable UUID meetingUuid) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingMemberResponse.from(
				leaveMeetingUseCase.leave(meetingUuid, memberUuid)
		)));
	}

	@DeleteMapping("/members/{memberUuid}")
	@Operation(summary = "구성원 강퇴")
	public ResponseEntity<ApiResponse<MeetingMemberResponse>> kick(
			@PathVariable UUID meetingUuid,
			@PathVariable UUID memberUuid
	) {
		UUID actorMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingMemberResponse.from(
				kickMeetingMemberUseCase.kick(meetingUuid, actorMemberUuid, memberUuid)
		)));
	}
}
