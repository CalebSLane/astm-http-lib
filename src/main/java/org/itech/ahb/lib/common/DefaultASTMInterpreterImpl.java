package org.itech.ahb.lib.common;

import java.util.ArrayList;
import java.util.List;

import org.itech.ahb.lib.astm.servlet.LIS01A2Communicator;
import org.itech.ahb.lib.common.ASTMFrame.FrameType;
import org.itech.ahb.lib.common.exception.FrameParsingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultASTMInterpreterImpl implements ASTMInterpreter {

	@Override
	public List<ASTMMessage> interpretFramesToASTMMessages(List<ASTMFrame> frames) throws FrameParsingException {
		log.debug("interpreting frames as astm messages...");
		List<ASTMMessage> messages = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (ASTMFrame frame : frames) {
			if (frame.getType() == FrameType.INTERMEDIATE) {
				sb.append(frame.getText());
			} else if (frame.getType() == FrameType.END) {
				sb.append(frame.getText());
				messages.add(new DefaultASTMMessage(sb.toString()));
				sb = new StringBuilder();
			} else {
				throw new FrameParsingException(
						"frame type is an unrecognized type so message cannot be reconstructed");
			}
		}
		log.debug("finished interpreting frames as astm messages");
		return messages;
	}

	@Override
	public List<ASTMFrame> interpretASTMMessageToFrames(ASTMMessage message) {
		log.debug("interpreting astm messages as frames...");
		List<ASTMFrame> frames = new ArrayList<>();
		String[] frameTexts = message.toString().split("(?<=\\G.{" + LIS01A2Communicator.MAX_TEXT_SIZE + "})");
		for (int i = 0; i < frameTexts.length; i++) {
			ASTMFrame curFrame = new ASTMFrame();
			curFrame.setText(frameTexts[i]);
			curFrame.setFrameNumber((i + 1) % 8);
			curFrame.setType(i != (frameTexts.length - 1) ? FrameType.INTERMEDIATE : FrameType.END);
			frames.add(curFrame);
		}
		log.debug("finished interpreting astm message as frames");
		return frames;
	}

}
