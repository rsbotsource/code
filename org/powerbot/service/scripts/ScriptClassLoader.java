package org.powerbot.service.scripts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.powerbot.util.io.IOHelper;

/**
 * @author Paris
 */
public final class ScriptClassLoader extends ClassLoader {
	private final URL base;
	final Map<String, byte[]> files;

	public ScriptClassLoader(final URL base) {
		this.base = base;
		files = null;
	}

	public ScriptClassLoader(final ZipInputStream in) throws IOException {
		files = new HashMap<String, byte[]>();
		ZipEntry entry;
		while ((entry = in.getNextEntry()) != null) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] data = new byte[IOHelper.BUFFER_SIZE];
			int len;
			while ((len = in.read(data)) != -1) {
				out.write(data, 0, len);
			}
			files.put(entry.getName(), out.toByteArray());
			in.closeEntry();
		}
		in.close();
		base = null;
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		final Class<?> clazz = findLoadedClass(name);
		if (clazz != null) {
			return clazz;
		}
		try {
			final String path = name.replace('.', '/') + ".class";
			final byte[] buf = base == null ? files.get(path) : IOHelper.read(getResourceAsStream(path));
			return defineClass(name, buf, 0, buf.length);
		} catch (final Exception ignored) {
			return super.loadClass(name);
		}
	}

	@Override
	public InputStream getResourceAsStream(final String name) {
		if (base == null) {
			return files.containsKey(name) ? new ByteArrayInputStream(files.get(name)) : null;
		}
		try {
			return new URL(base, name).openStream();
		} catch (IOException ignored) {
			return null;
		}
	}
}
