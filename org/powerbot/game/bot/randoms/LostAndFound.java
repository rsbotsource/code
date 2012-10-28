package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.SceneObject;

@Manifest(name = "Lost and Found", authors = {"Timer"}, version = 1.0d)
public class LostAndFound extends AntiRandom {
	private static final int APPENDAGE_N = 8995;
	private static final int APPENDAGE_E = 8994;
	private static final int APPENDAGE_S = 8997;
	private static final int APPENDAGE_W = 8996;
	private static final int SETTING = 531;

	private final static int[] APPENDAGES = {APPENDAGE_N, APPENDAGE_E, APPENDAGE_S, APPENDAGE_W};
	private final static int[] APPENDAGE_AN = {32, 64, 135236, 67778, 135332, 34017, 202982, 101443, 101603, 236743, 33793, 67682, 135172,
			236743, 169093, 33889, 202982, 67714, 101539};
	private final static int[] APPENDAGE_AE = {4, 6, 101474, 101473, 169124, 169123, 67648, 135301, 135298, 67651, 169121, 33827, 67652,
			236774, 101479, 33824, 202951};
	private final static int[] APPENDAGE_AS = {4228, 32768, 68707, 167011, 38053, 230433, 164897, 131072, 168068, 65536, 35939, 103589,
			235718, 204007, 100418, 133186, 99361, 136357, 1057, 232547};
	private final static int[] APPENDAGE_AW = {105571, 37921, 131204, 235751, 1024, 165029, 168101, 68674, 203974, 2048, 100451, 6144,
			39969, 69698, 32801, 136324};
	private final static int[][] ANSWERS = {APPENDAGE_AN, APPENDAGE_AE, APPENDAGE_AS, APPENDAGE_AW};

	public boolean validate() {
		return Game.isLoggedIn() && SceneEntities.getNearest(new Filter<SceneObject>() {
			public boolean accept(final SceneObject location) {
				final int id = location.getId();
				for (final int appendageId : APPENDAGES) {
					if (id == appendageId) {
						return true;
					}
				}
				return false;
			}
		}) != null;
	}

	public void run() {
		final Player localPlayer = Players.getLocal();
		if (localPlayer.getSpeed() > 0) {
			Time.sleep(Random.nextInt(200, 300));
			return;
		}

		final int appendageId = getOdd();
		final SceneObject appendage = SceneEntities.getNearest(new Filter<SceneObject>() {
			public boolean accept(final SceneObject location) {
				return location.getId() == appendageId;
			}
		});
		if (appendage != null) {
			if (appendage.isOnScreen()) {
				appendage.interact("Operate", "Appendage");
				final Timer timer = new Timer(5000);
				while (timer.isRunning()) {
					if (localPlayer.isMoving()) {
						break;
					}
					Time.sleep(500);
				}
			} else {
				Walking.walk(appendage.getLocation());
				final Timer timer = new Timer(5000);
				while (timer.isRunning()) {
					if (localPlayer.isMoving()) {
						break;
					}
					Time.sleep(500);
				}
			}
		}
	}

	private int getOdd() {
		final int answer_appendage = Settings.get(SETTING);
		int index = 0;
		for (final int[] appendages : ANSWERS) {
			for (final int appendage : appendages) {
				if (appendage == answer_appendage) {
					return APPENDAGES[index];
				}
			}
			++index;
		}
		verbose("UNKNOWN APPENDAGE: " + answer_appendage);
		return APPENDAGES[Random.nextInt(0, APPENDAGES.length)];
	}
}
