package org.powerbot.game.client;

public interface RSPlayer extends RSCharacter {
	public int getTeam();

	public Object getComposite();

	public Object getName();

	public int getPrayerIcon();

	public int getSkullIcon();

	public int getLevel();
}
