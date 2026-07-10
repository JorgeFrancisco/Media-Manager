package br.com.jorgemelo.nimbusfilemanager.media.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgemelo.nimbusfilemanager.media.application.dto.MediaSearchCriteria;
import br.com.jorgemelo.nimbusfilemanager.media.application.dto.MediaSearchResponse;
import br.com.jorgemelo.nimbusfilemanager.media.domain.repository.MediaSearchRepository;
import br.com.jorgemelo.nimbusfilemanager.media.domain.repository.projection.MediaSearchFilter;
import br.com.jorgemelo.nimbusfilemanager.media.domain.repository.projection.MediaSearchRawResponse;
import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;
import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PageUtils;
import br.com.jorgemelo.nimbusfilemanager.shared.util.TextUtils;

@Service
@Transactional(readOnly = true)
public class MediaSearchService {

	private final MediaSearchRepository mediaSearchRepository;
	private final AppSettingService appSettingService;
	private final NimbusFileManagerProperties properties;

	public MediaSearchService(MediaSearchRepository mediaSearchRepository, AppSettingService appSettingService,
			NimbusFileManagerProperties properties) {
		this.mediaSearchRepository = mediaSearchRepository;
		this.appSettingService = appSettingService;
		this.properties = properties;
	}

	public Page<MediaSearchResponse> search(MediaSearchCriteria criteria, Pageable pageable) {
		Pageable safePageable = PageUtils.capped(pageable, maxPageSize());

		MediaSearchFilter filter = new MediaSearchFilter(criteria.fileType(),
				TextUtils.upperBlankToNull(criteria.codec()), TextUtils.blankToNull(criteria.folder()),
				TextUtils.blankToNull(criteria.extension()), criteria.year(), criteria.month(), criteria.minSizeBytes(),
				criteria.maxSizeBytes());

		return mediaSearchRepository.search(filter, safePageable).map(this::toResponse);
	}

	private MediaSearchResponse toResponse(MediaSearchRawResponse raw) {
		return new MediaSearchResponse(raw.id(), raw.fileName(), raw.extension(), raw.fileType(),
				SizeResponse.of(raw.sizeBytes()), raw.currentPath(), raw.currentFolder(), raw.createdAt(),
				raw.modifiedAt(), raw.year(), raw.month(), raw.day(), raw.yearMonth(), raw.videoCodec(),
				raw.audioCodec(), raw.durationSeconds(), raw.width(), raw.height(), raw.manufacturer(), raw.model());
	}

	private int maxPageSize() {
		return appSettingService.intValue(SettingsConstants.API_MAX_PAGE_SIZE, properties.api().maxPageSize());
	}
}