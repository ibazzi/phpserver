package org.eclipse.php.builtin.server.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;

public interface IPHPServerRunner {

	public void run(PHPServerRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor)
			throws CoreException;

	public void stop();
	
	public int getServerPort();

}
