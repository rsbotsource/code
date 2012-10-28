package org.powerbot.game;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.powerbot.asm.NodeManipulator;
import org.powerbot.concurrent.TaskContainer;
import org.powerbot.concurrent.TaskProcessor;
import org.powerbot.game.loader.AdaptException;
import org.powerbot.game.loader.Crawler;
import org.powerbot.game.loader.PackEncryption;
import org.powerbot.game.loader.applet.ClientStub;
import org.powerbot.game.loader.applet.Rs2Applet;
import org.powerbot.util.StringUtil;
import org.powerbot.util.io.HttpClient;
import org.powerbot.util.io.IOHelper;

/**
 * A definition of a <code>GameEnvironment</code> that manages all the data associated with this environment.
 *
 * @author Timer
 */
public abstract class GameDefinition implements GameEnvironment {
	private static final Logger log = Logger.getLogger(GameDefinition.class.getName());
	protected TaskContainer container;
	private final Map<String, byte[]> classes;
	public static final String THREADGROUPNAMEPREFIX = "GameDefinition-";

	public Crawler crawler;
	public volatile Rs2Applet appletContainer;
	public Runnable callback;
	public volatile ClientStub stub;
	protected String packHash;
	public ThreadGroup threadGroup;
	protected volatile boolean killed;

	public GameDefinition() {
		threadGroup = new ThreadGroup(THREADGROUPNAMEPREFIX + hashCode());
		container = new TaskProcessor(threadGroup);
		classes = new HashMap<String, byte[]>();

		crawler = new Crawler();
		appletContainer = null;
		callback = null;
		stub = null;
		packHash = null;
		killed = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean initializeEnvironment() {
		this.killed = false;
		log.info("Initializing game environment");
		classes.clear();
		log.fine("Crawling (for) game information");
		if (!crawler.crawl()) {
			log.severe("Please try again");
			return false;
		}
		if (killed) {
			return false;
		}
		log.fine("Downloading loader");
		final byte[] loader = getLoader(crawler);
		if (loader != null) {
			final String secretKeySpecKey = crawler.parameters.get("0");
			final String ivParameterSpecKey = crawler.parameters.get("-1");
			if (secretKeySpecKey == null || ivParameterSpecKey == null) {
				log.fine("Invalid secret spec key and/or iv parameter spec key");
				return false;
			}
			if (killed) {
				return false;
			}
			log.fine("Removing key ciphering");
			final byte[] secretKeySpecBytes = PackEncryption.toByte(secretKeySpecKey);
			final byte[] ivParameterSpecBytes = PackEncryption.toByte(ivParameterSpecKey);
			log.fine("Extracting classes from loader");
			final Map<String, byte[]> classes = PackEncryption.extract(secretKeySpecBytes, ivParameterSpecBytes, loader);
			log.fine("Generating client hash");
			packHash = StringUtil.byteArrayToHexString(PackEncryption.inner_pack_hash);
			log.fine("Client hash (" + packHash + ")");
			if (classes != null && classes.size() > 0) {
				this.classes.putAll(classes);
				classes.clear();
			}
			if (killed) {
				return false;
			}
			if (this.classes.size() > 0) {
				NodeManipulator nodeManipulator;
				try {
					nodeManipulator = getNodeManipulator();
				} catch (final Throwable e) {
					log.log(Level.FINE, "Failed to load manipulator: ", e);
					return false;
				}
				if (nodeManipulator != null) {
					log.fine("Running node manipulator");
					try {
						nodeManipulator.adapt();
					} catch (final AdaptException e) {
						log.log(Level.FINE, "Node manipulation failed", e);
						return false;
					}
					log.fine("Processing classes");
					for (final Map.Entry<String, byte[]> clazz : this.classes.entrySet()) {
						final String name = clazz.getKey();
						this.classes.put(name, nodeManipulator.process(name, clazz.getValue()));
					}
				}
				return true;
			}
		}
		return false;
	}

	public TaskContainer getContainer() {
		return container;
	}

	public static byte[] getLoader(final Crawler crawler) {
		try {
			final URLConnection clientConnection = HttpClient.getHttpConnection(new URL(crawler.archive));
			clientConnection.addRequestProperty("Referer", crawler.game);
			return IOHelper.read(HttpClient.getInputStream(clientConnection));
		} catch (final IOException ignored) {
		}
		return null;
	}

	public Map<String, byte[]> classes() {
		final Map<String, byte[]> classes = new HashMap<String, byte[]>();
		classes.putAll(this.classes);
		return classes;
	}
}
