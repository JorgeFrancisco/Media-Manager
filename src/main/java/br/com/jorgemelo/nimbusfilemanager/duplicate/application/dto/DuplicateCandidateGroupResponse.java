package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.List;

import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;

public record DuplicateCandidateGroupResponse(String sha256, long files, SizeResponse wastedSize,
		DuplicateCandidateFileResponse keep, List<DuplicateCandidateFileResponse> deleteCandidates,
		List<DuplicateCandidateFileResponse> reviewCandidates) {

	public DuplicateCandidateGroupResponse(String sha256, long files, SizeResponse wastedSize,
			DuplicateCandidateFileResponse keep, List<DuplicateCandidateFileResponse> deleteCandidates) {
		this(sha256, files, wastedSize, keep, deleteCandidates, List.of());
	}
}