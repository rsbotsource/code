package org.powerbot.game.loader.applet;

import java.applet.Applet;
import java.awt.Graphics;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.powerbot.game.loader.RSClassLoader;

/**
 * An applet for manipulation of the wrapped client class.
 * Loads the game for use.
 *
 * @author Timer
 */
public final class Rs2Applet extends Applet {
	private static Logger log = Logger.getLogger(Rs2Applet.class.getName());
	private static final long serialVersionUID = 1L;
	private final Map<String, byte[]> classes = new HashMap<String, byte[]>();
	private final String game;
	public Runnable callback;

	public Object clientInstance = null;
	private Class<?> clientClass = null;

	public Rs2Applet(final Map<String, byte[]> classes, final String game, final Runnable callback) {
		this.classes.putAll(classes);
		this.game = game;
		this.callback = callback;
	}

	@Override
	public final void init() {
		try {
			final RSClassLoader loader = new RSClassLoader(classes, new URL(game));
			clientClass = loader.loadClass("client");
		} catch (final Exception e) {
			log.log(Level.SEVERE, "Failed to load classes: ", e);
		}

		if (clientClass == null) {
			throwException(new RuntimeException("unable to find client.class"));
			return;
		}

		Constructor<?> constructor;
		try {
			constructor = clientClass.getConstructor((Class[]) null);
			clientInstance = constructor.newInstance((Object[]) null);
		} catch (final Exception e) {
			log.log(Level.SEVERE, "Failed to construct client class: ", e);
		}

		invokeMethod(new Object[]{this}, new Class[]{Applet.class}, "supplyApplet");
		if (callback != null) {
			try {
				callback.run();
			} catch (final Throwable t) {
				log.log(Level.SEVERE, "Callback failed to execute: ", t);
			}
		}
		invokeMethod(null, null, "init");
	}

	@Override
	public final void start() {
		if (clientInstance != null) {
			invokeMethod(null, null, "start");
		}
	}

	@Override
	public final void stop() {
		if (clientInstance != null) {
			invokeMethod(null, null, "stop");
		}
	}

	@Override
	public final void destroy() {
		if (clientInstance != null) {
			invokeMethod(null, null, "destroy");
		}
	}

	@Override
	public final void paint(final Graphics render) {
		if (clientInstance != null) {
			invokeMethod(new Object[]{render}, new Class[]{Graphics.class}, "paint");
		}
	}

	@Override
	public final void update(final Graphics render) {
		if (clientInstance != null) {
			invokeMethod(new Object[]{render}, new Class[]{Graphics.class}, "update");
		}
	}

	private void invokeMethod(final Object[] parameters, final Class<?>[] parameterTypes, final String name) {
		try {
			final Method method = clientClass.getMethod(name, parameterTypes);
			method.invoke(clientInstance, parameters);
		} catch (final Exception e) {
			log.log(Level.SEVERE, "Error invoking client method: ", e);
		}
	}


	private void throwException(final Throwable throwable) {
		log.log(Level.SEVERE, "Client exception: ", throwable);
	}
}