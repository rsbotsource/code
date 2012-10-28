package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Certer", authors = {"Timer"}, version = 1.0)
public class Certer extends AntiRandom {
	private static final int LOCATION_ID_PORTAL = 11368;
	private static final int SETTING_SOLVED = 807;
	private static final int WIDGET_IDENTIFY = 184;
	private static final int WIDGET_IDENTIFY_ITEM = 8;
	private static final int WIDGET_IDENTIFY_ITEM_MODEL = 3;
	private static final int WIDGET_CHAT = 1184;
	private static final int WIDGET_CHAT_TEXT = 13;
	private static final int[] WIDGET_MODEL_IDS = {2807, 8828, 8829, 8832, 8833, 8834, 8835, 8836, 8837};
	private static final int[] LOCATION_ID_BOOKS = {42352, 42354};
	private static final String[] ITEM_NAMES = {"bowl", "battleaxe", "fish",
			"shield", "helmet", "ring", "shears", "sword", "spade"};
	private int settingValue = 0;

	@Override
	public boolean validate() {
		return Game.isLoggedIn() && SceneEntities.getNearest(LOCATION_ID_BOOKS) != null;
	}

	@Override
	public void run() {
		if (Players.getLocal().getAnimation() != -1 || Players.getLocal().isMoving()) {
			verbose("SLEEP: Walking or animating ...");
			Time.sleep(Random.nextInt(500, 1000));
			return;
		}
		verbose("SETTING_SOLVED: " + Settings.get(SETTING_SOLVED));
		if (settingValue == 0) {
			settingValue = Settings.get(SETTING_SOLVED);
		}
		if (Settings.get(SETTING_SOLVED) > settingValue) {
			verbose("SOLVED: Attempt exit");
			final SceneObject portal = SceneEntities.getNearest(LOCATION_ID_PORTAL);
			if (portal != null) {
				if (!portal.isOnScreen()) {
					walk(portal);
					return;
				}
				if (portal.interact("Enter", "Exit")) {
					Time.sleep(Random.nextInt(3000, 5000));
				}
			}
			return;
		}
		if (Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT).validate() &&
				Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT).getText().contains("portal")) {
			settingValue--;
		}
		if (Widgets.clickContinue()) {
			verbose("Conversation ...");
			Time.sleep(Random.nextInt(500, 1000));
			return;
		}
		if (Widgets.get(WIDGET_IDENTIFY, WIDGET_IDENTIFY_ITEM).validate()) {
			verbose("WIDGET VALIDATED: Quiz window");
			final int model = Widgets.get(WIDGET_IDENTIFY, WIDGET_IDENTIFY_ITEM).getChild(WIDGET_IDENTIFY_ITEM_MODEL).getModelId();
			String itemName = null;
			verbose("Model id: " + model);
			for (int i = 0; i < WIDGET_MODEL_IDS.length; i++) {
				if (WIDGET_MODEL_IDS[i] == model) {
					itemName = ITEM_NAMES[i];
					verbose("Identified: " + itemName);
					break;
				}
			}
			if (itemName != null) {
				verbose("Crawling widgets");
				for (int j = 0; j < 3; j++) {
					final WidgetChild child = Widgets.get(WIDGET_IDENTIFY, WIDGET_IDENTIFY_ITEM).getChild(j);
					verbose("TEXT: " + child.getText().toLowerCase());
					if (child.getText().toLowerCase().contains(itemName.toLowerCase())) {
						verbose("FOUND!  Attempt click...");
						if (child.click(true)) {
							Time.sleep(Random.nextInt(1000, 1200));
						}
						break;
					}
				}
			}
			return;
		}

		verbose("We need to speak with ?iles!");
		final NPC man = NPCs.getNearest(new Filter<NPC>() {
			@Override
			public boolean accept(final NPC npc) {
				final String name = npc.getName().toLowerCase();
				return name.equals("giles") || name.equals("miles") || name.equals("niles");
			}
		});

		if (man != null) {
			if (!man.isOnScreen()) {
				walk(man);
				return;
			}
			if (man.interact("Talk-to")) {
				final Timer timer = new Timer(2000);
				while (timer.isRunning() && !Widgets.canContinue()) {
					Time.sleep(150);
					if (Players.getLocal().isMoving()) {
						timer.reset();
					}
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
}
