package org.powerbot.game.bot.event.impl;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.event.listener.PaintListener;

public class DrawInventory implements PaintListener {
	public void onRepaint(final Graphics render) {
		if (!Game.isLoggedIn() || Tabs.getCurrent() != Tabs.INVENTORY) {
			return;
		}
		render.setColor(Color.green);
		final FontMetrics fontMetrics = render.getFontMetrics();
		final Item[] items = Inventory.getItems();
		if (items != null) {
			for (final Item item : items) {
				if (item != null) {
					final WidgetChild child = item.getWidgetChild();
					if (child != null && child.validate()) {
						final Point center = child.getAbsoluteLocation();
						final String id = item.getId() + "";
						render.drawString(id, center.x, center.y + fontMetrics.getHeight());
					}
				}
			}
		}
	}
}
