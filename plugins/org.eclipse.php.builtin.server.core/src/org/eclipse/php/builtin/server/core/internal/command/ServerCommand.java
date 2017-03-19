package org.eclipse.php.builtin.server.core.internal.command;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.php.builtin.server.core.internal.IPHPServerWorkingCopy;
import org.eclipse.php.builtin.server.core.internal.PHPServer;

/**
 * A command on a PHP server.
 */
public abstract class ServerCommand extends AbstractOperation {
	protected PHPServer server;

	/**
	 * ServerCommand constructor comment.
	 * 
	 * @param server
	 *            a Tomcat server
	 * @param label
	 *            a label
	 */
	public ServerCommand(IPHPServerWorkingCopy server, String label) {
		super(label);
		this.server = (PHPServer) server;
	}

	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	public abstract void execute();

	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		execute();
		return null;
	}

	public abstract void undo();

	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		undo();
		return null;
	}
}