package br.com.jorgemelo.nimbusfilemanager.organization.application;

/**
 * Thrown when the post-move integrity check fails: after a physical move, the
 * source still exists, the target is missing, the target size differs from the
 * source, or the target SHA-256 does not match the source. Distinct from a
 * database-update failure so {@link OrganizationExecutor} can classify the
 * {@code Movement} with {@code INTEGRITY_CHECK_FAILED} and roll the file back.
 */
public class MoveIntegrityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MoveIntegrityException(String message) {
		super(message);
	}
}