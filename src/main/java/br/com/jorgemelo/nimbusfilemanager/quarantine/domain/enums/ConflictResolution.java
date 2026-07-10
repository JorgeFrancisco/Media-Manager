package br.com.jorgemelo.nimbusfilemanager.quarantine.domain.enums;

/**
 * How to handle a name collision when a file already exists at the restore
 * destination.
 *
 * <ul>
 * <li>{@code BLOCK} - do not restore; report the conflict so the user can
 * decide (default).</li>
 * <li>{@code RENAME} - restore next to the existing file under a numbered name,
 * e.g. {@code foo (1).jpg}.</li>
 * <li>{@code SKIP} - leave this file in quarantine untouched.</li>
 * </ul>
 *
 * <p>
 * There is deliberately no OVERWRITE: restoring never destroys a file already
 * on disk.
 */
public enum ConflictResolution {

	BLOCK, RENAME, SKIP
}