package org.powerbot.game.bot.event.impl;

import java.awt.Graphics;

import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.bot.event.listener.TextPaintListener;
import org.powerbot.util.StringUtil;

public class TMapBase implements TextPaintListener {
	public int draw(int idx, final Graphics render) {
		StringUtil.drawLine(render, idx++, new StringBuilder("Map base: ").append(new Tile(Game.getBaseX(), Game.getBaseY(), -1).toString()).toString());
		return idx;
	}
}
