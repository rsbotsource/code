package org.powerbot.game.bot.randoms;

import java.util.Arrays;
import java.util.HashMap;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.SceneObject;

@Manifest(name = "Drill Demon", description = "Exercises.", version = 1.0, authors = {"Timer"})
public class DrillDemon extends AntiRandom {
	private static final Area AREA = new Area(new Tile(3159, 4818, 0), new Tile(3167, 4822, 0));
	private static final int NPC_ID_DEMON = 2790;
	private static final int WIDGET_MAT = 1190;
	private static final int WIDGET_MAT_ICON = 1;

	private final static HashMap<Integer, Integer> mat_indices = new HashMap<Integer, Integer>();
	private final static HashMap<Integer, int[]> setting_arrays = new HashMap<Integer, int[]>();

	static {
		mat_indices.put(10949, 0); //Star jumps
		mat_indices.put(10946, 1); //Push ups
		mat_indices.put(10948, 2); //Sit ups
		mat_indices.put(10947, 3); //Jog

		setting_arrays.put(668, new int[]{0, 1, 2, 3});
		setting_arrays.put(675, new int[]{1, 0, 2, 3});
		setting_arrays.put(724, new int[]{0, 2, 1, 3});
		setting_arrays.put(738, new int[]{2, 0, 1, 3});
		setting_arrays.put(787, new int[]{1, 2, 0, 3});
		setting_arrays.put(794, new int[]{2, 1, 0, 3});
		setting_arrays.put(1116, new int[]{0, 1, 3, 2});
		setting_arrays.put(1123, new int[]{1, 0, 3, 2});
		setting_arrays.put(1228, new int[]{0, 3, 1, 2});
		setting_arrays.put(1249, new int[]{3, 0, 1, 2});
		setting_arrays.put(1291, new int[]{1, 3, 0, 2});
		setting_arrays.put(1305, new int[]{3, 1, 0, 2});
		setting_arrays.put(1620, new int[]{0, 2, 3, 1});
		setting_arrays.put(1634, new int[]{2, 0, 3, 1});
		setting_arrays.put(1676, new int[]{0, 3, 2, 1});
		setting_arrays.put(1697, new int[]{3, 0, 2, 1});
		setting_arrays.put(1802, new int[]{2, 3, 0, 1});
		setting_arrays.put(1809, new int[]{3, 2, 0, 1});
		setting_arrays.put(2131, new int[]{1, 2, 3, 0});
		setting_arrays.put(2138, new int[]{2, 1, 3, 0});
		setting_arrays.put(2187, new int[]{1, 3, 2, 0});
		setting_arrays.put(2201, new int[]{3, 1, 2, 0});
		setting_arrays.put(2250, new int[]{2, 3, 1, 0});
		setting_arrays.put(2257, new int[]{3, 2, 1, 0});
	}

	@Override
	public boolean validate() {
		return Game.isLoggedIn() && AREA.contains(Players.getLocal().getLocation());
	}

	@Override
	public void run() {
		if (Camera.getPitch() < 90) {
			verbose("Increasing pitch ...");
			Camera.setPitch(true);
		}
		Camera.setAngle('n');
		final Player localPlayer = Players.getLocal();

		if (localPlayer.isMoving()) {
			verbose("Character is moving");
			for (int i = 0; i < 50; i++) {
				if (!localPlayer.isMoving()) {
					break;
				}
				Time.sleep(Random.nextInt(75, 80));
			}
			Time.sleep(Random.nextInt(1800, 2000));
			return;
		}

		if (localPlayer.getAnimation() != -1) {
			verbose("ANIMATION != -1");
			for (int i = 0; i < 50; i++) {
				if (localPlayer.getAnimation() == -1) {
					break;
				}
				Time.sleep(Random.nextInt(60, 90));
			}
			for (int i = 0; i < 50; i++) {
				if (Widgets.get(241, 0).validate()) {
					break;
				}
				Time.sleep(Random.nextInt(30, 40));
			}
			Time.sleep(Random.nextInt(400, 700));
			return;
		}

		if (Widgets.get(WIDGET_MAT).validate()) {
			verbose("WIDGET VALIDATED: Mat objective");
			final int setting_value = Settings.get(Settings.VALUE_RANDOMEVENT_DRILLDEMON_MAT);
			verbose("Mat mask: " + setting_value);
			final int child_id = Widgets.get(WIDGET_MAT, WIDGET_MAT_ICON).getChildId();
			verbose("Child id: " + child_id);
			for (int i = 0; i < setting_arrays.get(setting_value).length; i++) {
				if (setting_arrays.get(setting_value)[i] == mat_indices.get(child_id)) {
					verbose("MATCH " + Arrays.toString(setting_arrays.get(setting_value)) + " (" + i + ") & " + mat_indices.get(child_id));
					if (findAndUseMat(i)) {
						Time.sleep(800);
						return;
					}
				}
			}
		}

		if (Widgets.clickContinue()) {
			verbose("Conversing ...");
			Time.sleep(Random.nextInt(2000, 3000));
			return;
		}

		if (!Widgets.clickContinue() && localPlayer.getAnimation() == -1) {
			verbose("Engaging in communication!");
			final NPC demon = NPCs.getNearest(NPC_ID_DEMON);
			demon.interact("Talk-to");
		}
		Time.sleep(Random.nextInt(2000, 2500));
	}

	private boolean findAndUseMat(final int sign_id) {
		final SceneObject[] game_mats = {
				SceneEntities.getNearest(10076),
				SceneEntities.getNearest(10077),
				SceneEntities.getNearest(10078),
				SceneEntities.getNearest(10079)
		};
		if (game_mats[sign_id] != null) {
			if (!game_mats[sign_id].isOnScreen()) {
				verbose("MAT OFF SCREEN!");
				if (Walking.walk(game_mats[sign_id].getLocation())) {
					verbose("Walking...");
					Time.sleep(500);
				}
			} else {
				if (Players.getLocal().getAnimation() == -1) {
					verbose("PERFORMING OBJECTIVE");
					if (game_mats[sign_id].interact("Use")) {
						Time.sleep(900);
						return true;
					}
				} else {
					verbose("INTERACTION (OBJECTIVE): Already performing...");
				}
			}
		}
		return false;
	}
}
