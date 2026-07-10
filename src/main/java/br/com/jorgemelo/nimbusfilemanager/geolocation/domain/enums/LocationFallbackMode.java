package br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums;

/**
 * What organization does when a media has no resolved location or its
 * confidence is below the configured minimum.
 */
public enum LocationFallbackMode {

	IGNORE("Ignorar localização"), FALLBACK_FOLDER("Usar pasta SEM_LOCALIZACAO_CONFIAVEL");

	private final String label;

	LocationFallbackMode(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}