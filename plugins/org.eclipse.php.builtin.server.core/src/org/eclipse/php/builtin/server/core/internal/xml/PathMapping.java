package org.eclipse.php.builtin.server.core.internal.xml;

public class PathMapping extends XMLElement {

	public String getLocalPath() {
		return getAttributeValue("local");
	}

	public String getRemotePath() {
		return getAttributeValue("remote");
	}

	public void setLocalPath(String path) {
		setAttributeValue("local", path);
	}

	public void setRemotePath(String path) {
		setAttributeValue("remote", path);
	}

}
