package org.powerbot.gui.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.powerbot.gui.BotChrome;
import org.powerbot.util.io.Resources;

/**
 * @author Paris
 */
public final class BotSocialPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public BotSocialPanel() {
		super();
		setBackground(Color.BLACK);

		final int d = 40;
		add(new ImageButton(Resources.Paths.TWITTER, "twitter", "@rsbotorg"));
		add(Box.createHorizontalStrut(d));
		add(new ImageButton(Resources.Paths.FACEBOOK, "facebook", "powerbot"));
		add(Box.createHorizontalStrut(d));
		add(new ImageButton(Resources.Paths.YOUTUBE, "youtube", "OfficialPowerbot"));
	}

	private class ImageButton extends JPanel {
		private static final long serialVersionUID = 1L;

		public ImageButton(final String icon, final String url, final String text) {
			super();
			setBackground(Color.BLACK);

			final JLabel label = new JLabel(new ImageIcon(Resources.getImage(icon)));
			label.setToolTipText(text);
			label.setBackground(getBackground());

			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent arg0) {
					BotChrome.openURL(Resources.getServerLinks().get(url));
				}
			});

			add(label);
		}
	}
}
