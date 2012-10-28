package org.powerbot.game.bot.randoms;

import java.util.HashMap;
import java.util.Map;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.node.SceneObject;

@Manifest(name = "Pillory", authors = {"Timer"}, version = 1.0)
public class Pillory extends AntiRandom {
	private static final int WIDGET_LOCK = 189;
	private static final Tile[] CAGE_TILES = {
			new Tile(2608, 3105, 0), new Tile(2606, 3105, 0), new Tile(2604, 3105, 0),
			new Tile(3226, 3407, 0), new Tile(3228, 3407, 0), new Tile(3230, 3407, 0),
			new Tile(2685, 3489, 0), new Tile(2683, 3489, 0), new Tile(2681, 3489, 0)
	};
	private static final HashMap<Integer, String> keys = new HashMap<Integer, String>();

	static {
		keys.put(9753, "Diamond");
		keys.put(9754, "Square");
		keys.put(9755, "Circle");
		keys.put(9756, "Triangle");
	}

	@Override
	public boolean validate() {
		if (Game.getClientState() == Game.INDEX_MAP_LOADED) {
			final Tile pos = Players.getLocal().getLocation();
			for (final Tile t : CAGE_TILES) {
				if (t.equals(pos)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (!Widgets.get(WIDGET_LOCK).validate()) {
			final Tile cageTile = Players.getLocal().getLocation().derive(0, 1);
			final SceneObject location = SceneEntities.getNearest(new Filter<SceneObject>() {
				@Override
				public boolean accept(final SceneObject location) {
					return location.getLocation().equals(cageTile) && location.getType() == SceneEntities.TYPE_BOUNDARY;
				}
			});
			if (location != null && location.interact("unlock")) {
				Time.sleep(Random.nextInt(1000, 1500));
			}
			return;
		}

		final int key = getKey();
		if (key > 4 && key < 8) {
			if (Widgets.get(WIDGET_LOCK, (key + 3)).interact("Select")) {
				Time.sleep(Random.nextInt(1300, 2500));
			} else {
				Time.sleep(200);
			}
		} else {
			verbose("We couldn't find correct the key.  Going to guess... (" + key + ").");
			if (Widgets.get(WIDGET_LOCK, Random.nextInt(5, 8)).interact("Select")) {
				Time.sleep(Random.nextInt(1300, 2500));
			} else {
				Time.sleep(Random.nextInt(500, 900));
			}
		}
	}

	private int getKey() {
		for (Map.Entry<Integer, String> key : keys.entrySet()) {
			if (Widgets.get(WIDGET_LOCK, 4).getModelId() == key.getKey()) {
				log.info("Key needed: " + key.getValue());
				for (int i = 5; i < 8; i++) {
					if (Widgets.get(WIDGET_LOCK, i).getModelId() == key.getKey() - 4) {
						verbose("KEY FOUND: " + (i - 4));
						return i;
					}
				}
			}
		}
		return -1;
	}
}
