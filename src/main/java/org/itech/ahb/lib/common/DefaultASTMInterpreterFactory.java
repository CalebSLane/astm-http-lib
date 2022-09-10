package org.itech.ahb.lib.common;

import java.util.List;

public class DefaultASTMInterpreterFactory implements ASTMInterpreterFactory {

	@Override
	public ASTMInterpreter createInterpreter(List<ASTMFrame> frames) {
		return new DefaultASTMInterpreterImpl();
	}

	@Override
	public ASTMInterpreter createInterpreter(ASTMMessage message) {
		return new DefaultASTMInterpreterImpl();
	}

}
