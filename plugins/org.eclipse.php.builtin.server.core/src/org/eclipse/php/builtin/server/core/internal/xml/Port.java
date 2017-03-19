package org.eclipse.php.builtin.server.core.internal.xml;

public class Port extends XMLElement {

	public String getName() {
		return getAttributeValue("name");
	}

	public String getProtocol() {
		return getAttributeValue("protocol");
	}

	public int getPort() {
		int port = -1;
		try {
			port = Integer.parseInt(getElementValue());
		} catch (Exception e) {
			// ignore
		}
		return port;
	}

	public void setPort(int port) {
		setElementValue(getElementNode(), String.valueOf(port));
	}

}
