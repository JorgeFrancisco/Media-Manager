package br.com.jorgemelo.nimbusfilemanager.execution.application.dto;

import java.util.List;

/**
 * A user-facing execution message expressed as a stable bundle key plus its
 * typed arguments, captured at emission time and localized only on read. The
 * {@code code} is a {@code backend.execution.*} key; {@code args} are the raw
 * typed values (file path, counts, error tail) that fill the {@code {0}}, ...
 * placeholders. No localized text is ever built at emission - that keeps the
 * message language-agnostic until a request locale is known.
 */
public record ExecutionMessage(String code, List<Object> args) {
}