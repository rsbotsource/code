package org.powerbot.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread factory to maintain scripts within their bot (for context) and security.
 *
 * @author Timer
 */
public class ThreadPool implements ThreadFactory {
	public static final String THREADGROUPNAMEPREFIX = "ThreadPool-";

	private final AtomicInteger threadNumber;
	private final ThreadGroup threadGroup;

	public ThreadPool(final ThreadGroup threadGroup) {
		threadNumber = new AtomicInteger(1);
		this.threadGroup = threadGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	public Thread newThread(final Runnable r) {
		final Thread current = Thread.currentThread();
		final StringBuilder builder = new StringBuilder(THREADGROUPNAMEPREFIX);
		builder.append(hashCode()).append("@").append(current.getName()).append("/").append(current.getThreadGroup().toString());
		builder.append("#").append(threadNumber.getAndIncrement());
		return new Thread(threadGroup, r, builder.toString());
	}
}