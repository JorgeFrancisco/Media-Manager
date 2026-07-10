package br.com.jorgemelo.nimbusfilemanager.media.domain.enums;

/**
 * Sort choices offered on the file explorer toolbar. Kept here (backend)
 * instead of hardcoded as literal &lt;option&gt; tags in files.html, so the
 * template only loops over values() and adding or renaming a sort order never
 * requires touching the page.
 */
public enum FileSortOption {

	NAME("name", "Nome (A-Z)"),

	NAME_DESC("name-desc", "Nome (Z-A)"),

	DATE_OLDEST("date-oldest", "Data (mais antigo)"),

	DATE_NEWEST("date-newest", "Data (mais recente)");

	private final String value;
	private final String label;

	FileSortOption(String value, String label) {
		this.value = value;
		this.label = label;
	}

	public String value() {
		return value;
	}

	public String label() {
		return label;
	}

	public static FileSortOption fromValue(String value) {
		for (FileSortOption option : values()) {
			if (option.value.equals(value)) {
				return option;
			}
		}

		return NAME;
	}
}