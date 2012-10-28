package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.GroundItems;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Entity;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.GroundItem;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Evil Bob", authors = {"Timer"}, version = 1.0)
public class EvilBob extends AntiRandom {
	private static final Tile CENTER_TILE = new Tile(3421, 4777, 0);

	private static final int[] LOCATION_STATUE_IDS = {8992, 8993, 8990, 8991};
	private static final int LOCATION_ID_FISH = 8986;
	private static final int LOCATION_ID_PORTAL = 8987;
	private static final int LOCATION_ID_POT_OF_MAGIC_UN_COOKING_POWERS_RULER_WAS_HERE = 8985;

	private static final int NPC_ID_EVIL_BOB = 2479;
	private static final int NPC_ID_SERVANT = 2481;

	private static final int WIDGET_CHAT = 1184;
	private static final int WIDGET_CHAT_TEXT = 13;

	private static final int SETTING_STAGE = 334;

	private static final int[] ITEM_ID_UNCOOKED_FISH = {6200, 6204};
	private static final int[] ITEM_ID_COOKED_FISH = {6202, 6206};
	private static final int ITEM_ID_NET = 6209;

	private int statueId = -1;
	private boolean forceLeave = false;

	@Override
	public boolean validate() {
		return Game.isLoggedIn() && Calculations.distance(CENTER_TILE, Players.getLocal().getLocation()) < 50;
	}

	@Override
	public void run() {
		if (Camera.getPitch() < 70) {
			Camera.setPitch(true);
		}
		final WidgetChild chat = Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT);
		if (chat.validate() && chat.getText().contains("tell you")) {
			statueId = -1;
		} else if (chat.validate() && chat.getText().contains("belong there")) {
			forceLeave = true;
		} else if (chat.validate() && chat.getText().contains("human")) {
			forceLeave = false;
		}

		if (Inventory.getItem(ITEM_ID_UNCOOKED_FISH) != null) {
			verbose("We have the uncooked fish!");
			final NPC bob = NPCs.getNearest(NPC_ID_EVIL_BOB);
			if (!bob.isOnScreen()) {
				walk(bob);
				return;
			}
			if (useItem(Inventory.getItem(ITEM_ID_UNCOOKED_FISH), bob, "Raw fish-like thing -> Evil Bob")) {
				final Timer timer = new Timer(2000);
				while (timer.isRunning()) {
					if (Players.getLocal().isMoving()) {
						timer.reset();
					}
					if (Widgets.clickContinue()) {
						timer.reset();
					}
					if (chat.validate() && (chat.getText().contains("catnap") || chat.getText().contains("fallen asleep"))) {
						Time.sleep(Random.nextInt(1200, 2000));
						if (Widgets.clickContinue()) {
							Time.sleep(Random.nextInt(1200, 2000));
						}
						return;
					}
					Time.sleep(150);
				}
			}
			return;
		}

		if (Inventory.getItem(ITEM_ID_COOKED_FISH) != null) {
			verbose("We have a cooked fish...");
			final SceneObject pot = SceneEntities.getNearest(LOCATION_ID_POT_OF_MAGIC_UN_COOKING_POWERS_RULER_WAS_HERE);
			if (pot != null) {
				if (!pot.isOnScreen()) {
					walk(pot);
					return;
				}
				if (useItem(Inventory.getItem(ITEM_ID_COOKED_FISH), pot, "Fish-like thing -> Uncooking pot")) {
					final Timer timer = new Timer(2000);
					while (timer.isRunning() && Inventory.getItem(ITEM_ID_UNCOOKED_FISH) == null) {
						if (Players.getLocal().isMoving()) {
							timer.reset();
						} else if (Players.getLocal().getAnimation() > 0) {
							timer.reset();
						}
						Time.sleep(150);
					}
				}
			} else {
				walk(CENTER_TILE);
			}
			return;
		}

		final int setting = Settings.get(SETTING_STAGE);
		if (setting == 2 || forceLeave) {
			verbose("SETTING: Stage 2 - EXIT");
			final WidgetChild child = Widgets.get(566, 16);//no i left no items valuable
			if (child != null && child.validate()) {
				child.click(true);
				Time.sleep(Random.nextInt(1000, 2000));
				return;
			}

			final SceneObject portal = SceneEntities.getNearest(LOCATION_ID_PORTAL);
			if (portal != null) {
				if (!portal.isOnScreen()) {
					walk(portal);
					return;
				}

				if (portal.interact("Enter", "Portal")) {
					forceLeave = false;
					Time.sleep(Random.nextInt(7000, 10000));
					return;
				}
			}
			return;
		} else if (setting == 1 && statueId != -1) {
			verbose("Settings: Stage 1 - FISH");
			if (Inventory.getItem(ITEM_ID_NET) == null) {
				final GroundItem groundItem = GroundItems.getNearest(ITEM_ID_NET);
				if (!groundItem.isOnScreen()) {
					walk(groundItem);
					return;
				}

				if (groundItem.interact("Take", "Small fishing net")) {
					final Timer timer = new Timer(2000);
					while (timer.isRunning() && Inventory.getItem(ITEM_ID_NET) == null) {
						if (Players.getLocal().isMoving()) {
							timer.reset();
						}
						Time.sleep(150);
					}
				}
				return;
			}

			verbose("Location statue");
			final SceneObject statue = SceneEntities.getNearest(statueId);
			if (statue != null) {
				verbose("Statue location: " + statue.getLocation());
				final SceneObject fishingSpot = SceneEntities.getNearest(new Filter<SceneObject>() {
					@Override
					public boolean accept(final SceneObject location) {
						return location.getId() == LOCATION_ID_FISH && Calculations.distance(location.getLocation(), statue.getLocation()) < 10;
					}
				});
				verbose("Nearest fishing spot: " + fishingSpot);

				if (fishingSpot != null) {
					if (!fishingSpot.isOnScreen()) {
						walk(fishingSpot);
						return;
					}

					if (fishingSpot.interact("Net", "Fishing spot")) {
						final Timer timer = new Timer(3000);
						while (timer.isRunning() && Inventory.getItem(ITEM_ID_COOKED_FISH) == null) {
							if (Players.getLocal().isMoving()) {
								timer.reset();
							} else if (Players.getLocal().getAnimation() > 0) {
								timer.reset();
							}
							Time.sleep(150);
						}
						return;
					}
				}
			}
			return;
		}

		if (chat.validate() && chat.getText().contains("contains")) {
			verbose("Found contains text... waiting for location.");
			Time.sleep(5000);

			verbose("Iterating locations...");
			for (final SceneObject location : SceneEntities.getLoaded(LOCATION_STATUE_IDS)) {
				if (location.isOnScreen()) {
					statueId = location.getId();
					verbose("Found statue: " + statueId);
					final Timer timer = new Timer(7000);
					while (timer.isRunning() && !Widgets.getContinue().interact("Continue")) {
						Time.sleep(Random.nextInt(800, 1200));
					}
					timer.reset();
					while (timer.isRunning() && chat.validate() && chat.getText().contains("contains")) {
						Time.sleep(Random.nextInt(100, 300));
					}
					return;
				}
			}
			return;
		}
		if (Widgets.clickContinue()) {
			verbose("Continuing with text!");
			Time.sleep(Random.nextInt(2000, 3200));
			return;
		}

		verbose("Fall onto servant communication...");
		final NPC servant = NPCs.getNearest(NPC_ID_SERVANT);
		if (servant != null) {
			if (!servant.isOnScreen()) {
				walk(servant);
				return;
			}
			if (servant.interact("Talk-to", "Servant")) {
				final Timer timer = new Timer(2000);
				while (timer.isRunning() && !Widgets.canContinue()) {
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

	private static boolean useItem(final Item item, final Entity entity, final String option) {
		Tabs.INVENTORY.open();
		return Inventory.selectItem(item) && entity.interact("Use", option);
	}
}
