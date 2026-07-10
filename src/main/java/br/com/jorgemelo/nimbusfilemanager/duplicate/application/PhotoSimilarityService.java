package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgemelo.nimbusfilemanager.duplicate.application.constants.DuplicateConstants;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.constants.FingerprintAlgorithm;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.CachedGroups;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.DuplicateCandidateFileResponse;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.DuplicateFileResponse;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.GroupParts;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.PairKey;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.SimilarPhotoGroupResponse;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintKind;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.MediaFingerprintRepository;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection.MediaQuality;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection.PhotoHashRawResponse;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.PhotoPerceptualHashService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;
import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PageUtils;

/**
 * Finds visually related photos in two stages: a 256-bit pHash cheaply rejects
 * unrelated pairs, then SSIM confirms candidates and supplies the percentage
 * shown in the UI. A pHash match is never described as an equality or
 * percentage.
 */
@Service
@Transactional(readOnly = true)
public class PhotoSimilarityService {

	/** Safety cap while grouping remains an in-memory O(n²) operation. */
	static final int MAX_CANDIDATES = 8000;

	/**
	 * Generous pHash candidate radius (37.5% of 256 bits). SSIM makes the final
	 * decision, so this stage is intentionally optimized for recall rather than
	 * precision.
	 */
	static final int MAX_PHASH_CANDIDATE_DISTANCE = 96;

	private final MediaFingerprintRepository mediaFingerprintRepository;
	private final DuplicateGroupAssembler duplicateGroupAssembler;
	private final PhotoSsimService photoSsimService;
	private final AppSettingService appSettingService;
	private final NimbusFileManagerProperties properties;
	private final DuplicateExclusionService duplicateExclusionService;

	/**
	 * Caches the heavy grouping (clustering + SSIM) per similarity threshold.
	 * Invalidated automatically when the fingerprint set changes (via
	 * {@link #fingerprintSignature()}), so paginating or re-opening the Fotos
	 * Semelhantes tab is instant instead of recomputing everything each request.
	 */
	private final Map<Integer, CachedGroups> cache = new ConcurrentHashMap<>();

	public PhotoSimilarityService(MediaFingerprintRepository mediaFingerprintRepository,
			DuplicateGroupAssembler duplicateGroupAssembler, PhotoSsimService photoSsimService,
			AppSettingService appSettingService, NimbusFileManagerProperties properties,
			DuplicateExclusionService duplicateExclusionService) {
		this.mediaFingerprintRepository = mediaFingerprintRepository;
		this.duplicateGroupAssembler = duplicateGroupAssembler;
		this.photoSsimService = photoSsimService;
		this.appSettingService = appSettingService;
		this.properties = properties;
		this.duplicateExclusionService = duplicateExclusionService;
	}

	/**
	 * Synchronous read used by tests and as a fallback: returns the cached page,
	 * computing (blocking) on a miss. The Duplicados screen does NOT use this - it
	 * uses {@link #cachedPage} plus the background
	 * {@code PhotoSimilarityAsyncRunner} so the page never blocks on the heavy
	 * grouping.
	 */
	public Page<SimilarPhotoGroupResponse> groups(Integer minSimilarityPercent, Pageable pageable) {
		int minimumSsim = clampSimilarity(minSimilarityPercent);

		if (!isCached(minimumSsim)) {
			computeAndCache(minimumSsim, (_, _) -> {
			});
		}

		return cachedPage(minimumSsim, pageable)
				.orElseGet(() -> paginate(List.of(), PageUtils.capped(pageable, maxPageSize())));
	}

	/**
	 * Whether the grouping for this threshold is already cached for the current
	 * fingerprint set.
	 */
	public boolean isCached(int minSimilarityPercent) {
		int minimumSsim = clampSimilarity(minSimilarityPercent);

		CachedGroups cached = cache.get(minimumSsim);

		return cached != null && cached.signature().equals(fingerprintSignature());
	}

	/**
	 * Page of the cached grouping for this threshold, or empty when it has not been
	 * computed yet - no blocking compute happens here, so the caller can show a
	 * "computing" state and let the background runner do the work.
	 */
	public Optional<Page<SimilarPhotoGroupResponse>> cachedPage(int minSimilarityPercent, Pageable pageable) {
		int minimumSsim = clampSimilarity(minSimilarityPercent);

		CachedGroups cached = cache.get(minimumSsim);

		if (cached == null || !cached.signature().equals(fingerprintSignature())) {
			return Optional.empty();
		}

		return Optional.of(paginate(cached.groups(), PageUtils.capped(pageable, maxPageSize())));
	}

	/**
	 * Runs the heavy grouping (clustering + SSIM) for a threshold and caches the
	 * result, reporting how many candidates have been processed to {@code progress}
	 * so a background runner can show a bar.
	 */
	void computeAndCache(int minSimilarityPercent, SimilarityProgressCallback progress) {
		int minimumSsim = clampSimilarity(minSimilarityPercent);

		String signature = fingerprintSignature();

		List<PhotoHashRawResponse> candidates = withoutExcluded(
				mediaFingerprintRepository
						.findFingerprintedPhotos(FingerprintKind.PHOTO_PHASH,
								FingerprintAlgorithm.FFMPEG_LANCZOS_PHASH_256_V1, PageUtils.firstPage(MAX_CANDIDATES))
						.getContent());

		List<UUID> allIds = candidates.stream().map(PhotoHashRawResponse::id).toList();

		Map<UUID, MediaQuality> quality = duplicateGroupAssembler.qualityByPublicId(allIds);

		Map<PairKey, Integer> scores = new HashMap<>();

		List<List<PhotoHashRawResponse>> groups = group(candidates, minimumSsim, scores, progress);

		List<SimilarPhotoGroupResponse> responses = groups.stream().map(group -> toResponse(group, scores, quality))
				.sorted((first, second) -> Long.compare(second.wastedSize().bytes(), first.wastedSize().bytes()))
				.toList();

		cache.put(minimumSsim, new CachedGroups(signature, responses));
	}

	/**
	 * Drops the given photos from the cached groupings after a soft-delete, so a
	 * follow-up reload shows the updated groups without recomputing. A group that
	 * loses any member is removed entirely (a pair becomes a single and disappears;
	 * a larger group is conservatively dropped and rebuilt on the next full
	 * recompute). The signature is refreshed to the post-delete state so the pruned
	 * result stays a cache hit instead of an immediate miss.
	 */
	void evictFromCache(Collection<UUID> removedPublicIds) {
		if (removedPublicIds == null || removedPublicIds.isEmpty() || cache.isEmpty()) {
			return;
		}

		Set<UUID> removed = new HashSet<>(removedPublicIds);

		String signature = fingerprintSignature();

		cache.replaceAll((_, cached) -> new CachedGroups(signature,
				cached.groups().stream().filter(group -> retains(group, removed)).toList()));
	}

	/**
	 * Clears every cached grouping so the next Fotos Semelhantes load recomputes
	 * from scratch. Used when the comparison-exclusion lists change: the
	 * fingerprint set is untouched (so the signature would not move on its own),
	 * but the excluded files/folders must be dropped from the groups on the next
	 * compute.
	 */
	public void invalidateCache() {
		cache.clear();
	}

	/**
	 * Drops candidates that are hidden from comparison - by their own public id or
	 * because they sit at or under an excluded folder - before the O(n²) grouping,
	 * so excluded photos never surface in the Fotos Semelhantes tab.
	 */
	private List<PhotoHashRawResponse> withoutExcluded(List<PhotoHashRawResponse> candidates) {
		Set<UUID> excludedIds = new HashSet<>(duplicateExclusionService.excludedFilePublicIds());
		List<String> excludedFolders = duplicateExclusionService.excludedFolders();

		if (excludedIds.isEmpty() && excludedFolders.isEmpty()) {
			return candidates;
		}

		return candidates.stream().filter(candidate -> !excludedIds.contains(candidate.id()))
				.filter(candidate -> !isUnderExcludedFolder(candidate.currentFolder(), excludedFolders)).toList();
	}

	private boolean isUnderExcludedFolder(String folder, List<String> excludedFolders) {
		// Excluded folders are stored separator-agnostic (forward slashes); a
		// candidate's
		// current folder (NOT NULL in the schema) is OS-native, so normalize it the
		// same
		// way before matching the folder itself or any subfolder under it.
		String normalized = folder.replace('\\', '/');

		for (String excluded : excludedFolders) {
			if (normalized.equals(excluded) || normalized.startsWith(excluded + "/")) {
				return true;
			}
		}

		return false;
	}

	private boolean retains(SimilarPhotoGroupResponse group, Set<UUID> removed) {
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
		List<Object[]> rows = mediaFingerprintRepository.fingerprintSignature(FingerprintKind.PHOTO_PHASH,
				FingerprintAlgorithm.FFMPEG_LANCZOS_PHASH_256_V1);

		if (rows.isEmpty()) {
			return "empty";
		}

		Object[] row = rows.get(0);

		return row[0] + "-" + row[1] + "-" + row[2];
	}

	private List<List<PhotoHashRawResponse>> group(List<PhotoHashRawResponse> candidates, int minimumSsim,
			Map<PairKey, Integer> scores, SimilarityProgressCallback progress) {
		int total = candidates.size();

		int processed = 0;

		progress.update(0, total);

		List<List<PhotoHashRawResponse>> clusters = new ArrayList<>();

		for (PhotoHashRawResponse candidate : candidates) {
			List<PhotoHashRawResponse> target = null;

			for (List<PhotoHashRawResponse> cluster : clusters) {
				if (withinThresholdOfAll(candidate, cluster, minimumSsim, scores)) {
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

	private boolean withinThresholdOfAll(PhotoHashRawResponse candidate, List<PhotoHashRawResponse> cluster,
			int minimumSsim, Map<PairKey, Integer> scores) {
		for (PhotoHashRawResponse member : cluster) {
			if (score(candidate, member, scores) < minimumSsim) {
				return false;
			}
		}
		return true;
	}

	private int score(PhotoHashRawResponse first, PhotoHashRawResponse second, Map<PairKey, Integer> scores) {
		if (PhotoPerceptualHashService.distance(first.phash(), second.phash()) > MAX_PHASH_CANDIDATE_DISTANCE) {
			return -1;
		}

		return scores.computeIfAbsent(PairKey.of(first.id(), second.id()),
				_ -> photoSsimService.similarityPercent(first.luminance(), second.luminance()));
	}

	private SimilarPhotoGroupResponse toResponse(List<PhotoHashRawResponse> group, Map<PairKey, Integer> scores,
			Map<UUID, MediaQuality> quality) {
		List<DuplicateFileResponse> files = group.stream().map(this::toFileResponse).toList();

		GroupParts parts = duplicateGroupAssembler.assemble(files, quality, false);

		return new SimilarPhotoGroupResponse(String.valueOf(parts.keep().id()), group.size(), worstSsim(group, scores),
				SizeResponse.of(parts.wastedBytes()), parts.keep(), parts.deleteCandidates(), parts.reviewCandidates());
	}

	/**
	 * Lowest pairwise SSIM in the group, so the displayed percentage is guaranteed.
	 */
	private int worstSsim(List<PhotoHashRawResponse> group, Map<PairKey, Integer> scores) {
		int worst = 100;

		for (int first = 0; first < group.size(); first++) {
			for (int second = first + 1; second < group.size(); second++) {
				worst = Math.min(worst, score(group.get(first), group.get(second), scores));
			}
		}

		return worst;
	}

	private DuplicateFileResponse toFileResponse(PhotoHashRawResponse raw) {
		return new DuplicateFileResponse(raw.id(), raw.fileName(), raw.extension(), "PHOTO",
				SizeResponse.of(raw.sizeBytes()), raw.currentPath(), raw.currentFolder(), raw.modifiedAt());
	}

	private Page<SimilarPhotoGroupResponse> paginate(List<SimilarPhotoGroupResponse> all, Pageable pageable) {
		int start = Math.min((int) pageable.getOffset(), all.size());

		int end = Math.min(start + pageable.getPageSize(), all.size());

		return new PageImpl<>(all.subList(start, end), pageable, all.size());
	}

	private int clampSimilarity(Integer requested) {
		if (requested == null) {
			return DuplicateConstants.MIN_SIMILARITY_PERCENT;
		}

		return Math.clamp(requested, DuplicateConstants.MIN_SIMILARITY_PERCENT, DuplicateConstants.MAX_SIMILARITY_PERCENT);
	}

	private int maxPageSize() {
		return appSettingService.intValue(SettingsConstants.API_MAX_PAGE_SIZE, properties.api().maxPageSize());
	}
}