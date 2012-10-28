package org.powerbot.util.io;

import org.powerbot.util.Configuration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * @author Paris
 */
public class Resources {
    private final static String SERVERDATAPATH = "/" + Configuration.NAME.toLowerCase() + "/server.ini";
    private static Map<String, Map<String, String>> serverData;

    public static class Paths {
        public static final String ROOT = "resources";
        public static final String LICENSE = "license.txt";
        public static final String SERVER = ROOT + "/server.ini";
        public static final String ROOT_IMG = ROOT + "/images";
        public static final String ICON = ROOT_IMG + "/icon.png";
        public static final String ICON_SMALL = ROOT_IMG + "/icon_small.png";
        public static final String INFO = ROOT_IMG + "/glyphicons_195_circle_info.png";
        public static final String ADD = ROOT_IMG + "/glyphicons_190_circle_plus.png";
        public static final String ARROWS = ROOT_IMG + "/arrows.png";
        public static final String REFRESH = ROOT_IMG + "/glyphicons_081_refresh.png";
        public static final String PAUSE = ROOT_IMG + "/control_pause.png";
        public static final String PLAY = ROOT_IMG + "/control_play.png";
        public static final String STOP = ROOT_IMG + "/control_stop.png";
        public static final String CROSS_SMALL = ROOT_IMG + "/cross_small.png";
        public static final String CROSS_SMALL_GRAY = ROOT_IMG + "/cross_small_gray.png";
        public static final String REMOVE = ROOT_IMG + "/glyphicons_197_remove.png";
        public static final String KEYS = ROOT_IMG + "/glyphicons_044_keys.png";
        public static final String KEYBOARD = ROOT_IMG + "/glyphicons_269_keyboard_wired.png";
        public static final String ADDRESS = ROOT_IMG + "/glyphicons_088_adress_book.png";
        public static final String FILE = ROOT_IMG + "/glyphicons_036_file.png";
        public static final String SEARCH = ROOT_IMG + "/glyphicons_027_search.png";
        public static final String PENCIL = ROOT_IMG + "/glyphicons_030_pencil.png";
        public static final String SETTINGS = ROOT_IMG + "/glyphicons_280_settings.png";
        public static final String TWITTER = ROOT_IMG + "/twitter.png";
        public static final String FACEBOOK = ROOT_IMG + "/facebook.png";
        public static final String YOUTUBE = ROOT_IMG + "/youtube.png";
        public static final String SKILLS = ROOT_IMG + "/skills.png";
    }

    public static URL getResourceURL(final String path) throws MalformedURLException {
        return Configuration.FROMJAR ? Configuration.class.getResource("/" + path) : new File(path).toURI().toURL();
    }

    public static Image getImage(final String resource) {
        try {
            return Toolkit.getDefaultToolkit().getImage(getResourceURL(resource));
        } catch (final Exception ignored) {
        }
        return null;
    }

    public static Map<String, Map<String, String>> getServerData() throws IOException {
        if (serverData == null) {
            if (Configuration.SUPERDEV) {
                serverData = IniParser.deserialise(new File(Paths.SERVER));
            } else {
                final HttpURLConnection con = HttpClient.getHttpConnection(new URL(Configuration.URLs.CONTROL));
                final URL base = new URL(con.getHeaderField("Location"));
                final URL location = new URL(base, SERVERDATAPATH);
                serverData = IniParser.deserialise(HttpClient.openStream(location));
            }
        }
        return serverData;
    }

    public static Map<String, String> getServerLinks() {
        try {
            return getServerData().get("links");
        } catch (final IOException ignored) {
            return null;
        }
    }

    public static void printAllLinks() {
        for (Map.Entry<String, String> es : getServerLinks().entrySet()) {
            System.out.println("Key: " + es.getKey() + ", value: " + es.getValue());
        }
    }

    public static void printAll() {
        try {
            for (Map.Entry<String, Map<String, String>> es : getServerData().entrySet()) {
                System.out.println("Key: " + es.getKey() + ", value: " + es.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream openHttpStream(String link, final Object... args) throws IOException {
        link = getServerLinks().get(link);
        if (link == null || link.isEmpty()) {
            return null;
        }
        return openHttpStreamFromLink(link, args);
    }

    public static InputStream openHttpStreamFromLink(String link, final Object... args) throws IOException {
        URL url = new URL(String.format(link, args));
        final String query = url.getFile(), marker = "{POST}";
        final int z = query.indexOf(marker);
        if (z == -1) {
            return HttpClient.openStream(url);
        }
        String pre = z == 0 ? "" : query.substring(0, z), post = z + marker.length() >= query.length() ? null : query.substring(z + marker.length());
        if (post == null || post.isEmpty()) {
            return HttpClient.openStream(url);
        }
        if (pre.length() > 0 && pre.charAt(pre.length() - 1) == '?') {
            pre = pre.substring(0, pre.length() - 1);
        }
        url = new URL(url.getProtocol(), url.getHost(), url.getPort(), pre);
        final URLConnection con = HttpClient.getHttpConnection(url);
        con.setDoOutput(true);
        final OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(post);
        out.flush();
        return HttpClient.getInputStream(con);
    }
}
