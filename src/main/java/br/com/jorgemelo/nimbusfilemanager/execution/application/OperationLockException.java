package br.com.jorgemelo.nimbusfilemanager.execution.application;

public class OperationLockException extends RuntimeException {

	private static final long serialVersionUID = 2535403180609423052L;

	public OperationLockException(String message) {
		super(message);
	}
}