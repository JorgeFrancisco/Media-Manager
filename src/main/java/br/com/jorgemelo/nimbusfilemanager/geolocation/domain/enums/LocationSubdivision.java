package br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums;

/**
 * Geographic subdivision applied under the chosen organization layout, right
 * after the date segments (e.g. Year/Month/Brasil/Paraná/Curitiba/CAMERA).
 */
public enum LocationSubdivision {

	NONE("Nenhuma"), COUNTRY("País"), COUNTRY_STATE("País / Estado"), COUNTRY_STATE_CITY("País / Estado / Cidade");

	private final String label;

	LocationSubdivision(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}