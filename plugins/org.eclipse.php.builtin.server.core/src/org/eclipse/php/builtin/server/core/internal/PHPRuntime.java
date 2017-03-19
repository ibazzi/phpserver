package org.eclipse.php.builtin.server.core.internal;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.PHPExeException;
import org.eclipse.php.internal.debug.core.PHPExeUtil;
import org.eclipse.php.internal.debug.core.PHPExeUtil.PHPExeInfo;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

@SuppressWarnings("restriction")
public class PHPRuntime extends RuntimeDelegate implements IPHPRuntimeWorkingCopy {

	public PHPRuntime() {
	}

	public IStatus validate() {
		IStatus status = super.validate();
		if (!status.isOK())
			return status;

		String id = getRuntime().getRuntimeType().getId();

		File f = getRuntime().getLocation().toFile();
		if (!f.canRead())
			return new Status(IStatus.WARNING, PHPServerPlugin.PLUGIN_ID, 0, Messages.warningCantReadConfig, null);
		File[] files = f.listFiles();
		boolean isExeFound = false;
		boolean isIniFound = false;
		String executableFile = null;
		if (files != null) {
			int size = files.length;
			for (int i = 0; i < size; i++) {
				File file = files[i];
				String fileName = file.getName();
				if (fileName.equalsIgnoreCase("php.exe") && file.canExecute()) {
					executableFile = file.getAbsolutePath();
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
		PHPExeInfo exeinfo = null;
		try {
			exeinfo = PHPExeUtil.getPHPInfo(new File(executableFile), false);
		} catch (PHPExeException e) {
			return new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0, e.getMessage(), null);
		}
		if (!exeinfo.getSapiType().equals("CLI")) {
			return new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
					"Only the CLI SAPI provides a built-in web server", null);
		}

		String[] splitVersion = exeinfo.getVersion().split("\\.", 3);
		String mainVersion = splitVersion[0] + splitVersion[1];
		if (!id.endsWith(mainVersion)) {
			return new Status(IStatus.ERROR, PHPServerPlugin.PLUGIN_ID, 0,
					"Selected PHP runtime does not match the target version", null);
		}

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