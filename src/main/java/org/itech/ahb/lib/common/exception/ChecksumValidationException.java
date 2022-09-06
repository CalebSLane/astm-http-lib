package org.itech.ahb.lib.common.exception;

public class ChecksumValidationException extends Exception {

	private static final long serialVersionUID = -807749402799682905L;

	private String errorMesage;

	public ChecksumValidationException(String errorMessage) {
		this.errorMesage = errorMessage;
	}

	@Override
	public String getMessage() {
		return this.errorMesage;
	}

}
