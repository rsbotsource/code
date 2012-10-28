package org.powerbot.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;

import javax.crypto.Cipher;

import org.powerbot.util.Configuration;
import org.powerbot.util.StringUtil;

/**
 * @author Paris
 */
public final class CryptFile {
	public static final Map<File, Class<?>[]> PERMISSIONS = new HashMap<File, Class<?>[]>();

	private final String name;
	private final File store;
	private final byte[] key;

	public CryptFile(final String id, final Class<?>... parents) {
		final long uid = Configuration.getUID();
		String hash = null;

		final MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(StringUtil.getBytesUtf8(id));
			for (int i = 0; i < 8; i++) {
				md.update((byte) ((uid >> (i << 3)) & 0xff));
			}
			hash = StringUtil.byteArrayToHexString(md.digest()).substring(0, 6);
		} catch (final NoSuchAlgorithmException ignored) {
			final Adler32 c = new Adler32();
			c.update(StringUtil.getBytesUtf8(id));
			c.update((int) (uid & 0xffffffff));
			c.update((int) (uid >> 32));
			final long l = c.getValue();
			hash = Long.toHexString(l);
		}

		if (hash == null) {
			hash = Integer.toHexString(id.hashCode());
		}

		name = hash + ".tmp";
		store = new File(System.getProperty("java.io.tmpdir"), name);
		key = CipherStreams.getSharedKey(StringUtil.getBytesUtf8(name));
		PERMISSIONS.put(store, parents);
	}

	public boolean exists() {
		return store.isFile() && store.canRead() && store.canWrite();
	}

	public void delete() {
		store.delete();
	}

	public InputStream download(final URL url) throws IOException {
		final HttpURLConnection con = HttpClient.getHttpConnection(url);

		if (exists()) {
			con.setIfModifiedSince(store.lastModified());
		}

		if (con.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
			IOHelper.write(HttpClient.getInputStream(con), getOutputStream());
		}

		con.disconnect();
		return getInputStream();
	}

	public InputStream getInputStream() throws IOException {
		if (!store.isFile()) {
			throw new FileNotFoundException(store.toString());
		}
		return new XORInputStream(new FileInputStream(store), key, Cipher.ENCRYPT_MODE);
	}

	public OutputStream getOutputStream() throws IOException {
		return new XOROutputStream(new FileOutputStream(store), key, Cipher.DECRYPT_MODE);
	}
}
