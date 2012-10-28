package org.powerbot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import org.powerbot.game.bot.event.listener.PaintListener;

/**
 * @author Timer
 */
public class BotWidgetExplorer extends JFrame implements PaintListener {
	private static final long serialVersionUID = 1L;
	private JTree tree;
	private WidgetTreeModel treeModel;
	private JPanel infoArea;
	private JTextField searchBox;
	private Rectangle highlightArea = null;

	private Context context;
	private static BotWidgetExplorer instance;

	private static BotWidgetExplorer getInstance(final Context context) {
		if (instance == null) {
			instance = new BotWidgetExplorer(context);
		}
		return instance;
	}

	public static void display(final Context context) {
		final BotWidgetExplorer botWidgetExplorer = getInstance(context);
		if (botWidgetExplorer.isVisible()) {
			botWidgetExplorer.context.getBot().getEventDispatcher().remove(botWidgetExplorer);
			botWidgetExplorer.highlightArea = null;
		}
		botWidgetExplorer.context = context;
		botWidgetExplorer.treeModel.update("");
		botWidgetExplorer.context.getBot().getEventDispatcher().accept(botWidgetExplorer);
		botWidgetExplorer.setVisible(true);
	}

	public BotWidgetExplorer(final Context context) {
		super("Widget Explorer");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				setVisible(false);
				context.getBot().getEventDispatcher().remove(this);
				highlightArea = null;
			}
		});
		this.context = context;
		treeModel = new WidgetTreeModel();
		treeModel.update("");
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				context.associate(Thread.currentThread().getThreadGroup());
				try {
					final Object node = tree.getLastSelectedPathComponent();
					if (node == null || node instanceof WidgetWrapper) {
						return;
					}
					infoArea.removeAll();
					WidgetChild widgetChild = null;
					if (node instanceof WidgetChildWrapper) {
						highlightArea = ((WidgetChildWrapper) node).get().getBoundingRectangle();
						widgetChild = ((WidgetChildWrapper) node).get();
					}
					if (widgetChild == null) {
						return;
					}
					addInfo("Index: ", Integer.toString(widgetChild.getIndex()));
					addInfo("Validated: ", Boolean.toString(widgetChild.validate()));
					addInfo("Visible: ", Boolean.toString(widgetChild.visible()));
					addInfo("Absolute location: ", widgetChild.getAbsoluteLocation().toString());
					addInfo("Relative location: ", widgetChild.getRelativeLocation().toString());
					addInfo("Width: ", Integer.toString(widgetChild.getWidth()));
					addInfo("Height: ", Integer.toString(widgetChild.getHeight()));
					addInfo("Id: ", Integer.toString(widgetChild.getId()));
					addInfo("Type: ", Integer.toString(widgetChild.getType()));
					addInfo("Special type: ", Integer.toString(widgetChild.getSpecialType()));
					addInfo("Child id: ", Integer.toString(widgetChild.getChildId()));
					addInfo("Child index: ", Integer.toString(widgetChild.getChildIndex()));
					addInfo("Texture id: ", Integer.toString(widgetChild.getTextureId()));
					addInfo("Text: ", widgetChild.getText());
					addInfo("Text color: ", Integer.toString(widgetChild.getTextColor()));
					addInfo("Shadow color: ", Integer.toString(widgetChild.getShadowColor()));
					addInfo("Tooltip: ", widgetChild.getTooltip());
					addInfo("Border thickness: ", Integer.toString(widgetChild.getBorderThickness()));
					addInfo("Selected action: ", widgetChild.getSelectedAction());
					addInfo("Model id: ", Integer.toString(widgetChild.getModelId()));
					addInfo("Model type: ", Integer.toString(widgetChild.getModelType()));
					addInfo("Model zoom: ", Integer.toString(widgetChild.getModelZoom()));
					addInfo("Inventory: ", Boolean.toString(widgetChild.isInventory()));
					addInfo("Child stack size: ", Integer.toString(widgetChild.getChildStackSize()));
					addInfo("Bound array index: ", Integer.toString(widgetChild.getBoundsArrayIndex()));
					addInfo("Scrollable area: ", Boolean.toString(widgetChild.isInScrollableArea()));
					addInfo("Parent id: ", Integer.toString(widgetChild.getParentId()));
					infoArea.validate();
					infoArea.repaint();
				} finally {
					context.disregard(Thread.currentThread().getThreadGroup());
				}
			}

			private void addInfo(final String key, final String value) {
				final JPanel row = new JPanel();
				row.setAlignmentX(Component.LEFT_ALIGNMENT);
				row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
				for (final String data : new String[]{key, value}) {
					final JLabel label = new JLabel(data);
					label.setAlignmentY(Component.TOP_ALIGNMENT);
					row.add(label);
				}
				infoArea.add(row);
			}
		});
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(250, 500));
		add(scrollPane, BorderLayout.WEST);

		infoArea = new JPanel();
		infoArea.setLayout(new BoxLayout(infoArea, BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane(infoArea);
		scrollPane.setPreferredSize(new Dimension(250, 500));
		add(scrollPane, BorderLayout.CENTER);

		final ActionListener actionListener = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				treeModel.update(searchBox.getText());
				infoArea.removeAll();
				infoArea.validate();
				infoArea.repaint();
			}
		};

		final JPanel toolArea = new JPanel();
		toolArea.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolArea.add(new JLabel("Filter:"));

		searchBox = new JTextField(20);
		searchBox.addActionListener(actionListener);
		toolArea.add(searchBox);

		final JButton updateButton = new JButton("Update");
		updateButton.addActionListener(actionListener);
		toolArea.add(updateButton);
		add(toolArea, BorderLayout.NORTH);

		pack();
		setLocationRelativeTo(getOwner());
		setVisible(false);
	}

	private final class WidgetTreeModel implements TreeModel {
		private final Object root = new Object();
		private final List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
		private final List<WidgetWrapper> widgetWrappers = new ArrayList<WidgetWrapper>();

		public Object getRoot() {
			return root;
		}

		public Object getChild(final Object parent, final int index) {
			context.associate(Thread.currentThread().getThreadGroup());
			try {
				if (parent == root) {
					return widgetWrappers.get(index);
				} else if (parent instanceof WidgetWrapper) {
					return new WidgetChildWrapper(((WidgetWrapper) parent).get().getChildren()[index]);
				} else if (parent instanceof WidgetChildWrapper) {
					return new WidgetChildWrapper(((WidgetChildWrapper) parent).get().getChildren()[index]);
				}
				return null;
			} finally {
				context.disregard(Thread.currentThread().getThreadGroup());
			}
		}

		public int getChildCount(final Object parent) {
			context.associate(Thread.currentThread().getThreadGroup());
			try {
				if (parent == root) {
					return widgetWrappers.size();
				} else if (parent instanceof WidgetWrapper) {
					return ((WidgetWrapper) parent).get().getChildren().length;
				} else if (parent instanceof WidgetChildWrapper) {
					return ((WidgetChildWrapper) parent).get().getChildren().length;
				}
				return 0;
			} finally {
				context.disregard(Thread.currentThread().getThreadGroup());
			}
		}

		public boolean isLeaf(final Object node) {
			context.associate(Thread.currentThread().getThreadGroup());
			try {
				return node instanceof WidgetChildWrapper && ((WidgetChildWrapper) node).get().getChildren().length == 0;
			} finally {
				context.disregard(Thread.currentThread().getThreadGroup());
			}
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
		}

		public int getIndexOfChild(final Object parent, final Object child) {
			context.associate(Thread.currentThread().getThreadGroup());
			try {
				if (parent == root) {
					return widgetWrappers.indexOf(child);
				} else if (parent instanceof WidgetWrapper) {
					return Arrays.asList(((WidgetWrapper) parent).get().getChildren()).indexOf(((WidgetChildWrapper) child).get());
				} else if (parent instanceof WidgetChildWrapper) {
					return Arrays.asList(((WidgetChildWrapper) parent).get().getChildren()).indexOf(((WidgetChildWrapper) child).get());
				}
				return -1;
			} finally {
				context.disregard(Thread.currentThread().getThreadGroup());
			}
		}

		public void addTreeModelListener(final TreeModelListener l) {
			treeModelListeners.add(l);
		}

		public void removeTreeModelListener(final TreeModelListener l) {
			treeModelListeners.remove(l);
		}

		private void fireTreeStructureChanged(final Object oldRoot) {
			final TreeModelEvent e = new TreeModelEvent(this, new Object[]{oldRoot});
			for (final TreeModelListener tml : treeModelListeners) {
				tml.treeStructureChanged(e);
			}
		}

		public void update(final String search) {
			widgetWrappers.clear();
			context.associate(Thread.currentThread().getThreadGroup());
			for (final Widget widget : Widgets.getLoaded()) {
				children:
				for (final WidgetChild widgetChild : widget.getChildren()) {
					if (search(widgetChild, search)) {
						widgetWrappers.add(new WidgetWrapper(widget));
						break;
					}
					for (final WidgetChild widgetSubChild : widgetChild.getChildren()) {
						if (search(widgetSubChild, search)) {
							widgetWrappers.add(new WidgetWrapper(widget));
							break children;
						}
					}
				}
			}
			fireTreeStructureChanged(root);
			context.disregard(Thread.currentThread().getThreadGroup());
		}

		private boolean search(final WidgetChild child, final String string) {
			return child.getText().toLowerCase().contains(string.toLowerCase());
		}
	}

	private final class WidgetWrapper {
		private Widget widget;

		public WidgetWrapper(final Widget widget) {
			this.widget = widget;
		}

		public Widget get() {
			return widget;
		}

		@Override
		public boolean equals(final Object object) {
			return object != null && object instanceof WidgetWrapper && widget.equals(((WidgetWrapper) object).get());
		}

		@Override
		public String toString() {
			return "Widget-" + widget.getIndex();
		}
	}

	private final class WidgetChildWrapper {
		private WidgetChild widgetChild;

		public WidgetChildWrapper(final WidgetChild widgetChild) {
			this.widgetChild = widgetChild;
		}

		public WidgetChild get() {
			return widgetChild;
		}

		@Override
		public boolean equals(final Object object) {
			return object != null && object instanceof WidgetChildWrapper && widgetChild.equals(((WidgetChildWrapper) object).get());
		}

		@Override
		public String toString() {
			return "WidgetChild-" + widgetChild.getIndex();
		}
	}

	public void onRepaint(final Graphics g) {
		if (highlightArea != null) {
			g.setColor(Color.orange);
			g.drawRect(highlightArea.x, highlightArea.y, highlightArea.width, highlightArea.height);
		}
	}
}
