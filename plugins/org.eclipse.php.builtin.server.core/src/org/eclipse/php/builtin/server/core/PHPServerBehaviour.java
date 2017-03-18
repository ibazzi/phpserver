package org.eclipse.php.builtin.server.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

public class PHPServerBehaviour extends ServerBehaviourDelegate {
	private static final String ATTR_STOP = "stop-server";

	protected transient PingThread ping = null;
	protected transient IDebugEventSetListener processListener;
	private ILaunch fLaunch;

	public PHPServerBehaviour() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void stop(boolean force) {
		terminate();
	}

	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
			throws CoreException {
		// PHPRuntime runtime =
		// getServer().getRuntime().getAdapter(PHPRuntime.class);
		// workingCopy.setAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION,
		// runtime.getPhpExecutableLocation());
		// workingCopy.setAttribute(IPHPDebugConstants.ATTR_INI_LOCATION,
		// runtime.getPhpIniLocation());
		// IPath location = runtime
		// String exeFile = location.append("php").addFileExtension("exe");
		// workingCopy.setAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION,
		// value);
	}

	protected void addProcessListener(final IProcess newProcess) {
		if (processListener != null || newProcess == null)
			return;

		processListener = new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				if (events != null) {
					int size = events.length;
					for (int i = 0; i < size; i++) {
						if (newProcess != null && newProcess.equals(events[i].getSource())
								&& events[i].getKind() == DebugEvent.TERMINATE) {
							stopImpl();
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(processListener);
	}

	protected void setServerStarted() {
		setServerState(IServer.STATE_STARTED);
	}

	protected void stopImpl() {
		if (ping != null) {
			ping.stop();
			ping = null;
		}
		if (processListener != null) {
			DebugPlugin.getDefault().removeDebugEventListener(processListener);
			processListener = null;
		}
		setServerState(IServer.STATE_STOPPED);
	}

	/**
	 * Setup for starting the server.
	 * 
	 * @param launch
	 *            ILaunch
	 * @param launchMode
	 *            String
	 * @param monitor
	 *            IProgressMonitor
	 * @throws CoreException
	 *             if anything goes wrong
	 */
	public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
		if ("true".equals(launch.getLaunchConfiguration().getAttribute(ATTR_STOP, "false"))) {
			launch.terminate();
			stopImpl();
			return;
		}
		fLaunch = launch;
		// if (getTomcatRuntime() == null)
		// throw new CoreException();

		// IStatus status = getTomcatRuntime().validate();
		// if (status != null && status.getSeverity() == IStatus.ERROR)
		// throw new CoreException(status);
		//
		// // setRestartNeeded(false);
		// TomcatConfiguration configuration = getTomcatConfiguration();

		// check that ports are free
		// Iterator iterator = configuration.getServerPorts().iterator();
		// List<ServerPort> usedPorts = new ArrayList<ServerPort>();
		// while (iterator.hasNext()) {
		// ServerPort sp = (ServerPort) iterator.next();
		// if (sp.getPort() < 0)
		// throw new CoreException(
		// new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
		// Messages.errorPortInvalid, null));
		// if (SocketUtil.isPortInUse(sp.getPort(), 5)) {
		// usedPorts.add(sp);
		// }
		// }
		// if (usedPorts.size() == 1) {
		// ServerPort port = usedPorts.get(0);
		// throw new CoreException(new Status(IStatus.ERROR,
		// PHPServerPlugin.PLUGIN_ID, 0,
		// NLS.bind(Messages.errorPortInUse, new String[] { port.getPort() + "",
		// getServer().getName() }),
		// null));
		// } else if (usedPorts.size() > 1) {
		// String portStr = "";
		// iterator = usedPorts.iterator();
		// boolean first = true;
		// while (iterator.hasNext()) {
		// if (!first)
		// portStr += ", ";
		// first = false;
		// ServerPort sp = (ServerPort) iterator.next();
		// portStr += "" + sp.getPort();
		// }
		// throw new CoreException(new Status(IStatus.ERROR,
		// PHPServerPlugin.PLUGIN_ID, 0,
		// NLS.bind(Messages.errorPortsInUse, new String[] { portStr,
		// getServer().getName() }), null));
		// }
		//
		// // check that there is only one app for each context root
		// iterator = configuration.getWebModules().iterator();
		// List<String> contextRoots = new ArrayList<String>();
		// while (iterator.hasNext()) {
		// WebModule module = (WebModule) iterator.next();
		// String contextRoot = module.getPath();
		// if (contextRoots.contains(contextRoot))
		// throw new CoreException(new Status(IStatus.ERROR,
		// TomcatPlugin.PLUGIN_ID, 0,
		// NLS.bind(Messages.errorDuplicateContextRoot, new String[] {
		// contextRoot }), null));
		//
		// contextRoots.add(contextRoot);
		// }

		setServerRestartState(false);
		setServerState(IServer.STATE_STARTING);
		setMode(launchMode);

		// ping server to check for startup
		try {
			String url = "http://" + getServer().getHost();
			url += ":" + 10000;
			ping = new PingThread(getServer(), url, -1, this);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Can't ping for PHP Server startup.");
		}
	}

	/**
	 * Terminates the server.
	 */
	protected void terminate() {
		if (getServer().getServerState() == IServer.STATE_STOPPED)
			return;

		try {
			setServerState(IServer.STATE_STOPPING);
			if (Trace.isTraceEnabled())
				Trace.trace(Trace.FINER, "Killing the PHP Server process");
			if (fLaunch != null) {
				fLaunch.terminate();
				stopImpl();
			}
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error killing the process", e);
		}
	}

}
