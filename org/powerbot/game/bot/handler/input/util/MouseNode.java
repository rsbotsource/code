package org.powerbot.game.bot.handler.input.util;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.ViewportEntity;

public class MouseNode {
	public static final Map<ThreadGroup, Integer> threadSpeed = new HashMap<ThreadGroup, Integer>();

	private final int priority;
	private final ViewportEntity viewportEntity;
	private final Filter<Point> filter;
	private final Object lock;
	private double speed;

	private Timer timer;

	private boolean completed;
	private boolean canceled;
	private boolean consumed;

	public MouseNode(final ViewportEntity viewportEntity, final Filter<Point> filter) {
		this(Mouse.PRIORITY_DEFAULT, viewportEntity, filter);
	}

	public MouseNode(final int priority, final ViewportEntity viewportEntity, final Filter<Point> filter) {
		final Integer speed = threadSpeed.get(Thread.currentThread().getThreadGroup());
		this.priority = priority;
		this.viewportEntity = viewportEntity;
		this.filter = filter;
		lock = new Object();
		this.speed = speed != null ? (double) speed / 1000d : 1.0;
		this.timer = null;
		completed = false;
		canceled = false;
		consumed = false;
	}

	public Timer getTimer() {
		if (timer == null) {
			timer = new Timer(Random.nextInt(6000, 8000));
		}
		return timer;
	}

	public int getPriority() {
		return priority;
	}

	public ViewportEntity getViewportEntity() {
		return viewportEntity;
	}

	public Filter<Point> getFilter() {
		return filter;
	}

	public Object getLock() {
		return lock;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void complete() {
		completed = true;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void cancel() {
		canceled = true;
	}

	public void consume() {
		if (consumed) {
			throw new RuntimeException("dual consume");
		}
		consumed = true;
	}

	public void reset() {
		consumed = false;
	}

	public boolean processable() {
		return !canceled && !completed && !consumed;
	}

	public double getSpeed() {
		return speed;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof MouseNode) {
			final MouseNode mouseNode = (MouseNode) o;
			return priority == mouseNode.priority && viewportEntity.equals(mouseNode.viewportEntity);
		}
		return false;
	}
}
