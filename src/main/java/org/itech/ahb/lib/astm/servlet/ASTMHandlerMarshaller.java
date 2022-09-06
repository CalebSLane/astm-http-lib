package org.itech.ahb.lib.astm.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.itech.ahb.lib.common.ASTMMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ASTMHandlerMarshaller {

	private List<ASTMHandler> handlers;

	public ASTMHandlerMarshaller(List<ASTMHandler> handlers) {
		this.handlers = handlers;
	}

	public void handle(List<ASTMMessage> messages) {
		Map<ASTMMessage, ASTMHandler> messageHandlers = new HashMap<>();
		log.debug("pairing astm messages with handler...");
		for (ASTMMessage message : messages) {
			log.debug("finding a handler for astm message: " + message.hashCode());
			for (ASTMHandler handler : handlers) {
				if (handler.matches(message)) {
					log.debug("handler found for astm message: " + message.hashCode());
					messageHandlers.put(message, handler);
					break;
				}
			}
			if (!messageHandlers.containsKey(message)) {
				log.warn("astm message received but no handler was configured to handle the message");
			}
		}
		log.debug("finished pairing astm messages with handler");

		log.debug("handling astm messages...");
		for (Entry<ASTMMessage, ASTMHandler> messageHandler : messageHandlers.entrySet()) {
			try {
				messageHandler.getValue().handle(messageHandler.getKey());
			} catch (RuntimeException e) {
				log.error("unexpected error occurred during handling astm message: " + messageHandler.getKey(), e);
				// TODO add some handle exception handling. retry queue? db save?
			}
		}
		log.debug("finished handling astm messages");

	}

}
