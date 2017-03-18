package org.eclipse.php.builtin.server.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;

public class PHPServer extends ServerDelegate {

	public PHPServer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];

			}
		}

		return Status.OK_STATUS;
	}

	@Override
	public IModule[] getChildModules(IModule[] module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		// TODO Auto-generated method stub
		return new IModule[] { module };
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	public ServerPort[] getServerPorts() {
		ServerPort port = new ServerPort("server", "port", 8000, "TCPIP");
		return new ServerPort[] { port };
	}
	
	@Override
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		super.importRuntimeConfiguration(runtime, monitor);
	}

}
