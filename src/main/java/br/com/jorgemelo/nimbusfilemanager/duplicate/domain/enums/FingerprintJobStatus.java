package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums;

/**
 * Terminal/lifecycle state of one background fingerprint run recorded in
 * {@code fingerprint_job_run}. {@link #CANCELLED} covers a safe pause (app
 * shutdown or an inventory taking priority); the remaining pending work is
 * picked up by the next run since the queue is derived, not stored.
 */
public enum FingerprintJobStatus {

	RUNNING, FINISHED, CANCELLED, FAILED
}