package br.com.jorgemelo.nimbusfilemanager.organization.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationPlan;

/**
 * Keeps the last few computed OrganizationPlan results in memory, keyed by
 * executionId, so the async preview flow can hand the (potentially large, up to
 * hundreds of thousands of items) plan back to the web layer once the
 * background scan finishes, without persisting it to the database. Bounded to a
 * small number of entries since this is a single-user desktop application.
 */
@Service
public class OrganizationPlanStore {

	private static final int MAX_ENTRIES = 5;

	private final Map<Long, OrganizationPlan> plans = Collections
			.synchronizedMap(new LinkedHashMap<>(MAX_ENTRIES + 1, 0.75f, true) {

				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<Long, OrganizationPlan> eldest) {
					return size() > MAX_ENTRIES;
				}
			});

	public void put(Long executionId, OrganizationPlan plan) {
		plans.put(executionId, plan);
	}

	public OrganizationPlan get(Long executionId) {
		return plans.get(executionId);
	}
}