package org.itech.ahb.lib.common;

public class DefaultASTMMessage 
//extends	Message<MessageHeaderRecord, PatientInformationUniCellDxHRecord, TestOrderUniCellDxHRecord, ResultUniCellDxHrecord, RequestInformationUniCellDxHRecord, ScientificRecord, MessageTerminatorRecord, CommentRecord, IWithCommentsUniCellDxH>
		implements ASTMMessage {

	private String message;
	
	public DefaultASTMMessage(String message) {
		this.message = message;
	}

	@Override
	public int getMessageLength() {
		if (message == null) {
			return 0;
		} else {
			return message.length();
		}
	}
	
	@Override
	public String toString() {
		if (message == null) {
			return "";
		}
		return message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


}
