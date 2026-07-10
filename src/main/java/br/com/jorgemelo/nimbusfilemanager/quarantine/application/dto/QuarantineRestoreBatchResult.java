package br.com.jorgemelo.nimbusfilemanager.quarantine.application.dto;

import java.util.List;

/**
 * Aggregated outcome of restoring every still-quarantined file of one deletion
 * (execution) at once. The easy cases restore immediately; items that need a
 * decision (a name collision at the original path, or a vanished origin folder)
 * come back as {@code conflicts}/{@code originMissing} and are left in
 * quarantine for the user to resolve one by one.
 */
public record QuarantineRestoreBatchResult(boolean success, int total, int restored, int skipped, int conflicts,
		int originMissing, int errors, String message, List<QuarantineRestoreResult> items) {
}