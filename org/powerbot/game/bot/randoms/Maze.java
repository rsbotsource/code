package org.powerbot.game.bot.randoms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.map.LocalPath;
import org.powerbot.game.api.wrappers.map.Path;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Maze", authors = {"Timer"}, version = 1.0)
public class Maze extends AntiRandom {
	private static final Tile TILE_CENTER = new Tile(2911, 4576, 0);
	private static final int LOCATION_ID_STRANGE_OBJECT = 3634;
	private static final int WIDGET_CHAT = 1184;
	private static final int WIDGET_CHAT_TEXT = 13;

	private final Timer timeout = new Timer(600000);
	private LinkedList<Tile> path = null;

	private final Door[] DOORS = {
			new Door(2930, 4590, 'w'), new Door(2901, 4598, 's'), new Door(2903, 4588, 's'),
			new Door(2902, 4575, 'e'), new Door(2932, 4598, 's'), new Door(2909, 4600, 's'),
			new Door(2908, 4592, 's'), new Door(2906, 4586, 's'), new Door(2916, 4568, 'n'),
			new Door(2904, 4573, 'e'), new Door(2892, 4578, 'e'), new Door(2930, 4561, 'w'),
			new Door(2915, 4560, 'n'), new Door(2909, 4562, 'n'), new Door(2898, 4570, 'e'),
			new Door(2913, 4598, 's'), new Door(2936, 4556, 'w'), new Door(2896, 4562, 'e'),
			new Door(2926, 4575, 'w'), new Door(2890, 4566, 'e'), new Door(2916, 4586, 's'),
			new Door(2930, 4554, 'n'), new Door(2910, 4572, 'n'), new Door(2908, 4596, 's'),
			new Door(2893, 4600, 's'), new Door(2896, 4582, 'e'), new Door(2911, 4566, 'n'),
			new Door(2894, 4587, 'e'), new Door(2921, 4600, 's'), new Door(2932, 4575, 'w'),
			new Door(2900, 4567, 'e'), new Door(2919, 4594, 's'), new Door(2910, 4582, 's'),
			new Door(2894, 4567, 'e'), new Door(2898, 4585, 'e'), new Door(2897, 4596, 's'),
			new Door(2924, 4583, 'w'), new Door(2924, 4592, 's'), new Door(2920, 4582, 'w'),
			new Door(2905, 4560, 'n'), new Door(2930, 4581, 'w'), new Door(2890, 4588, 'e'),
			new Door(2922, 4575, 'w'), new Door(2903, 4554, 'n'), new Door(2888, 4596, 'e'),
			new Door(2912, 4552, 'n'), new Door(2924, 4566, 'w'), new Door(2910, 4576, 'e'),
			new Door(2934, 4568, 'w')
	};
	private LinkedList<Door> allowedDoors = null;

	@Override
	public boolean validate() {
		final boolean validated = Calculations.distanceTo(TILE_CENTER) < 100 && SceneEntities.getNearest(3626, 3649) != null;
		if (!validated) {
			clean();
		}
		return validated;
	}

	@Override
	public void run() {
		if (allowedDoors == null) {
			allowedDoors = new LinkedList<Door>(Arrays.asList(DOORS));
		}

		if (!timeout.isRunning()) {
			bot.stopScript();
			return;
		}

		if (Players.getLocal().isMoving() || Players.getLocal().getAnimation() != -1 || Game.getClientState() != 11) {
			Time.sleep(Random.nextInt(80, 150));
			return;
		}

		if (Players.getLocal().getLocation().equals(TILE_CENTER)) {
			final SceneObject shrine = SceneEntities.getNearest(LOCATION_ID_STRANGE_OBJECT);
			if (shrine != null && Players.getLocal().getAnimation() == -1 && shrine.interact("Touch")) {
				for (int i = 0; i < 3000; i += 20) {
					if (Players.getLocal().getAnimation() != -1) {
						break;
					}
				}
				Time.sleep(20);
			}
			Time.sleep(Random.nextInt(500, 700));
			return;
		}

		path = findCentre();
		Door nearestDoor = null;
		System.out.println(path);

		if (path != null) {
			main:
			for (final Tile doorStep : path) {
				for (final Door door : allowedDoors) {
					if ((door.main.equals(doorStep) || door.after.equals(doorStep)) && (door.main.canReach() || door.after.canReach())) {
						nearestDoor = door;
						break main;
					}
				}
			}

			if (nearestDoor != null) {
				if (!nearestDoor.main.isOnScreen()) {
					final Path path = Walking.findPath(nearestDoor.main);
					if (path.traverse()) {
						Time.sleep(Random.nextInt(800, 1400));
					}
					return;
				}

				final WidgetChild notification = Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT);
				if (notification != null && notification.validate()) {
					final String text = notification.getText().toLowerCase().trim();
					if (text.contains("right way")) {
						allowedDoors.remove(nearestDoor);
						path = null;
						Time.sleep(Random.nextInt(1000, 1800));
						return;
					}
				}

				final SceneObject door = getDoor(nearestDoor);
				if (door != null && door.isOnScreen()) {
					if (door.interact("Open", "Door")) {
						Time.sleep(Random.nextInt(1800, 3500));
						return;
					}
					Camera.setAngle(nearestDoor.direction);
					Camera.setPitch(true);
				}
				Time.sleep(Random.nextInt(800, 1400));
				return;
			}
		}

		Time.sleep(Random.nextInt(1000, 2500));
	}

	private void clean() {
		allowedDoors = null;
		path = null;
		timeout.reset();
	}

	private SceneObject getDoor(final Door door) {
		return SceneEntities.getNearest(new Filter<SceneObject>() {
			public boolean accept(final SceneObject location) {
				return location.getId() >= 3628 && location.getId() <= 3632 && (location.getLocation().equals(door.main));
			}
		});
	}

	private LinkedList<Tile> findCentre() {
		final Tile start = Players.getLocal().getLocation();
		final Tile end = TILE_CENTER;
		if (start.getPlane() != end.getPlane()) {
			return null;
		}
		final Tile base = Game.getMapBase();

		final int curr_plane = start.getPlane();
		final int base_x = base.getX(), base_y = base.getY();
		final int curr_x = start.getX() - base_x, curr_y = start.getY() - base_y;
		int dest_x = end.getX() - base_x, dest_y = end.getY() - base_y;

		final int plane = Game.getPlane();
		if (curr_plane != plane) {
			return null;
		}
		final int[][] flags = Walking.getCollisionFlags(plane);
		final Tile offset = Walking.getCollisionOffset(plane);
		final int offX = offset.getX();
		final int offY = offset.getY();

		if (flags == null || curr_x < 0 || curr_y < 0 || curr_x >= flags.length || curr_y >= flags.length) {
			return null;
		} else if (dest_x < 0 || dest_y < 0 || dest_x >= flags.length || dest_y >= flags.length) {
			if (dest_x < 0) {
				dest_x = 0;
			} else if (dest_x >= flags.length) {
				dest_x = flags.length - 1;
			}
			if (dest_y < 0) {
				dest_y = 0;
			} else if (dest_y >= flags.length) {
				dest_y = flags.length - 1;
			}
		}

		final HashSet<LocalPath.Vertex> open = new HashSet<LocalPath.Vertex>();
		final HashSet<LocalPath.Vertex> closed = new HashSet<LocalPath.Vertex>();
		LocalPath.Vertex curr = new LocalPath.Vertex(curr_x, curr_y, curr_plane);
		final LocalPath.Vertex dest = new LocalPath.Vertex(dest_x, dest_y, curr_plane);

		curr.f = LocalPath.heuristic(curr, dest);
		open.add(curr);

		while (!open.isEmpty()) {
			curr = LocalPath.lowest_f(open);
			if (curr.equals(dest)) {
				return path(curr, base_x, base_y);
			}
			open.remove(curr);
			closed.add(curr);
			for (final LocalPath.Vertex next : successors(curr, base, offX, offY, flags)) {
				if (!closed.contains(next)) {
					final double t = curr.g + LocalPath.dist(curr, next);
					boolean use_t = false;
					if (!open.contains(next)) {
						open.add(next);
						use_t = true;
					} else if (t < next.g) {
						use_t = true;
					}
					if (use_t) {
						next.prev = curr;
						next.g = t;
						next.f = t + LocalPath.heuristic(next, dest);
					}
				}
			}
		}

		return null;
	}

	public LinkedList<Tile> path(final LocalPath.Vertex end, final int base_x, final int base_y) {
		final LinkedList<Tile> path = new LinkedList<Tile>();
		LocalPath.Vertex p = end;
		while (p != null) {
			if (p.special || p.equals(end)) {
				path.addFirst(p.get(base_x, base_y));
			}
			p = p.prev;
		}
		return path;
	}

	private List<LocalPath.Vertex> successors(final LocalPath.Vertex t, final Tile base, final int offX, final int offY, final int[][] flags) {
		final LinkedList<LocalPath.Vertex> tiles = new LinkedList<LocalPath.Vertex>();
		final int x = t.x, y = t.y, z = t.z;
		final int f_x = x - offX, f_y = y - offY;
		final int upper = flags.length - 1;
		if (f_y > 0 && ((flags[f_x][f_y - 1] & 0x1280102) == 0 || (flags[f_x][f_y - 1] & 0x1280108) == 0x1000000 ||
				hasDoor(t.x, t.y, base, 'n') || hasDoor(t.x, t.y + 1, base, t))) {
			tiles.add(new LocalPath.Vertex(x, y - 1, z, hasDoor(t.x, t.y, base, 'n') || hasDoor(t.x, t.y + 1, base, t)));
		}
		if (f_x > 0 && ((flags[f_x - 1][f_y] & 0x1280108) == 0 || (flags[f_x - 1][f_y] & 0x1280108) == 0x1000000 ||
				hasDoor(t.x, t.y, base, 'w') || hasDoor(t.x - 1, t.y, base, t))) {
			tiles.add(new LocalPath.Vertex(x - 1, y, z, hasDoor(t.x, t.y, base, 'w') || hasDoor(t.x - 1, t.y, base, t)));
		}
		if (f_y < upper && ((flags[f_x][f_y + 1] & 0x1280120) == 0 || (flags[f_x][f_y + 1] & 0x1280108) == 0x1000000 ||
				hasDoor(t.x, t.y, base, 's') || hasDoor(t.x, t.y - 1, base, t))) {
			tiles.add(new LocalPath.Vertex(x, y + 1, z, hasDoor(t.x, t.y, base, 's') || hasDoor(t.x, t.y - 1, base, t)));
		}
		if (f_x < upper && ((flags[f_x + 1][f_y] & 0x1280180) == 0 || (flags[f_x + 1][f_y] & 0x1280108) == 0x1000000 ||
				hasDoor(t.x, t.y, base, 'e') || hasDoor(t.x + 1, t.y, base, t))) {
			tiles.add(new LocalPath.Vertex(x + 1, y, z, hasDoor(t.x, t.y, base, 'e') || hasDoor(t.x + 1, t.y, base, t)));
		}
		return tiles;
	}


	private boolean hasDoor(final int x, final int y, final Tile base, final char direction) {
		for (final Door door : allowedDoors) {
			if (door.main.getX() == x + base.getX() && door.main.getY() == y + base.getY() && door.direction == direction) {
				return true;
			}
		}
		return false;
	}

	private boolean hasDoor(final int x, final int y, final Tile base, final LocalPath.Vertex source) {
		char dir = 'a';
		if (source.y < y) {
			dir = 's';
		}
		if (source.x < x) {
			dir = 'w';
		} else if (source.y > y) {
			dir = 'n';
		} else if (source.x > x) {
			dir = 'e';
		}
		return hasDoor(x, y, base, dir);
	}


	private final class Door {
		private final char direction;
		private final Tile main;
		private final Tile after;

		public Door(int x, int y, final char direction) {
			this.main = new Tile(x, y, 0);
			this.direction = direction;
			switch (direction) {
			case 'n':
				y += 1;
				break;
			case 's':
				y -= 1;
				break;
			case 'w':
				x -= 1;
				break;
			case 'e':
				x += 1;
				break;
			}
			this.after = new Tile(x, y, 0);
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof Door) {
				Door d = (Door) o;
				return this.main.equals(d.main) && this.after.equals(d.after) && this.direction == d.direction;
			}
			return false;
		}
	}
}
