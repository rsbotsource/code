package org.powerbot.game.client;

public interface Render {
	public float getXMultiplier();

	public float getYMultiplier();

	public float getAbsoluteY();

	public float getAbsoluteX();

	public RenderData getViewport();
}
