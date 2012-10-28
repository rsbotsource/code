package org.powerbot.game.bot.event.impl;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.GroundItems;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.GroundItem;
import org.powerbot.game.api.wrappers.node.ItemDefinition;
import org.powerbot.game.bot.event.listener.PaintListener;

public class DrawGroundItems implements PaintListener {
	public void onRepaint(final Graphics render) {
		try {
			if (!Game.isLoggedIn()) {
				return;
			}
			final Player player = Players.getLocal();
			if (player == null) {
				return;
			}
			final Tile location = player.getLocation();
			final FontMetrics metrics = render.getFontMetrics();
			final int tHeight = metrics.getHeight();
			final int lX = location.getX(), lY = location.getY();
			for (int x = lX - 10; x < lX + 10; x++) {
				for (int y = lY - 10; y < lY + 10; y++) {
					final GroundItem[] groundItems = GroundItems.getLoadedAt(x, y);
					int i = 0;
					for (final GroundItem groundItem : groundItems) {
						final Tile itemLocation = groundItem.getLocation();
						final Point screen = itemLocation.getCentralPoint();
						if (screen.x == -1 || screen.y == -1) {
							continue;
						}
						render.setColor(Color.red);
						render.fillRect((int) screen.getX() - 1, (int) screen.getY() - 1, 2, 2);
						final ItemDefinition itemDefinition = groundItem.getGroundItem().getDefinition();
						final StringBuilder sB = new StringBuilder();
						String name;
						if (itemDefinition != null && (name = itemDefinition.getName()) != null) {
							sB.append(name);
							sB.append(' ');
						}
						sB.append(groundItem.getGroundItem().getId());
						final int ss = groundItem.getGroundItem().getStackSize();
						if (ss > 1) {
							sB.append(" (").append(ss).append(')');
						}
						final String s = sB.toString();
						final int ty = screen.y - tHeight * (++i) + tHeight / 2;
						final int tx = screen.x - metrics.stringWidth(s) / 2;
						render.setColor(Color.green);
						render.drawString(s, tx, ty);
					}
				}
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}
}
