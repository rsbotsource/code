package org.powerbot.game.bot.event.impl;

import java.awt.Color;
import java.awt.Graphics;

import org.powerbot.game.bot.Bot;
import org.powerbot.game.bot.event.listener.PaintListener;
import org.powerbot.game.client.input.Mouse;

public class DrawMouse implements PaintListener {
	private final Bot bot;

	public DrawMouse(final Bot bot) {
		this.bot = bot;
	}

	public void onRepaint(final Graphics render) {
		if (bot.getClient() == null) {
			return;
		}
		final Mouse mouse = bot.getClient().getMouse();
		if (mouse != null) {
			final int mouse_x = mouse.getX(), mouse_y = mouse.getY();
			final int mouse_press_x = mouse.getPressX(), mouse_press_y = mouse.getPressY();
			final long mouse_press_time = mouse.getPressTime();
			render.setColor(Color.YELLOW.darker());
			render.drawLine(mouse_x - 5, mouse_y - 5, mouse_x + 5, mouse_y + 5);
			render.drawLine(mouse_x + 5, mouse_y - 5, mouse_x - 5, mouse_y + 5);
			if (System.currentTimeMillis() - mouse_press_time < 1000) {
				render.setColor(Color.RED);
				render.drawLine(mouse_press_x - 5, mouse_press_y - 5, mouse_press_x + 5, mouse_press_y + 5);
				render.drawLine(mouse_press_x + 5, mouse_press_y - 5, mouse_press_x - 5, mouse_press_y + 5);
			}
		}
	}
}