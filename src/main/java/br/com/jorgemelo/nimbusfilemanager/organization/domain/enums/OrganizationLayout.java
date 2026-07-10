package br.com.jorgemelo.nimbusfilemanager.organization.domain.enums;

public enum OrganizationLayout {

	DEFAULT("Padrão", "Layout padrão do sistema.", ""),

	YEAR_MONTH_DAY_SUBCATEGORY_FILE_TYPE("Ano-mês / Dia / Categoria / Tipo (mais detalhado)",
			"Uma pasta por dia, com subpastas de categoria e tipo de arquivo dentro.", "2026-07/10/Fotos/IMAGE"),

	YEAR_MONTH_DAY("Ano-mês / Dia (mais simples)", "Agrupa só por data, sem separar por categoria ou tipo de arquivo.",
			"2026-07/10"),

	YEAR_MONTH_SUBCATEGORY_FILE_TYPE("Ano-mês / Categoria / Tipo (sem pasta por dia)",
			"Agrupa o mês inteiro numa pasta só, sem uma subpasta por dia.", "2026-07/Fotos/IMAGE"),

	SUBCATEGORY_YEAR_MONTH_DAY("Categoria / Ano-mês / Dia (categoria primeiro)",
			"Agrupa primeiro por categoria (Fotos, Vídeos, ...) e a data fica dentro dela.", "Fotos/2026-07/10"),

	// Kept last on purpose so it is the last option in the dropdown (which iterates
	// the enum order).
	FLAT("Sem separar por pastas",
			"Move todos os arquivos direto para a pasta de destino, sem criar subpastas por data, categoria ou tipo. "
					+ "Útil ao reorganizar uma pasta cujas datas não são confiáveis.",
			"direto na pasta de destino");

	private final String label;
	private final String description;
	private final String example;

	OrganizationLayout(String label, String description, String example) {
		this.label = label;
		this.description = description;
		this.example = example;
	}

	public String label() {
		return label;
	}

	public String description() {
		return description;
	}

	public String example() {
		return example;
	}
}