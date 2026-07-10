package br.com.jorgemelo.nimbusfilemanager.timeline.application;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.jorgemelo.nimbusfilemanager.timeline.application.dto.TimelineCursor;
import br.com.jorgemelo.nimbusfilemanager.timeline.application.dto.TimelineUndatedCursor;
import br.com.jorgemelo.nimbusfilemanager.timeline.domain.enums.TimelineMediaType;

@Component
public class TimelineCursorCodec {

	private final ObjectMapper objectMapper;

	public TimelineCursorCodec(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String encode(TimelineCursor cursor) {
		try {
			return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(cursor));
		} catch (Exception e) {
			throw new IllegalStateException("Could not encode Timeline cursor", e);
		}
	}

	public TimelineCursor decode(String value, TimelineMediaType expectedType) {
		try {
			byte[] json = Base64.getUrlDecoder().decode(value.getBytes(StandardCharsets.US_ASCII));

			TimelineCursor cursor = objectMapper.readValue(json, TimelineCursor.class);

			if (cursor.captureDate() == null || cursor.internalId() < 1 || cursor.type() != expectedType) {
				throw new IllegalArgumentException("Invalid Timeline cursor");
			}

			return cursor;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid Timeline cursor", e);
		}
	}

	String encodeUndated(TimelineUndatedCursor cursor) {
		try {
			return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(cursor));
		} catch (Exception e) {
			throw new IllegalStateException("Could not encode undated Timeline cursor", e);
		}
	}

	TimelineUndatedCursor decodeUndated(String value, TimelineMediaType expectedType) {
		try {
			TimelineUndatedCursor cursor = objectMapper.readValue(Base64.getUrlDecoder().decode(value),
					TimelineUndatedCursor.class);

			if (cursor.internalId() < 1 || cursor.type() != expectedType) {
				throw new IllegalArgumentException("Invalid Timeline cursor");
			}

			return cursor;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid Timeline cursor", e);
		}
	}
}