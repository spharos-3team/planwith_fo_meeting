package com.planwith.planwith_fo_meeting.adapter.out.schedule;

import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_meeting.application.exception.BusinessException;
import com.planwith.planwith_fo_meeting.application.exception.ErrorCode;
import com.planwith.planwith_fo_meeting.application.port.out.ScheduleQueryPort;
import com.planwith.planwith_fo_meeting.config.GatewayTrustProperties;
import com.planwith.planwith_fo_meeting.config.ScheduleClientProperties;
import com.planwith.planwith_fo_meeting.domain.meeting.ScheduleSnapshot;

@Component
@Profile("!test")
public class HttpScheduleQueryAdapter implements ScheduleQueryPort {

	private static final Logger log = LoggerFactory.getLogger(HttpScheduleQueryAdapter.class);

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final GatewayTrustProperties gatewayTrustProperties;

	public HttpScheduleQueryAdapter(
			ScheduleClientProperties properties,
			ObjectMapper objectMapper,
			GatewayTrustProperties gatewayTrustProperties
	) {
		this.objectMapper = objectMapper;
		this.gatewayTrustProperties = gatewayTrustProperties;
		String baseUrl = StringUtils.hasText(properties.baseUrl())
				? properties.baseUrl().replaceAll("/$", "")
				: "http://planwith-fo-schedule:8081";
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
	}

	@Override
	public ScheduleSnapshot requireSchedule(UUID scheduleUuid) {
		if (scheduleUuid == null) {
			throw new BusinessException(ErrorCode.SCHEDULE_REQUIRED);
		}
		try {
			String body = restClient.get()
					.uri("/api/v1/schedules/{scheduleUuid}", scheduleUuid)
					.accept(MediaType.APPLICATION_JSON)
					.headers(headers -> {
						if (StringUtils.hasText(gatewayTrustProperties.internalToken())) {
							headers.set("X-Gateway-Internal-Token", gatewayTrustProperties.internalToken());
						}
					})
					.retrieve()
					.body(String.class);
			ScheduleSnapshot snapshot = parse(scheduleUuid, body);
			if (snapshot != null) {
				return snapshot;
			}
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == 404) {
				throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
			}
			log.warn("Schedule lookup failed for {}: HTTP {}", scheduleUuid, exception.getStatusCode().value());
		}
		catch (RuntimeException exception) {
			log.warn("Schedule lookup failed for {}", scheduleUuid, exception);
		}
		return new ScheduleSnapshot(scheduleUuid, null, null, null);
	}

	private ScheduleSnapshot parse(UUID scheduleUuid, String body) {
		if (!StringUtils.hasText(body)) {
			return null;
		}
		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode data = root.path("data");
			JsonNode schedule = data.path("schedule").isMissingNode() || data.path("schedule").isNull()
					? data
					: data.path("schedule");
			String destination = text(schedule, "destination");
			LocalDate startDate = date(schedule, "startDate");
			LocalDate endDate = date(schedule, "endDate");
			return new ScheduleSnapshot(scheduleUuid, destination, startDate, endDate);
		}
		catch (Exception exception) {
			log.warn("Schedule response parse failed for {}", scheduleUuid, exception);
			return null;
		}
	}

	private String text(JsonNode node, String field) {
		JsonNode value = node.path(field);
		if (value.isMissingNode() || value.isNull()) {
			return null;
		}
		String text = value.asText();
		return StringUtils.hasText(text) ? text : null;
	}

	private LocalDate date(JsonNode node, String field) {
		String text = text(node, field);
		if (text == null || text.length() < 10) {
			return null;
		}
		try {
			return LocalDate.parse(text.substring(0, 10));
		}
		catch (RuntimeException exception) {
			return null;
		}
	}
}
