package org.powerbot.game.bot.event.impl;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.bot.event.listener.PaintListener;

public class DrawNPCs implements PaintListener {
	public void onRepaint(final Graphics render) {
		if (!Game.isLoggedIn()) {
			return;
		}
		final NPC[] npcs = NPCs.getLoaded();
		final FontMetrics metrics = render.getFontMetrics();
		for (final NPC npc : npcs) {
			final Point location = npc.getCentralPoint();
			if (location.x == -1 || location.y == -1) {
				continue;
			}
			render.setColor(Color.red);
			render.fillRect((int) location.getX() - 1, (int) location.getY() - 1, 2, 2);
			String s = npc.getName() + " (" + npc.getLevel() + ") - " + npc.getId();
			render.setColor(npc.isInCombat() ? Color.RED : npc.isMoving() ? Color.GREEN : Color.WHITE);
			render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() / 2);
			final String msg = npc.getMessage();
			boolean raised = false;
			if (npc.getAnimation() != -1 || npc.getPassiveAnimation() != -1) {
				s = "";
				s += "(";
				if (npc.getAnimation() != -1 || npc.getPassiveAnimation() > 0) {
					s += "A: " + npc.getAnimation() + " | ST: " + npc.getPassiveAnimation() + " | ";
				}

				s = s.substring(0, s.lastIndexOf(" | "));
				s += ")";

				render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() * 3 / 2);
				raised = true;
			}
			if (msg != null) {
				render.setColor(Color.ORANGE);
				render.drawString(msg, location.x - metrics.stringWidth(msg) / 2, location.y - metrics.getHeight() * (raised ? 5 : 3) / 2);
			}
		}
	}
}