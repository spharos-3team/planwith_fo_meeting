package com.planwith.planwith_fo_meeting.adapter.in.web;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.MyMeetingsResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.PagedResponse;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.RecruitmentStatusRequest;
import com.planwith.planwith_fo_meeting.adapter.in.web.dto.UpdateMeetingRequest;
import com.planwith.planwith_fo_meeting.application.port.in.BumpMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ChangeMeetingRecruitmentStatusUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.CompleteMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.CreateMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.DisbandMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.GetMeetingDetailUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ListMeetingsUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.ListMyMeetingsUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.UpdateMeetingUseCase;
import com.planwith.planwith_fo_meeting.application.port.in.UploadMeetingCoverImageUseCase;
import com.planwith.planwith_fo_meeting.config.OpenApiConfig;
import com.planwith.planwith_fo_meeting.domain.meeting.MeetingStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/meetings")
@Tag(name = "meetings", description = "모임 생성·조회·수정·완료·해체")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MeetingController {

	private final GatewayAuthenticationContextResolver authContextResolver;
	private final CreateMeetingUseCase createMeetingUseCase;
	private final UploadMeetingCoverImageUseCase uploadMeetingCoverImageUseCase;
	private final ListMeetingsUseCase listMeetingsUseCase;
	private final ListMyMeetingsUseCase listMyMeetingsUseCase;
	private final GetMeetingDetailUseCase getMeetingDetailUseCase;
	private final UpdateMeetingUseCase updateMeetingUseCase;
	private final ChangeMeetingRecruitmentStatusUseCase changeMeetingRecruitmentStatusUseCase;
	private final BumpMeetingUseCase bumpMeetingUseCase;
	private final CompleteMeetingUseCase completeMeetingUseCase;
	private final DisbandMeetingUseCase disbandMeetingUseCase;

	public MeetingController(
			GatewayAuthenticationContextResolver authContextResolver,
			CreateMeetingUseCase createMeetingUseCase,
			UploadMeetingCoverImageUseCase uploadMeetingCoverImageUseCase,
			ListMeetingsUseCase listMeetingsUseCase,
			ListMyMeetingsUseCase listMyMeetingsUseCase,
			GetMeetingDetailUseCase getMeetingDetailUseCase,
			UpdateMeetingUseCase updateMeetingUseCase,
			ChangeMeetingRecruitmentStatusUseCase changeMeetingRecruitmentStatusUseCase,
			BumpMeetingUseCase bumpMeetingUseCase,
			CompleteMeetingUseCase completeMeetingUseCase,
			DisbandMeetingUseCase disbandMeetingUseCase
	) {
		this.authContextResolver = authContextResolver;
		this.createMeetingUseCase = createMeetingUseCase;
		this.uploadMeetingCoverImageUseCase = uploadMeetingCoverImageUseCase;
		this.listMeetingsUseCase = listMeetingsUseCase;
		this.listMyMeetingsUseCase = listMyMeetingsUseCase;
		this.getMeetingDetailUseCase = getMeetingDetailUseCase;
		this.updateMeetingUseCase = updateMeetingUseCase;
		this.changeMeetingRecruitmentStatusUseCase = changeMeetingRecruitmentStatusUseCase;
		this.bumpMeetingUseCase = bumpMeetingUseCase;
		this.completeMeetingUseCase = completeMeetingUseCase;
		this.disbandMeetingUseCase = disbandMeetingUseCase;
	}

	@GetMapping
	@Operation(summary = "모임 목록 카드. 일정은 생성 시 스냅샷. page/size, 기본 해체·완료 제외")
	public ResponseEntity<ApiResponse<PagedResponse<MeetingListItemResponse>>> list(
			@RequestParam(required = false) MeetingStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		ListMeetingsUseCase.Result result = listMeetingsUseCase.list(status, page, size);
		PagedResponse<MeetingListItemResponse> body = new PagedResponse<>(
				result.content().stream().map(MeetingListItemResponse::from).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages()
		);
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@GetMapping("/me")
	@Operation(summary = "내 모임. scope=hosted|joined|pending")
	public ResponseEntity<ApiResponse<MyMeetingsResponse<MeetingListItemResponse>>> myMeetings(
			@RequestParam(defaultValue = "hosted") String scope,
			@RequestParam(required = false) MeetingStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		ListMyMeetingsUseCase.Result result = listMyMeetingsUseCase.list(
				memberUuid,
				ListMyMeetingsUseCase.parseScope(scope),
				status,
				page,
				size
		);
		MyMeetingsResponse<MeetingListItemResponse> body = new MyMeetingsResponse<>(
				result.content().stream().map(MeetingListItemResponse::from).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.canCreate()
		);
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@GetMapping("/{meetingUuid}")
	@Operation(summary = "모임 상세. 여행 기간·비용·이동수단은 scheduleUuid로 schedule 서비스 조회")
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
				request.coverImage()
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

	@PatchMapping("/{meetingUuid}")
	@Operation(summary = "모임 소개·일정·최대인원 수정")
	public ResponseEntity<ApiResponse<MeetingResponse>> update(
			@PathVariable UUID meetingUuid,
			@Valid @RequestBody UpdateMeetingRequest request
	) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingResponse.from(
				updateMeetingUseCase.update(new UpdateMeetingUseCase.Command(
						meetingUuid,
						hostMemberUuid,
						request.scheduleUuid(),
						request.title(),
						request.intro(),
						request.maxMemberCount()
				))
		)));
	}

	@PatchMapping("/{meetingUuid}/recruitment-status")
	@Operation(summary = "모집중/모집완료 전환")
	public ResponseEntity<ApiResponse<MeetingResponse>> changeRecruitmentStatus(
			@PathVariable UUID meetingUuid,
			@Valid @RequestBody RecruitmentStatusRequest request
	) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingResponse.from(
				changeMeetingRecruitmentStatusUseCase.change(meetingUuid, hostMemberUuid, request.status())
		)));
	}

	@PostMapping("/{meetingUuid}/bump")
	@Operation(summary = "모임 끌어올리기")
	public ResponseEntity<ApiResponse<MeetingResponse>> bump(@PathVariable UUID meetingUuid) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingResponse.from(
				bumpMeetingUseCase.bump(meetingUuid, hostMemberUuid)
		)));
	}

	@PostMapping("/{meetingUuid}/complete")
	@Operation(summary = "모임 완료. 채팅방은 유지되고 입력만 막힘 (chat ENDED)")
	public ResponseEntity<ApiResponse<MeetingResponse>> complete(@PathVariable UUID meetingUuid) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingResponse.from(
				completeMeetingUseCase.complete(meetingUuid, hostMemberUuid)
		)));
	}

	@PostMapping("/{meetingUuid}/disband")
	@Operation(summary = "모임 해체. 공개 목록에서 제거하고 채팅방 삭제")
	public ResponseEntity<ApiResponse<MeetingResponse>> disband(@PathVariable UUID meetingUuid) {
		UUID hostMemberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(MeetingResponse.from(
				disbandMeetingUseCase.disband(meetingUuid, hostMemberUuid)
		)));
	}

	private UUID viewerMemberUuid() {
		AuthenticatedUser user = AuthenticatedUserContext.get();
		return user == null ? null : user.userId();
	}
}
