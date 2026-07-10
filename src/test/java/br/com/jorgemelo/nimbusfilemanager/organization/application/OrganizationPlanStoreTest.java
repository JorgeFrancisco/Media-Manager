package br.com.jorgemelo.nimbusfilemanager.organization.application;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationPlan;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationSummary;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationLayout;

class OrganizationPlanStoreTest {

	private final OrganizationPlanStore store = new OrganizationPlanStore();

	@Test
	void shouldReturnNullWhenExecutionIdIsUnknown() {
		Assertions.assertThat(store.get(999L)).isNull();
	}

	@Test
	void shouldStoreAndRetrievePlanByExecutionId() {
		OrganizationPlan plan = plan();

		store.put(1L, plan);

		Assertions.assertThat(store.get(1L)).isSameAs(plan);
	}

	@Test
	void shouldEvictOldestEntryWhenMoreThanFiveAreStored() {
		for (long id = 1; id <= 5; id++) {
			store.put(id, plan());
		}

		store.put(6L, plan());

		Assertions.assertThat(store.get(1L)).isNull();
		Assertions.assertThat(store.get(6L)).isNotNull();

		for (long id = 2; id <= 5; id++) {
			Assertions.assertThat(store.get(id)).isNotNull();
		}
	}

	private OrganizationPlan plan() {
		return new OrganizationPlan("C:/input", "C:/target", OrganizationLayout.DEFAULT, false,
				new OrganizationSummary(0, 0, 0, 0, 0, 0, 0, 0, 0), List.of());
	}
}