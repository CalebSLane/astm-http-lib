package org.itech.ahb.lib.common;

import java.util.List;

public interface ASTMInterpreterFactory {

	ASTMInterpreter createInterpreter(List<ASTMFrame> frames);

	ASTMInterpreter createInterpreter(ASTMMessage message);

}
