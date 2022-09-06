package org.itech.ahb.lib.astm.servlet;

import org.itech.ahb.lib.common.ASTMMessage;

public interface ASTMHandler {

	void handle(ASTMMessage message);
	
	boolean matches(ASTMMessage message);

}
