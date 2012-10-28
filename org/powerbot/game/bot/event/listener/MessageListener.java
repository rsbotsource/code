package org.powerbot.game.bot.event.listener;

import java.util.EventListener;

import org.powerbot.game.bot.event.MessageEvent;

/**
 * An interface that represents a class object that listens for messages.
 *
 * @author Timer
 */
public interface MessageListener extends EventListener {
	public void messageReceived(final MessageEvent e);
}
