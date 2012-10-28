package org.powerbot.concurrent.strategy;

import java.util.EventListener;
import java.util.concurrent.Future;

import org.powerbot.concurrent.Task;

/**
 * A strategy that is performed when its policy allows so.
 *
 * @author Timer
 */
public class Strategy implements Condition, EventListener {
	boolean lock;
	boolean reset;
	boolean sync;

	Task[] tasks;
	Future<?>[] executingFutures;

	private Condition policy;

	public Strategy() {
		this((Task) null);
		if (this instanceof Task) {
			setTask((Task) this);
		}
	}

	/**
	 * Initializes this <code>Strategy</code> with appropriate information required for processing.
	 *
	 * @param task The task associated with this <code>Strategy</code>.
	 */
	public Strategy(final Task task) {
		this(null, new Task[]{task});
	}

	/**
	 * Initializes this <code>Strategy</code> with appropriate information required for processing.
	 *
	 * @param tasks The tasks associated with this <code>Strategy</code>.
	 */
	public Strategy(final Task[] tasks) {
		this(null, tasks);
	}

	/**
	 * Initializes this <code>Strategy</code> with appropriate information required for processing.
	 *
	 * @param policy The policy associated with this <code>Strategy</code>.
	 * @param task   The task associated with this <code>Strategy</code>.
	 */
	public Strategy(final Condition policy, final Task task) {
		this(policy, new Task[]{task});
	}

	/**
	 * Initializes this <code>Strategy</code> with appropriate information required for processing.
	 *
	 * @param policy The policy associated with this <code>Strategy</code>.
	 * @param tasks  The tasks associated with this <code>Strategy</code>.
	 */
	public Strategy(final Condition policy, final Task[] tasks) {
		this.policy = policy;
		this.tasks = tasks;

		lock = true;
		reset = false;
		sync = true;
		executingFutures = null;
	}

	public boolean validate() {
		if (policy != null) {
			return policy.validate();
		}
		throw new RuntimeException("unable to validate this strategy (missing policy)");
	}

	boolean isIdle() {
		if (executingFutures != null) {
			for (final Future<?> future : executingFutures) {
				if (!future.isDone()) {
					return false;
				}
			}
		}
		return true;
	}

	public void setTasks(final Task[] tasks) {
		this.tasks = tasks;
	}

	public void setTask(final Task task) {
		setTasks(new Task[]{task});
	}

	public void setLock(final boolean lock) {
		this.lock = lock;
	}

	public void setReset(final boolean reset) {
		this.reset = reset;
	}

	public void setSync(final boolean sync) {
		this.sync = sync;
	}
}
