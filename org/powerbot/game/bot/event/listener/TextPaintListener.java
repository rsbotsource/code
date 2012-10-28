package org.powerbot.game.bot.event.listener;

import java.awt.Graphics;
import java.util.EventListener;

/**
 * @author Timer
 */
public interface TextPaintListener extends EventListener {
	public int draw(int idx, Graphics render);
}
