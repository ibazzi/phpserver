package org.eclipse.php.builtin.server.core.internal;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;
import org.eclipse.php.builtin.server.core.internal.debugger.HttpReverseProxyServer;
import org.eclipse.php.builtin.server.core.internal.debugger.HttpReverseProxyServer.IHttpRequestHandler;
import org.eclipse.php.builtin.server.core.internal.debugger.PHPServerDebugTarget;
import org.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;

@SuppressWarnings("restriction")
public class PHPServerLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		if (server == null) {
			Trace.trace(Trace.FINEST, "Launch configuration could not find server");
			// throw CoreException();
			return;
		}

		PHPServer sd = (PHPServer) server.loadAdapter(PHPServer.class, monitor);
		IPHPRuntime runtime = sd.getPHPRuntime();
		String phpExeString = runtime.getPHPExecutableLocation();
		String phpIniPath = configuration.getAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, "");

		PHPServerBehaviour phpServer = (PHPServerBehaviour) server.loadAdapter(PHPServerBehaviour.class, null);
		phpServer.setupLaunch(launch, mode, monitor);

		if (server.shouldPublish() && ServerCore.isAutoPublishing())
			server.publish(IServer.PUBLISH_INCREMENTAL, monitor);

		// resolve location
		File phpExeFile = new File(phpExeString);

		// Determine PHP configuration file location:
		String workingDir = phpServer.getServerDeployDirectory().toOSString();
		int port = phpServer.getPHPServerConfiguration().getMainPort().getPort();

		if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			int phpServerPort = port + 1;
			HttpReverseProxyServer proxyServer = new HttpReverseProxyServer(new IHttpRequestHandler() {

				@Override
				public void handle(HttpRequest request, HttpResponse response, HttpContext context)
						throws HttpException, IOException {
					HttpClient client = HttpClientBuilder.create().build();
					HttpResponse response1 = client.execute(new HttpHost("localhost", phpServerPort), request);
					response.setEntity(response1.getEntity());
					response.setStatusCode(response1.getStatusLine().getStatusCode());
				}

				@Override
				public void close(HttpServerConnection connection) throws IOException {
					if (connection != null) {
						connection.close();
					}
				}
			});
			try {
				proxyServer.start(port);
			} catch (Exception e) {
				e.printStackTrace();
			}
			port = phpServerPort;
		}

		String[] cmdLine = new String[] { phpExeFile.getAbsolutePath(), "-S", "0.0.0.0:" + port, "-t", workingDir, "-c",
				phpIniPath };
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
		phpServer.addProcessListener(process);

		if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			IDebugTarget target = new PHPServerDebugTarget(launch, process);
			launch.setAttribute(IDebugParametersKeys.BUILTIN_SERVER_DEBUGGER, "true");
			launch.addDebugTarget(target);
		}
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
