package br.com.jorgemelo.nimbusfilemanager.processing.application;

/**
 * A worker turning one input into a result on a pool thread. Must not touch
 * JPA.
 */
@FunctionalInterface
public interface Worker<I, O> {

	O apply(I item) throws Exception;
}