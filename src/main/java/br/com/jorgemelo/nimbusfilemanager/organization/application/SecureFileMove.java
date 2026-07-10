package br.com.jorgemelo.nimbusfilemanager.organization.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.MoveBaseline;
import lombok.extern.slf4j.Slf4j;

/**
 * The single secure-move primitive shared by every feature that relocates a
 * real file on disk - organization, duplicate quarantine, and the undo of both.
 * It captures a SHA-256 baseline from the source, creates the target's parent
 * directories, moves the file, and verifies the target byte-for-byte against
 * the baseline, throwing {@link MoveIntegrityException} on any mismatch.
 *
 * <p>
 * Persistence and audit rows are the caller's responsibility (they differ per
 * feature); the caller wraps this call in its own transaction and applies
 * {@link #rollback} if its own post-move step fails, so the integrity guarantee
 * and the rollback primitive stay identical everywhere while the bookkeeping
 * stays local. This is the one place file-move safety, integrity and physical
 * rollback live - features never call {@code Files.move} directly.
 */
@Slf4j
@Component
public class SecureFileMove {

	private final OrganizationMoveVerifier verifier;

	public SecureFileMove(OrganizationMoveVerifier verifier) {
		this.verifier = verifier;
	}

	/**
	 * Captures the baseline, creates parents, moves {@code source} to
	 * {@code target} and verifies the moved file. An IO failure before the move
	 * leaves the source in place; a verification failure throws
	 * {@link MoveIntegrityException} <em>without</em> rolling back, so the caller
	 * applies its own rollback/audit policy (mirrors the organization executor).
	 */
	public void move(Path source, Path target, boolean overwrite) throws IOException {
		MoveBaseline baseline = verifier.capture(source);

		Path parent = target.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}

		if (overwrite) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		} else {
			Files.move(source, target);
		}

		verifier.verify(source, target, baseline);
	}

	/**
	 * Best-effort physical move-back after a failed post-move step. Returns false
	 * if it could not.
	 */
	public boolean rollback(Path from, Path to) {
		try {
			Files.move(from, to);

			return true;
		} catch (IOException e) {
			log.error("Could not roll back move from {} to {}", from, to, e);

			return false;
		}
	}
}