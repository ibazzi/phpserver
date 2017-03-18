/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.php.builtin.server.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.wst.server.ui.ServerLaunchConfigurationTab;
/**
 * A debug tab group for launching Tomcat. 
 */
public class PHPServerLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	/*
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[4];
		tabs[0] = new ServerLaunchConfigurationTab(new String[] { "org.eclipse.php.server.builtin" });
		tabs[0].setLaunchConfigurationDialog(dialog);
//		tabs[1] = new JavaArgumentsTab();
//		tabs[1].setLaunchConfigurationDialog(dialog);
//		tabs[2] = new JavaClasspathTab();
//		tabs[2].setLaunchConfigurationDialog(dialog);
		tabs[1] = new SourceLookupTab();
		tabs[1].setLaunchConfigurationDialog(dialog);
		tabs[2] = new EnvironmentTab();
		tabs[2].setLaunchConfigurationDialog(dialog);
		tabs[3] = new CommonTab();
		tabs[3].setLaunchConfigurationDialog(dialog);
		setTabs(tabs);
	}
}