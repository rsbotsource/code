package org.powerbot.game.client.input;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import org.powerbot.game.api.util.Time;
import org.powerbot.game.bot.Bot;
import org.powerbot.gui.BotChrome;

public class Canvas extends java.awt.Canvas {
	private static final long serialVersionUID = -2276037172265300477L;

	private static final int GRAPHICS_DELAY = 6;

	private boolean visible, focused, loader_identified;
	private Bot bot;

	@Override
	public final Graphics getGraphics() {
		if (bot == null) {
			if (loader_identified) {
				return super.getGraphics();
			} else {
				bot = Bot.resolve(this);
				BotChrome.getInstance().panel.offset();
				loader_identified = true;
			}
		}
		Time.sleep(GRAPHICS_DELAY);
		return bot.getBufferGraphics();
	}

	@Override
	public final boolean hasFocus() {
		return focused;
	}

	@Override
	public final boolean isValid() {
		return visible;
	}

	@Override
	public final boolean isVisible() {
		return visible;
	}

	@Override
	public final boolean isDisplayable() {
		return true;
	}

	@Override
	public final Dimension getSize() {
		if (bot != null) {
			return bot.appletContainer.getSize();
		}
		return BotChrome.getInstance().panel.getSize();
	}

	@Override
	public final void setVisible(final boolean visible) {
		super.setVisible(visible);
		this.visible = visible;
	}

	public final void setFocused(final boolean focused) {
		if (focused && !this.focused) {
			super.processEvent(new FocusEvent(this, FocusEvent.FOCUS_GAINED, false, null));
		} else if (this.focused) {
			super.processEvent(new FocusEvent(this, FocusEvent.FOCUS_LOST, true, null));
		}
		this.focused = focused;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Image createImage(final int width, final int height) {
		final int[] pixels = new int[height * width];
		final DataBufferInt databufferint = new DataBufferInt(pixels, pixels.length);
		final DirectColorModel directcolormodel = new DirectColorModel(32, 0xff0000, 0xff00, 255);
		final WritableRaster writableraster = Raster.createWritableRaster(directcolormodel.createCompatibleSampleModel(width, height), databufferint, null);
		return new BufferedImage(directcolormodel, writableraster, false, new Hashtable());
	}

	@Override
	protected final void processEvent(final AWTEvent e) {
		if (!(e instanceof FocusEvent)) {
			super.processEvent(e);
		}
	}
}