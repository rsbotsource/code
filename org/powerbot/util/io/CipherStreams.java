package org.powerbot.util.io;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.powerbot.util.StringUtil;

/**
 * @author Paris
 */
public final class CipherStreams {
	private final static byte[] SHAREDKEY = {0, 0x32, 0x4f, 0x2a, 0x7f, 0x01, 0x5a, 0x69};

	private static Cipher getCipher(final int opmode, final byte[] key, final String cipherAlgorithm, String keyAlgorithm) throws GeneralSecurityException {
		final Cipher c = Cipher.getInstance(cipherAlgorithm);
		byte[] iv = null;
		int len = 16;
		final String[] keyArgs = keyAlgorithm.split("/");
		if (keyArgs.length > 1 && !keyArgs[1].isEmpty()) {
			iv = Base64.decode(StringUtil.getBytesUtf8(keyArgs[1]));
		}
		if (keyArgs.length > 2) {
			try {
				len = Integer.parseInt(keyArgs[2]);
			} catch (final NumberFormatException ignored) {
				len = 16;
			}
		}
		final SecretKeySpec sks = new SecretKeySpec(Arrays.copyOf(key, len), keyArgs[0]);
		if (iv == null) {
			c.init(opmode, sks);
		} else {
			c.init(opmode, sks, new IvParameterSpec(iv));
		}
		return c;
	}

	public static FilterInputStream getCipherInputStream(final InputStream in, final int opmode, final byte[] key, final String cipherAlgorithm, final String keyAlgorithm) throws GeneralSecurityException {
		if (cipherAlgorithm.equals("XOR")) {
			return new XORInputStream(in, getSharedKey(key), opmode);
		}
		return new CipherInputStream(in, getCipher(opmode, key, cipherAlgorithm, keyAlgorithm));
	}

	public static FilterOutputStream getCipherOutputStream(final OutputStream out, final int opmode, final byte[] key, final String cipherAlgorithm, final String keyAlgorithm) throws GeneralSecurityException {
		if (cipherAlgorithm.equals("XOR")) {
			return new XOROutputStream(out, getSharedKey(key), opmode);
		}
		return new CipherOutputStream(out, getCipher(opmode, key, cipherAlgorithm, keyAlgorithm));
	}

	public static byte[] getSharedKey(final byte[] salt) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (final NoSuchAlgorithmException ignored) {
		}
		md.update(SHAREDKEY);
		md.update(salt);
		return md.digest();
	}
}
