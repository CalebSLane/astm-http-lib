package org.itech.ahb.lib.common.exception;

public class DecodeException extends Exception {

	private static final long serialVersionUID = -8418792525686162611L;

	private String errorMesage;

	public DecodeException(String errorMessage) {
		this.errorMesage = errorMessage;
	}

	@Override
	public String getMessage() {
		return this.errorMesage;
	}

}
