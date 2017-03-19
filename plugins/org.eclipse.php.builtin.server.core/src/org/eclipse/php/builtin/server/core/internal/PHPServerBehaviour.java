package org.eclipse.php.builtin.server.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.IModulePublishHelper;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.PublishHelper;

public class PHPServerBehaviour extends ServerBehaviourDelegate implements IPHPServerBehaviour, IModulePublishHelper {
	private static final String ATTR_STOP = "stop-server";

	protected transient PingThread ping = null;
	protected transient IDebugEventSetListener processListener;
	private ILaunch fLaunch;

	public PHPServerBehaviour() {
	}

	@Override
	public void stop(boolean force) {
		terminate();
	}

	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
			throws CoreException {

		// PHPRuntime runtime = getServer().get
		// workingCopy.setAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION,
		// runtime.getPhpExecutableLocation());
		workingCopy.setAttribute(IPHPDebugConstants.ATTR_INI_LOCATION,
				getServer().getServerConfiguration().getRawLocation().append("php.ini").toOSString());
	}

	protected IModuleResource[] getResources(IModule[] module) {
		return super.getResources(module);
	}

	protected IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return super.getPublishedResourceDelta(module);
	}

	/**
	 * Cleans the entire work directory for this server. This involves deleting
	 * all subdirectories of the server's work directory.
	 * 
	 * @param monitor
	 *            a progress monitor
	 * @return results of the clean operation
	 * @throws CoreException
	 */
	public IStatus cleanServerWorkDir(IProgressMonitor monitor) throws CoreException {
		IStatus result;
		IPath basePath = getRuntimeBaseDirectory();
		IPath workPath = getPHPServerConfiguration().getServerWorkDirectory(basePath);
		if (workPath != null) {
			File workDir = workPath.toFile();
			result = Status.OK_STATUS;
			if (workDir.exists() && workDir.isDirectory()) {
				// Delete subdirectories of the server's work dir
				File[] files = workDir.listFiles();
				if (files != null && files.length > 0) {
					MultiStatus ms = new MultiStatus(PHPServerPlugin.PLUGIN_ID, 0,
							"Problem occurred deleting work directory for module.", null);
					int size = files.length;
					monitor = ProgressUtil.getMonitorFor(monitor);
					monitor.beginTask(
							NLS.bind("Cleaning Server Work Directory", new String[] { workDir.getAbsolutePath() }),
							size * 10);

					for (int i = 0; i < size; i++) {
						File current = files[i];
						if (current.isDirectory()) {
							IStatus[] results = PublishHelper.deleteDirectory(current,
									ProgressUtil.getSubMonitorFor(monitor, 10));
							if (results != null && results.length > 0) {
								for (int j = 0; j < results.length; j++) {
									ms.add(results[j]);
								}
							}
						}
					}
					monitor.done();
					result = ms;
				}
			}
		} else {
			result = new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
					"Could not determine work directory for module", null);
		}
		return result;
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
	@Override
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

	/**
	 * Returns the runtime base path for relative paths in the server
	 * configuration.
	 * 
	 * @return the base path
	 */
	public IPath getRuntimeBaseDirectory() {
		return getPHPServer().getRuntimeBaseDirectory();
	}

	public PHPServer getPHPServer() {
		return (PHPServer) getServer().loadAdapter(PHPServer.class, null);
	}

	public PHPServerConfiguration getPHPServerConfiguration() throws CoreException {
		return getPHPServer().getPHPServerConfiguration();
	}

	public IPath getTempDirectory() {
		return super.getTempDirectory(false);
	}

	@Override
	public IPath getPublishDirectory(IModule[] module) {
		if (module == null || module.length != 1)
			return null;

		return getModuleDeployDirectory(module[0]);
	}

	/**
	 * Gets the directory to which to deploy a module's web application.
	 * 
	 * @param module
	 *            a module
	 * @return full path to deployment directory for the module
	 */
	public IPath getModuleDeployDirectory(IModule module) {
		return getServerDeployDirectory().append(module.getName());
	}

	public void setModulePublishState2(IModule[] module, int state) {
		setModulePublishState(module, state);
	}

	public Properties loadModulePublishLocations() {
		Properties p = new Properties();
		IPath path = getTempDirectory().append("publish.txt");
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(path.toFile());
			p.load(fin);
		} catch (Exception e) {
			// ignore
		} finally {
			try {
				fin.close();
			} catch (Exception ex) {
				// ignore
			}
		}
		return p;
	}

	public void saveModulePublishLocations(Properties p) {
		IPath path = getTempDirectory().append("publish.txt");
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(path.toFile());
			p.store(fout, "PHP Server publish data");
		} catch (Exception e) {
			// ignore
		} finally {
			try {
				fout.close();
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	public void setPHPServerPublishState(int state) {
		setServerPublishState(state);
	}

	public void setPHPServerRestartState(boolean state) {
		setServerRestartState(state);
	}

	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
		if (getServer().getRuntime() == null)
			return;

		IPath installDir = getServer().getRuntime().getLocation();
		IPath confDir = null;
		confDir = installDir;
		IStatus status = PHPServerHelper.createDeploymentDirectory(getServerDeployDirectory());
		if (status != null && !status.isOK())
			throw new CoreException(status);

		monitor = ProgressUtil.getMonitorFor(monitor);
		monitor.beginTask(Messages.publishServerTask, 600);

		status = getPHPServerConfiguration().cleanupServer(confDir, installDir, false,
				ProgressUtil.getSubMonitorFor(monitor, 100));
		if (status != null && !status.isOK())
			throw new CoreException(status);

		status = getPHPServerConfiguration().backupAndPublish(confDir, false,
				ProgressUtil.getSubMonitorFor(monitor, 400));
		if (status != null && !status.isOK())
			throw new CoreException(status);

		status = getPHPServerConfiguration().localizeConfiguration(confDir, getServerDeployDirectory(), getPHPServer(),
				ProgressUtil.getSubMonitorFor(monitor, 100));
		if (status != null && !status.isOK())
			throw new CoreException(status);

		monitor.done();

		setServerPublishState(IServer.PUBLISH_STATE_NONE);
	}

	/*
	 * Publishes the given module to the server.
	 */
	protected void publishModule(int kind, int deltaKind, IModule[] moduleTree, IProgressMonitor monitor)
			throws CoreException {

		Properties p = loadModulePublishLocations();

		PublishHelper helper = new PublishHelper(getRuntimeBaseDirectory().append("temp").toFile());
		// If parent web module
		if (moduleTree.length == 1) {
			publishDir(deltaKind, p, moduleTree, helper, monitor);
		}
		// // Else a child module
		// else {
		// // Try to determine the URI for the child module
		// IWebModule webModule = (IWebModule)
		// moduleTree[0].loadAdapter(IWebModule.class, monitor);
		// String childURI = null;
		// if (webModule != null) {
		// childURI = webModule.getURI(moduleTree[1]);
		// }
		// // Try to determine if child is binary
		// IJ2EEModule childModule = (IJ2EEModule)
		// moduleTree[1].loadAdapter(IJ2EEModule.class, monitor);
		// boolean isBinary = false;
		// if (childModule != null) {
		// isBinary = childModule.isBinary();
		// }
		//
		// if (isBinary) {
		// publishArchiveModule(childURI, kind, deltaKind, p, moduleTree,
		// helper, monitor);
		// } else {
		// publishJar(childURI, kind, deltaKind, p, moduleTree, helper,
		// monitor);
		// }
		// }

		setModulePublishState(moduleTree, IServer.PUBLISH_STATE_NONE);

		saveModulePublishLocations(p);
	}

	/**
	 * Publish a web module.
	 * 
	 * @param deltaKind
	 * @param p
	 * @param module
	 * @param monitor
	 * @throws CoreException
	 */
	private void publishDir(int deltaKind, Properties p, IModule module[], PublishHelper helper,
			IProgressMonitor monitor) throws CoreException {
		List<IStatus> status = new ArrayList<IStatus>();
		// Remove if requested or if previously published and are now serving
		// without publishing
		if (deltaKind == REMOVED) {
			String publishPath = (String) p.get(module[0].getId());
			if (publishPath != null) {
				try {
					File f = new File(publishPath);
					if (f.exists()) {
						IStatus[] stat = PublishHelper.deleteDirectory(f, monitor);
						PublishOperation2.addArrayToList(status, stat);
					}
				} catch (Exception e) {
					throw new CoreException(new Status(IStatus.WARNING, PHPServerPlugin.PLUGIN_ID, 0,
							NLS.bind(Messages.errorPublishCouldNotRemoveModule, module[0].getName()), e));
				}
				p.remove(module[0].getId());
			}
		} else {
			IPath path = getModuleDeployDirectory(module[0]);
			IModuleResource[] mr = getResources(module);
			IPath[] jarPaths = null;
			// IWebModule webModule =
			// (IWebModule)module[0].loadAdapter(IWebModule.class, monitor);
			// IModule [] childModules = getServer().getChildModules(module,
			// monitor);
			// if (childModules != null && childModules.length > 0) {
			// jarPaths = new IPath[childModules.length];
			// for (int i = 0; i < childModules.length; i++) {
			// if (webModule != null) {
			// jarPaths[i] = new Path(webModule.getURI(childModules[i]));
			// }
			// else {
			// IJ2EEModule childModule =
			// (IJ2EEModule)childModules[i].loadAdapter(IJ2EEModule.class,
			// monitor);
			// if (childModule != null && childModule.isBinary()) {
			// jarPaths[i] = new
			// Path("WEB-INF/lib").append(childModules[i].getName());
			// }
			// else {
			// jarPaths[i] = new
			// Path("WEB-INF/lib").append(childModules[i].getName() + ".jar");
			// }
			// }
			// }
			// }
			IStatus[] stat = helper.publishSmart(mr, path, jarPaths, monitor);
			PublishOperation2.addArrayToList(status, stat);
			p.put(module[0].getId(), path.toOSString());
		}
		PublishOperation2.throwException(status);
	}

	/**
	 * Gets the directory to which modules should be deployed for this server.
	 * 
	 * @return full path to deployment directory for the server
	 */
	public IPath getServerDeployDirectory() {
		return getPHPServer().getServerDeployDirectory();
	}

}