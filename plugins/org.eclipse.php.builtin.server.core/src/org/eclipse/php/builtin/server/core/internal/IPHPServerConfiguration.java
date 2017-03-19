/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.php.builtin.server.core.internal;

import java.util.List;

import org.eclipse.wst.server.core.ServerPort;

/**
 * 
 */
public interface IPHPServerConfiguration {

	/**
	 * Returns a ServerPort that this configuration uses.
	 *
	 * @return the server ports
	 */
	public List<ServerPort> getServerPorts();

	/**
	 * Return a list of the web modules in this server.
	 * 
	 * @return the web modules
	 */
	public List<WebModule> getWebModules();

}