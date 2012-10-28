package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.node.SceneObject;

@Manifest(name = "First Time Death", authors = {"Timer"}, version = 1.1)
public class FirstTimeDeath extends AntiRandom {
	@Override
	public boolean validate() {
		return Game.isLoggedIn() && SceneEntities.getNearest(45802) != null;
	}

	@Override
	public void run() {
		if (Widgets.get(1184, 13).validate() && Widgets.get(1184, 13).getText().contains("over here")) {
			if (Widgets.clickContinue()) {
				final SceneObject reaper = SceneEntities.getNearest(45802);
				if (reaper != null) {
					if (!reaper.isOnScreen()) {
						walk(reaper);
					}

					reaper.interact("Talk-to");
					final Timer timer = new Timer(2000);
					while (timer.isRunning() && !Widgets.canContinue()) {
						Time.sleep(150);
						if (Players.getLocal().isMoving()) {
							timer.reset();
						}
					}
				}
			}
			return;
		}

		if (Widgets.clickContinue()) {
			Time.sleep(Random.nextInt(2000, 3800));
			return;
		}
		if (Widgets.get(1188, 3).validate() && (Widgets.get(1188, 3).getText().contains("How do I") || Widgets.get(1188, 3).getText().contains("bye"))) {
			Widgets.get(1188, 3).interact("Continue");
			Time.sleep(Random.nextInt(2000, 3800));
			return;
		}
		if (Widgets.get(1188, 24).validate()) {
			Widgets.get(1188, 24).interact("Continue");
			Time.sleep(Random.nextInt(2000, 3800));
			return;
		}

		final SceneObject portal = SceneEntities.getNearest(45803);
		if (portal.isOnScreen()) {
			portal.interact("Enter");

			final Timer timer = new Timer(5000);
			while (timer.isRunning() && !Widgets.canContinue()) {
				if (Players.getLocal().isMoving()) {
					timer.reset();
				}
			}
		} else {
			Camera.setPitch(true);
			Camera.turnTo(portal);
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
