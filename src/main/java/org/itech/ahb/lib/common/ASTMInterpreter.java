package org.itech.ahb.lib.common;

import java.util.List;

public interface ASTMInterpreter {

	public List<ASTMMessage> interpretFramesToASTMMessages(
			List<ASTMFrame> frames);

	List<ASTMFrame> interpretASTMMessageToFrames(ASTMMessage message);

}
