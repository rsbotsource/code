package org.powerbot.game.bot.randoms;

import java.awt.Point;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Bee-keeper", description = "Assembles a bee house for those stinging nuisances", authors = {"Timer"}, version = 1.0)
public class Beekeeper extends AntiRandom {
	private static final int NPC_ID_BEE_KEEPER = 8649;
	private static final int LOCATION_ID_BEE_HOUSE = 16168;

	private static final int WIDGET_HIVE = 420;
	private static final int WIDGET_HIVE_BUILD = 40;
	private static final int WIDGET_HIVE_CLOSE = 38;
	private static final int WIDGET_CHAT = 1188;
	private static final int WIDGET_CHAT_TEXT_INDEX_2 = 24;

	private static final int LID = 8, UP_MID = 9, LOW_MID = 10, LEGS = 11;
	private static final int LOWERMID = 16022, UPPERMID = 16025, BOTTOM = 16034, TOP = 16036;
	private static final int[][] WIDGET_HIVE_MODEL_IDS = {
			{TOP, -1, LID},
			{UPPERMID, -1, UP_MID},
			{LOWERMID, -1, LOW_MID},
			{BOTTOM, -1, LEGS}
	};
	private static final String[] WIDGET_HIVE_NAMES = {"Top", "Upper mid", "Entry", "Bottom"};

	@Override
	public boolean validate() {
		return Game.isLoggedIn() && NPCs.getNearest(NPC_ID_BEE_KEEPER) != null && SceneEntities.getNearest(LOCATION_ID_BEE_HOUSE) != null;
	}

	@Override
	public void run() {
		final Player player = Players.getLocal();

		if (Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT_INDEX_2).validate()) {
			verbose("WIDGET VALIDATED: Let's try again!");
			Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT_INDEX_2).click(true);
			Time.sleep(Random.nextInt(1800, 2500));
			return;
		}
		if (Widgets.clickContinue()) {
			verbose("Following through with dialogue... man this guy is boring.");
			Time.sleep(Random.nextInt(1800, 2500));
			return;
		}

		final Widget widget = Widgets.get(WIDGET_HIVE);
		if (widget.validate()) {
			verbose("WIDGET VALIDATED: Hive construction imminent");
			for (WidgetChild child : widget.getChildren()) {
				if (child.getIndex() < 30) {
					for (int i = 0; i < WIDGET_HIVE_MODEL_IDS.length; i++) {
						verbose("VALIDATE: " + child.getModelId() + " (" + child.getIndex() + ") == " + WIDGET_HIVE_MODEL_IDS[i][0] + " (" + WIDGET_HIVE_NAMES[i] + ") ???");
						if (child.getModelId() == WIDGET_HIVE_MODEL_IDS[i][0]) {
							verbose("MODEL_MATCH: " + WIDGET_HIVE_NAMES[i] + " == " + child.getModelId());
							WIDGET_HIVE_MODEL_IDS[i][1] = child.getIndex();
							break;
						}
					}
				}
			}

			verbose("Constructing hive");
			for (int i = 0; i < 4; i++) {
				verbose("MERGE " + WIDGET_HIVE_MODEL_IDS[i][1] + " | " + WIDGET_HIVE_MODEL_IDS[i][2]);
				merge(widget.getChild(WIDGET_HIVE_MODEL_IDS[i][1]), widget.getChild(WIDGET_HIVE_MODEL_IDS[i][2]));
				Time.sleep(Random.nextInt(300, 800));
			}
			Time.sleep(Random.nextInt(1800, 2800));
			verbose("Checking solution (0x68d1000)");
			if (Settings.get(805) == 0x68d1000) {
				verbose("Constructed hive successfully!");
				widget.getChild(WIDGET_HIVE_BUILD).click(true);
				Time.sleep(Random.nextInt(1800, 2800));
				return;
			}

			verbose("We messed up the construction... let's try this bastard again...");
			widget.getChild(WIDGET_HIVE_CLOSE).interact("Close");
			return;
		}

		if (player.getInteracting() == null) {
			verbose("INTERACTION = NULL");
			verbose("Engaging communication !!!");
			NPCs.getNearest(NPC_ID_BEE_KEEPER).interact("Talk-to");
			Time.sleep(Random.nextInt(800, 1200));
		}
	}

	private void merge(final WidgetChild child1, final WidgetChild child2) {
		final Point center_1 = child1.getCentralPoint();
		final Point center_2 = child2.getCentralPoint();
		Mouse.move(center_1.x, center_1.y);
		Mouse.drag(center_2.x, center_2.y, 25, 10);
	}
}
