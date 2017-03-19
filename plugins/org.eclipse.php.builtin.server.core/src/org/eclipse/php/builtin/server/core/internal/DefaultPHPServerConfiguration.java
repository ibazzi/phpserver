package org.eclipse.php.builtin.server.core.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerPort;

public class DefaultPHPServerConfiguration extends PHPServerConfiguration {

	protected String fPhpIniFile;

	public DefaultPHPServerConfiguration(IFolder path) {
		super(path);
	}

	@Override
	public ServerPort getServerPort() {
		return getMainPort();
	}

	@Override
	public void modifyServerPort(String id, int port) {
		// TODO Auto-generated method stub

	}

	@Override
	public IStatus localizeConfiguration(IPath baseDir, IPath deployDir, PHPServer server, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerPort getMainPort() {
		// TODO Auto-generated method stub
		return new ServerPort("server", "HTTP Port", 10000, "TCPIP");
	}

	@Override
	protected void save(IFolder folder, IProgressMonitor monitor) throws CoreException {
		try {
			monitor = ProgressUtil.getMonitorFor(monitor);
			monitor.beginTask(Messages.savingTask, 1200);

			// // save server.xml
			// byte[] data = serverFactory.getContents();
			// InputStream in = new ByteArrayInputStream(data);
			// IFile file = folder.getFile("server.xml");
			// if (file.exists()) {
			// if (isServerDirty)
			// file.setContents(in, true, true,
			// ProgressUtil.getSubMonitorFor(monitor, 200));
			// else
			// monitor.worked(200);
			// } else
			// file.create(in, true, ProgressUtil.getSubMonitorFor(monitor,
			// 200));
			// isServerDirty = false;

			// save catalina.properties
			if (fPhpIniFile != null) {
				InputStream in = new ByteArrayInputStream(fPhpIniFile.getBytes());
				IFile file = folder.getFile("php.ini");
				if (file.exists())
					monitor.worked(200);
				// file.setContents(in, true, true,
				// ProgressUtil.getSubMonitorFor(monitor, 200));
				else
					file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
			} else
				monitor.worked(200);

			if (monitor.isCanceled())
				return;
			monitor.done();
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not save PHP 7.0 Built-in Server configuration to " + folder.toString(),
					e);
			throw new CoreException(new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
					NLS.bind(Messages.errorCouldNotSaveConfiguration, new String[] { e.getLocalizedMessage() }), e));
		}
	}

	@Override
	protected void load(IPath path, IProgressMonitor monitor) throws CoreException {
		try {
			monitor = ProgressUtil.getMonitorFor(monitor);
			monitor.beginTask(Messages.loadingTask, 7);

			// load properties file
			File file = path.append("php.ini").toFile();
			if (file.exists())
				fPhpIniFile = PHPServerHelper.getFileContents(new FileInputStream(file));
			else
				fPhpIniFile = null;
			monitor.worked(1);

			if (monitor.isCanceled())
				return;
			monitor.done();
		} catch (Exception e) {
			Trace.trace(Trace.WARNING, "Could not load PHP ini from " + path.toOSString() + ": " + e.getMessage());
			throw new CoreException(new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
					NLS.bind(Messages.errorCouldNotLoadConfiguration, path.toOSString()), e));
		}
	}

	/**
	 * @see TomcatConfiguration#load(IFolder, IProgressMonitor)
	 */
	public void load(IFolder folder, IProgressMonitor monitor) throws CoreException {
		try {
			monitor = ProgressUtil.getMonitorFor(monitor);
			monitor.beginTask(Messages.loadingTask, 1200);

			// load server.xml
			// file = folder.getFile("server.xml");
			// InputStream in = file.getContents();
			// serverFactory = new Factory();
			// serverFactory.setPackageName("org.eclipse.jst.server.tomcat.core.internal.xml.server40");
			// server = (Server) serverFactory.loadDocument(in);
			// serverInstance = new ServerInstance(server, null, null);
			// monitor.worked(200);

			// load catalina.properties
			IFile file = folder.getFile("php.ini");
			if (file.exists()) {
				InputStream in = file.getContents();
				fPhpIniFile = PHPServerHelper.getFileContents(in);
			} else
				fPhpIniFile = null;
			monitor.worked(200);

			if (monitor.isCanceled())
				throw new Exception("Cancelled");
			monitor.done();
		} catch (Exception e) {
			Trace.trace(Trace.WARNING, "Could not load PHP ini from " + folder.getFullPath() + ": " + e.getMessage());
			throw new CoreException(new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
					NLS.bind(Messages.errorCouldNotLoadConfiguration, folder.getFullPath().toOSString()), e));
		}
	}

	@Override
	public IPath getServerWorkDirectory(IPath basePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath getContextWorkDirectory(IPath basePath, IModule module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WebModule> getWebModules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addWebModule(int index, IPHPWebModule module) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeWebModule(int index) {
		// TODO Auto-generated method stub

	}

}