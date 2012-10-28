package org.powerbot.util;

import java.awt.Color;
import java.awt.Graphics;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class StringUtil {
	public static String stripHtml(final String s) {
		return s.replaceAll("\\<.*?\\>", "");
	}

	/**
	 * Draws a line on the screen at the specified index.
	 */
	public static void drawLine(final Graphics render, final int row, final String text) {
		final int height = render.getFontMetrics().getHeight() + 4;
		final int x = 7, y = row * height + height + 19;
		render.setColor(Color.GREEN);
		render.drawString(text, x, y);
	}

	public static String unescapeXmlEntities(String text) {
		text = text.replaceAll("&amp;", "&");
		text = text.replaceAll("&lt;", "<");
		text = text.replaceAll("&gt;", ">");
		text = text.replaceAll("&quot;", "\"");
		text = text.replaceAll("&apos;", "'");
		return text;
	}

	public static String urlEncode(final String text) {
		if (text == null) {
			return null;
		}
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (final UnsupportedEncodingException ignored) {
			return text;
		}
	}

	public static String urlDecode(final String text) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		} catch (final Exception ignored) {
			return text;
		}
	}

	public static String fileNameWithoutExtension(String path) {
		int z = path.lastIndexOf('/');
		if (z != -1) {
			if (++z == path.length()) {
				return "";
			} else {
				path = path.substring(z);
			}
		}
		z = path.indexOf('.');
		if (z != -1) {
			path = path.substring(0, z);
		}
		return path;
	}

	public static String throwableToString(final Throwable t) {
		if (t != null) {
			final Writer exception = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(exception);
			t.printStackTrace(printWriter);
			return exception.toString();
		}
		return "";
	}

	public static byte[] getBytesUtf8(final String string) {
		try {
			return string.getBytes("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String newStringUtf8(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			return new String(bytes, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String byteArrayToHexString(final byte[] b) {
		final StringBuilder s = new StringBuilder(b.length * 2);
		for (final byte aB : b) {
			s.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
		}
		return s.toString();
	}

	public static byte[] hexStringToByteArray(final String s) {
		final byte[] data = new byte[s.length() / 2];
		for (int i = 0; i < s.length(); i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
