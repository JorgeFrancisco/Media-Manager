package br.com.jorgemelo.nimbusfilemanager.metadata.application.filename;

import java.time.LocalDateTime;

public interface FileNameDateRule {

	boolean supports(String fileName);

	LocalDateTime resolve(String fileName);

	String name();
}