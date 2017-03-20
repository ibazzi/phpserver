package org.eclipse.php.builtin.server.core.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;

@SuppressWarnings("restriction")
public class PHPProjectModuleFactory extends ProjectModuleFactoryDelegate {

	private static final IModule[] EMPTY_MODULE = new IModule[0];

	public PHPProjectModuleFactory() {
	}

	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		return new PHPProjectModule(module.getProject());
	}

	protected IModule[] createModules(IProject project) {
		try {
			IProjectNature nature = project.getNature(PHPNature.ID);
			if (nature == null)
				return EMPTY_MODULE;
		} catch (CoreException e) {
			return EMPTY_MODULE;
		}

		PHPVersion phpVersion = ProjectOptions.getPhpVersion(project);
		String moduleVersion = getModuleVersion(phpVersion);
		if (moduleVersion == null) {
			return EMPTY_MODULE;
		}
		String id = project.getName();
		String name = project.getName();
		IModule module = createModule(id, name, "php.web", moduleVersion, project);
		return new IModule[] { module };

	}

	private String getModuleVersion(PHPVersion phpVersion) {
		switch (phpVersion) {
		case PHP5:
			return "5.0";
		case PHP5_3:
			return "5.3";
		case PHP5_4:
			return "5.4";
		case PHP5_5:
			return "5.5";
		case PHP5_6:
			return "5.6";
		case PHP7_0:
			return "7.0";
		case PHP7_1:
			return "7.1";
		default:
			return null;
		}
	}

}
