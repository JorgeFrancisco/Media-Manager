package br.com.jorgemelo.nimbusfilemanager.organization.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationReconcileIssueResponse;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.PathSync;

/**
 * Package-private holder that tallies reconcile findings (missing on disk,
 * missing in database, path mismatches) and keeps bounded samples of each, plus
 * the queued current_path syncs. Used only by
 * {@link OrganizationReconcileService} while scanning a source tree.
 */
class ReconcileAccumulator {

	private final int sampleLimit;
	private final Set<String> dbPaths = new HashSet<>();
	private final ArrayList<OrganizationReconcileIssueResponse> missingOnDiskSamples = new ArrayList<>();
	private final ArrayList<OrganizationReconcileIssueResponse> missingInDatabaseSamples = new ArrayList<>();
	private final ArrayList<OrganizationReconcileIssueResponse> pathMismatchSamples = new ArrayList<>();
	private final ArrayList<PathSync> pathSyncs = new ArrayList<>();
	private long missingOnDisk;
	private long missingInDatabase;
	private long pathMismatches;

	ReconcileAccumulator(int sampleLimit) {
		this.sampleLimit = sampleLimit;
	}

	void addDatabasePath(String path) {
		dbPaths.add(path);
	}

	void addMissingOnDisk(Long catalogFileId, String currentPath) {
		missingOnDisk++;

		addSample(missingOnDiskSamples, new OrganizationReconcileIssueResponse(catalogFileId, currentPath, currentPath,
				null, "File is registered in database but does not exist on disk."));
	}

	void addMissingInDatabase(String path) {
		missingInDatabase++;

		addSample(missingInDatabaseSamples, new OrganizationReconcileIssueResponse(null, path, null, path,
				"File exists on disk but is not registered in database."));
	}

	void addPathMismatch(Long catalogFileId, String fileKey, String currentPath) {
		pathMismatches++;

		addSample(pathMismatchSamples, new OrganizationReconcileIssueResponse(catalogFileId, currentPath, currentPath,
				fileKey, "catalog_file.file_key differs from catalog_file_location.current_path."));
	}

	void addPathSync(Long catalogFileId, String fileKey) {
		pathSyncs.add(new PathSync(catalogFileId, fileKey));
	}

	List<PathSync> pathSyncs() {
		return pathSyncs;
	}

	private void addSample(List<OrganizationReconcileIssueResponse> samples,
			OrganizationReconcileIssueResponse sample) {
		if (samples.size() < sampleLimit) {
			samples.add(sample);
		}
	}

	Set<String> dbPaths() {
		return dbPaths;
	}

	long filesInDatabase() {
		return dbPaths.size();
	}

	long missingOnDisk() {
		return missingOnDisk;
	}

	long missingInDatabase() {
		return missingInDatabase;
	}

	long pathMismatches() {
		return pathMismatches;
	}

	List<OrganizationReconcileIssueResponse> missingOnDiskSamples() {
		return missingOnDiskSamples;
	}

	List<OrganizationReconcileIssueResponse> missingInDatabaseSamples() {
		return missingInDatabaseSamples;
	}

	List<OrganizationReconcileIssueResponse> pathMismatchSamples() {
		return pathMismatchSamples;
	}
}