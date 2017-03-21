package org.eclipse.php.builtin.server.core.internal;

/**
 * Holder for various arguments passed to a VM runner. Mandatory parameters are
 * passed in the constructor; optional arguments, via setters.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * 
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class PHPServerRunnerConfiguration {
	private String fHost;
	private String fExeFilePath;
	private int fPort;
	private String fIniFilePath;
	private String fWorkingDirectory;

	public PHPServerRunnerConfiguration(String exeFilePath, int port) {
		fExeFilePath = exeFilePath;
		fPort = port;
	}

	public String getExeFilePath() {
		return fExeFilePath;
	}

	public void setExeFilePath(String exeFilePath) {
		this.fExeFilePath = exeFilePath;
	}

	public int getPort() {
		return fPort;
	}

	public void setPort(int port) {
		this.fPort = port;
	}

	public String getIniFilePath() {
		return fIniFilePath;
	}

	public void setIniFilePath(String iniFilePath) {
		this.fIniFilePath = iniFilePath;
	}

	/**
	 * Sets the working directory for a launched VM.
	 * 
	 * @param path
	 *            the absolute path to the working directory to be used by a
	 *            launched VM, or <code>null</code> if the default working
	 *            directory is to be inherited from the current process
	 * @since 2.0
	 */
	public void setWorkingDirectory(String path) {
		fWorkingDirectory = path;
	}

	/**
	 * Returns the working directory of a launched VM.
	 * 
	 * @return the absolute path to the working directory of a launched VM, or
	 *         <code>null</code> if the working directory is inherited from the
	 *         current process
	 * @since 2.0
	 */
	public String getWorkingDirectory() {
		return fWorkingDirectory;
	}

	public String getHost() {
		return fHost;
	}

	public void setHost(String host) {
		this.fHost = host;
	}

}
