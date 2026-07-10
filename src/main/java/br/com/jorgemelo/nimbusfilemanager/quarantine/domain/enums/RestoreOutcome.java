package br.com.jorgemelo.nimbusfilemanager.quarantine.domain.enums;

/**
 * Result of a single quarantine restore attempt.
 *
 * <ul>
 * <li>{@code RESTORED} - the file was moved back and the catalog
 * re-activated.</li>
 * <li>{@code SKIPPED} - the user chose to skip this file (no-op, stays in
 * quarantine).</li>
 * <li>{@code CONFLICT} - a file already exists at the destination and no
 * resolution was chosen.</li>
 * <li>{@code ORIGIN_MISSING} - the original folder no longer exists and no
 * alternate was chosen.</li>
 * <li>{@code MISSING_IN_QUARANTINE} - the quarantined copy is no longer on disk
 * (already purged).</li>
 * <li>{@code LOCKED} - another operation holds the path; try again later.</li>
 * <li>{@code ERROR} - an IO or integrity failure; the file was left safely in
 * quarantine.</li>
 * </ul>
 */
public enum RestoreOutcome {

	RESTORED, SKIPPED, CONFLICT, ORIGIN_MISSING, MISSING_IN_QUARANTINE, LOCKED, ERROR
}