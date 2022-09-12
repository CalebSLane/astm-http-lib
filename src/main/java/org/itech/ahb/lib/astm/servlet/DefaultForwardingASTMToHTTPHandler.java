package org.itech.ahb.lib.astm.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Base64;

import org.itech.ahb.lib.common.ASTMMessage;
import org.itech.ahb.lib.common.HandleStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultForwardingASTMToHTTPHandler implements ASTMHandler {

	private final URI forwardingUri;
	private final String username;
	private final char[] password;

	public DefaultForwardingASTMToHTTPHandler(URI forwardingUri) {
		this.forwardingUri = forwardingUri;
		this.username = null;
		this.password = new char[0];
	}

	public DefaultForwardingASTMToHTTPHandler(URI forwardingUri, String username, char[] password) {
		this.forwardingUri = forwardingUri;
		this.username = username;
		this.password = password;
	}

	@Override
	public HandleStatus handle(ASTMMessage message) {
		HttpClient client = HttpClient.newHttpClient();
		log.debug("creating request to forward to http server at " + forwardingUri.toString());
		Builder requestBuilder = HttpRequest.newBuilder()//
				.uri(forwardingUri)//
				.POST(HttpRequest.BodyPublishers.ofString(message.toString()))//
		;
		if (!(username == null || username.equals(""))) {
			log.debug("using username '" + username + "' to forward to http server at " + forwardingUri.toString());
			requestBuilder.header("Authorization",
					"Basic " + Base64.getEncoder().encodeToString((username + ":" + new String(password)).getBytes()));
		}
		HttpRequest request = requestBuilder.build();

		try {
			log.debug("forwarding request to http server at " + forwardingUri.toString());
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				return HandleStatus.SUCCESS;
			}
		} catch (IOException | InterruptedException e) {
			log.error("error occurred communicating with http server at " + forwardingUri.toString(), e);
		}
		return HandleStatus.FAIL;
	}

	@Override
	public boolean matches(ASTMMessage message) {
		return true;
	}

}
