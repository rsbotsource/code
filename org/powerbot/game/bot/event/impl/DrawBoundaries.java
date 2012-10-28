package org.powerbot.game.bot.event.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.bot.event.listener.PaintListener;

public class DrawBoundaries implements PaintListener {
	private final Point[][] minimapPoints = new Point[105][105];
	private final Point[][] screenPoints = new Point[105][105];

	public void onRepaint(final Graphics render) {
		if (!Game.isLoggedIn()) {
			return;
		}
		final int plane = Game.getPlane();
		final int[][] blocks = Walking.getCollisionFlags(plane);
		final int baseX = Game.getBaseX();
		final int baseY = Game.getBaseY();
		for (int i = 0; i < screenPoints.length; i++) {
			for (int j = 0; j < screenPoints[i].length; j++) {
				final int x = i + baseX - 1;
				final int y = j + baseY - 1;
				Point mini = Calculations.worldToMap(x - 0.5, y - 0.5);
				if (mini.x == -1 || mini.y == -1) {
					mini = null;
				}
				minimapPoints[i][j] = mini;
				Point screen = new Tile(x, y, plane).getPoint(0, 0, 0);
				if (screen.x == -1 || screen.y == -1) {
					screen = null;
				}
				screenPoints[i][j] = screen;
			}
		}

		render.setColor(Color.YELLOW);
		for (int i = 1; i < 104; i++) {
			for (int j = 1; j < 104; j++) {
				final int curBlock = blocks[i][j];
				final Point miniBL = minimapPoints[i][j];
				final Point miniBR = minimapPoints[i][j + 1];
				final Point miniTL = minimapPoints[i + 1][j];
				final Point miniTR = minimapPoints[i + 1][j + 1];
				final Point bl = screenPoints[i][j];
				final Point br = screenPoints[i][j + 1];
				final Point tl = screenPoints[i + 1][j];
				final Point tr = screenPoints[i + 1][j + 1];
				if ((curBlock & 0x1280100) != 0) {
					render.setColor(Color.black);
					if (tl != null && br != null && tr != null && bl != null) {
						render.fillPolygon(new int[]{bl.x, br.x, tr.x, tl.x}, new int[]{bl.y, br.y, tr.y, tl.y}, 4);
					}
					if (miniBL != null && miniBR != null && miniTR != null && miniTL != null) {
						render.fillPolygon(new int[]{miniBL.x, miniBR.x, miniTR.x, miniTL.x},
								new int[]{miniBL.y, miniBR.y, miniTR.y, miniTL.y}, 4);
					}
				}
				if ((blocks[i][j - 1] & 0x1280102) != 0 || (curBlock & 0x1280120) != 0) {
					render.setColor(Color.red);
					if (tl != null && bl != null) {
						render.drawLine(bl.x, bl.y, tl.x, tl.y);
					}
					if (miniBL != null && miniTL != null) {
						render.drawLine(miniBL.x, miniBL.y, miniTL.x, miniTL.y);
					}
				}
				if ((blocks[i - 1][j] & 0x1280108) != 0 || (curBlock & 0x1280180) != 0) {
					render.setColor(Color.red);
					if (br != null && bl != null) {
						render.drawLine(bl.x, bl.y, br.x, br.y);
					}
					if (miniBR != null && miniBL != null) {
						render.drawLine(miniBL.x, miniBL.y, miniBR.x, miniBR.y);
					}
				}

				render.setColor(Color.cyan);
				if ((curBlock & 0x100000) != 0) {
					if (miniBL != null && miniBR != null && miniTR != null && miniTL != null) {
						render.fillPolygon(new int[]{miniBL.x, miniBR.x, miniTR.x, miniTL.x},
								new int[]{miniBL.y, miniBR.y, miniTR.y, miniTL.y}, 4);
					}
					if (tl != null && br != null && tr != null && bl != null) {
						render.fillPolygon(new int[]{bl.x, br.x, tr.x, tl.x},
								new int[]{bl.y, br.y, tr.y, tl.y}, 4);
					}
				}
			}
		}
		final Tile pos = Players.getLocal().getLocation();
		final Point mini = Calculations.worldToMap(pos.getX(), pos.getY());
		render.setColor(Color.red);
		render.fillRect((int) mini.getX() - 1, (int) mini.getY() - 1, 2, 2);
	}
}
