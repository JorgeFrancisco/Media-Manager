package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.io.IOException;
import java.nio.file.Path;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

@FunctionalInterface
interface MetadataReader {

	Metadata read(Path file) throws ImageProcessingException, IOException;
}