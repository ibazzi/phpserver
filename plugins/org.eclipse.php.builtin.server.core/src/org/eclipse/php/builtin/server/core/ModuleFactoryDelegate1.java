package org.eclipse.php.builtin.server.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;

public class ModuleFactoryDelegate1 extends ProjectModuleFactoryDelegate {

	public ModuleFactoryDelegate1() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		// TODO Auto-generated method stub
		return null;
	}

	protected IModule[] createModules(IProject project) {
		try {
			IProjectNature nature = project.getNature(PHPNature.ID);
			if (nature == null)
				return new IModule[0];
		} catch (CoreException e) {
			e.printStackTrace();
		}

		String id = project.getName();
		String name = project.getName();
		IModule module = createModule(id, name, "pdt.web", "1.0", project);
		if (module == null)
			return new IModule[0];

		return new IModule[] { module };

	}

}
