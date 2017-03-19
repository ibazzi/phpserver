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

public interface IPHPServerConfigurationWorkingCopy extends IPHPServerConfiguration {

	/**
	 * Modify the port with the given id.
	 *
	 * @param id
	 *            java.lang.String
	 * @param port
	 *            int
	 */
	public void modifyServerPort(String id, int port);

}