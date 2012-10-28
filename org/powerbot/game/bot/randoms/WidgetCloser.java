package org.powerbot.game.bot.randoms;

import java.util.HashMap;
import java.util.Map;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(name = "Widget Closer", description = "Closes widgets that interrupt scripts", version = 0.1, authors = {"Timer"})
public class WidgetCloser extends AntiRandom {
	private static final Map<Integer, Integer> children = new HashMap<Integer, Integer>();
	private int failure = 0;
	private Timer timer;

	static {
		children.put(21, 43);//beholding a player's statuette (duellist's cap)
		children.put(1234, 15);//membership offers
		children.put(906, 354);//membership offers
		children.put(906, 493);//email register
		children.put(1252, 6);//Squeal of Fortune notification
		children.put(1253, 76);//Squeal of Fortune window
		children.put(1218, 77);//Advanced skill guide
		children.put(1107, 157);//clan thingys
		children.put(755, 44);//world map
	}

	public boolean validate() {
		if (Game.isLoggedIn()) {
			if (timer != null) {
				if (timer.isRunning()) {
					return false;
				}
				timer = null;
				failure = 0;
			}
			if (failure >= 3) {
				timer = new Timer(60000);
				return false;
			}

			for (final Map.Entry<Integer, Integer> child : children.entrySet()) {
				final WidgetChild widgetChild = Widgets.get(child.getKey(), child.getValue());
				if (widgetChild.validate()) {
					return true;
				}
			}
		}
		return false;
	}

	public void run() {
		try {
			for (final Map.Entry<Integer, Integer> child : children.entrySet()) {
				final WidgetChild widgetChild = Widgets.get(child.getKey(), child.getValue());
				if (widgetChild.validate()) {
					widgetChild.click(true);
					Time.sleep(Random.nextInt(1200, 2400));
					if (widgetChild.validate()) {
						failure++;
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
