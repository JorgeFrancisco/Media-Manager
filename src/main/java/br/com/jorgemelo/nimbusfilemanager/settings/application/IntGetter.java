package br.com.jorgemelo.nimbusfilemanager.settings.application;

/**
 * Reads an {@code int} property from an object of type {@code T}.
 * Package-private functional seam used by {@link AppSettingService} to derive
 * integer defaults from the typed configuration records.
 */
@FunctionalInterface
interface IntGetter<T> {

	int get(T object);
}