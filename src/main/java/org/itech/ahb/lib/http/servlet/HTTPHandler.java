package org.itech.ahb.lib.http.servlet;

import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.HandleStatus;
import org.itech.ahb.lib.common.exception.FrameParsingException;

public interface HTTPHandler {
	
	HandleStatus handle(ASTMMessage message) throws FrameParsingException;
	
	boolean matches(ASTMMessage message);


}
