package org.itech.ahb.lib.http.servlet;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.itech.ahb.lib.astm.servlet.ASTMHandlerMarshaller;
import org.itech.ahb.lib.astm.servlet.ASTMReceiveThread;
import org.itech.ahb.lib.astm.servlet.LIS01A2Communicator;
import org.itech.ahb.lib.common.ASTMInterpreterFactory;
import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.HandleStatus;
import org.itech.ahb.lib.common.exception.ASTMCommunicationException;
import org.itech.ahb.lib.common.exception.FrameParsingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultForwardingHTTPToASTMHandler implements HTTPHandler {

	private final String defaultForwardingAddress;
	private final int defaultForwardingPort;
	private final ASTMInterpreterFactory interpreterFactory;
	private final ASTMHandlerMarshaller astmHandlerMarshaller; // this is necessary in case of line contention

	public DefaultForwardingHTTPToASTMHandler(String forwardingAddress, int forwardingPort,
			ASTMHandlerMarshaller astmHandlerMarshaller, ASTMInterpreterFactory interpreterFactory) {
		this.defaultForwardingAddress = forwardingAddress;
		this.defaultForwardingPort = forwardingPort;
		this.interpreterFactory = interpreterFactory;
		this.astmHandlerMarshaller = astmHandlerMarshaller;
	}

	@Override
	public HandleStatus handle(ASTMMessage message, Set<HTTPHandlerInfo> handlerInfos) throws FrameParsingException {
		Socket socket = null;
		boolean success = false;
		boolean closeSocket = true;
		LIS01A2Communicator communicator = null;
		String forwardingAddress = this.defaultForwardingAddress;
		int forwardingPort = this.defaultForwardingPort;
		for (HTTPHandlerInfo handlerInfo : handlerInfos ) {
			if (handlerInfo instanceof HttpForwardingHandlerInfo) {
				HttpForwardingHandlerInfo httpForwardingHandlerInfo = (HttpForwardingHandlerInfo) handlerInfo;
				forwardingAddress = StringUtils.isBlank(httpForwardingHandlerInfo.getForwardAddress())
						? forwardingAddress
						: httpForwardingHandlerInfo.getForwardAddress();
				forwardingPort = httpForwardingHandlerInfo.getForwardPort() <= 0 ? forwardingPort
								: httpForwardingHandlerInfo.getForwardPort();
			}
		}
		try {
			log.debug("connecting to forward to astm server at " + forwardingAddress + ":" + forwardingPort);
			socket = new Socket(forwardingAddress, forwardingPort);
			log.debug("connected to astm server at " + forwardingAddress + ":" + forwardingPort);

			log.debug("forwarding to astm server at " + forwardingAddress + ":" + forwardingPort);
			communicator = new LIS01A2Communicator(interpreterFactory, socket);
			log.debug("successfully created communicator " + communicator.getID() + " for astm server at "
					+ forwardingAddress + ":" + forwardingPort);
			success = communicator.sendProtocol(message);

			if (!success) {
				log.warn("line was contested by the remote server, defaulting to receive information from "
						+ forwardingAddress);
				// the communicator must remain open to receive the line contention. The thread
				// will close the socket
				closeSocket = false;
				ASTMReceiveThread receiveThread = new ASTMReceiveThread(communicator, astmHandlerMarshaller);
				receiveThread.start();

				if (message.getMessageLength() == 0) {
					log.info("since message request was empty, it is assumed this was a ping to trigger an action");
					success = true;
				}
			}
		} catch (IOException | ASTMCommunicationException e) {
			log.error("error occurred communicating with astm server at " + forwardingAddress + ":" + forwardingPort,
					e);
			return HandleStatus.FAIL;
		} finally {
			if (closeSocket) {
				if (communicator != null) {
					try {
						communicator.close();
						log.debug("successfully closed communicator with astm server at " + forwardingAddress + ":"
								+ forwardingPort);
					} catch (IOException e) {
						log.error("error occurred closing communicator with astm server at " + forwardingAddress + ":"
								+ forwardingPort, e);
					}
				}
				if (socket != null && !socket.isClosed()) {
					try {
						socket.close();
						log.debug("successfully closed socket with astm server at " + forwardingAddress + ":"
								+ forwardingPort);
					} catch (IOException e) {
						log.error("error occurred closing socket with astm server at " + forwardingAddress + ":"
								+ forwardingPort, e);
					}
				}
			}
		}
		return success ? HandleStatus.SUCCESS : HandleStatus.FAIL;

	}

	@Override
	public boolean matches(ASTMMessage message) {
		return true;
	}

}
