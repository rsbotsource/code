package org.powerbot.game.bot.randoms;

import java.util.HashMap;
import java.util.Map;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

/**
 * Solves the mime random event by mimicking the actions he performs.
 * <p/>
 * The player must copy the mime's actions to entertain the three Strange Watchers.
 *
 * @author Timer
 */
@Manifest(name = "Mime", description = "Mimics the mime", version = 0.1, authors = {"Timer"})
public class Mime extends AntiRandom {
	private static final int WIDGET = 188;
	private static final int NPC_MIME_ID = 1056;
	private static final Tile PERFORMANCE_TILE = new Tile(2008, 4762, 0);

	private static final Map<Integer, String> emotes = new HashMap<Integer, String>();
	private int animation;

	public Mime() {
		animation = -1;
	}

	static {
		emotes.put(857, "Think");
		emotes.put(860, "Cry");
		emotes.put(861, "Laugh");
		emotes.put(866, "Dance");
		emotes.put(1128, "Glass Wall");
		emotes.put(1129, "Lean on air");
		emotes.put(1130, "Climb Rope");
		emotes.put(1131, "Glass Box");
	}

	public boolean validate() {
		return Game.isLoggedIn() && NPCs.getNearest(new Filter<NPC>() {
			public boolean accept(final NPC npc) {
				return npc.getId() == NPC_MIME_ID;
			}
		}) != null;
	}

	public void run() {
		if (Camera.getPitch() < 80) {
			Camera.setPitch(true);
		}
		Widgets.clickContinue();

		if (Players.getLocal().getLocation().equals(PERFORMANCE_TILE)) {
			final NPC mime = NPCs.getNearest(new Filter<NPC>() {
				public boolean accept(final NPC npc) {
					return npc.getId() == NPC_MIME_ID;
				}
			});
			if (mime != null) {
				final int mimeAnimation = mime.getAnimation();
				if (mimeAnimation != -1 && mimeAnimation != 858) {
					animation = mimeAnimation;
				}

				final Widget widget = Widgets.get(WIDGET);
				if (widget.validate()) {
					final String text = emotes.get(animation);
					if (text != null) {
						for (final WidgetChild widgetChild : widget.getChildren()) {
							if (widgetChild.getText().equalsIgnoreCase(text)) {
								if (widgetChild.interact(text)) {
									Time.sleep(Random.nextInt(1200, 2000));
								}
							}
						}
					}
				}
			}
		} else {
			animation = -1;
			PERFORMANCE_TILE.interact("Walk here");
			Time.sleep(Random.nextInt(2500, 3800));
		}
	}
}
