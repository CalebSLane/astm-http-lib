package org.itech.ahb.lib.common.exception;

public class ASTMCommunicationException extends Exception {

	private static final long serialVersionUID = -7478368614716068041L;

	public ASTMCommunicationException() {
		super();
	}

	public ASTMCommunicationException(String message) {
		super(message);
	}

	public ASTMCommunicationException(Throwable t) {
		super(t);
	}

	public ASTMCommunicationException(String message, Throwable t) {
		super(message, t);
	}
}
