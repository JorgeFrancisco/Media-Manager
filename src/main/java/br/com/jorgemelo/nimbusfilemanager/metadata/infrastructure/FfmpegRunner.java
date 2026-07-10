package br.com.jorgemelo.nimbusfilemanager.metadata.infrastructure;

import java.nio.file.Path;

@FunctionalInterface
public interface FfmpegRunner {

	byte[] run(String ffmpegPath, Path file) throws Exception;
}