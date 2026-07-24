package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgemelo.nimbusfilemanager.duplicate.application.constants.DuplicateConstants;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.DuplicateCandidateFileResponse;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.DuplicateFileResponse;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.GroupParts;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.PairKey;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.SimilarVideoGroupResponse;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.VideoCandidate;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.VideoFrameHash;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.VideoSignature;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.MediaFingerprintRepository;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection.MediaQuality;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection.VideoFrameRawResponse;
import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;
import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.VideoSimilarityProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PageUtils;

/**
 * Finds visually related videos. It reassembles each video's sampled frames into
 * a {@link VideoSignature}, buckets candidates by approximate duration so it
 * never runs an all-pairs O(n^2) comparison, and delegates the actual
 * comparison/aggregation to the pluggable {@link VideoSimilarityAlgorithm}
 * (frame pHash pre-filter + SSIM + trimmed mean + concordant-frame quorum). The
 * heavy grouping is cached per threshold by the shared
 * {@link SimilarityGroupCache}.
 */
@Service
@Transactional(readOnly = true)
public class VideoSimilarityService implements SimilarityGrouping {

	private final MediaFingerprintRepository mediaFingerprintRepository;
	private final DuplicateGroupAssembler duplicateGroupAssembler;
	private final VideoSimilarityAlgorithm algorithm;
	private final AppSettingService appSettingService;
	private final NimbusFileManagerProperties properties;
	private final DuplicateExclusionService duplicateExclusionService;
	private final VideoSimilarityProperties videoSimilarityProperties;

	private final SimilarityGroupCache<SimilarVideoGroupResponse> cache;

	public VideoSimilarityService(MediaFingerprintRepository mediaFingerprintRepository,
			DuplicateGroupAssembler duplicateGroupAssembler, VideoSimilarityAlgorithm algorithm,
			AppSettingService appSettingService, NimbusFileManagerProperties properties,
			DuplicateExclusionService duplicateExclusionService, VideoSimilarityProperties videoSimilarityProperties) {
		this.mediaFingerprintRepository = mediaFingerprintRepository;
		this.duplicateGroupAssembler = duplicateGroupAssembler;
		this.algorithm = algorithm;
		this.appSettingService = appSettingService;
		this.properties = properties;
		this.duplicateExclusionService = duplicateExclusionService;
		this.videoSimilarityProperties = videoSimilarityProperties;
		this.cache = new SimilarityGroupCache<>(this::fingerprintSignature, this::maxPageSize);
	}

	/** Synchronous read used by tests and as a fallback (blocking on a miss). */
	public Page<SimilarVideoGroupResponse> groups(Integer minSimilarityPercent, Pageable pageable) {
		int minimum = clampSimilarity(minSimilarityPercent);

		if (!cache.isCached(minimum)) {
			computeAndCache(minimum, (_, _) -> {
			});
		}

		return cache.cachedPage(minimum, pageable).orElseGet(() -> cache.emptyPage(pageable));
	}

	@Override
	public boolean isCached(int minSimilarityPercent) {
		return cache.isCached(clampSimilarity(minSimilarityPercent));
	}

	public Optional<Page<SimilarVideoGroupResponse>> cachedPage(int minSimilarityPercent, Pageable pageable) {
		return cache.cachedPage(clampSimilarity(minSimilarityPercent), pageable);
	}

	@Override
	public void computeAndCache(int minSimilarityPercent, SimilarityProgressCallback progress) {
		int minimum = clampSimilarity(minSimilarityPercent);

		String signature = cache.currentSignature();

		List<VideoCandidate> candidates = withoutExcluded(reassemble(mediaFingerprintRepository
				.findFingerprintedVideoFrames(algorithm.kind(), algorithm.algorithm(), PageUtils.firstPage(rowCap()))));

		List<UUID> allIds = candidates.stream().map(candidate -> candidate.signature().id()).toList();

		Map<UUID, MediaQuality> quality = duplicateGroupAssembler.qualityByPublicId(allIds);

		Map<UUID, Set<Long>> buckets = precomputeBuckets(candidates);

		Map<PairKey, Integer> scores = new HashMap<>();

		List<List<VideoCandidate>> groups = group(candidates, minimum, scores, buckets, progress);

		List<SimilarVideoGroupResponse> responses = groups.stream()
				.map(group -> toResponse(group, scores, buckets, quality))
				.sorted((first, second) -> Long.compare(second.wastedSize().bytes(), first.wastedSize().bytes()))
				.toList();

		cache.put(minimum, signature, responses);
	}

	void evictFromCache(Collection<UUID> removedPublicIds) {
		if (removedPublicIds == null || removedPublicIds.isEmpty()) {
			return;
		}

		Set<UUID> removed = new HashSet<>(removedPublicIds);

		cache.evict(group -> retains(group, removed));
	}

	public void invalidateCache() {
		cache.invalidate();
	}

	/** Reassembles the per-frame rows (ordered by file then sampleIndex) per video. */
	private List<VideoCandidate> reassemble(List<VideoFrameRawResponse> rows) {
		List<VideoCandidate> candidates = new ArrayList<>();

		UUID currentId = null;

		List<VideoFrameHash> frames = new ArrayList<>();

		VideoFrameRawResponse head = null;

		for (VideoFrameRawResponse row : rows) {
			if (!row.id().equals(currentId)) {
				if (head != null) {
					candidates.add(toCandidate(head, frames));
				}

				currentId = row.id();

				frames = new ArrayList<>();

				head = row;
			}

			frames.add(new VideoFrameHash(row.sampleIndex(), row.phash(), row.luminance()));
		}

		if (head != null) {
			candidates.add(toCandidate(head, frames));
		}

		return candidates;
	}

	private VideoCandidate toCandidate(VideoFrameRawResponse head, List<VideoFrameHash> frames) {
		VideoSignature signature = new VideoSignature(head.id(), List.copyOf(frames), head.durationSeconds(),
				head.width(), head.height());

		return new VideoCandidate(signature, head.fileName(), head.extension(), head.sizeBytes(), head.currentPath(),
				head.currentFolder(), head.modifiedAt());
	}

	private Map<UUID, Set<Long>> precomputeBuckets(List<VideoCandidate> candidates) {
		Map<UUID, Set<Long>> buckets = new HashMap<>();

		for (VideoCandidate candidate : candidates) {
			buckets.put(candidate.signature().id(), algorithm.candidateBuckets(candidate.signature()));
		}

		return buckets;
	}

	private List<VideoCandidate> withoutExcluded(List<VideoCandidate> candidates) {
		Set<UUID> excludedIds = new HashSet<>(duplicateExclusionService.excludedFilePublicIds());
		List<String> excludedFolders = duplicateExclusionService.excludedFolders();

		if (excludedIds.isEmpty() && excludedFolders.isEmpty()) {
			return candidates;
		}

		return candidates.stream().filter(candidate -> !excludedIds.contains(candidate.signature().id()))
				.filter(candidate -> !isUnderExcludedFolder(candidate.currentFolder(), excludedFolders)).toList();
	}

	private boolean isUnderExcludedFolder(String folder, List<String> excludedFolders) {
		if (folder == null) {
			return false;
		}

		String normalized = folder.replace('\\', '/');

		for (String excluded : excludedFolders) {
			if (normalized.equals(excluded) || normalized.startsWith(excluded + "/")) {
				return true;
			}
		}

		return false;
	}

	private boolean retains(SimilarVideoGroupResponse group, Set<UUID> removed) {
		if (removed.contains(group.keep().id())) {
			return false;
		}

		boolean removedDeleteCandidate = group.deleteCandidates().stream().map(DuplicateCandidateFileResponse::id)
				.anyMatch(removed::contains);

		boolean removedReviewCandidate = group.reviewCandidates().stream().map(DuplicateCandidateFileResponse::id)
				.anyMatch(removed::contains);

		return !removedDeleteCandidate && !removedReviewCandidate;
	}

	private String fingerprintSignature() {
		List<Object[]> rows = mediaFingerprintRepository.fingerprintSignature(algorithm.kind(), algorithm.algorithm());

		if (rows.isEmpty()) {
			return "empty";
		}

		Object[] row = rows.get(0);

		return row[0] + "-" + row[1] + "-" + row[2];
	}

	private List<List<VideoCandidate>> group(List<VideoCandidate> candidates, int minimum, Map<PairKey, Integer> scores,
			Map<UUID, Set<Long>> buckets, SimilarityProgressCallback progress) {
		int total = candidates.size();

		int processed = 0;

		progress.update(0, total);

		List<List<VideoCandidate>> clusters = new ArrayList<>();

		for (VideoCandidate candidate : candidates) {
			List<VideoCandidate> target = null;

			for (List<VideoCandidate> cluster : clusters) {
				if (withinThresholdOfAll(candidate, cluster, minimum, scores, buckets)) {
					target = cluster;

					break;
				}
			}

			if (target == null) {
				target = new ArrayList<>();

				clusters.add(target);
			}

			target.add(candidate);

			progress.update(++processed, total);
		}
		return clusters.stream().filter(cluster -> cluster.size() > 1).toList();
	}

	private boolean withinThresholdOfAll(VideoCandidate candidate, List<VideoCandidate> cluster, int minimum,
			Map<PairKey, Integer> scores, Map<UUID, Set<Long>> buckets) {
		for (VideoCandidate member : cluster) {
			if (score(candidate, member, minimum, scores, buckets) < minimum) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Bucket-gated similarity: a cheap disjoint-bucket check rejects distant videos
	 * before any frame comparison, so the expensive SSIM only runs for videos that
	 * share a duration bucket. Memoized per pair for the duration of one grouping.
	 */
	private int score(VideoCandidate first, VideoCandidate second, int minimum, Map<PairKey, Integer> scores,
			Map<UUID, Set<Long>> buckets) {
		UUID firstId = first.signature().id();
		UUID secondId = second.signature().id();

		if (Collections.disjoint(buckets.get(firstId), buckets.get(secondId))) {
			return -1;
		}

		return scores.computeIfAbsent(PairKey.of(firstId, secondId),
				_ -> algorithm.similarityPercent(first.signature(), second.signature(), minimum));
	}

	private SimilarVideoGroupResponse toResponse(List<VideoCandidate> group, Map<PairKey, Integer> scores,
			Map<UUID, Set<Long>> buckets, Map<UUID, MediaQuality> quality) {
		List<DuplicateFileResponse> files = group.stream().map(this::toFileResponse).toList();

		GroupParts parts = duplicateGroupAssembler.assemble(files, quality, false);

		return new SimilarVideoGroupResponse(String.valueOf(parts.keep().id()), group.size(),
				worstScore(group, scores, buckets), SizeResponse.of(parts.wastedBytes()), parts.keep(),
				parts.deleteCandidates(), parts.reviewCandidates());
	}

	/** Lowest pairwise similarity in the group, so the displayed floor is honest. */
	private int worstScore(List<VideoCandidate> group, Map<PairKey, Integer> scores, Map<UUID, Set<Long>> buckets) {
		int worst = 100;

		for (int first = 0; first < group.size(); first++) {
			for (int second = first + 1; second < group.size(); second++) {
				worst = Math.min(worst,
						score(group.get(first), group.get(second), DuplicateConstants.MIN_SIMILARITY_PERCENT, scores,
								buckets));
			}
		}

		return worst;
	}

	private DuplicateFileResponse toFileResponse(VideoCandidate candidate) {
		return new DuplicateFileResponse(candidate.signature().id(), candidate.fileName(), candidate.extension(),
				"VIDEO", SizeResponse.of(candidate.sizeBytes()), candidate.currentPath(), candidate.currentFolder(),
				candidate.modifiedAt());
	}

	private int rowCap() {
		return videoSimilarityProperties.maxCandidatesOrDefault() * algorithm.framesPerFingerprint();
	}

	private int clampSimilarity(Integer requested) {
		if (requested == null) {
			return DuplicateConstants.MIN_SIMILARITY_PERCENT;
		}

		return Math.clamp(requested, DuplicateConstants.MIN_SIMILARITY_PERCENT,
				DuplicateConstants.MAX_SIMILARITY_PERCENT);
	}

	private int maxPageSize() {
		return appSettingService.intValue(SettingsConstants.API_MAX_PAGE_SIZE, properties.api().maxPageSize());
	}
}
