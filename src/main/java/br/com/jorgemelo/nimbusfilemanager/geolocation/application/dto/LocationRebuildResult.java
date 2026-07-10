package br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationRebuildScope;

public record LocationRebuildResult(LocationRebuildScope scope, long candidates, long resolved, long unresolved,
		long errors) {
}