package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import java.util.List;
import java.util.Map;

public record FolderDatabaseState(Map<String, CatalogedFile> registeredFiles, List<FileExplorerEntry> missingEntries) {
}