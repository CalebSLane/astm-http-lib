package org.itech.ahb.lib.http.servlet;

import org.itech.ahb.lib.common.ASTMMessage;

public interface HTTPHandler {
	
	public enum HandleStatus {
		SUCCESS, FAIL, UNHANDLED
	}

	HandleStatus handle(ASTMMessage message);
	
	boolean matches(ASTMMessage message);


}
