package org.eclipse.php.builtin.server.core.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.php.internal.core.project.PHPNature;
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

		String id = project.getName();
		String name = project.getName();
		IModule module = createModule(id, name, "php.web", "1.0", project);
		if (module == null)
			return EMPTY_MODULE;

		return new IModule[] { module };

	}

}
