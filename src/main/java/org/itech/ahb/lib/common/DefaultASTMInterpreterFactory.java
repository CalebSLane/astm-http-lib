package org.itech.ahb.lib.common;

public class DefaultASTMInterpreterFactory implements ASTMInterpreterFactory {

	@Override
	public ASTMInterpreter createInterpreter() {
		return new DefaultASTMInterpreterImpl();
	}

}
