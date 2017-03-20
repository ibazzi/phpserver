package org.eclipse.php.builtin.server.core.internal.debugger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;
import org.eclipse.php.internal.debug.core.zend.model.PHPMultiDebugTarget;

@SuppressWarnings("restriction")
public class PHPServerDebugTarget extends PHPMultiDebugTarget {

	public PHPServerDebugTarget(ILaunch launch, IProcess process) throws CoreException {
		super(launch, process);
	}

	@Override
	public String getName() throws DebugException {
		return "sssss";
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			Object source = event.getSource();
			if (event.getKind() == DebugEvent.TERMINATE) {
				if (source instanceof PHPDebugTarget) {
					PHPDebugTarget target = (PHPDebugTarget) source;
					if (fDebugTargets.contains(target)) {
						try {
							if (!target.isTerminated()) {
								target.terminate();
							}
						} catch (DebugException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

}
