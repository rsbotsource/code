package org.powerbot.game.bot;

import org.powerbot.concurrent.TaskContainer;
import org.powerbot.event.EventManager;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.util.internal.Constants;
import org.powerbot.game.api.util.internal.Multipliers;
import org.powerbot.game.bot.handler.input.MouseExecutor;
import org.powerbot.game.bot.util.ScreenCapture;
import org.powerbot.game.client.Client;
import org.powerbot.game.loader.applet.Rs2Applet;
import org.powerbot.gui.BotChrome;
import org.powerbot.service.NetworkAccount;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Context {
	protected static final Map<ThreadGroup, Context> context = new HashMap<ThreadGroup, Context>();

	private final Bot bot;
	public int world = -1;

	public Context(final Bot bot) {
		this.bot = bot;
	}

	public static Context get() {
		final Context context = Context.context.get(Thread.currentThread().getThreadGroup());
		if (context == null) {
			throw new RuntimeException(Thread.currentThread() + "@" + Thread.currentThread().getThreadGroup());
		}
		return context;
	}

	public static Bot resolve() {
		return get().bot;
	}

	public static Client client() {
		return get().getClient();
	}

	public static Multipliers multipliers() {
		return get().bot.multipliers;
	}

	public static Constants constants() {
		return get().bot.constants;
	}

	public static BufferedImage captureScreen() {
		return ScreenCapture.capture(Context.get());
	}

	public static void saveScreenCapture() {
		ScreenCapture.save(Context.get());
	}

	public static void setLoginWorld(final int world) {
		get().world = world;
	}

	public static void cancelMouse() {
		get().bot.getExecutor().cancel();
	}

	public void b(final int s) {
		Bot.setSpeed(s);
	}

	public static void saveScreenCapture(final String fileName) {
		ScreenCapture.save(Context.get(), fileName);
	}

	public Bot getBot() {
		return bot;
	}

	public ActiveScript getActiveScript() {
		return bot.getActiveScript();
	}

	public Client getClient() {
		return bot.getClient();
	}

	public MouseExecutor getExecutor() {
		return bot.getExecutor();
	}

	public BufferedImage getImage() {
		return bot.getImage();
	}

	public TaskContainer getContainer() {
		return bot.getContainer();
	}

	public EventManager getEventManager() {
		return bot.getEventDispatcher();
	}

	public ThreadGroup getThreadGroup() {
		return bot.threadGroup;
	}

	public Rs2Applet getApplet() {
		return bot.appletContainer;
	}

	public Calculations.Toolkit getToolkit() {
		return bot.toolkit;
	}

	public Calculations.Viewport getViewport() {
		return bot.viewport;
	}

	public void ensureAntiRandoms() {
		bot.ensureAntiRandoms();
	}

	public void updateControls() {
		BotChrome.getInstance().toolbar.updateScriptControls();
	}

	public String getDisplayName() {
		if (NetworkAccount.getInstance().isLoggedIn()) {
			return NetworkAccount.getInstance().getAccount().getDisplayName();
		}
		return null;
	}

	public int getUserId() {
		if (NetworkAccount.getInstance().isLoggedIn()) {
			return NetworkAccount.getInstance().getAccount().getID();
		}
		return -1;
	}

	public void associate(final ThreadGroup threadGroup) {
		if (!EventQueue.isDispatchThread() && Context.context.containsKey(threadGroup)) {
			throw new RuntimeException("overlapping thread groups!");
		}
		Context.context.put(threadGroup, this);
	}

	public void disregard(final ThreadGroup threadGroup) {
		Context.context.remove(threadGroup);
	}
}
