package org.powerbot.concurrent;

import java.util.concurrent.Future;

/**
 * A container in which is able to perform basic, mandatory invocations in regards to tasks.
 *
 * @author Timer
 */
public interface TaskContainer {
	/**
	 * Submits a task for processing and execution.
	 *
	 * @param task The task to be deployed.
	 * @return The <code>Future</code> of this <code>Task</code>.
	 */
	public Future<?> submit(Task task);

	public <T> Future<T> submit(CallableTask<T> task);

	/**
	 * Determines if this container is currently processing tasks.
	 *
	 * @return <tt>true</tt> if it is active; otherwise <tt>false</tt>.
	 */
	public boolean isActive();


	public boolean isShutdown();

	/**
	 * Determines if this container is locked from use.
	 *
	 * @return <tt>true</tt> if it is locked; otherwise <tt>false</tt>.
	 */
	public boolean isLocked();

	/**
	 * Shuts down this container from accepting any more tasks.
	 */
	public void shutdown();

	/**
	 * Stops this container entirely.
	 */
	public void stop();
}
