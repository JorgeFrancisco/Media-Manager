package br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums;

/**
 * Normalized administrative level, independent of the source's own numbering
 * (OSM admin_level, geoBoundaries ADMx, etc.). The importer maps every source
 * level to one of these three canonical kinds, so the resolver stays generic.
 */
public enum AdminBoundaryKind {

	COUNTRY, STATE, MUNICIPALITY;
}