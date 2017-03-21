package org.eclipse.php.builtin.server.core.internal.xml;

public class PathMapping extends XMLElement {

	public String getModule() {
		return getAttributeValue("module");
	}

	public String getLocalPath() {
		return getAttributeValue("local");
	}

	public String getRemotePath() {
		return getAttributeValue("remote");
	}

	public void setModule(String module) {
		setAttributeValue("module", module);
	}

	public void setLocalPath(String path) {
		setAttributeValue("local", path);
	}

	public void setRemotePath(String path) {
		setAttributeValue("remote", path);
	}

}
