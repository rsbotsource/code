package org.powerbot.game.bot.randoms;

import org.powerbot.game.api.AntiRandom;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.bot.Context;

@Manifest(name = "Bank Pin", authors = {"Andy"}, version = 1.0)
public class BankPin extends AntiRandom {
	private final int[] PIN_COMPONENTS = {6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

	@Override
	public boolean validate() {
		return Game.isLoggedIn() && Widgets.get(13).validate() && getPin() != null;
	}

	@Override
	public void run() {
		final String pin = String.format(getPin());

		enterPin(pin);

		if (Widgets.get(211).validate()) {
			Widgets.get(211, 3).click(true);
		} else if (Widgets.get(217).validate()) {
			Time.sleep(1000, 1300);
		}
	}

	private String getPin() {
		try {
			return Context.resolve().getAccount().getPIN();
		} catch (final Exception ignored) {
		}
		return null;
	}

	private void enterPin(final String pin) {
		final int state = Settings.get(563);
		if (!Widgets.get(13).validate() || !Widgets.get(759).validate() || state == 4) {
			return;
		}
		final String pin_number = String.valueOf(pin.charAt(state));
		if (Widgets.get(13, PIN_COMPONENTS[Integer.valueOf(pin_number)]).click(true)) {
			Time.sleep(700, 1000);
		}
	}
}
