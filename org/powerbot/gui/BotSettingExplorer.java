package org.powerbot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.powerbot.concurrent.Task;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.bot.Context;

public class BotSettingExplorer extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 595;
	private static final int FRAME_HEIGHT = 335;

	private static BotSettingExplorer instance;
	private final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss");

	private int current = -1;
	private int[] settings_cache = null;
	private boolean paused = false;
	private final HashMap<Integer, String> lastChanges = new HashMap<Integer, String>();

	private String[] lastLabels = null;
	private JTextArea info = null;
	private JTextArea changes = null;
	private JScrollPane changesPane = null;
	private JList settingsList = null;

	private BotSettingExplorer() {
		create();
	}

	private static BotSettingExplorer getInstance() {
		if (instance == null) {
			instance = new BotSettingExplorer();
		}
		return instance;
	}

	public static void display(final Context context) {
		final BotSettingExplorer settingExplorer = getInstance();
		if (settingExplorer.isVisible()) {
			settingExplorer.clean();
		}
		settingExplorer.setVisible(true);
		try {
			context.associate(Thread.currentThread().getThreadGroup());
			settingExplorer.settings_cache = Settings.get();
			context.disregard(Thread.currentThread().getThreadGroup());
		} catch (final NullPointerException ignored) {
		}
		context.getBot().getContainer().submit(new Task() {
			@Override
			public void run() {
				while (settingExplorer.isVisible()) {
					settingExplorer.update();
					Time.sleep(100);
				}
			}
		});
	}

	private void clean() {
		changes.setText("");
	}

	private void update() {
		final int[] settings_clone = Settings.get();
		if (settings_cache == null) {
			settings_cache = settings_clone;
			return;
		}
		final String time = FORMATTER.format(new Date());
		for (int i = 0; i < settings_clone.length; i++) {
			final int cached_value = settings_cache[i];
			if (cached_value != settings_clone[i]) {
				if (!isPaused()) {
					changes.append("[" + time + "] " + i + " - " + buildSettingString(cached_value) + " -> " + buildSettingString(settings_clone[i]) + "\n");
					changesPane.getVerticalScrollBar().setValue(changesPane.getVerticalScrollBar().getMaximum());
				}
				lastChanges.put(i, time);
			}
		}
		final String[] labels = new String[settings_clone.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = Integer.toString(i) + ": " + Integer.toString(settings_clone[i]) + " [" + lastChanges.get(i) + "]";
		}
		if (!isPaused() && !Arrays.equals(labels, lastLabels)) {
			settingsList.setListData(labels);
			settingsList.setSelectedIndex(current);
		}
		lastLabels = labels;
		settings_cache = settings_clone;
		settingsList.repaint();
		changes.repaint();
		info.repaint();
	}

	private boolean isPaused() {
		return paused;
	}

	private String buildSettingString(final int setting) {
		return setting + " (0x" + Integer.toHexString(setting) + ")";
	}

	private void create() {
		setTitle("Setting Explorer");
		setResizable(false);
		setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				try {
					clean();
					setVisible(false);
				} catch (final Exception dd) {
					dd.printStackTrace();
				}
			}
		});

		final Font font = new Font("Dialog", Font.PLAIN, 12);

		final JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());

		final JPanel westPane = new JPanel();
		westPane.setLayout(new BorderLayout());
		contentPane.add(westPane, BorderLayout.WEST);

		settingsList = new JList();
		settingsList.setFont(font);
		settingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		settingsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					current = ((JList) e.getSource()).getSelectedIndex();
					if (current >= 0) {
						final int setting = settings_cache[current];
						info.setText("");
						info.append("Setting: " + current + "\n");
						info.append("Value: " + setting + "\n");
						info.append("Hex: 0x" + Integer.toHexString(setting) + "\n");
						info.append("Binary: " + Integer.toBinaryString(setting) + "\n");
						info.append("Last changed: " + lastChanges.get(current) + "\n");
						info.repaint();
					}
				}
			}
		});
		final JScrollPane settingsPane = new JScrollPane(settingsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		final Dimension listDimension = new Dimension(160, 258);
		settingsPane.setPreferredSize(listDimension);
		settingsPane.setMaximumSize(listDimension);
		settingsPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
		westPane.add(settingsPane, BorderLayout.NORTH);

		final JPanel controlsPane = new JPanel();
		controlsPane.setLayout(new BorderLayout());
		westPane.add(controlsPane, BorderLayout.SOUTH);

		final JNumberField gotoField = new JNumberField("Goto...");
		gotoField.setToolTipText("Goto Setting");
		final Color gotoBackground = new Color(230, 230, 230);
		gotoField.setBackground(gotoBackground);
		gotoField.setForeground(Color.DARK_GRAY);
		gotoField.setPreferredSize(new Dimension(160, 25));
		gotoField.addKeyListener(new KeyAdapter() {
			private boolean invalid = false;

			public void keyPressed(final KeyEvent e) {
				gotoField.setForeground(Color.BLACK);
				if (invalid) {
					gotoField.setText("");
					invalid = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						current = Integer.parseInt(gotoField.getText());
						if (current < 0 || current > settingsList.getModel().getSize()) {
							throw new Exception("invalid goto");
						}
						settingsList.ensureIndexIsVisible(current);
						settingsList.setSelectedIndex(current);
					} catch (Exception ex) {
						invalid = true;
						gotoField.setText("Invalid");
						gotoField.setForeground(Color.RED);
					}
				}
			}
		});
		gotoField.addFocusListener(new FocusAdapter() {
			public void focusGained(final FocusEvent e) {
				String curText = gotoField.getText();
				if (curText.equals("Goto...") || curText.equals("Invalid")) {
					gotoField.setText("");
				}
			}

			public void focusLost(final FocusEvent e) {
				if (gotoField.getText().isEmpty()) {
					gotoField.setForeground(Color.DARK_GRAY);
					gotoField.setText("Goto...");
				}
			}
		});
		controlsPane.add(gotoField, BorderLayout.NORTH);

		final JButton pauseButton = new JButton("Pause");
		pauseButton.setPreferredSize(new Dimension(160, 25));
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paused = !paused;
				pauseButton.setText(paused ? "Resume" : "Pause");
			}
		});
		controlsPane.add(pauseButton, BorderLayout.SOUTH);

		final JPanel eastPane = new JPanel();
		eastPane.setLayout(new BorderLayout());
		contentPane.add(eastPane, BorderLayout.EAST);

		changes = new JTextArea();
		changes.setFont(font);
		changes.setLineWrap(true);
		changes.setWrapStyleWord(true);
		changes.setEditable(false);
		final Dimension changeDimension = new Dimension(429, 225);
		changesPane = new JScrollPane(changes, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		changesPane.setPreferredSize(changeDimension);
		changesPane.setMaximumSize(changeDimension);
		changesPane.setBorder(BorderFactory.createMatteBorder(2, 1, 1, 1, Color.lightGray));
		eastPane.add(changesPane, BorderLayout.SOUTH);

		info = new JTextArea();
		info.setFont(font);
		info.setEditable(false);
		info.setPreferredSize(new Dimension(429, 83));
		info.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
		eastPane.add(info, BorderLayout.NORTH);

		pack();
		setLocationRelativeTo(getOwner());
		setVisible(false);
	}

	private static class JNumberField extends JTextField {
		private static final long serialVersionUID = 1L;
		private static final int[] VALID_KEYS = {KeyEvent.VK_ENTER, KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE};

		private JNumberField(final String text) {
			super(text);
			Arrays.sort(VALID_KEYS);
		}

		@Override
		public void processKeyEvent(final KeyEvent e) {
			if (Character.isDigit(e.getKeyChar()) || Arrays.binarySearch(VALID_KEYS, e.getKeyCode()) != -1) {
				super.processKeyEvent(e);
				return;
			}
			e.consume();
		}
	}
}
