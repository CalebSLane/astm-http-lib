package org.itech.ahb.lib.http.servlet;

import java.util.Set;

import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.HandleStatus;
import org.itech.ahb.lib.common.exception.FrameParsingException;

public interface HTTPHandler {
	
	boolean matches(ASTMMessage message);

	HandleStatus handle(ASTMMessage message, Set<HTTPHandlerInfo> handlerInfo) throws FrameParsingException;

}
