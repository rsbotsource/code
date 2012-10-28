package org.powerbot.game.bot;

import org.powerbot.game.bot.event.MessageEvent;
import org.powerbot.game.client.Callback;
import org.powerbot.game.client.Render;

/**
 * An implementation of callback responsible for processing client callbacks to appropriate bot functions.
 *
 * @author Timer
 */
public class CallbackImpl implements Callback {
	private final Bot bot;

	public CallbackImpl(final Bot bot) {
		this.bot = bot;
	}

	/**
	 * Updates this bot's render information used to calculate screen vectors.
	 *
	 * @param render The render provided from the game.
	 */
	public void updateRenderInfo(final Render render) {
		bot.updateToolkit(render);
	}

	/**
	 * Notifies the bot of of a message dispatched in the game.
	 *
	 * @param id      The Id of the message dispatched.
	 * @param sender  The name of the sender of this message.
	 * @param message The message contents the sender sent.
	 */
	public void notifyMessage(final int id, final String sender, final String message) {
		bot.getEventDispatcher().dispatch(new MessageEvent(id, sender, message));
	}
}
