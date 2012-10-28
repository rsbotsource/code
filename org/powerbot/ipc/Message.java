package org.powerbot.ipc;

import java.io.Serializable;

/**
 * @author Paris
 */
public final class Message implements Serializable {
	private static final long serialVersionUID = 5767516607322553568L;
	private final boolean response;
	private MessageType type;
	private Object[] args;

	public enum MessageType {
		NONE,
		ALIVE,
		DIE,
		MODE,
		RUNNING,
		LISTENING,
		LOADED,
		SIGNIN,
		SCRIPT,
	}

	public Message(final MessageType type) {
		this(false, type);
	}

	public Message(final boolean response, final MessageType type) {
		this(response, type, new Object[] {});
	}

	public Message(final boolean response, final MessageType type, final Object... args) {
		this.response = response;
		this.type = type;
		this.args = args;
	}

	public boolean isResponse() {
		return response;
	}

	public void setMessageType(final MessageType type) {
		this.type = type;
	}

	public MessageType getMessageType() {
		return type;
	}

	public void setArgs(final Object... args) {
		this.args = args;
	}

	public Object[] getArgs() {
		return args;
	}

	public int getIntArg() {
		return args.length > 0 && args[0] instanceof Integer ? (Integer) args[0] : -1;
	}

	public String getStringArg() {
		return args.length > 0 && args[0] instanceof String ? (String) args[0] : null;
	}
}
