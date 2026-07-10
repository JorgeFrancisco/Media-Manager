package br.com.jorgemelo.nimbusfilemanager.processing.application.dto;

import br.com.jorgemelo.nimbusfilemanager.processing.domain.enums.Status;

public record Outcome<I, O>(I item, O value, Exception error, Status status) {

	public static <I, O> Outcome<I, O> success(I item, O value) {
		return new Outcome<>(item, value, null, Status.EXECUTED);
	}

	public static <I, O> Outcome<I, O> cancelled(I item) {
		return new Outcome<>(item, null, null, Status.CANCELLED);
	}

	public static <I, O> Outcome<I, O> error(I item, Exception error) {
		return new Outcome<>(item, null, error, Status.ERROR);
	}

	public boolean executed() {
		return status == Status.EXECUTED;
	}

	public boolean wasCancelled() {
		return status == Status.CANCELLED;
	}

	public boolean failed() {
		return status == Status.ERROR;
	}
}