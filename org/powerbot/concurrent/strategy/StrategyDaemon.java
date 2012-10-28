package org.powerbot.concurrent.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.TaskContainer;
import org.powerbot.game.api.util.Time;
import org.powerbot.util.Configuration;

/**
 * A strategy daemon capable of dispatching strategies when activated within the a concurrent environment.
 *
 * @author Timer
 */
public class StrategyDaemon implements StrategyContainer, Task {
	private final TaskContainer container;
	private final TaskContainer owner;
	private final List<Strategy> strategies;
	public DaemonState state;
	private int iterationSleep = 200;

	/**
	 * Initializes this strategy daemon with appropriate objects.
	 *
	 * @param container The <code>TaskContainer</code> to use as a medium for processing.
	 * @param owner     The <code>TaskContainer</code> that owns this executor.
	 */
	public StrategyDaemon(final TaskContainer container, final TaskContainer owner) {
		this.container = container;
		this.owner = owner;
		strategies = Collections.synchronizedList(new ArrayList<Strategy>());
		state = DaemonState.DESTROYED;
	}

	/**
	 * {@inheritDoc}
	 */
	public void listen() {
		if (state != DaemonState.LISTENING) {
			final DaemonState previous = state;
			state = DaemonState.LISTENING;
			if (previous == DaemonState.LOCKED) {
				synchronized (this) {
					notify();
				}
				return;
			}
			owner.submit(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void lock() {
		state = DaemonState.LOCKED;
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		state = DaemonState.DESTROYED;
		synchronized (this) {
			notify();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void append(final Strategy strategy) {
		if (!strategies.contains(strategy)) {
			strategies.add(strategy);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void omit(final Strategy strategy) {
		strategies.remove(strategy);
	}

	/**
	 * Handles the dispatching of strategies within the given container.
	 */
	public void run() {
		final List<Future<?>> futures = Collections.synchronizedList(new ArrayList<Future<?>>());
		DaemonState cached_state;
		while (state != DaemonState.DESTROYED) {
			if (state == DaemonState.LOCKED) {
				synchronized (this) {
					try {
						wait();
					} catch (final InterruptedException ignored) {
					}
				}
			} else if (state == DaemonState.LISTENING) {
				try {
					final List<Strategy> strategies_clone = new ArrayList<Strategy>();
					strategies_clone.addAll(strategies);
					for (final Strategy strategy : strategies_clone) {
						if (state != DaemonState.LISTENING) {
							break;
						}
						if (strategy.tasks == null || !strategy.validate() ||
								(strategy.sync && !strategy.isIdle())) {
							continue;
						}
						for (final Task task : strategy.tasks) {
							try {
								final Future<?> future = container.submit(task);
								if (future != null) {
									futures.add(future);
								}
							} catch (final RejectedExecutionException ignored) {
								state = DaemonState.DESTROYED;
								break;
							}
						}
						cached_state = state;
						strategy.executingFutures = futures.toArray(new Future<?>[futures.size()]);
						if (strategy.lock) {
							container.submit(createFutureDisposer(futures, this));
							awaitNotify(futures);
							if (state == DaemonState.PROCESSING) {
								state = cached_state;
							}
						}
						futures.clear();
						if (strategy.reset) {
							break;
						}
					}
					Time.sleep(iterationSleep);
				} catch (final Throwable t) {
					if (Configuration.DEVMODE) {
						t.printStackTrace();
					}
				}
			} else {
				throw new RuntimeException("bad daemon-dispatch state");
			}
		}
	}

	public void setIterationSleep(final int milliseconds) {
		iterationSleep = milliseconds;
	}

	private void awaitNotify(final List<Future<?>> futures) {
		synchronized (this) {
			state = DaemonState.PROCESSING;

			if (futures.size() > 0) {
				try {
					wait();
				} catch (final InterruptedException ignored) {
				}
			}
		}
	}

	/**
	 * Creates a <code>RunnableTask</code> that notifies a thread to awaken when all futures are completed.
	 *
	 * @param lockingFutures The <code>List</code> of Futures to wait for.
	 * @param threadObject   The locking object to notify.
	 * @return The <code>RunnableTask</code> to be submitted.
	 */

	private Task createFutureDisposer(final List<Future<?>> lockingFutures, final Object threadObject) {
		return new Task() {
			public void run() {
				while (lockingFutures.size() > 0) {
					final Future<?> future = lockingFutures.get(0);
					try {
						future.get();
					} catch (final InterruptedException ignored) {
					} catch (final ExecutionException ignored) {
					}

					if (future.isDone()) {
						lockingFutures.remove(0);
					}
				}
				synchronized (threadObject) {
					threadObject.notify();
				}
			}
		};
	}

}
