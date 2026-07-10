package br.com.jorgemelo.nimbusfilemanager.execution.application;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.execution.application.dto.ExecutionResponse;
import br.com.jorgemelo.nimbusfilemanager.execution.application.dto.ExecutionStepResponse;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionStatus;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionTrigger;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionType;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.Execution;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.ExecutionStep;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.StatusMessage;
import br.com.jorgemelo.nimbusfilemanager.shared.i18n.LocalizedComponent;
import br.com.jorgemelo.nimbusfilemanager.shared.util.UuidV7;

/**
 * Builds the API responses for executions and their steps, resolving the stable
 * message code + typed args into localized text in the current request locale.
 * This is the single read-side localization point: emission stores only a code
 * and raw args (see {@link ExecutionMessages}); here they become the text the
 * user sees. Legacy rows without a code fall back to the persisted free-text
 * {@code message} verbatim - no regex or text matching.
 */
@Component
public class ExecutionMapper extends LocalizedComponent {

	private final ExecutionMessageCodec codec;

	public ExecutionMapper(ExecutionMessageCodec codec) {
		this.codec = codec;
	}

	public ExecutionResponse toResponse(Execution execution) {
		if (execution == null) {
			return null;
		}

		ExecutionStatus status = execution.getStatus();

		return new ExecutionResponse(UuidV7.orLegacy(execution.getPublicId(), execution.getId()),
				execution.getExecutionType().name(), status.name(), execution.getStartedAt(),
				execution.getFinishedAt(), execution.getSourcePath(), execution.getTargetPath(),
				execution.getFilesFound(), execution.getFilesAnalyzed(), execution.getCacheHits(),
				execution.getFilesMoved(), execution.getSimulatedFiles(), execution.getErrors(),
				execution.getTotalExpected(), percentComplete(execution),
				resolve(execution.getStatusMessage()),
				execution.getExecuteFlag(), statusLabel(status), status.isTerminal(),
				typeLabel(execution.getExecutionType()), triggerLabel(execution.getTriggerEvent()));
	}

	ExecutionStepResponse toStepResponse(ExecutionStep step) {
		return new ExecutionStepResponse(UuidV7.orLegacy(step.getPublicId(), step.getId()),
				UuidV7.orLegacy(step.getExecution().getPublicId(), step.getExecution().getId()),
				step.getStepType().name(), step.getPath(),
				resolve(step.getStatusMessage()), step.getFilesFound(),
				step.getFilesAnalyzed(), step.getCacheHits(), step.getErrors(), step.getCreatedAt());
	}

	/**
	 * Resolves a stored message to localized text. When a stable code is present
	 * it is looked up in the request locale with its typed args; otherwise the
	 * legacy free-text message is returned verbatim (older rows predate the code).
	 */
	private String resolve(StatusMessage statusMessage) {
		if (statusMessage == null || statusMessage.getCode() == null) {
			return statusMessage == null ? null : statusMessage.getText();
		}

		return message(statusMessage.getCode(), codec.decode(statusMessage.getArgs()));
	}

	/**
	 * Resolves the localized label for a status. Uses an exhaustive switch with the
	 * full bundle keys (rather than a computed prefix + enum name) so every key is a
	 * literal the i18n key-parity test can see and verify - the same idiom used for
	 * other enum-to-message mappings in the app.
	 */
	private String statusLabel(ExecutionStatus status) {
		return switch (status) {
		case STARTED -> message("backend.execution.status.started");
		case SCANNING_FILES -> message("backend.execution.status.scanning");
		case PROCESSING_FILES -> message("backend.execution.status.processing");
		case FINISHED -> message("backend.execution.status.finished");
		case FINISHED_WITH_ERRORS -> message("backend.execution.status.finishedWithErrors");
		case INTERRUPTED -> message("backend.execution.status.interrupted");
		case ERROR -> message("backend.execution.status.error");
		case CANCELLED -> message("backend.execution.status.cancelled");
		case REJECTED -> message("backend.execution.status.rejected");
		};
	}

	/**
	 * Localized label for the execution type. Exhaustive switch over literal keys
	 * (not a computed prefix + enum name) so every key is visible to the i18n
	 * key-parity test, mirroring {@link #statusLabel(ExecutionStatus)}.
	 */
	private String typeLabel(ExecutionType type) {
		return switch (type) {
		case INVENTORY -> message("backend.execution.type.INVENTORY");
		case ORGANIZATION -> message("backend.execution.type.ORGANIZATION");
		case UNDO -> message("backend.execution.type.UNDO");
		case EXPORT -> message("backend.execution.type.EXPORT");
		case SUMMARY -> message("backend.execution.type.SUMMARY");
		case DEDUP_DELETE -> message("backend.execution.type.DEDUP_DELETE");
		case RECONCILE -> message("backend.execution.type.RECONCILE");
		};
	}

	/**
	 * Localized label for what triggered the execution. Null for legacy rows that
	 * predate the trigger column, so the read side simply shows nothing.
	 */
	private String triggerLabel(ExecutionTrigger trigger) {
		if (trigger == null) {
			return null;
		}

		return switch (trigger) {
		case MANUAL -> message("backend.execution.trigger.MANUAL");
		case FILE_EVENT -> message("backend.execution.trigger.FILE_EVENT");
		case TIMER -> message("backend.execution.trigger.TIMER");
		};
	}

	private Double percentComplete(Execution execution) {
		Integer total = execution.getTotalExpected();
		Integer processed = execution.getFilesFound();

		if (total == null || total <= 0 || processed == null) {
			return null;
		}

		double percent = (processed * 100.0) / total;

		return Math.round(Math.min(percent, 100.0) * 10.0) / 10.0;
	}
}