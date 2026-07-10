package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import java.util.List;

public record DiskListing(List<FileExplorerEntry> entries, boolean accessDenied) {
}