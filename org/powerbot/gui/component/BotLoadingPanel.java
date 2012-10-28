package org.powerbot.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.powerbot.game.GameDefinition;
import org.powerbot.game.bot.Bot;
import org.powerbot.gui.BotChrome;
import org.powerbot.util.Configuration;
import org.powerbot.util.io.CryptFile;
import org.powerbot.util.io.Resources;

public final class BotLoadingPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public final JLabel status, info;
	private static final Map<ThreadGroup, LogRecord> logRecord = new HashMap<ThreadGroup, LogRecord>();
	private ThreadGroup listeningGroup = null;
	private final JPanel panelTop, panelBottom;
	private final int PANEL_WIDTH = 728, PANEL_HEIGHT = 120;
	private volatile DisplayAd ad;
	private final BotLoadingPanelLogHandler handler;

	public BotLoadingPanel(final Component parent) {
		setBackground(Color.BLACK);
		setLayout(new GridLayout(0, 1));

		final JPanel panel = new JPanel(new BorderLayout()), panelText = new JPanel(new GridLayout(0, 1)), panelTitle = new JPanel(new GridLayout(1, 0));
		panel.setBackground(getBackground());
		panelText.setBackground(getBackground());
		panelTitle.setBackground(getBackground());
		panelText.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));

		panelTop = new JPanel();
		panelTop.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		panelTop.setBackground(panel.getBackground());
		panelBottom = new BotSocialPanel();
		panelBottom.setBorder(new EmptyBorder(PANEL_HEIGHT - panelBottom.getPreferredSize().height, 0, 0, 0));
		panelBottom.setPreferredSize(panelTop.getPreferredSize());
		panelBottom.setBackground(panel.getBackground());

		final JLabel logo = new JLabel(new ImageIcon(Resources.getImage(Resources.Paths.ARROWS)));
		logo.setHorizontalAlignment(SwingConstants.RIGHT);
		panelTitle.add(logo);

		status = new JLabel();
		status.setHorizontalAlignment(SwingConstants.LEFT);
		status.setFont(status.getFont().deriveFont(Font.BOLD, 24));
		status.setForeground(Color.WHITE);
		status.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		panelTitle.add(status);

		panelText.add(panelTitle);

		info = new JLabel();
		info.setHorizontalAlignment(SwingConstants.CENTER);
		info.setFont(info.getFont().deriveFont(0, 14));
		panelText.add(info);

		panel.add(panelTop, BorderLayout.NORTH);
		panel.add(panelText);
		panel.add(panelBottom, BorderLayout.SOUTH);
		add(panel);

		handler = new BotLoadingPanelLogHandler(this);
		Logger.getLogger("").addHandler(handler);
	}

	public synchronized void setAdVisible(final boolean visible) {
		if (ad == null) {
			if (!visible) {
				return;
			}
			ad = new DisplayAd();
			final int delay = 100;
			final Timer t = new Timer(delay, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final Timer t = (Timer) e.getSource();
					if (!BotChrome.loaded) {
						return;
					}
					t.stop();
					ad.run();
					setAdVisible(visible);
				}
			});
			t.setCoalesce(false);
			t.start();
			return;
		}
		final Runnable act = new Runnable() {
			@Override
			public void run() {
				if (ad == null || ad.getAd() == null) {
					return;
				}
				if (visible) {
					final Dimension d1 = panelTop.getPreferredSize(), d2 = ad.getAd().getPreferredSize();
					final int dw = (d1.width - d2.width) / 2, dh = d1.height - d2.height;
					panelTop.setBorder(BorderFactory.createEmptyBorder(0, dw, dh, dw));
					panelTop.add(ad.getAd());
				} else {
					if (panelTop.getComponentCount() != 0) {
						panelTop.remove(0);
					}
				}
				validate();
				repaint();
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			act.run();
		} else {
			SwingUtilities.invokeLater(act);
		}
	}

	private final class DisplayAd implements Runnable {
		private JLabel ad;

		public JLabel getAd() {
			return ad;
		}

		public void run() {
			try {
				if (Resources.getServerData().containsKey("ads")) {
					final CryptFile cf = new CryptFile("ads/image.png");
					final String src = Resources.getServerData().get("ads").get("image"), link = Resources.getServerData().get("ads").get("link");
					BufferedImage image = ImageIO.read(cf.download(new URL(src)));
					if (image.getWidth() > PANEL_WIDTH || image.getHeight() > PANEL_HEIGHT) {
						final float factor = (float) Math.min((double) PANEL_WIDTH / image.getWidth(), (double) PANEL_HEIGHT / image.getHeight());
						final BufferedImage resized = new BufferedImage((int) (image.getWidth() * factor), (int) (image.getHeight() * factor), BufferedImage.TYPE_INT_ARGB);
						final Graphics2D g = resized.createGraphics();
						g.drawImage(image, 0, 0, resized.getWidth(), resized.getHeight(), null);
						g.dispose();
						image = resized;
					}
					ad = new JLabel(new ImageIcon(image));
					ad.setBorder(null);
					ad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					ad.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(final MouseEvent e) {
							BotChrome.openURL(link);
						}
					});
				}
			} catch (final IOException ignored) {
			}
		}
	}

	public void set(final ThreadGroup threadGroup) {
		this.listeningGroup = threadGroup;
		final LogRecord record;
		if (threadGroup != null && (record = logRecord.get(threadGroup)) != null) {
			handler.publish(record);
		}
	}

	private final class BotLoadingPanelLogHandler extends Handler {
		private final BotLoadingPanel panel;

		public BotLoadingPanelLogHandler(final BotLoadingPanel panel) {
			this.panel = panel;
		}

		@Override
		public void close() throws SecurityException {
		}

		@Override
		public void flush() {
		}

		@Override
		public void publish(final LogRecord record) {
			String src = record.getLoggerName();
			if (src == null || src.isEmpty()) {
				return;
			}
			final int x = src.indexOf('$');
			if (x > 0) {
				src = src.substring(0, x);
			}
			if (!(src.equals(BotChrome.class.getName()) || src.equals(GameDefinition.class.getName()) || src.equals(Bot.class.getName()))) {
				return;
			}
			final ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
			if (listeningGroup == null && currentGroup.getName().startsWith(GameDefinition.class.getName())) {
				return;
			}
			logRecord.put(currentGroup, record);
			if (listeningGroup != null && currentGroup != listeningGroup) {
				return;
			}
			Color c = new Color(200, 200, 200);
			final String title = record.getParameters() != null && record.getParameters().length == 1 ? (String) record.getParameters()[0] : null;
			if (record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING) {
				if (title == null) {
					panel.status.setText("Unavailable");
				}
				c = new Color(255, 87, 71);
			} else if (record.getLevel() == Level.INFO) {
				if (title == null) {
					panel.status.setText("Loading...");
				}
			}
			if (title != null) {
				panel.status.setText(title);
			}
			panel.info.setForeground(c);
			panel.info.setText(record.getMessage());

			if (title != null && title.equals("Outdated")) {
				final String msg = Configuration.NAME + " needs to be repaired after a recent game update.\nThis usually takes 1-5 days so please wait patiently.\n\n" +
						"You do not need to do anything, an update will be automatically downloaded for you.\n" +
						"All your scripts will work normally afterwards.\n\n" +
						"Please do not post or send in support messages about this as we are already aware.\n" +
						"Check our website forums and twitter for the latest info.";
				JOptionPane.showMessageDialog(BotChrome.getInstance(), msg, title, JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}
