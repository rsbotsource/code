package org.powerbot.gui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.powerbot.game.bot.Bot;
import org.powerbot.game.bot.event.impl.DrawBoundaries;
import org.powerbot.game.bot.event.impl.DrawGroundItems;
import org.powerbot.game.bot.event.impl.DrawInventory;
import org.powerbot.game.bot.event.impl.DrawModels;
import org.powerbot.game.bot.event.impl.DrawMouse;
import org.powerbot.game.bot.event.impl.DrawNPCs;
import org.powerbot.game.bot.event.impl.DrawPlayers;
import org.powerbot.game.bot.event.impl.DrawScene;
import org.powerbot.game.bot.event.impl.MessageLogger;
import org.powerbot.game.bot.event.impl.TCamera;
import org.powerbot.game.bot.event.impl.TClientState;
import org.powerbot.game.bot.event.impl.TDestination;
import org.powerbot.game.bot.event.impl.TLocation;
import org.powerbot.game.bot.event.impl.TMapBase;
import org.powerbot.game.bot.event.impl.TMenu;
import org.powerbot.game.bot.event.impl.TMousePosition;
import org.powerbot.game.bot.event.impl.TPlane;

/**
 * @author Paris
 */
public final class BotMenuView extends JMenu implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final BotMenu parent;
	private final Map<String, Class<? extends EventListener>> map;
	private final List<String> items;
	private static Map<Bot, Map<String, EventListener>> listeners;

	private static final String ALL = "All";
	private static final String MOUSE = "Mouse";
	private static final String PLAYERS = "Players";
	private static final String NPCS = "NPCs";
	private static final String GROUND_ITEMS = "Ground Items";
	private static final String SCENEENTITIES = "Scene Entities";
	private static final String MODELS = "Models";
	private static final String BOUNDARIES = "Boundaries";
	private static final String INVENTORY = "Inventory";
	private static final String CLIENTSTATE = "Client State";
	private static final String MOUSEPOSITION = "Mouse Position";
	private static final String MENU = "Menu";
	private static final String PLANE = "Plane";
	private static final String CAMERA = "Camera";
	private static final String MAPBASE = "Map Base";
	private static final String LOCATION = "Location";
	private static final String DESTINATION = "Destination";
	private static final String MESSAGES = "Messages";
	private static final String SEPERATOR = "-";

	public BotMenuView(final BotMenu parent) {
		super(BotLocale.VIEW);
		this.parent = parent;
		final int tab = parent.parent.getActiveTab();

		if (tab == -1) {
			setEnabled(false);
			map = null;
			items = null;
			return;
		}

		if (listeners == null) {
			listeners = new HashMap<Bot, Map<String, EventListener>>();
		}

		map = new LinkedHashMap<String, Class<? extends EventListener>>();
		map.put(BOUNDARIES, DrawBoundaries.class);
		map.put(MODELS, DrawModels.class);
		map.put(SCENEENTITIES, DrawScene.class);
		map.put(PLAYERS, DrawPlayers.class);
		map.put(NPCS, DrawNPCs.class);
		map.put(GROUND_ITEMS, DrawGroundItems.class);
		map.put(CLIENTSTATE, TClientState.class);
		map.put(MOUSEPOSITION, TMousePosition.class);
		map.put(MENU, TMenu.class);
		map.put(PLANE, TPlane.class);
		map.put(CAMERA, TCamera.class);
		map.put(MAPBASE, TMapBase.class);
		map.put(LOCATION, TLocation.class);
		map.put(DESTINATION, TDestination.class);
		map.put(MOUSE, DrawMouse.class);
		map.put(INVENTORY, DrawInventory.class);
		map.put(MESSAGES, MessageLogger.class);

		items = new ArrayList<String>(map.size());
		items.add(MOUSE);
		items.add(PLAYERS);
		items.add(NPCS);
		items.add(GROUND_ITEMS);
		items.add(SCENEENTITIES);
		items.add(MODELS);
		items.add(BOUNDARIES);
		items.add(INVENTORY);
		items.add(SEPERATOR);
		items.add(CLIENTSTATE);
		items.add(MOUSEPOSITION);
		items.add(MENU);
		items.add(PLANE);
		items.add(CAMERA);
		items.add(MAPBASE);
		items.add(LOCATION);
		items.add(DESTINATION);
		items.add(SEPERATOR);
		items.add(MESSAGES);

		final Bot bot = Bot.bots.get(tab);
		Map<String, EventListener> listeners = BotMenuView.listeners.get(bot);
		if (listeners == null) {
			listeners = new HashMap<String, EventListener>();
			BotMenuView.listeners.put(bot, listeners);
		}

		boolean selectedAll = true;

		for (final String key : items) {
			if (key.equals(SEPERATOR)) {
				continue;
			}
			if (!listeners.containsKey(map.get(key).getName())) {
				selectedAll = false;
				break;
			}
		}

		final JCheckBoxMenuItem all = new JCheckBoxMenuItem(ALL, selectedAll);
		all.addActionListener(this);
		add(all);
		addSeparator();

		for (final String key : items) {
			if (key.equals(SEPERATOR)) {
				addSeparator();
				continue;
			}
			final Class<? extends EventListener> eventListener = map.get(key);
			final boolean selected = listeners.containsKey(eventListener.getName());
			final JCheckBoxMenuItem item = new JCheckBoxMenuItem(key, selected);
			item.addActionListener(this);
			add(item);
		}
	}

	public void actionPerformed(final ActionEvent e) {
		final JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
		item.setSelected(!item.isSelected());
		if (item.getText().equals(ALL)) {
			for (final Entry<String, Class<? extends EventListener>> entry : map.entrySet()) {
				setView(entry.getValue(), item.isSelected());
			}
		} else {
			setView(map.get(item.getText()), item.isSelected());
		}
	}

	private void setView(final Class<? extends EventListener> eventListener, final boolean selected) {
		final Bot bot = Bot.bots.get(parent.parent.getActiveTab());
		final String name = eventListener.getName();
		Map<String, EventListener> listeners = BotMenuView.listeners.get(bot);
		if (listeners == null) {
			listeners = new HashMap<String, EventListener>();
			BotMenuView.listeners.put(bot, listeners);
		}
		if (!selected) {
			if (listeners.containsKey(name)) {
				return;
			}
			try {
				EventListener listener;
				try {
					Constructor<?> constructor = eventListener.getConstructor(Bot.class);
					listener = (EventListener) constructor.newInstance(bot);
				} catch (final Exception ignored) {
					listener = eventListener.asSubclass(EventListener.class).newInstance();
				}
				listeners.put(name, listener);
				bot.getEventDispatcher().accept(listener);
			} catch (final Exception ignored) {
			}
		} else {
			final EventListener listener = listeners.get(name);
			if (listener != null) {
				listeners.remove(name);
				bot.getEventDispatcher().remove(listener);
			}
		}
	}
}
