package com.planwith.planwith_fo_meeting.adapter.in.web.dto;

import java.util.List;

public record PagedResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
