package org.powerbot.game.bot.event.impl;

import java.awt.Graphics;

import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.bot.event.listener.TextPaintListener;
import org.powerbot.util.StringUtil;

public class TDestination implements TextPaintListener {
	public int draw(int idx, final Graphics render) {
		StringUtil.drawLine(render, idx++, new StringBuilder("Destination: ").append(Walking.getDestination().toString()).toString());
		return idx;
	}
}
