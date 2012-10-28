package org.powerbot.concurrent.strategy;

/**
 * A strategy container that is able to perform basic operations to a thread dispatching strategies based on policies.
 *
 * @author Timer
 */
public interface StrategyContainer {
	/**
	 * Begins listening to the policies associated with this <code>StrategyContainer</code>.
	 */
	public void listen();

	/**
	 * Locks this container from processing and dispatching of strategies.
	 */
	public void lock();

	/**
	 * Destroys this <code>StrategyContainer</code> and cleans up.
	 */
	public void destroy();

	/**
	 * Begins listening on a policy for appropriate dispatching.
	 *
	 * @param strategy The <code>Strategy</code> to handle.
	 */
	public void append(Strategy strategy);

	/**
	 * Terminates listening and dispatch of the specified <code>Strategy</code>.
	 *
	 * @param strategy The <code>Strategy</code> to lose handle of.
	 */
	public void omit(Strategy strategy);
}
