package org.powerbot.game.bot.event.impl;

import java.util.logging.Logger;

import org.powerbot.game.bot.event.MessageEvent;
import org.powerbot.game.bot.event.listener.MessageListener;

public class MessageLogger implements MessageListener {
	private static final Logger log = Logger.getLogger("Messages");

	public void messageReceived(final MessageEvent e) {
		if (e.getSender().equals("")) {
			log.info("[" + e.getId() + "] " + e.getMessage());
		} else {
			log.info("[" + e.getId() + "] " + e.getSender() + ": " + e.getMessage());
		}
	}
}
