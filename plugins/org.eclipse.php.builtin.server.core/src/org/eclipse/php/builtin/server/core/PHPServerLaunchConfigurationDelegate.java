package org.eclipse.php.builtin.server.core;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Trace;

public class PHPServerLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		PHPServer sd = server.getAdapter(PHPServer.class);
		sd.getServerPorts();
		if (server == null) {
			Trace.trace(Trace.STRING_FINEST, "Launch configuration could not find server");
			// throw CoreException();
			return;
		}

		String phpExeString = configuration.getAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, (String) null);
		String phpIniPath = configuration.getAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, (String) null);

		PHPServerBehaviour phpServer = (PHPServerBehaviour) server.loadAdapter(PHPServerBehaviour.class, null);
		phpServer.setupLaunch(launch, mode, monitor);

		// if (server.shouldPublish() && ServerCore.isAutoPublishing())
		// server.publish(IServer.PUBLISH_INCREMENTAL, monitor);

		// resolve location
		IPath phpExe = new Path(phpExeString);

		File phpExeFile = new File(phpExeString);

		// Determine PHP configuration file location:
		String workingDir = phpExeFile.getParent();
		String[] cmdLine = new String[] { phpExe.toOSString(), "-S", "0.0.0.0:10000" };
		Process p = DebugPlugin.exec(cmdLine, new File(workingDir), null);
		if (p == null) {
			return;
		}

		// check for cancellation
		if (monitor.isCanceled()) {
			p.destroy();
			return;
		}

		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
				.format(new Date(System.currentTimeMillis()));
		IProcess process = newProcess(launch, p, renderProcessLabel(cmdLine, timestamp), getDefaultProcessMap());
		process.setAttribute(DebugPlugin.ATTR_PATH, cmdLine[0]);
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
		String ltime = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		process.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, ltime != null ? ltime : timestamp);
		if (workingDir != null) {
			process.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, workingDir);
		}
		// if (envp != null) {
		// Arrays.sort(envp);
		// StringBuffer buff = new StringBuffer();
		// for (int i = 0; i < envp.length; i++) {
		// buff.append(envp[i]);
		// if (i < envp.length - 1) {
		// buff.append('\n');
		// }
		// }
		// process.setAttribute(DebugPlugin.ATTR_ENVIRONMENT, buff.toString());
		// }
		monitor.worked(1);
		monitor.done();

		phpServer.addProcessListener(launch.getProcesses()[0]);
	}

	/**
	 * Returns the 'rendered' name for the specified command line
	 * 
	 * @param commandLine
	 *            the command line
	 * @param timestamp
	 *            the run-at time for the process
	 * @return the name for the process
	 */
	public static String renderProcessLabel(String[] commandLine, String timestamp) {
		String format = "{0} ({1})";
		return NLS.bind(format, new String[] { commandLine[0], timestamp });
	}

	/**
	 * Prepares the command line from the specified array of strings
	 * 
	 * @param commandLine
	 *            the command line
	 * @return the command line label
	 */
	protected String renderCommandLine(String[] commandLine) {
		return DebugPlugin.renderArguments(commandLine, null);
	}

	/**
	 * Returns a new process aborting if the process could not be created.
	 * 
	 * @param launch
	 *            the launch the process is contained in
	 * @param p
	 *            the system process to wrap
	 * @param label
	 *            the label assigned to the process
	 * @param attributes
	 *            values for the attribute map
	 * @return the new process
	 * @throws CoreException
	 *             problems occurred creating the process
	 * @since 3.0
	 */
	protected IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes)
			throws CoreException {
		IProcess process = DebugPlugin.newProcess(launch, p, label, attributes);
		if (process == null) {
			p.destroy();
			abort("22222", null, 1);
		}
		return process;
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 * @throws CoreException
	 *             The exception encapsulating the reason for the abort
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, code, message, exception));
	}

	/**
	 * Returns the default process attribute map for Java processes.
	 * 
	 * @return default process attribute map for Java processes
	 */
	protected Map<String, String> getDefaultProcessMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(IProcess.ATTR_PROCESS_TYPE, "php");
		return map;
	}

}
