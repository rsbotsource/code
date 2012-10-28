package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
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
import org.powerbot.game.api.wrappers.graphics.CapturedModel;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Freaky Forester", authors = {"Timer"}, version = 1.0)
public class FreakyForester extends AntiRandom {
	private static final int LOCATION_ID_PORTAL = 15645;
	private static final int[] NPC_ID_FORESTER = {100, 2458};
	private static final int WIDGET_TALK = 1184;
	private static final int WIDGET_TALK_TEXT = 13;
	private static final String[] WIDGET_TALK_NUMBERS_TEXT = {" one", " two", " three", " four"};
	private static final String WIDGET_TALK_DONE_TEXT = "portal";
	private short[] phe = {};
	private final Filter<NPC> pheasantFilter = new Filter<NPC>() {
		public boolean accept(final NPC npc) {
			final Filter<CapturedModel> modelFilter = CapturedModel.newVertexFilter(phe);
			return modelFilter.accept(npc.getModel());
		}
	};

	@Override
	public boolean validate() {
		return NPCs.getNearest(NPC_ID_FORESTER) != null && SceneEntities.getNearest(LOCATION_ID_PORTAL) != null;
	}

	@Override
	public void run() {
		if (Widgets.canContinue()) {
			verbose("WIDGET VALIDATED: Continue");
			final WidgetChild textChild = Widgets.get(WIDGET_TALK, WIDGET_TALK_TEXT);
			if (textChild.validate()) {
				verbose("WIDGET VALIDATED: TEXT ???");
				final String text = textChild.getText().toLowerCase();
				if (text.contains(WIDGET_TALK_DONE_TEXT)) {
					verbose("WIDGET: Continuing due the fact we're done...");
					Widgets.clickContinue();
					Time.sleep(Random.nextInt(1800, 2500));
					return;
				}
				verbose("What is he saying???");
				for (int i = 0; i < WIDGET_TALK_NUMBERS_TEXT.length; i++) {
					if (text.contains(WIDGET_TALK_NUMBERS_TEXT[i])) {
						verbose("Found the text '" + WIDGET_TALK_NUMBERS_TEXT[i] + "'!  Setting model (" + Models.all[i].hashCode() + ").");
						phe = Models.all[i];
					}
				}
			}
			verbose("Nothing validated -- CONTINUE!");
			Widgets.clickContinue();
			Time.sleep(Random.nextInt(1800, 2500));
			return;
		}

		verbose("SETTING 334: " + Settings.get(334));
		if (Settings.get(334) == 0x2) {
			verbose("SETTING VALIDATED: Depart.");
			final SceneObject portal = SceneEntities.getNearest(LOCATION_ID_PORTAL);
			if (portal != null) {
				if (!portal.isOnScreen()) {
					walk(portal);
					return;
				}

				if (portal.interact("Enter")) {
					Time.sleep(Random.nextInt(2500, 4000));
				}
			}
			return;
		}

		if (Settings.get(334) == 0x1 && phe.length != 0) {
			verbose("SETTING VALIDATED: Kill!");
			final NPC pheasant = NPCs.getNearest(pheasantFilter);
			if (pheasant != null) {
				verbose("Found pheasant...");
				if (!pheasant.isOnScreen()) {
					verbose("NPC: Not on screen!  Walking...");
					walk(pheasant);
					return;
				}

				verbose("NPC: Attempting attack");
				if (pheasant.interact("Attack")) {
					final Timer timer = new Timer(5000);
					while (timer.isRunning() && !Widgets.canContinue()) {
						if (Players.getLocal().isMoving()) {
							timer.reset();
						}
						if (!pheasant.validate()) {
							timer.reset();
						} else if (pheasant.isInCombat()) {
							timer.reset();
						}
						Time.sleep(150);
					}
					phe = new short[0];
					verbose("CAN CONTINUE: " + Widgets.canContinue());
				}
			}
			return;
		}

		verbose("Require conversation with forester!");
		final NPC forester = NPCs.getNearest(NPC_ID_FORESTER);
		if (forester != null) {
			if (!forester.isOnScreen()) {
				walk(forester);
				return;
			}

			if (forester.interact("Talk-to")) {
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

	private static class Models {
		private static final short[] oneTail = {
				0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2,
				3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9, 10,
				10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14, 14,
				14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19, 19,
				34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28, 29,
				29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60, 63,
				63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70, 70,
				70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73, 61,
				59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79, 82,
				41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44, 44,
				95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101, 101,
				101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103, 104,
				104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106, 106,
				107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37, 38,
				110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99, 100,
				100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130, 130,
				130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116, 146,
				118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149, 149,
				153, 154, 154, 156, 157, 157, 150, 151, 151, 151, 152, 152,
				152, 155, 158, 159, 159, 159, 160, 160, 161, 161, 161, 164,
				164, 164, 164, 165, 165, 167, 167, 164, 164, 164, 164, 172,
				172, 172, 172, 172, 172, 171, 171
		};
		private static final short[] twoTail = {
				0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2,
				3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9, 10,
				10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14, 14,
				14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19, 19,
				34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28, 29,
				29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60, 63,
				63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70, 70,
				70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73, 61,
				59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79, 82,
				41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44, 44,
				95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101, 101,
				101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103, 104,
				104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106, 106,
				107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37, 38,
				110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99, 100,
				100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130, 130,
				130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116, 146,
				118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149, 149,
				149, 149, 149, 150, 150, 150, 150, 151, 151, 151, 151, 152,
				152, 152, 152, 153, 153, 153, 154, 154, 154, 154, 155, 155,
				166, 166, 156, 165, 155, 155, 156, 156, 169, 169, 172, 172,
				172, 172, 172, 172, 161, 162, 162, 162, 163, 163, 163, 164,
				158, 158, 158, 175, 175, 175, 175, 176, 176, 176, 176, 184,
				184, 185, 185, 178, 182, 182, 182, 182, 177, 189, 189, 189,
				189, 190, 190, 188, 188, 179, 179, 179, 160
		};
		private static final short[] threeTail = {
				0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2,
				2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9,
				10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14,
				14, 14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19,
				19, 34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28,
				29, 29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60,
				63, 63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70,
				70, 70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73,
				61, 59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79,
				82, 41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44,
				44, 95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101,
				101, 101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103,
				104, 104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106,
				106, 107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37,
				38, 110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99,
				100, 100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130,
				130, 130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116,
				146, 118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149,
				149, 149, 149, 149, 150, 150, 150, 150, 151, 151, 151, 151,
				152, 152, 152, 152, 152, 153, 153, 153, 154, 154, 154, 154,
				155, 155, 167, 167, 156, 166, 155, 155, 156, 156, 170, 170,
				173, 173, 173, 173, 173, 173, 176, 176, 176, 176, 176, 176,
				163, 163, 164, 164, 164, 180, 180, 180, 180, 181, 181, 183,
				183, 181, 178, 180, 180, 180, 180, 188, 188, 188, 188, 188,
				188, 187, 187, 192, 192, 192, 158, 158, 158, 158, 159, 159,
				159, 160, 160, 160, 161, 200, 200, 200, 200, 199, 201, 201,
				201, 201, 203, 203, 203, 203, 197, 197, 197, 197, 202, 202,
				206, 206, 209, 209
		};
		private static final short[] fourTail = {
				0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2,
				2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9,
				10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14,
				14, 14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19,
				19, 34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28,
				29, 29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60,
				63, 63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70,
				70, 70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73,
				61, 59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79,
				82, 41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44,
				44, 95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101,
				101, 101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103,
				104, 104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106,
				106, 107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37,
				38, 110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99,
				100, 100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130,
				130, 130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116,
				146, 118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149,
				149, 149, 149, 149, 150, 150, 150, 150, 150, 150, 150, 151,
				151, 152, 152, 164, 164, 166, 167, 167, 169, 169, 153, 154,
				154, 154, 155, 155, 155, 155, 156, 156, 156, 156, 177, 177,
				177, 177, 163, 165, 165, 168, 168, 170, 171, 171, 171, 171,
				171, 181, 181, 181, 181, 172, 172, 173, 173, 184, 184, 173,
				180, 180, 180, 180, 180, 180, 180, 180, 161, 161, 162, 162,
				160, 190, 190, 190, 190, 190, 190, 190, 190, 195, 195, 195,
				195, 158, 158, 157, 176, 176, 176, 176, 157, 157, 198, 198,
				201, 201, 182, 182, 183, 183, 204, 204, 205, 205, 206, 206,
				189, 189, 189, 189, 208, 208, 210, 210, 208, 208, 192, 192,
				211, 211, 213, 213, 211, 211, 214, 214, 215, 215, 160, 160,
				160, 160, 193, 193, 218, 218, 219, 219, 157, 157
		};
		private static final short[][] all = {oneTail, twoTail, threeTail, fourTail};
	}
}
