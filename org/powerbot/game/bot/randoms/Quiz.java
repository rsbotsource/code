package org.powerbot.game.bot.randoms;

import java.util.Arrays;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Quiz Master", authors = {"Timer"}, version = 1.0)
public class Quiz extends AntiRandom {
	private static final int WIDGET_QUIZ = 191;
	private static final int[] WIDGET_QUIZ_CHILD_ID_FISH = {6189, 6190};
	private static final int[] WIDGET_QUIZ_CHILD_ID_COMBAT = {6192, 6194};
	private static final int[] WIDGET_QUIZ_CHILD_ID_FARMING = {6195, 6196};
	private static final int[] WIDGET_QUIZ_CHILD_ID_JEWELRY = {6197, 6198};

	private static final int[][] WIDGET_QUIZ_ITEMS = {
			WIDGET_QUIZ_CHILD_ID_FISH,
			WIDGET_QUIZ_CHILD_ID_COMBAT,
			WIDGET_QUIZ_CHILD_ID_FARMING,
			WIDGET_QUIZ_CHILD_ID_JEWELRY
	};

	private static final String[] WIDGET_QUIZ_NAMES = {
			"Fish", "Combat", "Farming", "Jewelry"
	};

	@Override
	public boolean validate() {
		return NPCs.getNearest(2477) != null;
	}

	@Override
	public void run() {
		if (Widgets.clickContinue()) {
			verbose("Listening to his energetic shouting in this black hole...");
			Time.sleep(Random.nextInt(2000, 3500));
			return;
		}

		if (getSlotId(0) != -1) {
			verbose("WIDGET VALIDATED: Quiz question");
			final int[] slots = {getSlotId(0), getSlotId(1), getSlotId(2)};
			verbose("WIDGET DEBUG: Slots " + Arrays.toString(slots));

			int[] valid = null;
			verbose("Attempting matches");
			int name_index = 0;
			for (final int[] QUIZ_ITEMS : WIDGET_QUIZ_ITEMS) {
				verbose("Checking " + WIDGET_QUIZ_NAMES[name_index] + " " + Arrays.toString(QUIZ_ITEMS));
				int count = 0;
				for (final int ITEM : QUIZ_ITEMS) {
					verbose("SEARCH: " + ITEM + " in " + Arrays.toString(QUIZ_ITEMS));
					for (final int slot : slots) {
						if (slot == ITEM) {
							count++;
						}
					}
					verbose("NEW COUNT: " + count);
				}
				verbose("FINAL COUNT: " + count);

				if (count == 2) {
					verbose("Generalized type: " + WIDGET_QUIZ_NAMES[name_index] + " " + Arrays.toString(QUIZ_ITEMS) + ".");
					valid = QUIZ_ITEMS;
					break;
				}

				++name_index;
			}

			if (valid != null) {
				verbose("== ENTER SEARCH ==");
				for (int index = 0; index < slots.length; index++) {
					int count = 0;
					for (final int id : valid) {
						if (id == slots[index]) {
							count++;
						}
					}
					verbose("SEARCH " + index + " (" + slots[index] + ") IN " + Arrays.toString(valid) + " RETURNED " + count);
					if (count == 0) {
						verbose("NO MATCH == FOUND INVALID CHILD");
						final WidgetChild widgetChild = getSlot(index);
						verbose("Attempting click.");
						if (widgetChild != null && widgetChild.validate()) {
							verbose("WIDGET INTERACT: \"Continue\": " + Boolean.toString(widgetChild.interact("Continue")));
							Time.sleep(Random.nextInt(800, 1200));
						}
						return;
					}
				}
			}
		}
	}

	private int getSlotId(final int slot) {
		return Widgets.get(WIDGET_QUIZ, 5 + slot).getChildId();
	}

	private WidgetChild getSlot(final int slot) {
		return Widgets.get(WIDGET_QUIZ, 2 + slot);
	}
}