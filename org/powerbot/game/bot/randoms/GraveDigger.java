package org.powerbot.game.bot.randoms;

import java.util.Arrays;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Entity;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

/**
 * <p>
 * This short-sighted gravedigger has managed to put five coffins in the wrong
 * graves. <br />
 * If he'd looked more closely at the headstones, he might have known where each
 * one was supposed to go! <br />
 * Help him by matching the contents of each coffin with the headstones in the
 * graveyard. Easy, huh?
 * </p>
 * <p/>
 *
 * @author Timer
 */
@Manifest(name = "Gravedigger", authors = {"Timer"}, version = 0.1)
public class GraveDigger extends AntiRandom {
	private static final int LOCATION_ID_DEPOSIT_BOX = 12731;
	private static final int NPC_ID_LEO = 3508;

	private static final int WIDGET_INSTRUCTIONS = 220;
	private static final int WIDGET_INSTRUCTIONS_CLOSE = 16;
	private static final int WIDGET_GRAVE = 143;
	private static final int WIDGET_GRAVE_DATA = 2;
	private static final int WIDGET_GRAVE_CLOSE = 3;
	private static final int WIDGET_COFFIN = 141;
	private static final int[] WIDGET_COFFIN_DATA = {3, 4, 5, 6, 7, 8, 9, 10, 11};
	private static final int WIDGET_COFFIN_CLOSE = 12;

	private static final int[] LOCATION_ID_GRAVE_STONES = {12716, 12717, 12718, 12719, 12720};
	private static final int[] LOCATION_ID_FILLED_GRAVES = {12721, 12722, 12723, 12724, 12725};
	private static final int[] LOCATION_ID_EMPTY_GRAVES = {12726, 12727, 12728, 12729, 12730};
	private static final int[] ITEM_ID_COFFINS = {7587, 7588, 7589, 7590, 7591};

	private Coffin[] coffins = {
			new Coffin(7614, new int[]{7603, 7605, 7612}),//Woodcutter
			new Coffin(7615, new int[]{7600, 7601, 7604}),//Chef
			new Coffin(7616, new int[]{7597, 7606, 7607}),//Miner
			new Coffin(7617, new int[]{7602, 7609, 7610}),//Farmer
			new Coffin(7618, new int[]{7599, 7608, 7613})//Crafter
	};
	private boolean gatheredGraves = false;

	@Override
	public boolean validate() {
		SceneObject depositBox = null;
		if (Game.isLoggedIn()) {
			depositBox = SceneEntities.getNearest(LOCATION_ID_DEPOSIT_BOX);
			if (depositBox == null) {
				clean();
			}
		}
		return depositBox != null && Settings.get(696) != 0;
	}

	@Override
	public void run() {
		if (Camera.getPitch() < 50) {
			Camera.setPitch(true);
		}

		if (Widgets.clickContinue()) {
			verbose("Following conversation");
			Time.sleep(Random.nextInt(1000, 2000));
			final Timer timer = new Timer(Random.nextInt(1300, 2000));
			while (timer.isRunning() && !Widgets.canContinue()) {
				Time.sleep(150);
			}
			return;
		}
		if (Widgets.get(1188, 24).validate()) {
			final WidgetChild widgetChild = Widgets.get(1188, 24);
			if (widgetChild.getText().contains("know")) {
				widgetChild.interact("Continue");
				return;
			}
		}

		if (Widgets.get(WIDGET_INSTRUCTIONS, WIDGET_INSTRUCTIONS_CLOSE).validate()) {
			verbose("Closing given instructions");
			Widgets.get(WIDGET_INSTRUCTIONS, WIDGET_INSTRUCTIONS_CLOSE).interact("Close");
			Time.sleep(Random.nextInt(2000, 3000));
			return;
		}
		if (Widgets.get(WIDGET_GRAVE, WIDGET_GRAVE_CLOSE).interact("Close")) {
			Time.sleep(Random.nextInt(1800, 2500));
			return;
		}
		if (Widgets.get(WIDGET_COFFIN, WIDGET_COFFIN_CLOSE).interact("Close")) {
			Time.sleep(Random.nextInt(1800, 2500));
			return;
		}
		if (Players.getLocal().isMoving() || Players.getLocal().getAnimation() != -1) {
			Time.sleep(Random.nextInt(1000, 1300));
			return;
		}

		if (Settings.get(698) == 0x80000000 || Settings.get(699) != 0) {
			verbose("Grave unscrambling commencing");

			SceneObject coffinLocation;
			if (!gatheredGraves && (coffinLocation = SceneEntities.getNearest(LOCATION_ID_FILLED_GRAVES)) != null) {
				if (!coffinLocation.isOnScreen()) {
					walk(coffinLocation);
					return;
				}
				coffinLocation.interact("Take-coffin");
				Time.sleep(Random.nextInt(1200, 2000));
				return;
			} else {
				gatheredGraves = true;
			}

			int undecidedId;
			verbose("Checking for undetermined information");
			if ((undecidedId = getUndecidedGrave()) != -1) {
				final SceneObject grave = SceneEntities.getNearest(undecidedId);
				if (grave != null) {
					if (!grave.isOnScreen()) {
						walk(grave);
						return;
					}

					if (grave.interact("Read", "Gravestone")) {
						final Timer widgetWait = new Timer(2500);
						while (widgetWait.isRunning()) {
							if (Players.getLocal().isMoving()) {
								widgetWait.reset();
							}
							if (Widgets.get(WIDGET_GRAVE).validate()) {
								break;
							}
							Time.sleep(150);
						}

						if (Widgets.get(WIDGET_GRAVE).validate()) {
							Time.sleep(Random.nextInt(1800, 2500));
							final int data_id = Widgets.get(WIDGET_GRAVE, WIDGET_GRAVE_DATA).getChildId();
							if (data_id != -1) {
								for (final Coffin coffin : coffins) {
									if (coffin.getChildId() == data_id) {
										coffin.setStoneId(undecidedId);
										break;
									}
								}
							}

							if (Widgets.get(WIDGET_GRAVE, WIDGET_GRAVE_CLOSE).interact("Close")) {
								Time.sleep(Random.nextInt(1800, 2500));
							}
						}
					}
				}
				return;
			} else if ((undecidedId = getUndecidedCoffin()) != -1) {
				final Item coffinItem = Inventory.getItem(undecidedId);
				if (coffinItem != null && coffinItem.getWidgetChild() != null) {
					if (coffinItem.getWidgetChild().interact("Check", "Coffin")) {
						final Timer widgetWait = new Timer(3000);
						while (widgetWait.isRunning() && !Widgets.get(WIDGET_COFFIN).validate()) {
							Time.sleep(150);
						}

						if (Widgets.get(WIDGET_COFFIN).validate()) {
							Time.sleep(Random.nextInt(1800, 2500));
							final int[] items_data = new int[WIDGET_COFFIN_DATA.length];
							for (int index = 0; index < WIDGET_COFFIN_DATA.length; index++) {
								items_data[index] = Widgets.get(WIDGET_COFFIN, index).getChildId();
							}
							for (final Coffin coffin : coffins) {
								if (coffin.doesMatch(items_data)) {
									coffin.setCoffinId(undecidedId);
									break;
								}
							}

							if (Widgets.get(WIDGET_COFFIN, WIDGET_COFFIN_CLOSE).interact("Close")) {
								Time.sleep(Random.nextInt(1800, 2500));
							}
							return;
						}
					}
				}
				return;
			}

			for (final Coffin coffin : coffins) {
				final int grave_id = getEmptyGraveId(coffin.getStoneId());
				final SceneObject grave = SceneEntities.getNearest(grave_id);
				if (grave != null) {
					if (!grave.isOnScreen() || !grave.getLocation().isOnScreen()) {
						walk(grave);
						return;
					}

					final Item item = Inventory.getItem(coffin.getCoffinId());
					if (item != null && item.getWidgetChild() != null) {
						if (useItem(item, grave)) {
							final Timer timer = new Timer(3000);
							while (timer.isRunning()) {
								if (Players.getLocal().isMoving()) {
									timer.reset();
								}
								if (Players.getLocal().getAnimation() == 827) {
									break;
								}
								Time.sleep(100);
							}
							if (Players.getLocal().getAnimation() == 827) {
								Time.sleep(Random.nextInt(3000, 5500));
							}
						}
					}
					return;
				}
			}

			final NPC leo = NPCs.getNearest(NPC_ID_LEO);
			if (leo != null) {
				if (!leo.isOnScreen()) {
					walk(leo);
					return;
				}

				if (leo.interact("Talk-to", "Leo")) {
					final Timer timer = new Timer(2000);
					while (timer.isRunning() && !Widgets.canContinue()) {
						if (Players.getLocal().isMoving()) {
							timer.reset();
						}
						Time.sleep(150);
					}
				}
			}

			return;
		}


		final NPC npc = NPCs.getNearest(NPC_ID_LEO);
		if (npc != null) {
			if (!npc.isOnScreen()) {
				walk(npc);
				return;
			}
			if (npc.interact("Talk-to", "Leo")) {
				final Timer timer = new Timer(2000);
				while (timer.isRunning() && npc.validate()) {
					if (Widgets.canContinue()) {
						break;
					}
					if (Players.getLocal().isMoving()) {
						timer.reset();
					}
					Time.sleep(150);
				}
			}
		}
	}

	private void walk(final Locatable mobile) {
		Walking.walk(mobile);
		final Timer timer = new Timer(2000);
		while (timer.isRunning()) {
			if (mobile.getLocation().isOnScreen()) {
				break;
			}
			if (Players.getLocal().isMoving()) {
				timer.reset();
			}
			Time.sleep(150);
		}
	}

	private int getUndecidedGrave() {
		for (final int graveStone : LOCATION_ID_GRAVE_STONES) {
			boolean found = false;
			for (Coffin coffin : coffins) {
				if (coffin.getStoneId() == graveStone) {
					found = true;
				}
			}
			if (!found) {
				return graveStone;
			}
		}
		return -1;
	}

	private int getEmptyGraveId(final int graveStone) {
		return LOCATION_ID_EMPTY_GRAVES[Arrays.binarySearch(LOCATION_ID_GRAVE_STONES, graveStone)];
	}

	private int getUndecidedCoffin() {
		for (final int coffinID : ITEM_ID_COFFINS) {
			boolean found = false;
			for (Coffin coffin : coffins) {
				if (coffin.getCoffinId() == coffinID) {
					found = true;
				}
			}
			if (!found) {
				return coffinID;
			}
		}
		return -1;
	}

	private void clean() {
		for (final Coffin coffin : coffins) {
			coffin.clean();
		}
		gatheredGraves = false;
	}

	private class Coffin {
		private final int childId;
		private final int[] coffinItemIds;
		private int stoneId = -1, coffinId = -1;

		private Coffin(final int childId, final int[] coffinItemIds) {
			this.childId = childId;
			this.coffinItemIds = coffinItemIds;
		}

		private void setStoneId(final int id) {
			this.stoneId = id;
		}

		private void setCoffinId(final int id) {
			this.coffinId = id;
		}

		private int getChildId() {
			return childId;
		}

		private int getStoneId() {
			return stoneId;
		}

		private int getCoffinId() {
			return coffinId;
		}

		private void clean() {
			stoneId = -1;
			coffinId = -1;
		}

		private boolean doesMatch(final int[] arr) {
			for (final int checkItem : coffinItemIds) {
				if (Arrays.binarySearch(arr, checkItem) >= 0) {
					return true;
				}
			}
			return false;
		}
	}

	private static boolean useItem(final Item item, final Entity location) {
		Tabs.INVENTORY.open();
		return Inventory.selectItem(item) && location.interact("Use", "Coffin -> Grave");
	}
}
