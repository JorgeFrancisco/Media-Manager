package br.com.jorgemelo.nimbusfilemanager.metadata.application.date;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

interface FileDateReader {

	BasicFileAttributes readAttributes(Path file) throws IOException;

	FileTime getLastModifiedTime(Path file) throws IOException;
}