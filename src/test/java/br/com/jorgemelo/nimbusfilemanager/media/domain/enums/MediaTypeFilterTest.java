package br.com.jorgemelo.nimbusfilemanager.media.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

class MediaTypeFilterTest {

	@Test
	void eachGroupExpandsToItsFileTypes() {
		Assertions.assertThat(MediaTypeFilter.PHOTO.fileTypes()).containsExactly(FileType.PHOTO);
		Assertions.assertThat(MediaTypeFilter.VIDEO.fileTypes()).containsExactly(FileType.VIDEO);
		Assertions.assertThat(MediaTypeFilter.AUDIO.fileTypes()).containsExactly(FileType.AUDIO);
		Assertions.assertThat(MediaTypeFilter.DOCS.fileTypes()).containsExactlyInAnyOrder(FileType.PDF, FileType.WORD,
				FileType.EXCEL, FileType.POWERPOINT, FileType.TEXT);
		Assertions.assertThat(MediaTypeFilter.OTHERS.fileTypes()).containsExactlyInAnyOrder(FileType.ZIP, FileType.RAR,
				FileType.SEVEN_Z, FileType.OTHER);
	}

	@Test
	void theFiveGroupsTogetherSpanEveryFileType() {
		Set<FileType> all = MediaTypeFilter.fileTypesOf(Arrays.asList(MediaTypeFilter.values()));

		Assertions.assertThat(all).containsExactlyInAnyOrder(FileType.values());
	}

	@Test
	void anEmptyOrNullSelectionWidensToEveryFileType() {
		Assertions.assertThat(MediaTypeFilter.fileTypesOf(List.of())).containsExactlyInAnyOrder(FileType.values());
		Assertions.assertThat(MediaTypeFilter.fileTypesOf(null)).containsExactlyInAnyOrder(FileType.values());
	}

	@Test
	void aSubsetIsTheUnionOfItsGroups() {
		Assertions.assertThat(MediaTypeFilter.fileTypesOf(List.of(MediaTypeFilter.PHOTO, MediaTypeFilter.DOCS)))
				.containsExactlyInAnyOrder(FileType.PHOTO, FileType.PDF, FileType.WORD, FileType.EXCEL,
						FileType.POWERPOINT, FileType.TEXT);
	}
}