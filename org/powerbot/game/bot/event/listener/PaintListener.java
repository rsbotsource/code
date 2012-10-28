package org.powerbot.game.bot.event.listener;

import java.awt.Graphics;
import java.util.EventListener;

/**
 * A listener that represents a class object that listens for paint events.
 *
 * @author Timer
 */
public interface PaintListener extends EventListener {
	public void onRepaint(Graphics render);
}
