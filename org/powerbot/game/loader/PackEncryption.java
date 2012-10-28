package org.powerbot.game.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.powerbot.util.StringUtil;
import org.powerbot.util.io.IOHelper;

/**
 * A static utility to decrypt and extract classes from the RuneScape loader.
 *
 * @author Timer
 */
public class PackEncryption {
	private static final boolean CACHE = false;
	public static byte[] inner_pack_hash;

	public static Map<String, byte[]> extract(final byte[] secretKeySpecKey, final byte[] ivParameterSpecKey, final byte[] loader) {
		try {
			byte[] inner_pack = null;
			JarInputStream jarInputStream = new JarInputStream(new ByteArrayInputStream(loader));
			JarEntry entry;
			while ((entry = jarInputStream.getNextJarEntry()) != null && inner_pack == null) {
				if (entry.getName().equals("inner.pack.gz")) {
					inner_pack = read(jarInputStream);
					break;
				}
			}
			if (inner_pack == null) {
				return null;
			}
			final MessageDigest digest = MessageDigest.getInstance("SHA-1");
			PackEncryption.inner_pack_hash = digest.digest(inner_pack);

			final Map<String, byte[]> classes = new HashMap<String, byte[]>();
			final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeySpecKey, "AES");
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivParameterSpecKey));
			final byte[] unscrambled_inner_pack = cipher.doFinal(inner_pack);

			final Pack200.Unpacker unpacker = Pack200.newUnpacker();
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(0x500000);
			final JarOutputStream jos = new JarOutputStream(byteArrayOutputStream);
			final GZIPInputStream gzipIS = new GZIPInputStream(new ByteArrayInputStream(unscrambled_inner_pack));
			unpacker.unpack(gzipIS, jos);

			jarInputStream = new JarInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
			while ((entry = jarInputStream.getNextJarEntry()) != null) {
				String entryName = entry.getName();
				if (entryName.endsWith(".class")) {
					final byte[] read = read(jarInputStream);
					entryName = entryName.replace('/', '.');
					final String name = entryName.substring(0, entryName.length() - 6);
					classes.put(name, read);
				}
			}
			if (CACHE) {
				IOHelper.write(classes, new File(StringUtil.byteArrayToHexString(PackEncryption.inner_pack_hash).substring(0, 6) + ".jar"));
			}
			return classes;
		} catch (final Exception ignored) {
		}
		return null;
	}

	private static byte[] read(final JarInputStream inputStream) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final byte[] buffer = new byte[2048];
		int read;
		while (inputStream.available() > 0) {
			read = inputStream.read(buffer, 0, buffer.length);
			if (read < 0) {
				break;
			}
			out.write(buffer, 0, read);
		}
		return out.toByteArray();
	}

	public static byte[] toByte(final String key) {
		final int keyLength = key.length();
		if (keyLength == 0) {
			return new byte[0];
		} else {
			int unscrambledLength;
			final int lengthMod = -4 & keyLength + 3;
			unscrambledLength = lengthMod / 4 * 3;
			if (keyLength <= lengthMod - 2 || charIndex(key.charAt(lengthMod - 2)) == -1) {
				unscrambledLength -= 2;
			} else if (keyLength <= lengthMod - 1 || -1 == charIndex(key.charAt(lengthMod - 1))) {
				--unscrambledLength;
			}

			final byte[] keyBytes = new byte[unscrambledLength];
			unscramble(keyBytes, 0, key);
			return keyBytes;
		}
	}

	private static int charIndex(final char character) {
		return character >= 0 && character < charSet.length ? charSet[character] : -1;
	}

	private static int unscramble(final byte[] bytes, int offset, final String key) {
		final int start = offset;
		final int keyLength = key.length();
		int pos = 0;

		int readStart;
		int readOffset;
		while (true) {
			if (keyLength > pos) {
				final int currentChar = charIndex(key.charAt(pos));

				final int pos_1 = keyLength > pos + 1 ? charIndex(key.charAt(pos + 1)) : -1;
				final int pos_2 = pos + 2 < keyLength ? charIndex(key.charAt(2 + pos)) : -1;
				final int pos_3 = keyLength > pos + 3 ? charIndex(key.charAt(3 + pos)) : -1;
				bytes[offset++] = (byte) (pos_1 >>> 4 | currentChar << 2);
				if (pos_2 != -1) {
					bytes[offset++] = (byte) (pos_1 << 4 & 240 | pos_2 >>> 2);
					if (pos_3 != -1) {
						bytes[offset++] = (byte) (192 & pos_2 << 6 | pos_3);
						pos += 4;
						continue;
					}
				}
			}

			readOffset = offset;
			readStart = start;
			break;
		}

		return readOffset - readStart;
	}

	private static int[] charSet;

	static {
		int index;
		charSet = new int[128];
		for (index = 0; charSet.length > index; ++index) {
			charSet[index] = -1;
		}
		for (index = 65; index <= 90; ++index) {
			charSet[index] = index - 65;
		}
		for (index = 97; 122 >= index; ++index) {
			charSet[index] = index + 26 - 97;
		}
		for (index = 48; index <= 57; ++index) {
			charSet[index] = 4 + index;
		}
		final int[] var2 = charSet;
		charSet[43] = 62;
		var2[42] = 62;
		final int[] var1 = charSet;
		charSet[47] = 63;
		var1[45] = 63;
	}
}
