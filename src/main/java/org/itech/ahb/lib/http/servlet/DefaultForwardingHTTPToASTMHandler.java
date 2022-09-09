package org.itech.ahb.lib.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.itech.ahb.lib.astm.servlet.LIS01A2Communicator;
import org.itech.ahb.lib.common.ASTMInterpreterFactory;
import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.exception.ASTMCommunicationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultForwardingHTTPToASTMHandler implements HTTPHandler {

	private final String forwardingAddress;
	private final int forwardingPort;
	private final ASTMInterpreterFactory interpreterFactory;

	public DefaultForwardingHTTPToASTMHandler(String forwardingAddress, int forwardingPort,
			ASTMInterpreterFactory interpreterFactory) {
		this.forwardingAddress = forwardingAddress;
		this.forwardingPort = forwardingPort;
		this.interpreterFactory = interpreterFactory;
	}

	@Override
	public HandleStatus handle(ASTMMessage message) {
		LIS01A2Communicator communicator = new LIS01A2Communicator(interpreterFactory.createInterpreter());
		Socket socket = null;
		boolean success = false;
		try {
			log.debug("connecting to forward to astm server at " + forwardingAddress + ":" + forwardingPort);
			socket = new Socket(forwardingAddress, forwardingPort);
			log.debug("connected to astm server at " + forwardingAddress + ":" + forwardingPort);

			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

			OutputStream outputStream = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(outputStream, true);

			log.debug("forwarding to astm server at " + forwardingAddress + ":" + forwardingPort);
			communicator.sendProtocol(message, reader, writer);
		} catch (IOException | ASTMCommunicationException e) {
			log.error("error occurred communicating with astm server at " + forwardingAddress + ":" + forwardingPort,
					e);
			return HandleStatus.FAIL;
		} finally {
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
		return success ? HandleStatus.SUCCESS : HandleStatus.FAIL;

	}

	@Override
	public boolean matches(ASTMMessage message) {
		return true;
	}

}
