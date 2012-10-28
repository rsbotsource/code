package org.powerbot.game.bot.randoms;

import java.util.ArrayList;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.graphics.CapturedModel;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;

@Manifest(name = "Evil Twin", authors = {"Timer"}, version = 1.0)
public class EvilTwin extends AntiRandom {
	private static final int LOCATION_ID_DOOR = 14982;
	private static final int LOCATION_ID_CLAW = 14976;
	private static final int WIDGET_CONTROLS = 240;
	private static final int WIDGET_CONTROLS_UP = 29;
	private static final int WIDGET_CONTROLS_DOWN = 30;
	private static final int WIDGET_CONTROLS_LEFT = 31;
	private static final int WIDGET_CONTROLS_RIGHT = 32;
	private static final int WIDGET_CONTROLS_GRAB = 28;
	private CapturedModel model;
	private int xCheck = 0;
	private boolean finished = false;

	@Override
	public boolean validate() {
		return NPCs.getNearest(new Filter<NPC>() {
			@Override
			public boolean accept(final NPC npc) {
				return npc.getName().equalsIgnoreCase("Molly") && npc.getInteracting() != null && npc.getInteracting().equals(Players.getLocal());
			}
		}) != null || SceneEntities.getNearest(14978) != null;
	}

	@Override
	public void run() {
		final NPC molly = NPCs.getNearest(new Filter<NPC>() {
			@Override
			public boolean accept(final NPC npc) {
				return npc.getName().equalsIgnoreCase("Molly");
			}
		});
		if (molly != null) {
			xCheck = molly.getLocation().getX() + 4;
		}


		if (Widgets.get(1188, 3).validate()) {
			verbose("Selecting first chat option ...");
			Widgets.get(1188, 3).interact("Continue");
			Time.sleep(Random.nextInt(1000, 2000));
			return;
		}
		if (Widgets.clickContinue()) {
			verbose("Following conversation ...");
			Time.sleep(Random.nextInt(1500, 2200));
			final Timer timer = new Timer(2000);
			while (timer.isRunning() && !Widgets.canContinue()) {
				Time.sleep(150);
			}
			return;
		}

		final Player player = Players.getLocal();

		if (molly != null && player.getLocation().getX() <= xCheck && Settings.get(334) != 0x2 && !finished) {
			verbose("We have to leave the room.");
			verbose(molly.getModel().toString());
			model = molly.getModel();
			verbose("Molly: " + model.toString());
			final SceneObject location = SceneEntities.getNearest(LOCATION_ID_DOOR);
			if (location != null && location.interact("Open")) {
				final Timer t = new Timer(2000);
				while (t.isRunning()) {
					if (player.isMoving()) {
						t.reset();
					}
					Time.sleep(150);
				}
			}
			return;
		}

		if (molly == null && player.getLocation().getX() > xCheck) {
			verbose("Operate the claw!  Molly is not back yet...");
			final Widget widget = Widgets.get(WIDGET_CONTROLS);
			if (widget.validate()) {
				verbose("WIDGET VALIDATED");
				navigateClaw();
				final Timer t = new Timer(12000);
				while (!Widgets.canContinue() && t.isRunning()) {
					Time.sleep(150);
				}
				return;
			}

			verbose("We must interact with the control panel!");
			final SceneObject control = SceneEntities.getNearest(14978);
			if (control != null) {
				if (!control.isOnScreen()) {
					Camera.turnTo(control);
				} else {
					if (control.interact("Use")) {
						final Timer t = new Timer(5000);
						while (t.isRunning()) {
							if (Widgets.get(WIDGET_CONTROLS).validate()) {
								break;
							}
						}
					}
				}
			}
			return;
		}

		if (molly != null && player.getLocation().getX() > xCheck) {
			verbose("Molly is back, go through the door...");
			finished = true;
			final SceneObject location = SceneEntities.getNearest(LOCATION_ID_DOOR);
			if (location != null) {
				if (!location.isOnScreen()) {
					Camera.setPitch(false);
					Camera.turnTo(location);
				} else if (location.interact("Open")) {
					final Timer t = new Timer(2000);
					while (t.isRunning()) {
						if (player.isMoving()) {
							t.reset();
						}
						Time.sleep(150);
					}
				}
			}
		}

		if (molly != null) {
			if (!molly.isOnScreen()) {
				Camera.turnTo(molly);
				return;
			}

			if (molly.interact("Talk-to")) {
				final Timer timer = new Timer(5000);
				while (timer.isRunning() && !Widgets.canContinue()) {
					Time.sleep(150);
				}
				if (Widgets.canContinue()) {
					finished = false;
				}
			}
		}
	}

	private void navigateClaw() {
		SceneObject claw;
		NPC suspect;
		verbose("NAVIGATION: BEGIN");
		while ((claw = SceneEntities.getNearest(LOCATION_ID_CLAW)) != null && (suspect = NPCs.getNearest(new Filter<NPC>() {
			@Override
			public boolean accept(final NPC npc) {
				return npc.getModel().equals(model);
			}
		})) != null) {
			final Tile clawLoc = claw.getLocation();
			final Tile susLoc = suspect.getLocation();
			verbose("Claw: " + clawLoc.toString());
			verbose("Molly's twin: " + susLoc.toString());
			final ArrayList<Integer> options = new ArrayList<Integer>();
			if (susLoc.getX() > clawLoc.getX()) {
				options.add(WIDGET_CONTROLS_LEFT);
			}
			if (susLoc.getX() < clawLoc.getX()) {
				options.add(WIDGET_CONTROLS_RIGHT);
			}
			if (susLoc.getY() > clawLoc.getY()) {
				options.add(WIDGET_CONTROLS_DOWN);
			}
			if (susLoc.getY() < clawLoc.getY()) {
				options.add(WIDGET_CONTROLS_UP);
			}
			if (options.isEmpty()) {
				options.add(WIDGET_CONTROLS_GRAB);
			}
			final Widget i = Widgets.get(WIDGET_CONTROLS);
			if (i != null && i.validate()) {
				i.getChild(options.get(Random.nextInt(0, options.size()))).click(true);
			}
			final Timer timer = new Timer(3500);
			while (!hasClawMoved(clawLoc) && timer.isRunning()) {
				Time.sleep(10);
			}
		}
		verbose("NAVIGATION: END");
	}

	private boolean hasClawMoved(final Tile prevClawLoc) {
		final SceneObject claw = SceneEntities.getNearest(LOCATION_ID_CLAW);
		return claw != null && !prevClawLoc.equals(claw.getLocation());
	}
}
