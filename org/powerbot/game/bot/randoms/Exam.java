package org.powerbot.game.bot.randoms;

import java.util.Arrays;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
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

@Manifest(name = "Surprise Exam", authors = {"Timer"}, version = 1.0)
public class Exam extends AntiRandom {
	private static final int WIDGET_NEXT = 103;
	private static final int WIDGET_RELATED = 559;
	private static final int WIDGET_CHAT = 1184;
	private static final int WIDGET_CHAT_TEXT = 13;

	public static final int[] WIDGET_ITEM_RANGE = {11539, 11540, 11541, 11614, 11615, 11633};
	public static final int[] WIDGET_ITEM_CULINARY = {11526, 11529, 11545, 11549, 11550, 11555, 11560, 11563, 11564, 11607, 11608, 11616, 11620, 11621, 11622, 11623, 11628, 11629, 11634, 11639, 11641, 11649, 11624};
	public static final int[] WIDGET_ITEM_FISH = {11527, 11574, 11578, 11580, 11599, 11600, 11601, 11602, 11603, 11604, 11605, 11606, 11625};
	public static final int[] WIDGET_ITEM_COMBAT = {11528, 11531, 11536, 11537, 11579, 11591, 11592, 11593, 11597, 11627, 11631, 11635, 11636, 11638, 11642, 11648, 11617};
	public static final int[] WIDGET_ITEM_FARM = {11530, 11532, 11547, 11548, 11554, 11556, 11571, 11581, 11586, 11610, 11645};
	public static final int[] WIDGET_ITEM_MAGIC = {11533, 11534, 11538, 11562, 11567, 11582};
	public static final int[] WIDGET_ITEM_FIREMAKING = {11535, 11551, 11552, 11559, 11646};
	public static final int[] WIDGET_ITEM_HATS = {11540, 11557, 11558, 11560, 11570, 11619, 11626, 11630, 11632, 11637, 11654};
	public static final int[] WIDGET_ITEM_PIRATE = {11570, 11626, 11558};
	public static final int[] WIDGET_ITEM_JEWELLERY = {11572, 11576, 11652};
	public static final int[] WIDGET_ITEM_JEWELLERY_2 = {11572, 11576, 11652};
	public static final int[] WIDGET_ITEM_DRINKS = {11542, 11543, 11544, 11644, 11647};
	public static final int[] WIDGET_ITEM_LUMBER = {11573, 11595};
	public static final int[] WIDGET_ITEM_BOOTS = {11561, 11618, 11650, 11651};
	public static final int[] WIDGET_ITEM_CRAFT = {11546, 11553, 11565, 11566, 11568, 11569, 11572, 11575, 11576, 11577, 11581, 11583, 11584, 11585, 11643, 11652, 11653};
	public static final int[] WIDGET_ITEM_MINING = {11587, 11588, 11594, 11596, 11598, 11609, 11610};
	public static final int[] WIDGET_ITEM_SMITHING = {11611, 11612, 11613, 11553};

	public static final int[][] WIDGET_ITEMS = {
			WIDGET_ITEM_RANGE, WIDGET_ITEM_CULINARY,
			WIDGET_ITEM_FISH, WIDGET_ITEM_COMBAT,
			WIDGET_ITEM_FARM, WIDGET_ITEM_MAGIC,
			WIDGET_ITEM_FIREMAKING, WIDGET_ITEM_HATS,
			WIDGET_ITEM_DRINKS, WIDGET_ITEM_LUMBER,
			WIDGET_ITEM_BOOTS, WIDGET_ITEM_CRAFT,
			WIDGET_ITEM_MINING, WIDGET_ITEM_SMITHING
	};

	private SceneObject door = null;
	private static final int[] OBJECT_ID_DOORS = {2188, 2189, 2192, 2193};
	private static final String[] COLORS = {"red", "blue", "purple", "green"};

	public final ObjectRelations[] WIDGET_ITEM_RELATIONS = {
			new ObjectRelations("I never leave the house without some sort of jewellery.", WIDGET_ITEM_JEWELLERY),
			new ObjectRelations("There is no better feeling than", WIDGET_ITEM_JEWELLERY_2),
			new ObjectRelations("I'm feeling dehydrated", WIDGET_ITEM_DRINKS),
			new ObjectRelations("All this work is making me thirsty", WIDGET_ITEM_DRINKS),
			new ObjectRelations("quenched my thirst", WIDGET_ITEM_DRINKS),
			new ObjectRelations("light my fire", WIDGET_ITEM_FIREMAKING),
			new ObjectRelations("fishy", WIDGET_ITEM_FISH),
			new ObjectRelations("fishing for answers", WIDGET_ITEM_FISH),
			new ObjectRelations("fish out of water", WIDGET_ITEM_DRINKS),
			new ObjectRelations("strange headgear", WIDGET_ITEM_HATS),
			new ObjectRelations("tip my hat", WIDGET_ITEM_HATS),
			new ObjectRelations("thinking cap", WIDGET_ITEM_HATS),
			new ObjectRelations("wizardry here", WIDGET_ITEM_MAGIC),
			new ObjectRelations("rather mystical", WIDGET_ITEM_MAGIC),
			new ObjectRelations("abracada", WIDGET_ITEM_MAGIC),
			new ObjectRelations("hide one's face", WIDGET_ITEM_HATS),
			new ObjectRelations("shall unmask", WIDGET_ITEM_HATS),
			new ObjectRelations("hand-to-hand", WIDGET_ITEM_COMBAT),
			new ObjectRelations("melee weapon", WIDGET_ITEM_COMBAT),
			new ObjectRelations("prefers melee", WIDGET_ITEM_COMBAT),
			new ObjectRelations("me hearties", WIDGET_ITEM_PIRATE),
			new ObjectRelations("puzzle for landlubbers", WIDGET_ITEM_PIRATE),
			new ObjectRelations("mighty pirate", WIDGET_ITEM_PIRATE),
			new ObjectRelations("mighty archer", WIDGET_ITEM_RANGE),
			new ObjectRelations("as an arrow", WIDGET_ITEM_RANGE),
			new ObjectRelations("Ranged attack", WIDGET_ITEM_RANGE),
			new ObjectRelations("shiny things", WIDGET_ITEM_CRAFT),
			new ObjectRelations("igniting", WIDGET_ITEM_FIREMAKING),
			new ObjectRelations("sparks from my synapses.", WIDGET_ITEM_FIREMAKING),
			new ObjectRelations("fire.", WIDGET_ITEM_FIREMAKING),
			new ObjectRelations("disguised", WIDGET_ITEM_HATS),
			new ObjectRelations("range", WIDGET_ITEM_RANGE),
			new ObjectRelations("arrow", WIDGET_ITEM_RANGE),
			new ObjectRelations("drink", WIDGET_ITEM_DRINKS),
			new ObjectRelations("logs", WIDGET_ITEM_FIREMAKING),
			new ObjectRelations("light", WIDGET_ITEM_FIREMAKING),
			new ObjectRelations("headgear", WIDGET_ITEM_HATS),
			new ObjectRelations("hat", WIDGET_ITEM_HATS),
			new ObjectRelations("cap", WIDGET_ITEM_HATS),
			new ObjectRelations("mine", WIDGET_ITEM_MINING),
			new ObjectRelations("mining", WIDGET_ITEM_MINING),
			new ObjectRelations("ore", WIDGET_ITEM_MINING),
			new ObjectRelations("fish", WIDGET_ITEM_FISH),
			new ObjectRelations("fishing", WIDGET_ITEM_FISH),
			new ObjectRelations("thinking cap", WIDGET_ITEM_HATS),
			new ObjectRelations("cooking", WIDGET_ITEM_CULINARY),
			new ObjectRelations("cook", WIDGET_ITEM_CULINARY),
			new ObjectRelations("bake", WIDGET_ITEM_CULINARY),
			new ObjectRelations("farm", WIDGET_ITEM_FARM),
			new ObjectRelations("farming", WIDGET_ITEM_FARM),
			new ObjectRelations("cast", WIDGET_ITEM_MAGIC),
			new ObjectRelations("magic", WIDGET_ITEM_MAGIC),
			new ObjectRelations("craft", WIDGET_ITEM_CRAFT),
			new ObjectRelations("boot", WIDGET_ITEM_BOOTS),
			new ObjectRelations("chop", WIDGET_ITEM_LUMBER),
			new ObjectRelations("cut", WIDGET_ITEM_LUMBER),
			new ObjectRelations("tree", WIDGET_ITEM_LUMBER)
	};

	private class ObjectRelations {
		private final String text;
		private final int[] items;

		private ObjectRelations(final String text, final int[] items) {
			this.text = text;
			this.items = items;
		}
	}

	@Override
	public boolean validate() {
		final NPC mordaut;
		return (mordaut = NPCs.getNearest("Mr. Mordaut")) != null && Calculations.distanceTo(mordaut) < 15;
	}

	@Override
	public void run() {
		final Player local = Players.getLocal();

		if (local.isMoving() || local.getAnimation() != -1) {
			Time.sleep(150);
			return;
		}

		final Widget next = Widgets.get(WIDGET_NEXT);
		if (next.validate()) {
			verbose("WIDGET_VALIDATED: Next item");
			final int item_1 = next.getChild(6).getChildId();
			final int item_2 = next.getChild(7).getChildId();
			final int item_3 = next.getChild(8).getChildId();
			verbose("Items: " + item_1 + ", " + item_2 + ", " + item_3);

			final int[] item_arr_1 = getItemArray(item_1);
			final int[] item_arr_2 = getItemArray(item_2);
			final int[] item_arr_3 = getItemArray(item_3);
			final int[] item_arr;
			final int[] item_arr_o;
			if (Arrays.equals(item_arr_2, item_arr_3)) {
				item_arr = item_arr_2;
				item_arr_o = item_arr_1;
			} else {
				item_arr = item_arr_1;
				if (Arrays.equals(item_arr_1, item_arr_2)) {
					item_arr_o = item_arr_3;
				} else {
					item_arr_o = item_arr_2;
				}
			}
			verbose("Matched 1: " + Arrays.toString(item_arr_1));
			verbose("Matched 2: " + Arrays.toString(item_arr_2));
			verbose("Matched 3: " + Arrays.toString(item_arr_3));
			if (item_arr_1 != null && item_arr_2 != null && item_arr_3 != null) {
				final int[] choices = {
						next.getChild(10).getChildId(),
						next.getChild(11).getChildId(),
						next.getChild(12).getChildId(),
						next.getChild(13).getChildId()
				};
				verbose("Possible choices: " + Arrays.toString(choices));

				int index = 10;
				for (final int choice : choices) {
					Arrays.sort(item_arr);
					if (Arrays.binarySearch(item_arr, choice) >= 0) {
						verbose("Found choice at index " + index + ".");
						next.getChild(index).interact("Select");
						Time.sleep(Random.nextInt(1500, 2000));
						return;
					}
					++index;
				}

				index = 10;
				verbose("Unknown, making an educated guess.");
				for (final int choice : choices) {
					Arrays.sort(item_arr_o);
					if (Arrays.binarySearch(item_arr_o, choice) >= 0) {
						verbose("Found choice at index " + index + ".");
						next.getChild(index).interact("Select");
						Time.sleep(Random.nextInt(1500, 2000));
						return;
					}
					++index;
				}

				verbose("Just going to guess...");
				final int randomIndex = Random.nextInt(10, 14);
				next.getChild(randomIndex).interact("Select");
				Time.sleep(Random.nextInt(1500, 2000));
			}
			return;
		}

		final Widget related = Widgets.get(WIDGET_RELATED);
		if (related.validate()) {
			verbose("WIDGET_VALIDATED: Related items");
			final String text = related.getChild(25).getText();
			verbose("HINT: " + text);

			for (final ObjectRelations question : WIDGET_ITEM_RELATIONS) {
				if (text.toLowerCase().contains(question.text.toLowerCase())) {
					verbose("Relation validated: " + question.text);
					verbose("Searching children");
					for (int childIndex = 42; childIndex <= 56; childIndex++) {
						verbose("[" + childIndex + "] Searching for " + related.getChild(childIndex).getChildId() + " in " + Arrays.toString(question.items) + ".");
						Arrays.sort(question.items);
						if (Arrays.binarySearch(question.items, related.getChild(childIndex).getChildId()) >= 0) {
							verbose("Found relation for this index (" + childIndex + "), selecting.");
							related.getChild(childIndex).interact("Select");
							Time.sleep(Random.nextInt(1200, 2000));
						}
					}
					break;
				}
			}

			Time.sleep(Random.nextInt(1200, 2000));

			verbose("Confirming attempt");
			if (related.getChild(26).interact("Confirm")) {
				Time.sleep(Random.nextInt(1200, 2000));
			}
			return;
		}

		if (Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT).validate()) {
			door = null;
			final String text = Widgets.get(WIDGET_CHAT, WIDGET_CHAT_TEXT).getText().toLowerCase();
			for (int i = 0; i < COLORS.length; i++) {
				if (text.contains(COLORS[i])) {
					door = SceneEntities.getNearest(OBJECT_ID_DOORS[i]);
					break;
				}
			}

			if (door != null) {
				if (!door.isOnScreen()) {
					Walking.walk(door);
					final Timer timer = new Timer(2000);
					while (timer.isRunning()) {
						if (Players.getLocal().isMoving()) {
							timer.reset();
						}
						Time.sleep(150);
					}
					Camera.turnTo(door);
				}

				if (door.isOnScreen() && door.interact("Open")) {
					Time.sleep(Random.nextInt(4500, 7800));
				}
				return;
			}
		}

		if (Widgets.clickContinue()) {
			verbose("Following conversation");
			Time.sleep(Random.nextInt(1200, 1800));
			return;
		}

		verbose("Unknown position - talking");
		final NPC dude = NPCs.getNearest("Mr. Mordaut");
		if (!dude.isOnScreen()) {
			Camera.turnTo(dude);
			if (!dude.isOnScreen()) {
				Walking.walk(dude);
				Time.sleep(1500);
				Camera.turnTo(dude);
			}
		}

		if (dude.isOnScreen() && dude.interact("Talk-to")) {
			final Timer timer = new Timer(3000);
			while (timer.isRunning() && !Widgets.canContinue()) {
				Time.sleep(150);
			}
		}
	}

	private int[] getItemArray(final int item) {
		for (final int[] items : Exam.WIDGET_ITEMS) {
			Arrays.sort(items);
			if (Arrays.binarySearch(items, item) >= 0) {
				return items;
			}
		}
		return null;
	}
}
