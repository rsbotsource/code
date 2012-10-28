package org.powerbot.util;

import java.io.File;
import java.net.InetAddress;
import java.security.Permission;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.powerbot.concurrent.TaskContainer;
import org.powerbot.game.GameDefinition;
import org.powerbot.ipc.Controller;
import org.powerbot.service.scripts.ScriptClassLoader;
import org.powerbot.util.Configuration.OperatingSystem;
import org.powerbot.util.io.CryptFile;

/**
 * @author Paris
 */
public class RestrictedSecurityManager extends SecurityManager {
	private static final Logger log = Logger.getLogger("Security");
	public static final String DNS1 = "8.8.8.8", DNS2 = "8.8.4.4";

	@Override
	public void checkAccept(final String host, final int port) {
		if (isCallingClass(Controller.class)) {
			return;
		}
		if (port == 53 && (host.equals(DNS1) || host.equals(DNS2))) {
			super.checkAccept(host, port);
			return;
		}
		throw new SecurityException();
	}

	@Override
	public void checkAccess(final Thread t) {
		super.checkAccess(t);
	}

	@Override
	public void checkAccess(final ThreadGroup g) {
		if (isScriptThread() && !isCallingClass(TaskContainer.class, sun.net.www.http.KeepAliveCache.class, sun.net.www.protocol.http.HttpURLConnection.class)) {
			log.severe("Thread group access denied");
			throw new SecurityException();
		}
		super.checkAccess(g);
	}

	@Override
	public void checkConnect(final String host, final int port) {
		checkConnect(host, port, null);
	}

	@Override
	public void checkConnect(final String host, final int port, final Object context) {
		if (!(port == 80 || port == 443 || port == 53 || port == 43594 || port == -1)) {
			if (!isCallingClass(Controller.class)) {
				log.severe("Connection denied to port " + port);
				throw new SecurityException();
			}
		}
		if (host.equals("localhost") || host.endsWith(".localdomain") || host.startsWith("127.") || host.startsWith("192.168.") || host.startsWith("10.") || host.endsWith("::1")) {
			if (!isCallingClass(Configuration.class, Controller.class)) {
				log.severe("Connection denied to localhost");
				throw new SecurityException();
			}
		}
		if (context == null) {
			super.checkConnect(host, port);
		} else {
			super.checkConnect(host, port, context);
		}
	}

	@Override
	public void checkCreateClassLoader() {
		if (isScriptThread() && !isCallingClass(javax.swing.UIDefaults.class, java.io.ObjectOutputStream.class, java.io.ObjectInputStream.class,
				java.lang.reflect.Proxy.class)) {
			log.severe("Creating class loader denied");
			throw new SecurityException();
		}
		super.checkCreateClassLoader();
	}

	@Override
	public void checkDelete(final String file) {
		checkFilePath(file, false);
		super.checkDelete(file);
	}

	@Override
	public void checkExec(final String cmd) {
		if (isScriptThread()) {
			throw new SecurityException();
		}
		super.checkExec(cmd);
	}

	@Override
	public void checkExit(final int status) {
		if (isScriptThread()) {
			throw new SecurityException();
		}
		super.checkExit(status);
	}

	@Override
	public void checkListen(final int port) {
		if (port != 0) {
			throw new SecurityException();
		}
		super.checkListen(port);
	}

	@Override
	public void checkMulticast(final InetAddress maddr) {
		throw new SecurityException();
	}

	@Override
	public void checkMulticast(final InetAddress maddr, final byte ttl) {
		throw new SecurityException();
	}

	@Override
	public void checkPermission(final Permission perm) {
		if (perm instanceof RuntimePermission) {
			if (perm.getName().equals("setSecurityManager")) {
				throw new SecurityException();
			}
		}
	}

	@Override
	public void checkPermission(final Permission perm, final Object context) {
		checkPermission(perm);
	}

	@Override
	public void checkPrintJobAccess() {
		throw new SecurityException();
	}

	@Override
	public void checkRead(final String file) {
		checkFilePath(file, true);
		super.checkRead(file);
	}

	@Override
	public void checkRead(final String file, final Object context) {
		checkRead(file);
		super.checkRead(file, context);
	}

	@Override
	public void checkSetFactory() {
		if (isScriptThread()) {
			throw new SecurityException();
		}
		super.checkSetFactory();
	}

	@Override
	public void checkSystemClipboardAccess() {
		throw new SecurityException();
	}

	@Override
	public void checkWrite(final String file) {
		checkFilePath(file, false);
		super.checkWrite(file);
	}

	private void checkFilePath(final String pathRaw, final boolean readOnly) {
		final String path = StringUtil.urlDecode(new File(pathRaw).getAbsolutePath());
		final String tmp = System.getProperty("java.io.tmpdir");

		// permission controls for crypt files
		for (final Entry<File, Class<?>[]> entry : CryptFile.PERMISSIONS.entrySet()) {
			if (entry.getKey().equals(pathRaw)) {
				if (!isCallingClass(entry.getValue())) {
					throw new SecurityException();
				}
			}
		}

		// allow access for privileged thread groups
		if (Thread.currentThread().getThreadGroup().getName().startsWith(GameDefinition.THREADGROUPNAMEPREFIX)) {
			return;
		}

		// allow read permissions to all files
		if (readOnly) {
			return;
		}

		// hack fix for odd occurrence on OS X
		if (Configuration.OS == OperatingSystem.MAC && path.startsWith("/var/folders/") && Thread.currentThread().getName().equals("main")) {
			return;
		}

		// allow write access to temp directory
		if ((path + File.separator).startsWith(tmp)) {
			return;
		}

		log.severe((readOnly ? "Read" : "Write") + " denied: " + path + " on " + Thread.currentThread().getName() + "/" + Thread.currentThread().getThreadGroup().getName());
		throw new SecurityException();
	}

	private boolean isCallingClass(final Class<?>... classes) {
		for (final Class<?> context : getClassContext()) {
			for (final Class<?> clazz : classes) {
				if (clazz.isAssignableFrom(context)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isScriptThread() {
		final Class<?>[] context = getClassContext();
		for (int i = 1; i < context.length; i++) {
			final ClassLoader loader = context[i].getClassLoader();
			if (loader != null && loader.getClass().isAssignableFrom(ScriptClassLoader.class)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isScriptThread(final Thread t) {
		final ClassLoader loader = t.getContextClassLoader();
		return loader != null && loader.getClass().isAssignableFrom(ScriptClassLoader.class);
	}

	public static void assertNonScript() {
		final SecurityManager sm = System.getSecurityManager();
		if (sm == null || !(sm instanceof RestrictedSecurityManager) || ((RestrictedSecurityManager) sm).isScriptThread()) {
			throw new SecurityException();
		}
	}
}
