package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

/**
 * Solves the pinball random by tagging pillars.
 * <p/>
 * The object of the game is to tag each pillar that has glowing rings around it.
 * This scores the player one point.
 * After he or she scores ten points he or she is free to leave through the exit.
 * If a pillar that doesn't have the glowing rings around it is tagged,
 * the score is reset to zero and the player must try again.
 *
 * @author Timer
 */
@Manifest(name = "Pinball", description = "Tags the correct pillars and completes the random event", version = 0.1, authors = {"Timer"})
public class Pinball extends AntiRandom {
	private static final int[] INACTIVE_PILLARS = {15001, 15003, 15005, 15007, 15009};
	private static final int[] ACTIVE_PILLARS = {15000, 15002, 15004, 15006, 15008};
	private static final int INTERFACE_PINBALL_SCORE = 263;

	public boolean validate() {
		return Game.isLoggedIn() && SceneEntities.getNearest(INACTIVE_PILLARS) != null;
	}

	public void run() {
		if (Widgets.clickContinue()) {
			Time.sleep(Random.nextInt(300, 500));
			return;
		}

		if (Players.getLocal().isMoving() || Players.getLocal().getAnimation() != -1) {
			Time.sleep(Random.nextInt(300, 500));
			return;
		}

		if (getScore() >= 10) {
			final SceneObject exit = SceneEntities.getNearest(15010);
			if (exit != null) {
				if (exit.getLocation().isOnScreen()) {
					Time.sleep(exit.interact("Exit") ? Random.nextInt(4000, 4200) : 0);
					return;
				} else {
					Camera.setPitch(false);
					Camera.turnTo(exit);
					if (exit.getLocation().interact("Walk here")) {
						Time.sleep(Random.nextInt(1400, 1500));
					}
					return;
				}
			}
		}

		final SceneObject pillar = SceneEntities.getNearest(ACTIVE_PILLARS);
		if (pillar != null) {
			if (Calculations.distance(Players.getLocal().getLocation(), pillar.getLocation()) > 2 && !pillar.isOnScreen()) {
				pillar.getLocation().interact("Walk here");
				Time.sleep(Random.nextInt(500, 600));
				return;
			}

			if (pillar.interact("Tag")) {
				final int before = getScore();
				for (int i = 0; i < 50; i++) {
					if (getScore() > before) {
						Time.sleep(Random.nextInt(50, 100));
						return;
					}
					Time.sleep(Random.nextInt(70, 100));
				}
			}
		}
		Time.sleep(Random.nextInt(50, 100));
	}

	private int getScore() {
		final WidgetChild score = Widgets.get(INTERFACE_PINBALL_SCORE).getChild(1);
		try {
			return Integer.parseInt(score.getText().split(" ")[1]);
		} catch (final ArrayIndexOutOfBoundsException t) {
			return -1;
		}
	}
}
