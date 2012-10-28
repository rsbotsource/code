package org.powerbot.concurrent;

import java.util.EventListener;

import org.powerbot.game.api.util.Time;

public abstract class LoopTask implements Task, EventListener {
	private boolean running;
	private boolean killed;
	private Processor processor;

	public LoopTask() {
		running = true;
		killed = false;
		processor = null;
	}

	public abstract int loop();

	public void init(final Processor processor) {
		this.processor = processor;
	}

	@Override
	public void run() {
		while (running) {
			final int wait = loop();
			if (wait > 0) {
				Time.sleep(wait);
			} else if (wait == -1) {
				running = false;
				kill();
			}
		}
	}

	public void start() {
		running = true;
	}

	public void stop() {
		running = false;
		if (processor != null) {
			processor.terminated(this);
		}
	}

	public void kill() {
		killed = true;
		if (processor != null) {
			processor.terminated(this);
		}
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isKilled() {
		return killed;
	}
}
