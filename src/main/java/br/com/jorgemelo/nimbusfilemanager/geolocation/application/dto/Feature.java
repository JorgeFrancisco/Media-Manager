package br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto;

import org.locationtech.jts.geom.Geometry;

public record Feature(String name, String alpha3, Geometry geometry) {
}