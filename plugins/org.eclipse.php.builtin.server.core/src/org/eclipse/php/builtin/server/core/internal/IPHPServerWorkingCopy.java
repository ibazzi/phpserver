/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.php.builtin.server.core.internal;

/**
 * 
 */
public interface IPHPServerWorkingCopy extends IPHPServer {

	/**
	 * Sets the instance directory for the server. If set to null, the instance
	 * directory is derived from the testEnvironment setting.'
	 * 
	 * @param instanceDir
	 *            absolule path to the instance directory.
	 */
	public void setDocumentRootDirectory(String documentRootDir);

}
