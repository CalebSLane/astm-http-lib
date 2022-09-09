package org.itech.ahb.lib.common;

import java.util.List;

import org.itech.ahb.lib.common.exception.FrameParsingException;

public interface ASTMInterpreter {

	public List<ASTMMessage> interpretFramesToASTMMessages(
			List<ASTMFrame> frames) throws FrameParsingException;

	List<ASTMFrame> interpretASTMMessageToFrames(ASTMMessage message);

}
