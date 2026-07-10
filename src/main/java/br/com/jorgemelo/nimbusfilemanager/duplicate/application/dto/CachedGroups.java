package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.List;


public record CachedGroups(String signature, List<SimilarPhotoGroupResponse> groups) {
}