package org.itech.ahb.lib.common.exception;

public class FrameParsingException extends Exception {

	private static final long serialVersionUID = -7478368614716068041L;

	public FrameParsingException() {
		super();
	}

	public FrameParsingException(String message) {
		super(message);
	}

	public FrameParsingException(Throwable t) {
		super(t);
	}

	public FrameParsingException(String message, Throwable t) {
		super(message, t);
	}
}
