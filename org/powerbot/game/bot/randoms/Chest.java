package org.powerbot.game.bot.randoms;

import java.awt.Point;
import java.awt.Rectangle;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Cap'n Arnav's Chest", authors = {"Timer"}, version = 1.0)
public class Chest extends AntiRandom {
	private static final int LOCATION_ID_CHEST_SOLVED = 42338;
	private static final int LOCATION_ID_PORTAL = 11369;
	private static final int NPC_ID_CAPTAIN = 2308;
	private static final int WIDGET_CHEST = 185;
	private static final int WIDGET_CHEST_CENTER = 23;
	private static final int WIDGET_CHEST_UNLOCK = 28;
	private static final int WIDGET_CHEST_OBJECTIVE = 32;
	private static final int[][] WIDGET_CHEST_OBJECTIVE_IDS = {
			{7, 14, 21},//BOWL
			{5, 12, 19},//RING
			{6, 13, 20},//COIN
			{8, 15, 22}//BAR
	};
	private static final int[][] WIDGET_CHEST_ARROWS = {{2, 3}, {9, 10}, {16, 17}};

	@Override
	public boolean validate() {
		return Game.isLoggedIn() && NPCs.getNearest(NPC_ID_CAPTAIN) != null && SceneEntities.getNearest(LOCATION_ID_PORTAL) != null;
	}

	@Override
	public void run() {
		Camera.setPitch(true);
		if (Players.getLocal().isMoving()) {
			final Timer timer = new Timer(3000);
			while (timer.isRunning()) {
				if (Players.getLocal().isMoving()) {
					timer.reset();
				}
				Time.sleep(150);
			}
			return;
		}
		if (Widgets.clickContinue()) {
			Time.sleep(150);
			return;
		}
		if (Widgets.get(1188, 24).validate() && Widgets.get(1188, 24).getText().contains("ready")) {
			Widgets.get(1188, 24).interact("Continue");
			Time.sleep(Random.nextInt(2000, 3000));
			return;
		}

		final Widget chestWidget = Widgets.get(WIDGET_CHEST);
		if (chestWidget != null && chestWidget.validate()) {
			final WidgetChild container = chestWidget.getChild(WIDGET_CHEST_CENTER);
			final String objective = chestWidget.getChild(WIDGET_CHEST_OBJECTIVE).getText();
			int index = -1;
			if (objective.contains("Bowl")) {
				index = 0;
			} else if (objective.contains("Ring")) {
				index = 1;
			} else if (objective.contains("Coin")) {
				index = 2;
			} else if (objective.contains("Bar")) {
				index = 3;
			}
			if (solved(index)) {
				if (chestWidget.getChild(WIDGET_CHEST_UNLOCK).click(true)) {
					Time.sleep(Random.nextInt(600, 900));
				}
				return;
			}
			if (index != -1) {
				for (int i = 0; i < 3; i++) {
					final WidgetChild target = chestWidget.getChild(WIDGET_CHEST_OBJECTIVE_IDS[index][i]);
					final int y = target.getRelativeY();
					int direction;
					if (y < 50 && y > -50) {
						direction = 0;
					} else if (y >= 50) {
						direction = 1;
					} else {
						direction = Random.nextInt(0, 2);
					}
					final WidgetChild arrow = chestWidget.getChild(WIDGET_CHEST_ARROWS[i][direction]);
					while (container.validate() && target.validate() && arrow.validate() &&
							!container.getBoundingRectangle().contains(target.getCentralPoint()) && new Timer(10000).isRunning()) {
						if (arrow.click(true)) {
							Time.sleep(Random.nextInt(800, 1200));
						}
					}
				}
				return;
			}
			return;
		}

		final SceneObject chestSolved = SceneEntities.getNearest(LOCATION_ID_CHEST_SOLVED);
		if (chestSolved != null) {
			final SceneObject portal = SceneEntities.getNearest(LOCATION_ID_PORTAL);
			if (portal != null && portal.interact("Enter")) {
				final Timer timer = new Timer(3000);
				while (timer.isRunning()) {
					if (Players.getLocal().isMoving()) {
						timer.reset();
					}
					Time.sleep(150);
				}
			}
			return;
		}

		final NPC captain = NPCs.getNearest(NPC_ID_CAPTAIN);
		if (captain != null) {
			final Timer walkingCheck = new Timer(1200);
			while (walkingCheck.isRunning()) {
				if (Players.getLocal().isMoving()) {
					return;
				}
				Time.sleep(100);
			}
			if (captain.interact("Talk-to")) {
				final Timer timer = new Timer(2000);
				while (timer.isRunning() && !Widgets.canContinue()) {
					Time.sleep(150);
				}
			}
		}
	}

	private boolean solved(final int index) {
		final Widget chestWidget = Widgets.get(WIDGET_CHEST);
		if (chestWidget != null && chestWidget.validate()) {
			final WidgetChild container = chestWidget.getChild(WIDGET_CHEST_CENTER);
			final Rectangle centerArea = container.getBoundingRectangle();
			final Point p1 = chestWidget.getChild(WIDGET_CHEST_OBJECTIVE_IDS[index][0]).getCentralPoint();
			final Point p2 = chestWidget.getChild(WIDGET_CHEST_OBJECTIVE_IDS[index][1]).getCentralPoint();
			final Point p3 = chestWidget.getChild(WIDGET_CHEST_OBJECTIVE_IDS[index][2]).getCentralPoint();
			return centerArea.contains(p1) && centerArea.contains(p2) && centerArea.contains(p3);
		}
		return false;
	}
}
