package com.planwith.planwith_fo_meeting.adapter.in.web;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_meeting.adapter.in.web.auth.AuthenticatedUser;
import com.planwith.planwith_fo_meeting.adapter.in.web.auth.AuthenticatedUserContext;
import com.planwith.planwith_fo_meeting.adapter.in.web.auth.GatewayAuthenticationContextResolver;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.CreateMeetingRequest;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.MeetingDetailResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.MeetingListItemResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.MeetingResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.PagedResponse;
import com.planwith.planwith_fo_meeting.application.port.in.CreateMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingDetailUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingsUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.UploadMeetingCoverImageUseCase;
import com.planwith.planwith_fo_meeting.config.OpenApiConfig;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/meetings")
@Tag(name = "meetings", description = "모임 생성·조회")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MeetingController {

	private final GatewayAuthenticationContextResolver authContextResolver;
	private final CreateMeetingUseCase createMeetingUseCase;
	private final UploadMeetingCoverImageUseCase uploadMeetingCoverImageUseCase;
	private final ListMeetingsUseCase listMeetingsUseCase;
	private final GetMeetingDetailUseCase getMeetingDetailUseCase;

	public MeetingController(
			GatewayAuthenticationContextResolver authContextResolver,
			CreateMeetingUseCase createMeetingUseCase,
			UploadMeetingCoverImageUseCase uploadMeetingCoverImageUseCase,
			ListMeetingsUseCase listMeetingsUseCase,
			GetMeetingDetailUseCase getMeetingDetailUseCase
	) {
		this.authContextResolver = authContextResolver;
		this.createMeetingUseCase = createMeetingUseCase;
		this.uploadMeetingCoverImageUseCase = uploadMeetingCoverImageUseCase;
		this.listMeetingsUseCase = listMeetingsUseCase;
		this.getMeetingDetailUseCase = getMeetingDetailUseCase;
	}

	@GetMapping
	@Operation(summary = "모임 목록 (해체 제외, FULL 하단)")
	public ResponseEntity<ApiResponse<PagedResponse<MeetingListItemResponse>>> list(
			@RequestParam(required = false) MeetingStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		ListMeetingsUseCase.Result result = listMeetingsUseCase.list(status, page, size, viewerMemberUuid());
		PagedResponse<MeetingListItemResponse> body = new PagedResponse<>(
				result.content().stream().map(MeetingListItemResponse::from).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages()
		);
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@GetMapping("/{meetingUuid}")
	@Operation(summary = "모임 상세")
	public ResponseEntity<ApiResponse<MeetingDetailResponse>> detail(@PathVariable UUID meetingUuid) {
		return ResponseEntity.ok(ApiResponse.success(
				MeetingDetailResponse.from(getMeetingDetailUseCase.get(meetingUuid, viewerMemberUuid()))
		));
	}

	@PostMapping
	@Operation(summary = "모임 생성")
	public ResponseEntity<ApiResponse<MeetingResponse>> create(@Valid @RequestBody CreateMeetingRequest request) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		MeetingResponse response = MeetingResponse.from(createMeetingUseCase.create(new CreateMeetingUseCase.Command(
				hostMemberUuid,
				request.scheduleUuid(),
				request.title(),
				request.intro(),
				request.maxMemberCount(),
				request.coverImage(),
				new ScheduleSnapshot(
						request.scheduleUuid(),
						request.destination(),
						request.startAt(),
						request.endAt(),
						request.cost(),
						request.transport()
				)
		)));
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping(path = "/{meetingUuid}/cover-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "모임 대표 이미지 업로드 (stub URL)")
	public ResponseEntity<ApiResponse<MeetingResponse>> uploadCoverImage(
			@PathVariable UUID meetingUuid,
			@RequestPart("file") MultipartFile file
	) {
		UUID actorMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingResponse.from(
				uploadMeetingCoverImageUseCase.upload(meetingUuid, actorMemberUuid, file)
		)));
	}

	private UUID viewerMemberUuid() {
		AuthenticatedUser user = AuthenticatedUserContext.get();
		return user == null ? null : user.userId();
	}
}
