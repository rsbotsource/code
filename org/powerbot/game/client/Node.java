package org.powerbot.game.client;

public interface Node {
	public long getID();

	public Node getNext();

	public Node getPrevious();
}
