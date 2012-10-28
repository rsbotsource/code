package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.node.Item;

@Manifest(name = "Squeal of Fortune Ticket Claim", authors = {"Timer"})
public class SpinTickets extends AntiRandom {
	public static final int ITEM_ID_SPIN_TICKET = 24154;
	public static final int ITEM_ID_SPIN_TICKET_X2 = 24155;

	@Override
	public boolean validate() {
		return Game.isLoggedIn() && Tabs.getCurrent() == Tabs.INVENTORY &&
				(Inventory.getItem(ITEM_ID_SPIN_TICKET) != null || Inventory.getItem(ITEM_ID_SPIN_TICKET_X2) != null);
	}

	@Override
	public void run() {
		Item ticket = Inventory.getItem(ITEM_ID_SPIN_TICKET);
		if (ticket == null) {
			ticket = Inventory.getItem(ITEM_ID_SPIN_TICKET_X2);
		}

		if (ticket != null) {
			if (ticket.getWidgetChild().interact("Claim")) {
				Time.sleep(Random.nextInt(2000, 3500));
			}
		}
	}
}
