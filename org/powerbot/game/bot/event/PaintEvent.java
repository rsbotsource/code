package org.powerbot.game.bot.event;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.EventListener;

import org.powerbot.event.EventDispatcher;
import org.powerbot.event.GeneralEvent;
import org.powerbot.game.bot.event.listener.PaintListener;

/**
 * An event that is dispatched when the game requests the graphic buffer.
 *
 * @author Timer
 */
public class PaintEvent extends GeneralEvent {
	private static final long serialVersionUID = 1L;
	public Graphics graphics;

	public PaintEvent() {
		setType(EventDispatcher.PAINT_EVENT);
		this.graphics = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispatch(final EventListener eventListener) {
		if (graphics == null) {
			try {
				((PaintListener) eventListener).onRepaint(null);
			} catch (final Exception ignored) {
			}
			return;
		}
		final Graphics2D graphics2D = (Graphics2D) graphics;

		final Color s_background = graphics2D.getBackground();
		final Shape s_clip = graphics2D.getClip();
		final Color s_color = graphics2D.getColor();
		final Composite s_composite = graphics2D.getComposite();
		final Font s_font = graphics2D.getFont();
		final Paint s_paint = graphics2D.getPaint();
		final RenderingHints s_renderingHints = graphics2D.getRenderingHints();
		final Stroke s_stroke = graphics2D.getStroke();
		final AffineTransform s_transform = graphics2D.getTransform();

		try {
			((PaintListener) eventListener).onRepaint(graphics);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		graphics2D.setBackground(s_background);
		graphics2D.setClip(s_clip);
		graphics2D.setColor(s_color);
		graphics2D.setComposite(s_composite);
		graphics2D.setFont(s_font);
		graphics2D.setPaint(s_paint);
		graphics2D.setRenderingHints(s_renderingHints);
		graphics2D.setStroke(s_stroke);
		graphics2D.setTransform(s_transform);
	}
}
