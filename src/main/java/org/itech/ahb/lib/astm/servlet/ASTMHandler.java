package org.itech.ahb.lib.astm.servlet;

import org.itech.ahb.lib.common.ASTMMessage;

public interface ASTMHandler {

	public enum HandleStatus {
		SUCCESS, FAIL, UNHANDLED
	}

	HandleStatus handle(ASTMMessage message);
	
	boolean matches(ASTMMessage message);

}
