package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.UUID;

public record PairKey(UUID first, UUID second) {

	public static PairKey of(UUID first, UUID second) {
		return first.compareTo(second) <= 0 ? new PairKey(first, second) : new PairKey(second, first);
	}
}