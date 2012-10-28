package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Sandwich Lady", description = "Eats the bitch's food.", authors = {"Timer"}, version = 1.0)
public class SandwichLady extends AntiRandom {
	private final static int WIDGET_ID_SANDWICH_SELECTION = 297;
	private final static int WIDGET_ID_SANDWICH_SELECTION_HINT = 48;
	private final static int WIDGET_ID_CHAT = 1184;
	private final static int WIDGET_ID_CHAT_CONTINUE = 18;
	private final static int WIDGET_ID_CHAT_TEXT = 13;
	private final static int WIDGET_ID_CHAT_2 = 1191;
	private final static int WIDGET_ID_CHAT_CONTINUE_2 = 18;
	private final static int[] WIDGET_MODEL_ID_FOOD_ITEMS = {10728, 10732, 10727, 10730, 10726, 45666, 10731};
	private final static int NPC_ID_SANDWICH_LADY = 8630;
	private final static String[] FOOD_ITEM_NAMES = {"chocolate", "triangle", "roll", "pie", "baguette", "doughnut", "square"};

	@Override
	public boolean validate() {
		return NPCs.getNearest(NPC_ID_SANDWICH_LADY) != null;
	}

	@Override
	public void run() {
		final NPC lady = NPCs.getNearest(NPC_ID_SANDWICH_LADY);
		if (Widgets.get(WIDGET_ID_CHAT).validate()) {
			if (Widgets.get(WIDGET_ID_CHAT, WIDGET_ID_CHAT_TEXT).getText().contains("exit portal's")) {
				final SceneObject portal = SceneEntities.getNearest(12731, 11373);
				if (portal != null) {
					if (portal.isOnScreen()) {
						portal.interact("Enter", "Exit portal");
						Time.sleep(Random.nextInt(3000, 5000));
					} else {
						Walking.walk(portal.getLocation());
						final Timer timer = new Timer(3000);
						while (timer.isRunning() && !portal.isOnScreen()) {
							Time.sleep(150);
						}
					}
				}
				return;
			}

			Widgets.get(WIDGET_ID_CHAT, WIDGET_ID_CHAT_CONTINUE).click(true);
			Time.sleep(Random.nextInt(900, 1200));
			return;
		}
		if (Widgets.get(WIDGET_ID_CHAT_2).validate()) {
			Widgets.get(WIDGET_ID_CHAT_2, WIDGET_ID_CHAT_CONTINUE_2).click(true);
			Time.sleep(Random.nextInt(900, 1200));
			return;
		}
		final Player player = Players.getLocal();
		if (player.getAnimation() != -1) {
			Time.sleep(Random.nextInt(500, 1000));
			return;
		}

		if (Widgets.get(WIDGET_ID_SANDWICH_SELECTION).validate()) {
			final Widget window = Widgets.get(WIDGET_ID_SANDWICH_SELECTION);
			final String hint_text = window.getChild(WIDGET_ID_SANDWICH_SELECTION_HINT).getText();
			int item_index = 0;
			for (final String item : FOOD_ITEM_NAMES) {
				if (hint_text.contains(item)) {
					break;
				}
				++item_index;
			}
			final int item_id = WIDGET_MODEL_ID_FOOD_ITEMS[item_index];
			for (int i = 7; i < 48; i++) {
				final WidgetChild possible_foot = window.getChild(i);
				if (possible_foot.getModelId() == item_id) {
					if (possible_foot.click(true)) {
						final Timer timer = new Timer(5000);
						while (timer.isRunning() && Widgets.get(WIDGET_ID_SANDWICH_SELECTION).validate()) {
							Time.sleep(150);
						}
						Time.sleep(Random.nextInt(2000, 3000));
						return;
					}
				}
			}
			Time.sleep(Random.nextInt(900, 1200));
			return;
		}

		if (lady != null && lady.getAnimation() == -1) {
			if (!lady.getLocation().isOnScreen()) {
				Camera.turnTo(lady);
				Camera.setPitch(false);
			} else {
				if (lady.interact("Talk-to")) {
					Time.sleep(Random.nextInt(1000, 1500));
					return;
				}
			}
		}
		Time.sleep(Random.nextInt(900, 1200));
	}
}