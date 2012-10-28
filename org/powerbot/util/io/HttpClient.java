package org.powerbot.util.io;

import org.powerbot.util.Configuration;
import org.powerbot.util.StringUtil;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author Paris
 */
public class HttpClient {
	public static final String HTTP_USERAGENT_FAKE, HTTP_USERAGENT_REAL;

	static {
		final boolean x64 = System.getProperty("sun.arch.data.model").equals("64");
		final StringBuilder s = new StringBuilder(70);
		s.append("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; ");
		if (x64) {
			s.append("WOW64; ");
		}
		s.append("Trident/5.0)");
		HTTP_USERAGENT_FAKE = s.toString();
		s.setLength(0);
		s.append(Configuration.NAME);
		s.append('/');
		s.append(Configuration.VERSION);
		s.append(" (");
		s.append(System.getProperty("os.name"));
		s.append("; Java/");
		s.append(System.getProperty("java.version"));
		s.append(')');
		HTTP_USERAGENT_REAL = s.toString();
	}

	public static boolean isGameURL(final URL url) {
		return url.getHost().equalsIgnoreCase(Configuration.URLs.GAME) || url.getHost().toLowerCase().endsWith(Configuration.URLs.GAME);
	}

	public static String getHttpUserAgent(final URL url) {
		return isGameURL(url) ? HTTP_USERAGENT_FAKE : HTTP_USERAGENT_REAL;
	}

	public static HttpURLConnection getHttpConnection(final URL url) throws IOException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.addRequestProperty("Host", url.getHost());
		con.addRequestProperty("User-Agent", getHttpUserAgent(url));
        System.out.println(getHttpUserAgent((url)));
		con.addRequestProperty("Accept-Encoding", "gzip, deflate");
		con.addRequestProperty("Accept-Charset", "ISO-8859-1,UTF-8;q=0.7,*;q=0.7");
		con.addRequestProperty("Accept-Language", "en-us,en;q=0.5");
		con.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		if (!isGameURL(url)) {
			con.addRequestProperty("Connection", "close");
			con.addRequestProperty("Referer", url.toString());
		}
		con.setConnectTimeout(10000);
		return con;
	}

	public static HttpURLConnection download(final URL url, final File file) throws IOException {
		final HttpURLConnection con = getHttpConnection(url);

		if (file.exists()) {
			con.setIfModifiedSince(file.lastModified());
		}

		if (con.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
			IOHelper.write(getInputStream(con), file);
		}

		con.disconnect();
		return con;
	}

	public static InputStream openStream(final URL url) throws IOException {
		return getInputStream(getHttpConnection(url));
	}

	public static InputStream getInputStream(final URLConnection con) throws IOException {
		return getInputStream(con.getInputStream(), con.getHeaderField("Content-Encoding"));
	}

	public static InputStream getInputStream(InputStream in, final String encoding) throws IOException {
		if (encoding == null || encoding.isEmpty()) {
			return in;
		}
		for (final String mode : encoding.split(",")) {
			if (mode.equalsIgnoreCase("gzip")) {
				in = new GZIPInputStream(in);
			} else if (mode.equalsIgnoreCase("deflate")) {
				in = new InflaterInputStream(in);
			} else if (mode.startsWith("CIS:") || mode.startsWith("cis:")) {
				final String[] args = mode.split(":");
				try {
					in = CipherStreams.getCipherInputStream(in, Cipher.DECRYPT_MODE, CipherStreams.getSharedKey(StringUtil.getBytesUtf8(args[3])), args[1], args[2]);
				} catch (final GeneralSecurityException e) {
					throw new IOException(e);
				}
			}
		}
		return in;
	}

	public static OutputStream getOutputStream(OutputStream out, final String encoding) throws IOException {
		if (encoding == null || encoding.isEmpty()) {
			return out;
		}
		for (final String mode : encoding.split(",")) {
			if (mode.equalsIgnoreCase("gzip")) {
				out = new GZIPOutputStream(out);
			} else if (mode.equalsIgnoreCase("deflate")) {
				out = new DeflaterOutputStream(out);
			} else if (mode.startsWith("CIS:") || mode.startsWith("cis:")) {
				final String[] args = mode.split(":");
				try {
					out = CipherStreams.getCipherOutputStream(out, Cipher.ENCRYPT_MODE, CipherStreams.getSharedKey(StringUtil.getBytesUtf8(args[3])), args[1], args[2]);
				} catch (final GeneralSecurityException e) {
					throw new IOException(e);
				}
			}
		}
		return out;
	}
}
