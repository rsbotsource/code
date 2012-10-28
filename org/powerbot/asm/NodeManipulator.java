package org.powerbot.asm;

import org.powerbot.game.loader.AdaptException;

/**
 * Represents a container of ClassNodes and basic manipulative methods.
 *
 * @author Timer
 */
public interface NodeManipulator {
	public void adapt() throws AdaptException;

	public byte[] process(String name, byte[] data);
}
