package org.eclipse.php.builtin.server.core.internal;

import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerDelegate;

public class PHPServer extends ServerDelegate implements IPHPServer, IPHPServerWorkingCopy {
	private static final IModule[] EMPTY_LIST = new IModule[0];
	public static final String PROPERTY_DEBUG = "debug";
	private static final String DEPLOY_DIR = "htdocs";

	protected transient PHPServerConfiguration configuration;

	// Configuration version control
	private int currentVersion;
	private int loadedVersion;
	private Object versionLock = new Object();

	public PHPServer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];
				if (!"php.web".equals(module.getModuleType().getId()))
					return new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0, Messages.errorWebModulesOnly, null);

				// if (module.getProject() != null) {
				// IStatus status = FacetUtil.verifyFacets(module.getProject(),
				// getServer());
				// if (status != null && !status.isOK())
				// return status;
				// }
			}
		}

		return Status.OK_STATUS;
	}

	@Override
	public IModule[] getChildModules(IModule[] module) {
		return EMPTY_LIST;
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		if ("php.web".equals(module.getModuleType().getId())) {
			IStatus status = canModifyModules(new IModule[] { module }, null);
			if (status == null || !status.isOK())
				throw new CoreException(status);
			return new IModule[] { module };
		}
		return EMPTY_LIST;
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
		IStatus status = canModifyModules(add, remove);
		if (status == null || !status.isOK())
			throw new CoreException(status);

		// PHPServerConfiguration config = getPHPServerConfiguration();
		//
		// if (add != null) {
		// int size = add.length;
		// for (int i = 0; i < size; i++) {
		// IModule module3 = add[i];
		// IWebModule module = (IWebModule)
		// module3.loadAdapter(IWebModule.class, monitor);
		// String contextRoot = module.getContextRoot();
		// if (contextRoot != null && !contextRoot.startsWith("/") &&
		// contextRoot.length() > 0)
		// contextRoot = "/" + contextRoot;
		// String docBase = config.getDocBasePrefix() + module3.getName();
		// WebModule module2 = new WebModule(contextRoot, docBase,
		// module3.getId());
		// config.addWebModule(-1, module2);
		// }
		// }
		//
		// if (remove != null) {
		// int size2 = remove.length;
		// for (int j = 0; j < size2; j++) {
		// IModule module3 = remove[j];
		// String memento = module3.getId();
		// List modules = getPHPServerConfiguration().getWebModules();
		// int size = modules.size();
		// for (int i = 0; i < size; i++) {
		// WebModule module = (WebModule) modules.get(i);
		// if (memento.equals(module.getMemento()))
		// config.removeWebModule(i);
		// }
		// }
		// }
		// config.save(config.getFolder(), monitor);

	}

	public ServerPort[] getServerPorts() {
		if (getServer().getServerConfiguration() == null)
			return new ServerPort[0];

		try {
			ServerPort port = getPHPServerConfiguration().getServerPort();
			return new ServerPort[] { port };
		} catch (Exception e) {
			return new ServerPort[0];
		}
	}

	@Override
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
		// Initialize state
		synchronized (versionLock) {
			configuration = null;
			currentVersion = 0;
			loadedVersion = 0;
		}
		if (runtime == null) {
			return;
		}
		IPath path = runtime.getLocation();

		IFolder folder = getServer().getServerConfiguration();
		PHPServerConfiguration tcConfig = new DefaultPHPServerConfiguration(folder);
		//

		try {
			tcConfig.importFromPath(path, monitor);
		} catch (CoreException ce) {
			throw ce;
		}
		// Update version
		synchronized (versionLock) {
			// If not already initialized by some other thread, save the
			// configuration
			if (configuration == null) {
				configuration = tcConfig;
			}
		}
	}

	public IPath getRuntimeBaseDirectory() {
		return PHPServerHelper.getStandardBaseDirectory(this);
	}

	/**
	 * Get the Tomcat runtime for this server.
	 * 
	 * @return Tomcat runtime for this server
	 */
	public PHPRuntime getPHPRuntime() {
		if (getServer().getRuntime() == null)
			return null;

		return (PHPRuntime) getServer().getRuntime().loadAdapter(PHPRuntime.class, null);
	}

	public PHPServerConfiguration getPHPServerConfiguration() throws CoreException {
		int current;
		PHPServerConfiguration tcConfig;
		// Grab current state
		synchronized (versionLock) {
			current = currentVersion;
			tcConfig = configuration;
		}
		// If configuration needs loading
		if (tcConfig == null || loadedVersion != current) {
			IFolder folder = getServer().getServerConfiguration();
			if (folder == null || !folder.exists()) {
				String path = null;
				if (folder != null) {
					path = folder.getFullPath().toOSString();
					IProject project = folder.getProject();
					if (project != null && project.exists() && !project.isOpen())
						throw new CoreException(new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
								NLS.bind(Messages.errorConfigurationProjectClosed, path, project.getName()), null));
				}
				throw new CoreException(new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
						NLS.bind(Messages.errorNoConfiguration, path), null));
			}
			// If not yet loaded
			if (tcConfig == null) {

				tcConfig = new DefaultPHPServerConfiguration(folder);
				// String id = getServer().getServerType().getId();
				// if (id.indexOf("32") > 0)
				// tcConfig = new Tomcat32Configuration(folder);
				// else if (id.indexOf("40") > 0)
				// tcConfig = new Tomcat40Configuration(folder);
				// else if (id.indexOf("41") > 0)
				// tcConfig = new Tomcat41Configuration(folder);
				// else if (id.indexOf("50") > 0)
				// tcConfig = new Tomcat50Configuration(folder);
				// else if (id.indexOf("55") > 0)
				// tcConfig = new Tomcat55Configuration(folder);
				// else if (id.indexOf("60") > 0)
				// tcConfig = new Tomcat60Configuration(folder);
				// else if (id.indexOf("70") > 0)
				// tcConfig = new Tomcat70Configuration(folder);
				// else if (id.indexOf("80") > 0)
				// tcConfig = new Tomcat80Configuration(folder);
				// else if (id.indexOf("85") > 0)
				// tcConfig = new Tomcat85Configuration(folder);
				// else if (id.indexOf("90") > 0)
				// tcConfig = new Tomcat90Configuration(folder);
				// else {
				// throw new CoreException(new Status(IStatus.ERROR,
				// PHPServerPlugin.PLUGIN_ID, 0, Messages.errorUnknownVersion,
				// null));
				// }
			}
			try {

				tcConfig.load(folder, null);
				// Update loaded version
				synchronized (versionLock) {
					// If newer version not already loaded, update version
					if (configuration == null || loadedVersion < current) {
						configuration = tcConfig;
						loadedVersion = current;
					}
				}
			} catch (CoreException ce) {
				// Ignore
				throw ce;
			}
		}
		return tcConfig;
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
		PHPServerConfiguration tcConfig = configuration;
		if (tcConfig == null)
			return;
		tcConfig.save(getServer().getServerConfiguration(), monitor);
	}

	public void configurationChanged() {
		synchronized (versionLock) {
			// Alter the current version
			currentVersion++;
		}
	}

	/**
	 * Return the root URL of this module.
	 * 
	 * @param module
	 *            org.eclipse.wst.server.core.model.IModule
	 * @return java.net.URL
	 */
	public URL getModuleRootURL(IModule module) {
		try {
			if (module == null)
				return null;

			PHPServerConfiguration config = getPHPServerConfiguration();
			if (config == null)
				return null;

			String url = "http://" + getServer().getHost();
			int port = config.getMainPort().getPort();
			port = ServerUtil.getMonitoredPort(getServer(), port, "web");
			if (port != 80)
				url += ":" + port;

			url += config.getWebModuleURL(module);

			if (!url.endsWith("/"))
				url += "/";

			return new URL(url);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not get root URL", e);
			return null;
		}
	}

	@Override
	public void setDocumentRootDirectory(String instanceDir) {
		setAttribute(PROPERTY_DOCUMENT_ROOT_DIR, instanceDir);

	}

	@Override
	public String getDocumentRootDirectory() {
		return getAttribute(PROPERTY_DOCUMENT_ROOT_DIR, (String) null);
	}

	public void setDefaults(IProgressMonitor monitor) {
		setAttribute("auto-publish-setting", 2);
		setAttribute("auto-publish-time", 1);
	}

	/**
	 * Gets the directory to which modules should be deployed for this server.
	 * 
	 * @return full path to deployment directory for the server
	 */
	public IPath getServerDeployDirectory() {
		IPath deployPath = new Path(DEPLOY_DIR);
		if (!deployPath.isAbsolute()) {
			IPath base = getRuntimeBaseDirectory();
			deployPath = base.append(deployPath);
		}
		return deployPath;
	}

}
