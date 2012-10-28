package org.powerbot.concurrent.strategy;

/**
 * An enumeration of different states this <code>ActionDispatcher</code> can be in.
 *
 * @author Timer
 */
public enum DaemonState {
	LISTENING, LOCKED, DESTROYED, PROCESSING
}
