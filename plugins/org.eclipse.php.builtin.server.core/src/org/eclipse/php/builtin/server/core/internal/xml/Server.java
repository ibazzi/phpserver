package org.eclipse.php.builtin.server.core.internal.xml;

public class Server extends XMLElement {

	public Port getPort(int index) {
		return (Port) findElement("Port", index);
	}

	public int getPortCount() {
		return sizeOfElement("Port");
	}

}
