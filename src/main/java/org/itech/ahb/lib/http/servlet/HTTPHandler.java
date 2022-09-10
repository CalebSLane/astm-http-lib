package org.itech.ahb.lib.http.servlet;

import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.exception.FrameParsingException;

public interface HTTPHandler {
	
	public enum HandleStatus {
		SUCCESS, FAIL, UNHANDLED
	}

	HandleStatus handle(ASTMMessage message) throws FrameParsingException;
	
	boolean matches(ASTMMessage message);


}
