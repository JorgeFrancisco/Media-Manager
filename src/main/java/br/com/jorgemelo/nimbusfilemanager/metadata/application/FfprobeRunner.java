package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.nio.file.Path;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.FfprobeResult;

@FunctionalInterface
interface FfprobeRunner {

	FfprobeResult run(String ffprobePath, Path file) throws Exception;
}