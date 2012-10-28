package org.powerbot.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.powerbot.gui.component.BotLocale;
import org.powerbot.service.GameAccounts;
import org.powerbot.service.GameAccounts.Account;
import org.powerbot.util.io.Resources;

/**
 * @author Paris
 */
public final class BotAccounts extends JDialog implements WindowListener {
	private final Logger log = Logger.getLogger(BotAccounts.class.getName());
	private static final long serialVersionUID = 1L;
	private final JTable table;
	private final JButton delete;

	private static final String[] RANDOM_REWARDS = {"Cash", "Runes", "Coal", "Essence", "Ore", "Bars", "Gems", "Herbs",
			"Seeds", "Charms", "Surprise", "Emote", "Costume", "Attack",
			"Defence", "Strength", "Constitution", "Range", "Prayer", "Magic",
			"Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking",
			"Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving",
			"Slayer", "Farming", "Runecrafting", "Hunter", "Construction",
			"Summoning", "Dungeoneering"};

	public BotAccounts(final BotChrome parent) {
		super((Frame) parent, BotLocale.ACCOUNTS, true);
		setIconImage(Resources.getImage(Resources.Paths.ADDRESS));

		final JScrollPane scroll = new JScrollPane();

		table = new JTable(new AccountTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new TableSelectionListener());
		table.setShowGrid(false);

		final TableColumnModel cm = table.getColumnModel();
		for (int i = 1; i < 3; i++) {
			cm.getColumn(i).setCellRenderer(new PasswordCellRenderer());
			cm.getColumn(i).setCellEditor(new PasswordCellEditor());
		}
		cm.getColumn(4).setCellEditor(new RandomRewardEditor());

		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setViewportView(table);
		scroll.setPreferredSize(new Dimension(400, 150));
		add(scroll, BorderLayout.CENTER);

		final JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		final JButton add = new JButton(new ImageIcon(Resources.getImage(Resources.Paths.ADD)));
		add.setFocusable(false);
		add.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				final String str = JOptionPane.showInputDialog(getParent(), "Enter the account username:", "New Account", JOptionPane.QUESTION_MESSAGE);
				if (str == null || str.length() == 0) {
					return;
				}
				final Account existing = GameAccounts.getInstance().get(str);
				if (existing != null) {
					return;
				}
				final Account account = GameAccounts.getInstance().add(str);
				account.reward = RANDOM_REWARDS[0];
				final int row = table.getRowCount();
				((AccountTableModel) table.getModel()).fireTableRowsInserted(row, row);
			}
		});
		delete = new JButton(new ImageIcon(Resources.getImage(Resources.Paths.REMOVE)));
		delete.setFocusable(false);
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				final int row = table.getSelectedRow();
				final String user = ((AccountTableModel) table.getModel()).userForRow(row);
				if (user != null) {
					GameAccounts.getInstance().remove(GameAccounts.getInstance().get(user));
					((AccountTableModel) table.getModel()).fireTableRowsDeleted(row, row);
				}
			}
		});
		bar.add(add);
		bar.add(delete);
		add(bar, BorderLayout.SOUTH);

		final int row = table.getSelectedRow();
		delete.setEnabled(row > -1 && row < table.getRowCount());

		addWindowListener(this);
		pack();
		setMinimumSize(getSize());
		setResizable(false);
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	public void windowActivated(final WindowEvent arg0) {
	}

	public void windowClosed(final WindowEvent arg0) {
	}

	public void windowClosing(final WindowEvent arg0) {
		setVisible(false);
		try {
			log.info("Saving " + GameAccounts.getInstance().size() + " accounts");
			GameAccounts.getInstance().save();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void windowDeactivated(final WindowEvent arg0) {
	}

	public void windowDeiconified(final WindowEvent arg0) {
	}

	public void windowIconified(final WindowEvent arg0) {
	}

	public void windowOpened(final WindowEvent arg0) {
	}

	private static class RandomRewardEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;

		public RandomRewardEditor() {
			super(new JComboBox(RANDOM_REWARDS));
		}
	}

	private static class PasswordCellEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;

		public PasswordCellEditor() {
			super(new JPasswordField());
		}
	}

	private static class PasswordCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		protected void setValue(final Object value) {
			if (value == null) {
				setText("");
			} else {
				final String str = value.toString();
				final StringBuilder b = new StringBuilder();
				for (int i = 0; i < str.length(); ++i) {
					b.append("\u25CF");
				}
				setText(b.toString());
			}
		}
	}

	private class TableSelectionListener implements ListSelectionListener {
		public void valueChanged(final ListSelectionEvent evt) {
			final int row = table.getSelectedRow();
			if (!evt.getValueIsAdjusting()) {
				delete.setEnabled(row > -1 && row < table.getRowCount());
			}
		}
	}

	private class AccountTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		public int getRowCount() {
			return GameAccounts.getInstance().size();
		}

		public int getColumnCount() {
			return 5;
		}

		public Object getValueAt(final int row, final int column) {
			final Account account = GameAccounts.getInstance().get(userForRow(row));
			switch (column) {
			case 0:
				return account.toString();
			case 1:
				return account.getPassword();
			case 2:
				return account.pin == -1 ? "" : account.getPIN();
			case 3:
				return account.member;
			case 4:
				return account.reward;
			}
			return null;
		}

		@Override
		public String getColumnName(final int column) {
			switch (column) {
			case 0:
				return BotLocale.USERNAME;
			case 1:
				return BotLocale.PASSWORD;
			case 2:
				return BotLocale.PIN;
			case 3:
				return BotLocale.MEMBER;
			case 4:
				return BotLocale.REWARD;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			return column == 3 ? Boolean.class : Object.class;
		}

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return column > 0;
		}

		@Override
		public void setValueAt(final Object value, final int row, final int column) {
			final Account account = GameAccounts.getInstance().get(userForRow(row));
			if (account == null) {
				return;
			}
			final String str = String.valueOf(value);
			switch (column) {
			case 1:
				account.setPassword(str);
				break;
			case 2:
				try {
					account.pin = Integer.parseInt(str);
				} catch (final NumberFormatException ignored) {
					account.pin = -1;
				}
				if (account.pin < 1 || account.pin > 9999) {
					account.pin = -1;
				}
				break;
			case 3:
				account.member = str.equals("true");
				break;
			case 4:
				account.reward = str;
				break;
			}
			fireTableCellUpdated(row, column);
		}

		public String userForRow(final int row) {
			final Iterator<Account> i = GameAccounts.getInstance().iterator();
			for (int k = 0; i.hasNext() && k < row; k++) {
				i.next();
			}
			if (i.hasNext()) {
				return i.next().toString();
			}
			return null;
		}
	}
}
