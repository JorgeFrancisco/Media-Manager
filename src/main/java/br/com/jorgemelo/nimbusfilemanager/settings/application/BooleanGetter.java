package br.com.jorgemelo.nimbusfilemanager.settings.application;

/**
 * Reads a {@code boolean} property from an object of type {@code T}.
 * Package-private functional seam used by {@link AppSettingService} to derive
 * boolean defaults from the typed configuration records.
 */
@FunctionalInterface
interface BooleanGetter<T> {

	boolean get(T object);
}