package org.powerbot.util;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.powerbot.gui.component.BotLocale;
import org.powerbot.util.Configuration.OperatingSystem;
import org.powerbot.util.io.HttpClient;
import org.powerbot.util.io.Resources;

public final class LoadUpdates implements Callable<Boolean> {
	private static final Logger log = Logger.getLogger(LoadUpdates.class.getName());

	public Boolean call() throws Exception {
		final Map<String, Map<String, String>> data;
		try {
			data = Resources.getServerData();
		} catch (final Exception e) {
			String msg = "Error reading server data";
			if (SocketException.class.isAssignableFrom(e.getClass()) || SocketTimeoutException.class.isAssignableFrom(e.getClass())) {
				msg = "Could not connect to " + Configuration.URLs.DOMAIN + " server";
			}
			log.log(Level.SEVERE, msg, BotLocale.ERROR);
			return false;
		}
		final int version = Integer.parseInt(data.get("manifest").get("version"));
		if (version > Configuration.VERSION) {
			if (!Configuration.DEVMODE && Configuration.OS == OperatingSystem.WINDOWS) {
				log.log(Level.INFO, "Downloading update", "Update");
				final File file = new File(System.getProperty("java.io.tmpdir"), String.format("%s-%s.jar", Configuration.NAME, Integer.toString(version)));
				try {
					final URL url = new URL(String.format(Resources.getServerLinks().get("download"), Integer.toString(version)));
					HttpClient.download(url, file);
					if (file.isFile() && file.canRead()) {
						log.log(Level.INFO, "Launching update", "Update");
						Runtime.getRuntime().exec(new String[]{"java", "-jar", file.getCanonicalPath()});
						System.exit(0);
						return false;
					}
				} catch (final IOException ignored) {
					ignored.printStackTrace();
					log.log(Level.SEVERE, "A newer version of " + Configuration.NAME + " is available, please visit the website to download", "Update");
				}
			} else {
				log.log(Level.SEVERE, "A newer version of " + Configuration.NAME + " is available", "Update");
			}
			return false;
		}
		return true;
	}
}
