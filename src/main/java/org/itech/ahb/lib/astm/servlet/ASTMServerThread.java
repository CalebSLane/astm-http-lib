package org.itech.ahb.lib.astm.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.itech.ahb.lib.astm.servlet.ASTMHandler.HandleStatus;
import org.itech.ahb.lib.common.ASTMInterpreter;
import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.exception.ASTMCommunicationException;
import org.itech.ahb.lib.common.exception.FrameParsingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ASTMServerThread extends Thread {

	private Socket socket;
	private ASTMInterpreter interpreter;
	private ASTMHandlerMarshaller astmHandlerMarshaller;

	public ASTMServerThread(Socket socket, ASTMInterpreter interpreter, List<ASTMHandler> handlers) {
		this.socket = socket;
		this.interpreter = interpreter;
		this.astmHandlerMarshaller = new ASTMHandlerMarshaller(handlers);
	}

	@Override
	public void run() {
		try {
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

			OutputStream outputStream = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(outputStream, true);

			try {
				List<ASTMMessage> messages = new LIS01A2Communicator(interpreter).receiveProtocol(reader, writer);
				for (ASTMMessage message : messages) {
					HandleStatus status = astmHandlerMarshaller.handle(message);
					log.debug("astm HandleStatus is: " + status);
					if (status != HandleStatus.SUCCESS) {
						log.error("message was not handled successfully");
					}
				}
			} catch (IllegalStateException | FrameParsingException | ASTMCommunicationException e) {
				log.error("an error occurred understanding or handling what was received from the astm client", e);
			}

		} catch (IOException e) {
			log.error("error occurred communicating with astm client on socket " + socket.getLocalPort(), e);
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
					log.debug("successfully closed socket with astm client on socket " + socket.getLocalPort());
				} catch (IOException e) {
					log.error("error occurred closing socket with astm client on socket " + socket.getLocalPort(), e);
				}
			}
		}

	}

}
