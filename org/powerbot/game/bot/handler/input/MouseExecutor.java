package org.powerbot.game.bot.handler.input;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.ViewportEntity;
import org.powerbot.game.bot.Bot;
import org.powerbot.game.bot.handler.input.util.MouseNode;
import org.powerbot.game.client.Client;

public class MouseExecutor {
	private final Client client;
	private final List<ForceModifier> forceModifiers;
	private final Vector velocity;
	private volatile MouseNode stepping_node;
	private final Point target;

	public MouseExecutor(final Bot bot) {
		this.client = bot.getClient();
		forceModifiers = new ArrayList<ForceModifier>(5);
		velocity = new Vector();
		target = new Point(-1, -1);
		setup();
	}

	public void step(final MouseNode node) {
		if (stepping_node != node) {
			target.setLocation(-1, -1);
			stepping_node = node;
		}
		final double multiplier = node.getSpeed();
		final org.powerbot.game.client.input.Mouse mouse = client.getMouse();
		final ViewportEntity viewportEntity = node.getViewportEntity();
		if (viewportEntity.validate()) {
			if (target.x == -1 || target.y == -1 || !viewportEntity.contains(target)) {
				final Point viewPortPoint = viewportEntity.getNextViewportPoint();
				if (!Calculations.isOnScreen(viewPortPoint.x, viewPortPoint.y)) {
					Time.sleep(Random.nextInt(25, 51));
					return;
				}
				target.setLocation(viewPortPoint);
			} else if (!Calculations.isOnScreen(target.x, target.y)) {
				target.setLocation(-1, -1);
				Time.sleep(Random.nextInt(100, 200));
				return;
			}
			final Point currentPoint = mouse.getLocation();
			/* Check if target point is the current mouse point. */
			if (target.equals(currentPoint)) {
				/* Consume this node to prevent further processing until completed, cancelled, or reset. */
				node.consume();

				/* Load the filter onto stack. */
				final Filter<Point> filter = node.getFilter();
				/* Submit new thread for further processing of this node (i.e. menu call). */
				/* Check if the filter accepts it. */
				if (filter.accept(currentPoint)) {
					/* Complete it for returning later. */
					node.complete();
				} else {
					/* Reset the consumed status to false and insert again. */
					node.reset();
				}
				/* Notify the reactor to wake up. */
				return;
			}
			final double deltaTime = Random.nextDouble(7D, 10D) / 1000D;
			final Vector forceVector = new Vector();
			for (ForceModifier modifier : forceModifiers) {
				final Vector f = modifier.apply(deltaTime, target);
				if (f == null) {
					continue;
				}
				forceVector.add(f);
			}

			if (Double.isNaN(forceVector.xUnits) || Double.isNaN(forceVector.yUnits)) {
				node.cancel();
				return;
			}
			velocity.add(forceVector.multiply(deltaTime));

			final Vector deltaPosition = velocity.multiply(deltaTime);
			if (deltaPosition.xUnits != 0 && deltaPosition.yUnits != 0) {
				int x = (int) currentPoint.getX() + (int) deltaPosition.xUnits;
				int y = (int) currentPoint.getY() + (int) deltaPosition.yUnits;
				Mouse.hop(x, y);
			}
			try {
				Thread.sleep((long) (deltaTime * 1000 / multiplier));
			} catch (final InterruptedException ignored) {
			}
		} else {
			node.cancel();
		}
	}

	private void setup() {
		forceModifiers.add(new ForceModifier() {
			//Target tracking
			public Vector apply(final double delta, final Point direction) {
				final org.powerbot.game.client.input.Mouse mouse = client.getMouse();
				final Point currentLocation = mouse.getLocation();
				final Vector targetVector = new Vector();
				targetVector.xUnits = direction.x - currentLocation.getX();
				targetVector.yUnits = direction.y - currentLocation.getY();
				if (targetVector.xUnits == 0 && targetVector.yUnits == 0) {
					return null;
				}
				final double angle = targetVector.getAngle();
				final double acceleration = Random.nextInt(2500, 3000);
				final Vector force = new Vector();
				force.xUnits = Math.cos(angle) * acceleration;
				force.yUnits = Math.sin(angle) * acceleration;
				return force;
			}
		});

		forceModifiers.add(new ForceModifier() {
			//Friction
			public Vector apply(final double delta, final Point direction) {
				return velocity.multiply(-1);
			}
		});

		forceModifiers.add(new ForceModifier() {
			//Velocity killer on destination (prevent loop-back)
			public Vector apply(final double delta, final Point direction) {
				final org.powerbot.game.client.input.Mouse mouse = client.getMouse();
				final Point currentLocation = mouse.getLocation();
				final Vector targetVector = new Vector();
				targetVector.xUnits = direction.x - currentLocation.getX();
				targetVector.yUnits = direction.y - currentLocation.getY();
				if (targetVector.xUnits > -3 && targetVector.xUnits < 3 &&
						targetVector.yUnits > -3 && targetVector.yUnits < -3) {
					velocity.xUnits = 0;
					velocity.yUnits = 0;
				}
				return null;
			}
		});

		forceModifiers.add(new ForceModifier() {
			//Target noise
			public Vector apply(final double delta, final Point direction) {
				final org.powerbot.game.client.input.Mouse mouse = client.getMouse();
				final Point currentLocation = mouse.getLocation();
				final Vector targetVector = new Vector();
				targetVector.xUnits = direction.x - currentLocation.getX();
				targetVector.yUnits = direction.y - currentLocation.getY();
				final double targetMagnitude = targetVector.getMagnitude();
				if (targetMagnitude > Random.nextInt(10, 20)) {
					final double angle = Random.nextDouble(-Math.PI, Math.PI);
					final Vector force = new Vector();
					final int acceleration = targetMagnitude > Random.nextInt(120, 200) ? Random.nextInt(3000, 4000) : Random.nextInt(100, 300);
					force.xUnits = Math.cos(angle) * acceleration;
					force.yUnits = Math.sin(angle) * acceleration;
					return force;
				}
				return null;
			}
		});

		forceModifiers.add(new ForceModifier() {
			//Pass near-target fix (high-velocity curve)
			public Vector apply(final double delta, final Point direction) {
				final org.powerbot.game.client.input.Mouse mouse = client.getMouse();
				final Point currentLocation = mouse.getLocation();
				final Vector targetVector = new Vector();
				targetVector.xUnits = direction.x - currentLocation.getX();
				targetVector.yUnits = direction.y - currentLocation.getY();
				final double targetMagnitude = targetVector.getMagnitude();
				if (targetMagnitude < Random.nextInt(120, 200)) {
					final double velocityMagnitude = velocity.getMagnitude();
					final double velocityLength = Math.pow(velocityMagnitude, 2);
					final double targetLength = Math.pow(targetMagnitude, 2);
					if (targetLength == 0) {
						return null;
					}
					final double computedLength = Math.sqrt(velocityLength / targetLength);
					Vector adjustedToTarget = targetVector.multiply(computedLength);

					Vector force = new Vector();
					force.xUnits = (adjustedToTarget.xUnits - velocity.xUnits) / (delta);
					force.yUnits = (adjustedToTarget.yUnits - velocity.yUnits) / (delta);

					final double adjustmentFactor = 8D / targetMagnitude;
					if (adjustmentFactor < 1D) {
						force = force.multiply(adjustmentFactor);
					}
					if (targetMagnitude < 10D) {
						force = force.multiply(0.5D);
					}
					return force;
				}
				return null;
			}
		});
	}

	private interface ForceModifier {
		public Vector apply(final double delta, final Point direction);
	}

	private final class Vector {
		public double xUnits;
		public double yUnits;

		public void add(final Vector vector) {
			xUnits += vector.xUnits;
			yUnits += vector.yUnits;
		}

		public Vector multiply(final double factor) {
			final Vector out = new Vector();
			out.xUnits = xUnits * factor;
			out.yUnits = yUnits * factor;
			return out;
		}

		public double getMagnitude() {
			return Math.sqrt(xUnits * xUnits + yUnits * yUnits);
		}

		public double getAngle() {
			return Math.atan2(yUnits, xUnits);
		}
	}

	public void cancel() {
		if (stepping_node != null) {
			stepping_node.cancel();
		}
	}
}
