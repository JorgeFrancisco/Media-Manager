package br.com.jorgemelo.nimbusfilemanager.shared.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.util.SizeFormatter;

public record SizeResponse(long bytes, String formatted) {

	public static SizeResponse of(long bytes) {
		return new SizeResponse(bytes, SizeFormatter.format(bytes));
	}
}