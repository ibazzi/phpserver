package org.eclipse.php.builtin.server.core;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

public class PHPRuntime extends RuntimeDelegate implements IPHPRuntimeWorkingCopy {

	public PHPRuntime() {
	}

	public IStatus validate() {
		IStatus status = super.validate();
		if (!status.isOK())
			return status;

		// on Tomcat 5.5 and later, the Eclipse JDT compiler is used for JSP's
		String id = getRuntime().getRuntimeType().getId();

		File f = getRuntime().getLocation().toFile();
		if (!f.canRead())
			return new Status(IStatus.WARNING, PHPServerPlugin.PLUGIN_ID, 0, Messages.warningCantReadConfig, null);
		File[] files = f.listFiles();
		boolean isExeFound = false;
		boolean isIniFound = false;
		String exec = null;
		if (files != null) {
			int size = files.length;
			for (int i = 0; i < size; i++) {
				File file = files[i];
				String fileName = file.getName();
				if (fileName.equalsIgnoreCase("php.exe") && file.canExecute()) {
					setAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, file.getAbsolutePath());
					isExeFound = true;
				} else if (fileName.equalsIgnoreCase("php.ini") && file.canRead()) {
					setAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, file.getAbsolutePath());
					isIniFound = true;
				}

			}
		}
		if (!isExeFound) {
			return new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0, Messages.errorPhpExeNotFoundOrNotExecutable,
					null);
		}
		if (!isIniFound) {
			return new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0, Messages.errorPhpIniNotFoundOrNotReadable,
					null);
		}

		// // For Tomcat 6.0, ensure we have J2SE 5.0
		// if (id != null && id.indexOf("60") > 0) {
		// IVMInstall vmInstall = getVMInstall();
		// if (vmInstall instanceof IVMInstall2) {
		// String javaVersion = ((IVMInstall2) vmInstall).getJavaVersion();
		// if (javaVersion != null && !isVMMinimumVersion(javaVersion, 105)) {
		// return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0,
		// Messages.errorJRETomcat60, null);
		// }
		// }
		// }
		// // Else for Tomcat 7.0, ensure we have J2SE 6.0
		// else if (id != null && id.indexOf("70") > 0) {
		// IVMInstall vmInstall = getVMInstall();
		// if (vmInstall instanceof IVMInstall2) {
		// String javaVersion = ((IVMInstall2) vmInstall).getJavaVersion();
		// if (javaVersion != null && !isVMMinimumVersion(javaVersion, 106)) {
		// return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0,
		// Messages.errorJRETomcat70, null);
		// }
		// }
		// }
		// // Else for Tomcat 8.0, ensure we have J2SE 7.0
		// else if (id != null && id.indexOf("80") > 0) {
		// IVMInstall vmInstall = getVMInstall();
		// if (vmInstall instanceof IVMInstall2) {
		// String javaVersion = ((IVMInstall2) vmInstall).getJavaVersion();
		// if (javaVersion != null && !isVMMinimumVersion(javaVersion, 107)) {
		// return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0,
		// Messages.errorJRETomcat80, null);
		// }
		// }
		// }
		//
		// // Else for Tomcat 8.5, ensure we have J2SE 7.0
		// else if (id != null && id.indexOf("85") > 0) {
		// IVMInstall vmInstall = getVMInstall();
		// if (vmInstall instanceof IVMInstall2) {
		// String javaVersion = ((IVMInstall2) vmInstall).getJavaVersion();
		// if (javaVersion != null && !isVMMinimumVersion(javaVersion, 107)) {
		// return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0,
		// Messages.errorJRETomcat85, null);
		// }
		// }
		// }
		//
		// // Else for Tomcat 9.0, ensure we have J2SE 8.0
		// else if (id != null && id.indexOf("90") > 0) {
		// IVMInstall vmInstall = getVMInstall();
		// if (vmInstall instanceof IVMInstall2) {
		// String javaVersion = ((IVMInstall2) vmInstall).getJavaVersion();
		// if (javaVersion != null && !isVMMinimumVersion(javaVersion, 108)) {
		// return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0,
		// Messages.errorJRETomcat90, null);
		// }
		// }
		// }

		// try {
		// PHPExeInfo phpExeInfo = PHPExeUtil.getPHPInfo(new File(exec), false);
		// if (phpExeInfo != null)
		// setPHPExeInfo(phpExeInfo);
		// } catch (PHPExeException e1) {
		// e1.printStackTrace();
		// }

		return Status.OK_STATUS;
	}

	// @Override
	// public PHPExeInfo getPHPExeInfo() {
	// return fPhpExeInfo;
	// }

	// @Override
	// public void setPHPExeInfo(PHPExeInfo phpExeInfo) {
	// this.fPhpExeInfo = phpExeInfo;
	//
	// }

	@Override
	public String getPhpExecutableLocation() {
		return getAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, "");
	}

	@Override
	public String getPhpIniLocation() {
		return getAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, "");
	}

}
