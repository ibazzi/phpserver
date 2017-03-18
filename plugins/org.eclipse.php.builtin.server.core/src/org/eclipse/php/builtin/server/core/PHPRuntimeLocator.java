/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.php.builtin.server.core;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Trace;
import org.eclipse.wst.server.core.model.RuntimeLocatorDelegate;

/**
 * 
 */
public class PHPRuntimeLocator extends RuntimeLocatorDelegate {
	protected static final String[] runtimeTypes = new String[] { "org.eclipse.php.server.runtime.70" };
	private boolean isPhpExeFound;
	private boolean isPhpIniFound;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.server.core.model.IRuntimeFactoryDelegate#
	 * getKnownRuntimes()
	 */
	public void searchForRuntimes(IPath path, IRuntimeSearchListener listener, IProgressMonitor monitor) {
		searchForRuntimes2(path, listener, monitor);
	}

	protected void searchForRuntimes2(IPath path, IRuntimeSearchListener listener, IProgressMonitor monitor) {
		File[] files = null;
		if (path != null) {
			File f = path.toFile();
			if (f.exists())
				files = f.listFiles();
			else
				return;
		} else
			files = File.listRoots();

		if (files != null) {
			int size = files.length;
			int work = 100 / size;
			int workLeft = 100 - (work * size);
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled())
					return;
				if (files[i] != null && files[i].isFile())
					searchPhpExecutables(listener, files[i], 4, monitor);
				monitor.worked(work);
			}
			monitor.worked(workLeft);
		} else
			monitor.worked(100);
	}

	protected void searchPhpExecutables(IRuntimeSearchListener listener, File file, int depth,
			IProgressMonitor monitor) {
		if (file.getName().equals("php.exe")) {
			isPhpExeFound = true;
		} else if (file.getName().equals("php.ini")) {
			isPhpIniFound = true;
		}
		if (isPhpExeFound && isPhpIniFound) {
			IRuntimeWorkingCopy runtime = getRuntimeFromDir(file.getParentFile(), monitor);
			if (runtime != null) {
				listener.runtimeFound(runtime);
				return;
			}
		}
	}

	protected static IRuntimeWorkingCopy getRuntimeFromDir(File dir, IProgressMonitor monitor) {
		for (int i = 0; i < runtimeTypes.length; i++) {
			try {
				IRuntimeType runtimeType = ServerCore.findRuntimeType(runtimeTypes[i]);
				String absolutePath = dir.getAbsolutePath();
				String id = absolutePath.replace(File.separatorChar, '_').replace(':', '-');
				IRuntimeWorkingCopy runtime = runtimeType.createRuntime(id, monitor);
				runtime.setName(dir.getName());
				runtime.setLocation(new Path(absolutePath));
				IStatus status = runtime.validate(monitor);
				if (status == null || status.getSeverity() != IStatus.ERROR)
					return runtime;

				Trace.trace(Trace.STRING_FINER,
						"False runtime found at " + dir.getAbsolutePath() + ": " + status.getMessage());
			} catch (Exception e) {
				Trace.trace(Trace.STRING_SEVERE, "Could not find runtime", e);
			}
		}
		return null;
	}
}