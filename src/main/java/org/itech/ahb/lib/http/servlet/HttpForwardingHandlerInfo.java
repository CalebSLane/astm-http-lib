package org.itech.ahb.lib.http.servlet;

public class HttpForwardingHandlerInfo implements HTTPHandlerInfo {

	private String forwardAddress;
	private int forwardPort;

	public String getForwardAddress() {
		return forwardAddress;
	}

	public void setForwardAddress(String forwardAddress) {
		this.forwardAddress = forwardAddress;
	}

	public int getForwardPort() {
		return forwardPort;
	}

	public void setForwardPort(int forwardPort) {
		this.forwardPort = forwardPort;
	}

	@Override
	public boolean supports(HTTPHandler value) {
		return value instanceof DefaultForwardingHTTPToASTMHandler;
	}

}
