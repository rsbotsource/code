package org.powerbot.util.io;

import javax.crypto.Cipher;

/**
 * @author Paris
 */
public final class XOR {
	private final byte[] key;
	private final int opmode, l;
	private int n, d;

	public XOR(final byte[] key, final int opmode) {
		this.key = key;
		if (!(opmode == Cipher.DECRYPT_MODE || opmode == Cipher.ENCRYPT_MODE)) {
			throw new IllegalArgumentException();
		}
		this.opmode = opmode;
		l = this.key.length;
		n = 0;
		d = 0x9e3779b9;
	}

	public void rotate(final byte[] b, final int off, final int len) {
		for (int i = off; i < off + len; i++) {
			final int z = n++ % l, x = key[z];
			this.d += key[l - z - 1];
			final int d = this.d & 0x7f;
			b[i] = (byte) (opmode == Cipher.DECRYPT_MODE ? (b[i] + d) % 0xff ^ x : (b[i] ^ x) - d % 0xff);
		}
	}
}
