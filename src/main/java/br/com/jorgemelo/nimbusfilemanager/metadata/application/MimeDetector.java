package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
interface MimeDetector {

	String detect(Path file) throws IOException;
}