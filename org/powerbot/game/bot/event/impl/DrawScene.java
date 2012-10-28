package org.powerbot.game.bot.event.impl;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;

import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.bot.event.listener.PaintListener;

public class DrawScene implements PaintListener {
	private static final HashMap<Integer, Color> color_map = new HashMap<Integer, Color>();

	static {
		color_map.put(SceneEntities.TYPE_BOUNDARY, Color.BLACK);
		color_map.put(SceneEntities.TYPE_FLOOR_DECORATION, Color.YELLOW);
		color_map.put(SceneEntities.TYPE_INTERACTIVE, Color.WHITE);
		color_map.put(SceneEntities.TYPE_WALL_DECORATION, Color.GRAY);
	}

	public void onRepaint(final Graphics render) {
		if (!Game.isLoggedIn()) {
			return;
		}
		final Player player = Players.getLocal();
		if (player == null) {
			return;
		}
		final FontMetrics metrics = render.getFontMetrics();
		final Tile position = player.getLocation();
		final int textHeight = metrics.getHeight();
		for (int x = position.getX() - 25; x < position.getX() + 25; x++) {
			for (int y = position.getY() - 25; y < position.getY() + 25; y++) {
				final Tile accessPosition = new Tile(x, y, Game.getPlane());
				final Point accessPoint = accessPosition.getCentralPoint();
				if (!Calculations.isOnScreen(accessPoint)) {
					continue;
				}
				final SceneObject[] locations = SceneEntities.getLoaded(accessPosition);
				int i = 0;
				for (final SceneObject location : locations) {
					final Point locationPoint = location.getLocation().getCentralPoint();
					if (!Calculations.isOnScreen(locationPoint)) {
						continue;
					}
					if (accessPoint.x > -1) {
						render.setColor(Color.GREEN);
						render.fillRect(accessPoint.x - 1, accessPoint.y - 1, 2, 2);
						render.setColor(Color.RED);
						render.drawLine(accessPoint.x, accessPoint.y, locationPoint.x, locationPoint.y);
					}
					final String s = "" + location.getId();
					final int ty = locationPoint.y - textHeight / 2 - i++ * 15;
					final int tx = locationPoint.x - metrics.stringWidth(s) / 2;
					render.setColor(color_map.get(location.getType()));
					render.drawString(s, tx, ty);
				}
			}
		}
	}
}