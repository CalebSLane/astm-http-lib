package org.itech.ahb.lib.astm.servlet;

import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.HandleStatus;

public interface ASTMHandler {

	HandleStatus handle(ASTMMessage message);
	
	boolean matches(ASTMMessage message);

}
