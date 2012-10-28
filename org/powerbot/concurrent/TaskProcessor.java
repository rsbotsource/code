package org.powerbot.concurrent;

import java.util.concurrent.Future;

/**
 * A simple container capable of deploying tasks within a queued thread environment.
 *
 * @author Timer
 */
public class TaskProcessor implements TaskContainer {
	private final TaskExecutor executor;

	public TaskProcessor(final ThreadGroup threadGroup) {
		executor = new TaskExecutor(new ThreadPool(threadGroup));
	}

	/**
	 * {@inheritDoc}
	 */
	public Future<?> submit(final Task task) {
		return executor.submit(task);
	}

	public <T> Future<T> submit(final CallableTask<T> task) {
		return executor.submit(task);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isActive() {
		return executor.getActiveCount() > 0;
	}

	public boolean isShutdown() {
		return executor.isShutdown();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLocked() {
		return executor.isShutdown() && executor.isTerminated();
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutdown() {
		executor.shutdown();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		executor.shutdownNow();
	}
}
