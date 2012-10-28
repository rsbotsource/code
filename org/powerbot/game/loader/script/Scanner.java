package org.powerbot.game.loader.script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.powerbot.game.GameDefinition;

/**
 * @author Paris
 */
public class Scanner {
	private final static int EOL = 0xA;
	private final InputStream in;

	public Scanner(final byte[] data) {
		this(new ByteArrayInputStream(data));
	}

	public Scanner(final InputStream in) {
		this.in = in;
	}

	public int readByte() {
		try {
			return in.read();
		} catch (final IOException ignored) {
			return -1;
		}
	}

	public int readShort() {
		return readByte() << 8 | readByte();
	}

	public int readInt() {
		return readShort() << 16 | readShort();
	}

	public long readLong() {
		return readInt() << 32 | readInt();
	}

	public String readString() {
		return normaliseString(new String(readSegment()));
	}

	public byte[] readSegment() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int b;
		while ((b = readByte()) != EOL && b != -1) {
			out.write(b);
		}
		return out.toByteArray();
	}

	public void readSegment(final byte[] data, final int len, final int off) {
		for (int i = off; i < off + len; i++) {
			data[i] = (byte) readByte();
		}
	}

	private String normaliseString(String s) {
		final Package pack = GameDefinition.class.getPackage();
		final String prefix = pack == null ? "" : pack.getName().replace('.', '/');
		s = s.replace("org/powerbot", prefix);
		s = s.replace("org/rsbot", prefix);
		return s;
	}
}
