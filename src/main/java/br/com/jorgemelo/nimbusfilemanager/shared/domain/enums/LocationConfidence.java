package br.com.jorgemelo.nimbusfilemanager.shared.domain.enums;

/**
 * Reliability of a resolved location. Thresholds (in km) are centralized in
 * LocationConfidencePolicy - do not scatter distance ranges elsewhere.
 */
public enum LocationConfidence {

	VERY_HIGH("Muito alta", 5), HIGH("Alta", 4), MEDIUM("Média", 3), LOW("Baixa", 2), VERY_LOW("Muito baixa", 1);

	private final String displayName;
	private final int rank;

	LocationConfidence(String displayName, int rank) {
		this.displayName = displayName;
		this.rank = rank;
	}

	public String displayName() {
		return displayName;
	}

	public boolean atLeast(LocationConfidence minimum) {
		return minimum == null || rank >= minimum.rank;
	}
}