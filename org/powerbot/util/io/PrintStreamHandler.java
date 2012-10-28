package org.powerbot.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.powerbot.util.Configuration;
import org.powerbot.util.RestrictedSecurityManager;

/**
 * @author Paris
 */
public class PrintStreamHandler extends Handler {
	private final PrintStream out, err;

	public PrintStreamHandler() {
		this(System.out, System.err);
	}

	public PrintStreamHandler(final File file) throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(file));
		err = out;
	}

	public PrintStreamHandler(final PrintStream out, final PrintStream err) {
		this.out = out;
		this.err = err;
	}

	@Override
	public void close() throws SecurityException {
		out.close();
		err.close();
	}

	@Override
	public void flush() {
		out.flush();
		err.flush();
	}

	@Override
	public synchronized void publish(final LogRecord record) {
		final String text = record.getMessage().trim();
		if (text.length() == 0) {
			return;
		}
		final int level = record.getLevel().intValue();
		final PrintStream std = level >= Level.WARNING.intValue() ? err : out;
		std.print('[');
		std.print(record.getLevel().getName());
		std.print("] ");
		if (Configuration.SUPERDEV || (Configuration.DEVMODE && RestrictedSecurityManager.isScriptThread(Thread.currentThread()))) {
			std.print(record.getLoggerName());
			std.print(": ");
		}
		std.print(text);
		final Throwable throwable = record.getThrown();
		if (throwable != null) {
			throwable.printStackTrace(std);
		} else {
			std.println();
		}
	}
}
