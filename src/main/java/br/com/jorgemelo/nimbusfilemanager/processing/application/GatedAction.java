package br.com.jorgemelo.nimbusfilemanager.processing.application;

@FunctionalInterface
public interface GatedAction<T> {

	T run() throws Exception;
}