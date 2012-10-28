package org.powerbot.game.loader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.powerbot.util.io.HttpClient;
import org.powerbot.util.io.IOHelper;

/**
 * Crawls the game pages while faking HTTP header information to reduce detection rate.
 * Should appear as if a human is loading the game from a browser.
 *
 * @author Timer
 */
public class Crawler {
	public static final Pattern PATTERN_GAME = Pattern.compile("src=\"(.*)\" frameborder");
	public static final Pattern PATTERN_ARCHIVE = Pattern.compile("archive=(.*) ");
	public static final Pattern PATTERN_CLASS = Pattern.compile("code=(.*) ");
	public static final Pattern PATTERN_PARAMETER = Pattern.compile("<param name=\"([^\\s]+)\"\\s+value=\"([^>]*)\">");

	public String home, frame, game;
	public String archive, clazz;

	public final Map<String, String> parameters = new HashMap<String, String>();

	public Crawler() {
		home = "http://runescape.com/g=runescape/";
		frame = home + "game.ws?j=1";

		game = null;
		archive = null;
		clazz = null;
	}

	public boolean crawl() {
		final String frameHttpSource = download(frame, home);
		if (frameHttpSource != null) {
			final Matcher gameURLMatcher = PATTERN_GAME.matcher(frameHttpSource);
			if (gameURLMatcher.find()) {
				game = gameURLMatcher.group(1);
			}
		} else {
			return false;
		}

		if (game != null) {
			final String gameHttpSource = download(game, frame);
			if (gameHttpSource != null) {
				final Matcher archiveMatcher = PATTERN_ARCHIVE.matcher(gameHttpSource);
				if (archiveMatcher.find()) {
					final String archiveLink = archiveMatcher.group(1);
					URL gameURL;
					try {
						gameURL = new URL(game);
					} catch (final MalformedURLException e) {
						e.printStackTrace();
						return false;
					}
					archive = gameURL.getProtocol() + "://" + gameURL.getHost() + "/" + archiveLink;
				}

				final Matcher classMatcher = PATTERN_CLASS.matcher(gameHttpSource);
				if (classMatcher.find()) {
					clazz = classMatcher.group(1);
					clazz = clazz.substring(0, clazz.indexOf("."));
				}

				final Matcher parameterMatcher = PATTERN_PARAMETER.matcher(gameHttpSource);
				while (parameterMatcher.find()) {
					parameters.put(parameterMatcher.group(1), parameterMatcher.group(2));
				}
				if (parameters.containsKey("haveie6")) {
					parameters.put("haveie6", "false");
				}

				if (parameters.size() > 0 && clazz != null && archive != null) {
					return true;
				}
			}
		}
		return false;
	}

	private String download(final String url, final String referer) {
		try {
			final HttpURLConnection con = HttpClient.getHttpConnection(new URL(url));
			con.addRequestProperty("Referer", referer);
			return IOHelper.readString(HttpClient.getInputStream(con));
		} catch (final IOException ignored) {
		}
		return null;
	}
}
