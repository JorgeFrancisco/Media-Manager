package br.com.jorgemelo.nimbusfilemanager.media.application.explorer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.CatalogFileRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Write half of the Explorer refresh: when {@link FileExplorerService} finds
 * database records whose files no longer exist on disk, this service marks them
 * as MISSING (lifecycle_status) so they disappear on the next refresh instead
 * of resurfacing forever. This reconciles only the visible folder (no recursive
 * scan) and is the only reconciliation that reaches records outside the
 * monitored library, which the inventory watcher never sees.
 *
 * <p>
 * Runs in REQUIRES_NEW because {@link FileExplorerService} browses inside a
 * read-only transaction.
 * </p>
 */
@Slf4j
@Service
public class FileExplorerReconcileService {

	private final CatalogFileRepository catalogFileRepository;

	@Autowired
	public FileExplorerReconcileService(CatalogFileRepository catalogFileRepository) {
		this.catalogFileRepository = catalogFileRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markMissing(List<Long> catalogFileIds) {
		if (catalogFileIds.isEmpty()) {
			return;
		}

		int updated = catalogFileRepository.markMissingByIds(catalogFileIds);

		log.info("Explorer reconcile: marked {} database record(s) as missing on disk", updated);
	}
}