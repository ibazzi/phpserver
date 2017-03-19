package org.eclipse.php.builtin.server.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.PublishTaskDelegate;

@SuppressWarnings("rawtypes")
public class PublishTask extends PublishTaskDelegate {

	public PublishOperation[] getTasks(IServer server, int kind, List modules, List kindList) {
		if (modules == null)
			return null;

		PHPServerBehaviour tomcatServer = (PHPServerBehaviour) server.loadAdapter(PHPServerBehaviour.class, null);

		List<PublishOperation> tasks = new ArrayList<PublishOperation>();
		int size = modules.size();
		for (int i = 0; i < size; i++) {
			IModule[] module = (IModule[]) modules.get(i);
			Integer in = (Integer) kindList.get(i);
			tasks.add(new PublishOperation2(tomcatServer, kind, module, in.intValue()));
		}

		return tasks.toArray(new PublishOperation[tasks.size()]);
	}

}
