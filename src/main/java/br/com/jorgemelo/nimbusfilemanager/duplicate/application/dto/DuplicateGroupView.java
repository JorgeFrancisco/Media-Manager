package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.List;

/**
 * View-model for one group card on the Duplicados screen - see
 * {@link DuplicateFileView}. {@code files} always has the "keep" file first,
 * followed by every deletion candidate.
 */
public record DuplicateGroupView(String groupId, String headerText, String badgeText, List<DuplicateFileView> files) {
}