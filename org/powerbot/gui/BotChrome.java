package org.powerbot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.powerbot.game.bot.Bot;
import org.powerbot.gui.component.BotLocale;
import org.powerbot.gui.component.BotPanel;
import org.powerbot.gui.component.BotToolBar;
import org.powerbot.service.NetworkAccount;
import org.powerbot.util.Configuration;
import org.powerbot.util.LoadUpdates;
import org.powerbot.util.io.Resources;

/**
 * @author Paris
 */
public class BotChrome extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;
	private static BotChrome instance;
	private static Logger log = Logger.getLogger(BotChrome.class.getName());
	public static final int PANEL_WIDTH = 765, PANEL_HEIGHT = 553, MAX_BOTS;
	public BotPanel panel;
	public BotToolBar toolbar;
	public JPanel header;
	public static volatile boolean loaded = false;

	static {
		MAX_BOTS = (int) Math.max(1, Math.min(6, Runtime.getRuntime().maxMemory() / 1024 / 1024 / 256));
	}

	private BotChrome() {
		setTitle(Configuration.NAME);
		setIconImage(Resources.getImage(Resources.Paths.ICON));
		addWindowListener(this);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		if (Configuration.DEVMODE) {
			setTitle(getTitle() + " (developer mode)");
		}

		panel = new BotPanel(this);
		add(panel);

		toolbar = new BotToolBar(this);
		header = new JPanel();
		header.setBackground(Color.BLACK);
		header.setPreferredSize(toolbar.getPreferredSize());
		add(header, BorderLayout.NORTH);

		log.log(Level.INFO, "Optimizing your experience", "Starting...");
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(getParent());
		setVisible(true);

		final ExecutorService exec = Executors.newFixedThreadPool(1);
		final List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
		tasks.add(exec.submit(new LoadUpdates()));
		tasks.add(exec.submit(new LoadAccount()));
		exec.execute(new LoadComplete(this, tasks));
		exec.shutdown();

		try {
			if (Resources.getServerData().containsKey("messages")) {
				if (Resources.getServerData().get("messages").containsKey("title")) {
					setTitle(getTitle() + " " + Resources.getServerData().get("messages").get("title"));
				}
				if (Resources.getServerData().get("messages").containsKey("start")) {
					final String msg = Resources.getServerData().get("messages").get("start").replace("\\n", "\n");
					if (Configuration.DEVMODE) {
						Logger.getLogger(log.getName() + "/Messages").info(msg);
					} else {
						JOptionPane.showMessageDialog(this, msg);
					}
				}
			}
		} catch (final IOException ignored) {
		}

		if (System.getProperty("java.version").indexOf("1.6.") == 0) {
			JOptionPane.showMessageDialog(this, BotLocale.UPDATEJAVA, "Java", JOptionPane.WARNING_MESSAGE);
		}
	}

	public static BotChrome getInstance() {
		if (instance == null) {
			instance = new BotChrome();
		}
		return instance;
	}

	public static void openURL(final String url) {
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			return;
		}
		final URI uri;
		try {
			uri = new URI(url);
		} catch (final URISyntaxException ignored) {
			return;
		}
		try {
			Desktop.getDesktop().browse(uri);
		} catch (final IOException ignored) {
		}
	}

	public void windowActivated(final WindowEvent arg0) {
	}

	public void windowClosed(final WindowEvent arg0) {
	}

	public void windowClosing(final WindowEvent arg0) {
		log.info("Shutting down");
		setVisible(false);
		int bots = Bot.bots.size();
		while (bots-- > 0) {
			try {
				Bot.bots.peekLast().killEnvironment();
			} catch (final Throwable e) {
				e.printStackTrace();
			}
		}
		dispose();
		System.exit(0);
	}

	public void windowDeactivated(final WindowEvent arg0) {
	}

	public void windowDeiconified(final WindowEvent arg0) {
	}

	public void windowIconified(final WindowEvent arg0) {
	}

	public void windowOpened(final WindowEvent arg0) {
	}

	private final class LoadAccount implements Callable<Boolean> {
		public Boolean call() throws Exception {
			NetworkAccount.getInstance();
			return true;
		}
	}

	private final class LoadComplete implements Runnable {
		private final BotChrome parent;
		private final List<Future<Boolean>> tasks;

		public LoadComplete(final BotChrome parent, final List<Future<Boolean>> tasks) {
			this.parent = parent;
			this.tasks = tasks;
		}

		public void run() {
			boolean pass = true;
			for (final Future<Boolean> task : tasks) {
				try {
					if (!task.get()) {
						pass = false;
					}
				} catch (final InterruptedException ignored) {
				} catch (final ExecutionException ignored) {
				}
			}
			if (pass) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						parent.remove(parent.header);
						parent.add(parent.toolbar, BorderLayout.NORTH);
						parent.validate();
						parent.repaint();
						parent.panel.loadingPanel.setAdVisible(!NetworkAccount.getInstance().isVIP());
						Logger.getLogger(BotChrome.class.getName()).log(Level.INFO, "Add a tab to start a new bot", "Welcome");
					}
				});
			}
			System.gc();
			loaded = true;
		}
	}
}
