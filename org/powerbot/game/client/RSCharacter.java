package org.powerbot.game.client;

public interface RSCharacter extends RSAnimable {
	public int getInteracting();

	public int isMoving();

	public Object getAnimationQueue();

	public Object getAnimation();

	public int getHeight();

	public Object getCombatStatusList();

	public int getOrientation();

	public Object getMessageData();

	public Object getPassiveAnimation();

	public Model getModel();
}
