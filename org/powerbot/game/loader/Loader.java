package org.powerbot.game.loader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.powerbot.concurrent.Task;
import org.powerbot.game.GameDefinition;
import org.powerbot.game.loader.applet.ClientStub;
import org.powerbot.game.loader.applet.Rs2Applet;
import org.powerbot.util.Configuration;

/**
 * A runnable loader that loads the game of a game environment.
 *
 * @author Timer
 */
public class Loader implements Task {
	private static Logger log = Logger.getLogger(Loader.class.getName());
	private final GameDefinition definition;

	public Loader(final GameDefinition definition) {
		this.definition = definition;
	}

	/**
	 * Creates the wrapping applet of the game, generates an appropriate stub, and initializes client activities.
	 */
	public void run() {
		log.fine("Generating applet wrapper");
		definition.appletContainer = new Rs2Applet(definition.classes(), "http://" + Configuration.URLs.GAME + "/", definition.callback);
		try {
			log.fine("Generating stub");
			definition.stub = new ClientStub(definition.crawler.game, definition.crawler.archive, definition.crawler.parameters);
		} catch (final Exception e) {
			log.log(Level.SEVERE, "Error creating stub: ", e);
			return;
		}
		log.fine("Setting stub");
		definition.appletContainer.setStub(definition.stub);
		definition.stub.setApplet(definition.appletContainer);
		definition.stub.setActive(true);
		log.fine("Initializing and starting applet wrapper");
		definition.appletContainer.init();
		definition.appletContainer.start();
	}
}
