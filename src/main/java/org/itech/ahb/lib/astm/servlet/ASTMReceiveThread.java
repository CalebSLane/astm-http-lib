package org.itech.ahb.lib.astm.servlet;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import org.itech.ahb.lib.common.ASTMInterpreterFactory;
import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.HandleStatus;
import org.itech.ahb.lib.common.exception.ASTMCommunicationException;
import org.itech.ahb.lib.common.exception.FrameParsingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ASTMReceiveThread extends Thread {

	private Socket socket;
	private ASTMInterpreterFactory astmInterpreterFactory;
	private LIS01A2Communicator communicator;
	private ASTMHandlerMarshaller astmHandlerMarshaller;

	public ASTMReceiveThread(Socket socket, ASTMInterpreterFactory astmInterpreterFactory,
			ASTMHandlerMarshaller astmHandlerMarshaller) {
		this.socket = socket;
		this.astmInterpreterFactory = astmInterpreterFactory;
		this.astmHandlerMarshaller = astmHandlerMarshaller;
	}

	public ASTMReceiveThread(LIS01A2Communicator communicator, ASTMHandlerMarshaller astmHandlerMarshaller) {
		this.communicator = communicator;
		this.astmHandlerMarshaller = astmHandlerMarshaller;
	}

	@Override
	public void run() {
		try {
			if (communicator == null) {
				int port = socket.getLocalPort();
				communicator = new LIS01A2Communicator(astmInterpreterFactory, socket);
				log.debug(
						"successfully created communicator " + communicator.getID() + " for receiving on port " + port);

			}
			try {
				List<ASTMMessage> messages = communicator.receiveProtocol();
				for (ASTMMessage message : messages) {
					HandleStatus status = astmHandlerMarshaller.handle(message);
					log.debug("astm HandleStatus is: " + status);
					if (status != HandleStatus.SUCCESS) {
						log.error("message was not handled successfully");
					}
				}
			} catch (IllegalStateException | FrameParsingException | ASTMCommunicationException e) {
				log.error("an error occurred understanding or handling what was received from the astm sender", e);
			}

		} catch (IOException e) {
			log.error("error occurred communicating with astm sender", e);
		} finally {
			if (communicator != null) {
				try {
					communicator.close();
					log.debug("successfully closed communicator " + communicator.getID() + " with astm sender");
				} catch (IOException e) {
					log.error("error occurred closing communicator " + communicator.getID() + " with astm sender",
							e);
				}
			}
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
					log.debug("successfully closed socket with astm sender");
				} catch (IOException e) {
					log.error("error occurred closing socket with astm sender", e);
				}
			}
		}

	}

}
