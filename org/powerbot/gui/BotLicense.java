package org.powerbot.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.powerbot.gui.component.BotLocale;
import org.powerbot.util.Configuration;
import org.powerbot.util.io.IOHelper;
import org.powerbot.util.io.Resources;

/**
 * @author Paris
 */
public final class BotLicense extends JDialog {
	private static final long serialVersionUID = 1L;

	public BotLicense(final Frame owner, final boolean confirm) {
		super(owner, BotLocale.LICENSETCS, true);

		String license = "Unable to locate license file, please visit " + Configuration.URLs.DOMAIN + " to view license information";
		try {
			license = IOHelper.readString(Resources.getResourceURL(Resources.Paths.LICENSE));
			license = license.substring(license.indexOf('\n')).trim();
		} catch (final IOException ignored) {
		}

		final JTextArea text = new JTextArea(license);
		text.setEditable(false);
		text.setFont(getFont());
		final JScrollPane scroll = new JScrollPane(text);
		scroll.setPreferredSize(new Dimension(600, 250));
		add(scroll);

		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton accept = new JButton(confirm ? BotLocale.ACCEPT : BotLocale.OK), decline = new JButton(BotLocale.DECLINE);
		if (!confirm) {
			accept.setPreferredSize(new Dimension((int) (accept.getPreferredSize().width * 1.75d), accept.getPreferredSize().height));
		}
		accept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		decline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				System.exit(1);
			}
		});
		decline.setVisible(confirm);
		panel.add(accept);
		panel.add(decline);
		add(panel, BorderLayout.SOUTH);

		pack();
		setMinimumSize(getSize());
		setResizable(false);
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);
	}
}
