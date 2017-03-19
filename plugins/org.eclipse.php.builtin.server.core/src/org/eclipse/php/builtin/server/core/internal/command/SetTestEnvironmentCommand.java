/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.php.builtin.server.core.internal.command;

import org.eclipse.php.builtin.server.core.internal.IPHPServerWorkingCopy;
import org.eclipse.php.builtin.server.core.internal.Messages;

/**
 * Command to change the server test mode. The server instance directory is
 * cleared in conjunction with this command for legacy support.
 */
public class SetTestEnvironmentCommand extends ServerCommand {
	protected String oldInstanceDir;

	/**
	 * SetTestEnvironmentCommand constructor comment.
	 * 
	 * @param server
	 *            a Tomcat server
	 * @param te
	 *            <code>true</code> for a test environment.
	 */
	public SetTestEnvironmentCommand(IPHPServerWorkingCopy server) {
		super(server, Messages.serverEditorActionSetServerDirectory);
	}

	/**
	 * Execute the command.
	 */
	public void execute() {
		// save old instance directory
		oldInstanceDir = server.getDocumentRootDirectory();
		// ensure instance directory is cleared
		server.setDocumentRootDirectory(null);
	}

	/**
	 * Undo the command.
	 */
	public void undo() {
		server.setDocumentRootDirectory(oldInstanceDir);
	}
}
