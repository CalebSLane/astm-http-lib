package org.itech.ahb.lib.common;

import lombok.Data;

@Data
public class ASTMFrame {
	
	public enum FrameType {
		INTERMEDIATE, END
	}

	private FrameType type;
	private int frameNumber = 0;
	private String text;

}
