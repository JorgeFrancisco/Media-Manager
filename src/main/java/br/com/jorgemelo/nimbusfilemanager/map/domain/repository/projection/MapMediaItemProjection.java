package br.com.jorgemelo.nimbusfilemanager.map.domain.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/** Compact media row for a map pin's paginated panel. */
public interface MapMediaItemProjection {

	UUID getPublicId();

	String getFileType();

	String getFileName();

	LocalDateTime getCaptureDate();
}