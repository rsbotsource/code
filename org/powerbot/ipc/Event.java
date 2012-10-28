package org.powerbot.ipc;

import java.net.SocketAddress;

/**
 * @author Paris
 */
public interface Event {
	public boolean call(Message msg, SocketAddress sender);
}
