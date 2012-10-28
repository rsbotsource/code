package org.powerbot.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Paris
 */
public final class XORInputStream extends FilterInputStream {
	private final XOR xor;

	public XORInputStream(final InputStream in, final byte[] key, final int opmode) {
		super(in);
		xor = new XOR(key, opmode);
	}

	@Override
	public synchronized int read() throws IOException {
		final byte[] b = {(byte) super.read()};
		xor.rotate(b, 0, b.length);
		return (b[0] & 0xff);
	}

	@Override
	public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
		final int result = super.read(b, off, len);
		xor.rotate(b, off, len);
		return result;
	}
}
