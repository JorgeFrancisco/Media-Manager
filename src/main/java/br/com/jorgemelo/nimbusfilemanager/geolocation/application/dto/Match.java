package br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.AdminBoundaryKind;

public record Match(AdminBoundaryKind kind, String cityName, String countryCode, String countryName, String stateName) {
}