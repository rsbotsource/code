package org.powerbot.ipc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import org.powerbot.gui.BotChrome;
import org.powerbot.ipc.Message.MessageType;
import org.powerbot.service.NetworkAccount;
import org.powerbot.util.Configuration;
import org.powerbot.util.StringUtil;
import org.powerbot.util.io.XORInputStream;
import org.powerbot.util.io.XOROutputStream;

/**
 * @author Paris
 */
public final class Controller implements Runnable {
	private final static Logger log = Logger.getLogger(Controller.class.getName());
	private static Controller instance;
	private final DatagramSocket sock;
	private final List<Event> callbacks;
	private final int[] ports = new int[12];
	private final byte[] key;

	private Controller() throws IOException {
		for (int i = 0; i < ports.length; i++) {
			ports[i] = 43600 + i;
		}

		DatagramSocket sock = null;
		for (final int port : ports) {
			if ((sock = getIfAvailable(port)) != null) {
				break;
			}
		}
		this.sock = sock;

		if (this.sock == null) {
			throw new IOException();
		}

		callbacks = new ArrayList<Event>();
		key = StringUtil.getBytesUtf8(Long.toBinaryString(Configuration.getUID()) + Integer.toHexString(Configuration.VERSION));
	}

	public static Controller getInstance() {
		if (instance == null) {
			try {
				instance = new Controller();
				new Thread(instance).start();
			} catch (final IOException ignored) {
				ignored.printStackTrace();
			}
		}
		return instance;
	}

	private static DatagramSocket getIfAvailable(int port) {
	    DatagramSocket sock = null;
	    try {
	        sock = new DatagramSocket(port, InetAddress.getLocalHost());
	    } catch (IOException ignored) {
	    }
	    return sock;
	}

	public boolean isBound() {
		return sock != null && sock.isBound();
	}

	@Override
	public void run() {
		while (true) {
			boolean stop = false;
			byte[] buf = new byte[4096];
			final DatagramPacket packet = new DatagramPacket(buf, buf.length);

			try {
				sock.receive(packet);
				final ObjectInput in = new ObjectInputStream(new XORInputStream(new ByteArrayInputStream(packet.getData()), key, Cipher.DECRYPT_MODE));
				final Message msg = (Message) in.readObject();
				in.close();

				log.fine("[" + sock.getLocalPort() + "] received from: " + packet.getSocketAddress().toString() + " " + (msg.isResponse() ? "response" : "broadcast") + " " + msg.getMessageType());

				Message reply = new Message(true, msg.getMessageType());

				switch (msg.getMessageType()) {
				case NONE:
					reply = null;
					break;

				case ALIVE:
					reply.setArgs(true);
					break;

				case DIE:
					stop = true;
					BotChrome.getInstance().windowClosing(null);
					break;

				case MODE:
					reply.setArgs(Configuration.SUPERDEV ? 2 : Configuration.DEVMODE ? 1 : 0);
					break;

				case LISTENING:
					reply.setArgs(sock.getLocalPort());
					break;

				case LOADED:
					reply.setArgs(BotChrome.loaded ? 1 : 2);
					break;

				case SIGNIN:
					reply = null;
					NetworkAccount.getInstance().revalidate();
					BotChrome.getInstance().panel.loadingPanel.setAdVisible(!NetworkAccount.getInstance().isVIP());
					break;
				}

				if (msg.isResponse()) {
					for (final Event task : callbacks) {
						try {
							if (!task.call(reply, packet.getSocketAddress())) {
								break;
							}
						} catch (final Exception ignored) {
							continue;
						}
					}
				} else if (reply != null) {
					reply(reply, packet.getAddress(), packet.getPort());
				}

				packet.setLength(buf.length);
			} catch (final IOException ignored) {
				continue;
			} catch (final ClassNotFoundException ignored) {
				continue;
			}

			if (stop || sock.isClosed() || !sock.isBound()) {
				break;
			}
		}
	}

	private void reply(final Message msg, final InetAddress addr, final int port) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ObjectOutput out = new ObjectOutputStream(new XOROutputStream(bos, key, Cipher.ENCRYPT_MODE));
		out.writeObject(msg);
		final byte[] buf = bos.toByteArray();
		bos.close();
		sock.send(new DatagramPacket(buf, buf.length, addr, port));
	}

	public void broadcast(final Message msg) {
		for (final int port : ports) {
			if (port != sock.getLocalPort()) {
				try {
					reply(msg, InetAddress.getLocalHost(), port);
				} catch (final IOException ignored) {
				}
			}
		}
		try {
			Thread.sleep(0);
		} catch (final InterruptedException ignored) {
		}
	}

	public int getRunningInstances() {
		final AtomicInteger i = new AtomicInteger(1);
		final Event c = new Event() {
			@Override
			public boolean call(final Message msg, final SocketAddress sender) {
				if (msg.getMessageType() == MessageType.RUNNING) {
					i.incrementAndGet();
					return false;
				}
				return true;
			}
		};
		callbacks.add(c);
		broadcast(new Message(MessageType.RUNNING));
		callbacks.remove(c);
		return i.get();
	}

	public boolean isAnotherInstanceLoading() {
		final AtomicBoolean n = new AtomicBoolean(false);
		final Event c = new Event() {
			@Override
			public boolean call(Message msg, final SocketAddress sender) {
				if (msg.getMessageType() == MessageType.LOADED && msg.getIntArg() == 2) {
					n.set(true);
					return false;
				}
				return true;
			}
		};
		callbacks.add(c);
		broadcast(new Message(MessageType.LOADED));
		callbacks.remove(c);
		return n.get();
	}
}
