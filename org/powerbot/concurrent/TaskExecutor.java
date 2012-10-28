package org.powerbot.concurrent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;

public class TaskExecutor {
	private ThreadFactory factory;
	private boolean shutdown;
	private boolean terminated;
	private final List<Thread> threads;

	public TaskExecutor(final ThreadFactory factory) {
		this.factory = factory;
		this.shutdown = false;
		this.terminated = false;
		this.threads = Collections.synchronizedList(new LinkedList<Thread>());
	}

	public Future<?> submit(final Task task) {
		if (task == null) {
			throw new NullPointerException();
		}
		final RunnableFuture<Object> ftask = newTaskFor(task, null);
		execute(ftask);
		return ftask;
	}

	public <T> Future<T> submit(final CallableTask<T> task) {
		if (task == null) {
			throw new NullPointerException();
		}
		final RunnableFuture<T> ftask = newTaskFor(task);
		execute(ftask);
		return ftask;
	}

	public int getActiveCount() {
		return threads.size();
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void shutdown() {
		shutdown = true;
	}

	public void shutdownNow() {
		terminated = true;

		while (threads.size() > 0) {
			final Thread thread = threads.remove(0);
			thread.interrupt();
		}
	}

	private <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
		return new FutureTask<T>(runnable, value);
	}

	private <T> RunnableFuture<T> newTaskFor(final Callable<T> callable) {
		return new FutureTask<T>(callable);
	}

	private void execute(final Runnable runnable) {
		if (shutdown) {
			throw new RejectedExecutionException();
		}
		final Thread thread = factory.newThread(new Runnable() {
			@Override
			public void run() {
				final Thread thread = Thread.currentThread();
				synchronized (threads) {
					threads.add(thread);
				}
				try {
					runnable.run();
				} finally {
					synchronized (threads) {
						threads.remove(thread);
						if (threads.size() == 0 && shutdown) {
							terminated = true;
						}
					}
				}
			}
		});
		thread.start();
	}
}
