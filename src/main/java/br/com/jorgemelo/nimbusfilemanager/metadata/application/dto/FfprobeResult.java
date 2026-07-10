package br.com.jorgemelo.nimbusfilemanager.metadata.application.dto;

public record FfprobeResult(boolean finished, int exitCode, String output, String errorOutput) {

	public FfprobeResult(boolean finished, int exitCode, String output) {
		this(finished, exitCode, output, "");
	}
}