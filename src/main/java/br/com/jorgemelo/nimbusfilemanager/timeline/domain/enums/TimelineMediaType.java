package br.com.jorgemelo.nimbusfilemanager.timeline.domain.enums;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

public enum TimelineMediaType {

	ALL, PHOTO, VIDEO;

	public FileType fileType() {
		return this == ALL ? null : FileType.valueOf(name());
	}
}