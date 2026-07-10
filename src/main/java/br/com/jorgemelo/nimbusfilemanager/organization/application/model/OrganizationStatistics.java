package br.com.jorgemelo.nimbusfilemanager.organization.application.model;

import java.util.List;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationItem;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationSummary;
import br.com.jorgemelo.nimbusfilemanager.shared.util.NumberUtils;

public class OrganizationStatistics {

	private int processed;

	private long totalFiles;
	private long filesWithDate;
	private long filesWithoutDate;
	private long alreadyOrganized;
	private long plannedMoves;
	private long totalSizeBytes;

	public void incrementProcessed() {
		processed++;
	}

	public void add(OrganizationItem item) {
		totalFiles++;
		totalSizeBytes += NumberUtils.zeroIfNull(item.sizeBytes());

		if (item.missingDate()) {
			filesWithoutDate++;
		} else {
			filesWithDate++;
		}

		if (item.samePath()) {
			alreadyOrganized++;
		} else {
			plannedMoves++;
		}
	}

	public OrganizationSummary toSummary(List<OrganizationItem> items) {
		long conflicts = items.stream().filter(OrganizationItem::conflict).count();
		long targetAlreadyExists = items.stream().filter(OrganizationItem::targetExists).count();
		long duplicateTargets = items.stream().filter(OrganizationItem::duplicateTarget).count();

		return new OrganizationSummary(totalFiles, filesWithDate, filesWithoutDate, alreadyOrganized, plannedMoves,
				totalSizeBytes, conflicts, targetAlreadyExists, duplicateTargets);
	}

	public int processed() {
		return processed;
	}

	public long totalFiles() {
		return totalFiles;
	}

	public long plannedMoves() {
		return plannedMoves;
	}

	public long alreadyOrganized() {
		return alreadyOrganized;
	}
}