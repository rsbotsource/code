package org.powerbot.concurrent;

import java.util.concurrent.Future;

public interface Processor {
	/**
	 * Submits a task for processing and execution.
	 *
	 * @param task The task to be deployed.
	 * @return The <code>Future</code> of this <code>Task</code>.
	 */
	public Future<?> submit(Task task);

	public void terminated(Task task);
}
