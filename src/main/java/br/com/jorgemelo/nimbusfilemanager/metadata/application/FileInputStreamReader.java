package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@FunctionalInterface
interface FileInputStreamReader {

	InputStream open(Path file) throws IOException;
}