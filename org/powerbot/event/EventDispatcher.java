package org.powerbot.event;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.powerbot.concurrent.Task;
import org.powerbot.game.bot.event.listener.MessageListener;
import org.powerbot.game.bot.event.listener.PaintListener;
import org.powerbot.game.bot.event.listener.TextPaintListener;

/**
 * An event manager responsible for dispatching events to listeners associated with this dispatcher.
 *
 * @author Timer
 */
public class EventDispatcher implements EventManager, Task {
	private static final Logger log = Logger.getLogger(EventDispatcher.class.getName());
	private volatile boolean active;
	private final List<EventObject> queue = new ArrayList<EventObject>();
	private final List<EventListener> listeners = new ArrayList<EventListener>();
	private final List<Long> listenerMasks = new ArrayList<Long>();
	private final Object treeLock = new Object();

	public static final int MOUSE_EVENT = 0x1;
	public static final int MOUSE_MOTION_EVENT = 0x2;
	public static final int MOUSE_WHEEL_EVENT = 0x4;
	public static final int FOCUS_EVENT = 0x8;
	public static final int KEY_EVENT = 0x10;

	public static final int MESSAGE_EVENT = 0x400;
	public static final int PAINT_EVENT = 0x1000;
	public static final int TEXT_PAINT_EVENT = 0x2000;

	public EventDispatcher() {
		active = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatch(final EventObject event) {
		synchronized (queue) {
			queue.add(event);
			queue.notify();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void fire(final EventObject eventObject) {
		fire(eventObject, getType(eventObject));
	}

	/**
	 * Fires this event by matching masks of interfaces and the event type.
	 *
	 * @param eventObject The event to fire.
	 * @param type        The type of event to check for within the mask.
	 */
	public void fire(final EventObject eventObject, final int type) {
		synchronized (treeLock) {
			if (!active) {
				return;
			}
			final int size = listeners.size();
			for (int index = 0; index < size; index++) {
				final long listenerType = listenerMasks.get(index);
				if ((listenerType & type) == 0) {
					continue;
				}
				final EventListener listener = listeners.get(index);
				if (eventObject instanceof MouseEvent) {
					final MouseEvent me = (MouseEvent) eventObject;
					switch (me.getID()) {
					case MouseEvent.MOUSE_PRESSED:
						((MouseListener) listener).mousePressed(me);
						break;
					case MouseEvent.MOUSE_RELEASED:
						((MouseListener) listener).mouseReleased(me);
						break;
					case MouseEvent.MOUSE_CLICKED:
						((MouseListener) listener).mouseClicked(me);
						break;
					case MouseEvent.MOUSE_ENTERED:
						((MouseListener) listener).mouseEntered(me);
						break;
					case MouseEvent.MOUSE_EXITED:
						((MouseListener) listener).mouseExited(me);
						break;
					case MouseEvent.MOUSE_MOVED:
						((MouseMotionListener) listener).mouseMoved(me);
						break;
					case MouseEvent.MOUSE_DRAGGED:
						((MouseMotionListener) listener).mouseDragged(me);
						break;
					case MouseEvent.MOUSE_WHEEL:
						((MouseWheelListener) listener).mouseWheelMoved((MouseWheelEvent) me);
						break;
					}
					break;
				} else if (eventObject instanceof FocusEvent) {
					final FocusEvent focusEvent = (FocusEvent) listener;
					switch (focusEvent.getID()) {
					case FocusEvent.FOCUS_GAINED:
						((FocusListener) listener).focusGained(focusEvent);
						break;
					case FocusEvent.FOCUS_LOST:
						((FocusListener) listener).focusLost(focusEvent);
						break;
					}
				} else if (eventObject instanceof KeyEvent) {
					final KeyEvent ke = (KeyEvent) eventObject;
					switch (ke.getID()) {
					case KeyEvent.KEY_TYPED:
						((KeyListener) listener).keyTyped(ke);
						break;
					case KeyEvent.KEY_PRESSED:
						((KeyListener) listener).keyPressed(ke);
						break;
					case KeyEvent.KEY_RELEASED:
						((KeyListener) listener).keyReleased(ke);
						break;
					}
				} else if (eventObject instanceof GeneralEvent) {
					final GeneralEvent gameEvent = (GeneralEvent) eventObject;
					gameEvent.dispatch(listener);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void accept(final EventListener eventListener) {
		synchronized (treeLock) {
			if (!listeners.contains(eventListener)) {
				listeners.add(eventListener);
				listenerMasks.add(getType(eventListener));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(final EventListener eventListener) {
		synchronized (treeLock) {
			final int id = listeners.indexOf(eventListener);
			if (id != -1) {
				listeners.remove(id);
				listenerMasks.remove(id);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setActive(final boolean active) {
		this.active = active;
		synchronized (queue) {
			queue.notify();
		}
	}

	/**
	 * Returns the mask associated with this event listener.
	 *
	 * @param el The <code>EventListener</code> to analyze.
	 * @return The mask of this [these] listener[s].
	 */
	public static long getType(final EventListener el) {
		long mask = 0;
		if (el instanceof MouseListener) {
			mask |= EventDispatcher.MOUSE_EVENT;
		}
		if (el instanceof MouseMotionListener) {
			mask |= EventDispatcher.MOUSE_MOTION_EVENT;
		}
		if (el instanceof MouseWheelListener) {
			mask |= EventDispatcher.MOUSE_WHEEL_EVENT;
		}
		if (el instanceof KeyListener) {
			mask |= EventDispatcher.KEY_EVENT;
		}
		if (el instanceof FocusListener) {
			mask |= EventDispatcher.FOCUS_EVENT;
		}

		if (el instanceof MessageListener) {
			mask |= EventDispatcher.MESSAGE_EVENT;
		}
		if (el instanceof PaintListener) {
			mask |= EventDispatcher.PAINT_EVENT;
		}
		if (el instanceof TextPaintListener) {
			mask |= EventDispatcher.TEXT_PAINT_EVENT;
		}

		return mask;
	}

	/**
	 * Returns the type of event this object is.
	 *
	 * @param e The event to identify.
	 * @return The type of the event provided associative with listener masks.
	 */
	public static int getType(final EventObject e) {
		if (e instanceof MouseEvent) {
			final MouseEvent me = (MouseEvent) e;
			switch (me.getID()) {
			case MouseEvent.MOUSE_PRESSED:
			case MouseEvent.MOUSE_RELEASED:
			case MouseEvent.MOUSE_CLICKED:
			case MouseEvent.MOUSE_ENTERED:
			case MouseEvent.MOUSE_EXITED:
				return EventDispatcher.MOUSE_EVENT;

			case MouseEvent.MOUSE_MOVED:
			case MouseEvent.MOUSE_DRAGGED:
				return EventDispatcher.MOUSE_MOTION_EVENT;

			case MouseEvent.MOUSE_WHEEL:
				return EventDispatcher.MOUSE_WHEEL_EVENT;
			}
		} else if (e instanceof FocusEvent) {
			final FocusEvent fe = (FocusEvent) e;
			switch (fe.getID()) {
			case FocusEvent.FOCUS_GAINED:
			case FocusEvent.FOCUS_LOST:
				return EventDispatcher.FOCUS_EVENT;
			}
		} else if (e instanceof KeyEvent) {
			final KeyEvent ke = (KeyEvent) e;
			switch (ke.getID()) {
			case KeyEvent.KEY_TYPED:
			case KeyEvent.KEY_PRESSED:
			case KeyEvent.KEY_RELEASED:
				return EventDispatcher.KEY_EVENT;
			}
		} else if (e instanceof GeneralEvent) {
			return ((GeneralEvent) e).type;
		}

		throw new RuntimeException("bad event");
	}

	/**
	 * Handles the firing of events within the dispatch queue.
	 */
	public void run() {
		active = true;
		while (active) {
			EventObject event = null;
			synchronized (queue) {
				while (active && queue.isEmpty()) {
					try {
						queue.wait();
					} catch (final InterruptedException e) {
						log.log(Level.SEVERE, "Event dispatcher: ", e);
					}
				}

				if (!queue.isEmpty()) {
					event = queue.remove(0);
				}
			}

			if (event != null) {
				try {
					fire(event);
				} catch (final Throwable t) {
					log.log(Level.SEVERE, "Event dispatcher: ", t);
				}
			}
		}
	}
}
