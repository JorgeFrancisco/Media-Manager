package br.com.jorgemelo.nimbusfilemanager.statistics.infrastructure.web;

import java.util.List;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.jorgemelo.nimbusfilemanager.preferences.application.UserPagePreferenceService;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.ExecutionPhase;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.projection.ExecutionTelemetryRow;
import br.com.jorgemelo.nimbusfilemanager.shared.util.SecurityUtils;
import br.com.jorgemelo.nimbusfilemanager.statistics.application.StatisticsService;
import br.com.jorgemelo.nimbusfilemanager.telemetry.application.ExecutionTelemetryQueryService;
import br.com.jorgemelo.nimbusfilemanager.telemetry.application.dto.ExecutionComparison;

/**
 * Admin "Estatísticas" screen: library breakdown (by category, extension and
 * video codec, from {@link StatisticsService}) plus performance telemetry of
 * past executions (duration, files/s, config and per-phase timings).
 */
@Controller
public class StatisticsWebController {

	private static final int EXTENSIONS_LIMIT = 100;

	/**
	 * How many of the most recent executions the performance bar charts (files/s
	 * and duration) plot. The full list still shows in the telemetry table below;
	 * the charts stay short so they read as a trend instead of a wall of bars.
	 */
	private static final int PERF_CHART_LIMIT = 12;

	/**
	 * Preferences page/key so the version filter is remembered per user across
	 * visits.
	 */
	public static final String PAGE_KEY = "statistics";
	static final String VERSION_KEY = "version";
	private static final String REDIRECT_STATISTICS = "redirect:/app/statistics";

	private final StatisticsService statisticsService;
	private final ExecutionTelemetryQueryService executionTelemetryQueryService;
	private final UserPagePreferenceService userPagePreferenceService;

	public StatisticsWebController(StatisticsService statisticsService,
			ExecutionTelemetryQueryService executionTelemetryQueryService,
			UserPagePreferenceService userPagePreferenceService) {
		this.statisticsService = statisticsService;
		this.executionTelemetryQueryService = executionTelemetryQueryService;
		this.userPagePreferenceService = userPagePreferenceService;
	}

	@GetMapping("/app/statistics")
	public String statistics(@RequestParam(required = false) String version, Authentication authentication,
			Model model) {
		String selectedVersion = resolveVersion(version, authentication);

		List<ExecutionTelemetryRow> telemetry = executionTelemetryQueryService.recent(selectedVersion);

		model.addAttribute("summary", statisticsService.summary());
		model.addAttribute("extensions", statisticsService.extensions(EXTENSIONS_LIMIT));
		model.addAttribute("codecs", statisticsService.codecs());
		model.addAttribute("telemetry", telemetry);
		model.addAttribute("versions", executionTelemetryQueryService.versions());
		model.addAttribute("selectedVersion", selectedVersion);

		// Performance mini-charts: files/s and duration across the most recent
		// executions.
		List<ExecutionTelemetryRow> perfSeries = telemetry.stream().limit(PERF_CHART_LIMIT).toList();

		model.addAttribute("perfSeries", perfSeries);
		model.addAttribute("maxFilesPerSecond", perfSeries.stream().map(ExecutionTelemetryRow::filesPerSecond)
				.filter(Objects::nonNull).mapToDouble(Double::doubleValue).max().orElse(0.0));
		model.addAttribute("maxDurationMillis", perfSeries.stream().map(ExecutionTelemetryRow::durationMillis)
				.filter(Objects::nonNull).mapToLong(Long::longValue).max().orElse(0L));

		// "Tempo por fase" of the most recent execution, surfaced directly on this page
		// instead of only inside each execution's detail screen.
		ExecutionTelemetryRow latest = telemetry.isEmpty() ? null : telemetry.get(0);

		List<ExecutionPhase> latestPhases = latest == null ? List.of()
				: executionTelemetryQueryService.phases(latest.id());

		model.addAttribute("latestExecution", latest);
		model.addAttribute("latestPhases", latestPhases);
		model.addAttribute("maxLatestPhaseMillis",
				latestPhases.stream().mapToLong(ExecutionPhase::getDurationMillis).max().orElse(0L));

		return "app/statistics";
	}

	/**
	 * Resolves the version filter from the request or the user's saved preference,
	 * persisting it when the filter form is submitted (the form always sends the
	 * param; empty means "Todas"). A blank value is stored and returned as
	 * {@code null} so the query lists every version. Because it is kept per user,
	 * returning to the screen from the menu reopens the last-used filter.
	 */
	private String resolveVersion(String requested, Authentication authentication) {
		String username = SecurityUtils.usernameOr(authentication, "system");

		if (requested != null) {
			String normalized = requested.isBlank() ? "" : requested.trim();

			userPagePreferenceService.save(username, PAGE_KEY, VERSION_KEY, normalized);

			return normalized.isEmpty() ? null : normalized;
		}

		String saved = userPagePreferenceService.find(username, PAGE_KEY).get(VERSION_KEY);

		return saved == null || saved.isBlank() ? null : saved;
	}

	@GetMapping("/app/statistics/executions/{id}")
	public String executionTelemetry(@PathVariable Long id, Model model) {
		return executionTelemetryQueryService.byId(id).map(row -> {
			List<ExecutionPhase> phases = executionTelemetryQueryService.phases(id);

			model.addAttribute("execution", row);
			model.addAttribute("phases", phases);
			model.addAttribute("maxPhaseMillis",
					phases.stream().mapToLong(ExecutionPhase::getDurationMillis).max().orElse(0L));

			return "app/statistics-execution";
		}).orElse(REDIRECT_STATISTICS);
	}

	/**
	 * Side-by-side comparison of the executions selected in the telemetry table.
	 * Needs at least two; otherwise it just returns to the list.
	 */
	@GetMapping("/app/statistics/compare")
	public String compare(@RequestParam(required = false) List<Long> ids, Model model) {
		if (ids == null || ids.size() < 2) {
			return REDIRECT_STATISTICS;
		}

		ExecutionComparison comparison = executionTelemetryQueryService.compare(ids);

		if (comparison.executions().size() < 2) {
			return REDIRECT_STATISTICS;
		}

		model.addAttribute("comparison", comparison);

		return "app/statistics-compare";
	}
}