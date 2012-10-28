package org.powerbot.service.scripts;

import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.util.StringUtil;
import org.powerbot.util.io.IniParser;

/**
 * @author Paris
 */
public final class ScriptDefinition {
	private final String name, id, description, website;
	private final double version;
	private final String[] authors;
	private final boolean hidden;

	public String className;
	public URL source;
	public boolean local = false;
	private Category category = null;

	public enum Category {
		AGILITY(0, "course"),
		AIO(1, ""),
		COMBAT(2, "fight|kill|duel|soul"),
		CONSTRUCTION(3, "constr"),
		COOKING(4, "cook"),
		CRAFTING(5, "craft|spin|crush|grind|flax|vial"),
		DUNGEONEERING(6, "dung"),
		FARMING(7, "farm"),
		FIREMAKING(8, "fire"),
		FISHING(9, "fish"),
		FLETCHING(10, "fletch"),
		HERBLORE(11, "herb"),
		HUNTER(12, "hunt|catch"),
		MAGIC(13, "mage"),
		MINIGAME(14, "sorc|puzzle|minigame|pest"),
		MINING(15, "mine|ore"),
		MONEY(16, "gp|gold"),
		PRAYER(17, "pray|bones"),
		QUEST(18, "quest"),
		RANGED(19, "range|arrow"),
		RUNECRAFTING(20, "rune|ess"),
		SMITHING(21, "smith|bars"),
		SUMMONING(22, "summ"),
		THIEVING(23, "thief|steal|thiev"),
		WOODCUTTING(24, "wood|chop");

		public final int index;
		public final String regex;

		private Category(final int index, final String regex) {
			this.index = index;
			this.regex = regex;
		}
	}

	public ScriptDefinition(final Class<? extends ActiveScript> clazz) {
		this(clazz.getAnnotation(Manifest.class));
	}

	public ScriptDefinition(final Manifest manifest) {
		name = manifest.name();
		id = null;
		description = manifest.description();
		version = manifest.version();
		authors = manifest.authors();
		website = manifest.website();
		hidden = manifest.hidden();
	}

	public ScriptDefinition(final String name, final String id, final String description, final double version, final String[] authors, final String website, final boolean hidden) {
		this.name = name;
		this.id = id;
		this.description = description;
		this.version = version;
		this.authors = authors;
		this.website = website;
		this.hidden = hidden;
	}

	private String getCleanText(String s) {
		s = StringUtil.stripHtml(s.trim());
		s = s.replaceFirst("\\s*(?:\\(\\s*)?[Vv]?\\s*[\\d\\.]+\\s*(?:\\s*(?:[Aa]lpha|[Bb]eta|[AaBb]))?(?:\\)\\s*)?\\.?$", "");
		s = s.replaceFirst("\\s*[-~]+\\s*[Bb][Yy]\\s+.*$", "");
		s = s.replaceFirst("\\s+[-~]+\\s*.*$", "");
		s = s.replaceFirst("\\s*[Bb][Yy]\\s+.*$", "");
		return s;
	}

	public String getName() {
		String s = getCleanText(name);
		s = s.replaceFirst("^\\w+'[Ss]\\s+", "");
		s = s.replaceAll("^\\{[^\\}]*\\}|\\{[^\\}]*\\}$", "");
		s = s.replaceAll("\\s*(?:[~-]\\s*)?(?:[Vv]\\s*)?[\\d\\.]+\\s*$", "");
		s = s.replaceAll("\\s*\\(\\s*[\\d\\.]+\\s*\\)\\s*$", "");
		return s;
	}

	public String getID() {
		return id == null ? "" : id;
	}

	public String getDescription() {
		if (description == null || description.isEmpty()) {
			return "";
		}
		String s = getCleanText(description);
		if (s.length() > 2 && s.substring(s.length() - 1).equals(".") && !s.substring(0, s.length() - 1).contains(".")) {
			s = s.substring(0, s.length() - 1);
		}
		if (s.length() > 1 && s.substring(s.length() - 1).equals("!")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	public double getVersion() {
		return version;
	}

	public String[] getAllAuthors() {
		return authors;
	}

	public String getAuthors() {
		if (authors == null || authors.length == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		final String[] authors = getAllAuthors();
		for (int i = 0; i < authors.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(StringUtil.stripHtml(authors[i].trim()));
		}
		return sb.toString();
	}

	public String getWebsite() {
		final String url = website;
		return url != null && !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://")) ? url : null;
	}

	public boolean isHidden() {
		return hidden;
	}

	public Category getCategory() {
		if (category != null) {
			return category;
		}

		category = Category.AIO;
		final String sig = (getName() + " " + getDescription()).toLowerCase();
		int x = 0xfff;

		for (final Category c : Category.class.getEnumConstants()) {
			int z = sig.indexOf(c.name().toLowerCase());
			if (z != -1 && z < x) {
				x = z;
				category = c;
			}
			if (c.regex.length() == 0) {
				continue;
			}
			final Matcher m = Pattern.compile(c.regex).matcher(sig);
			if (m.find(0)) {
				z = m.start();
				if (z != -1 && z < x) {
					x = z;
					category = c;
				}
			}
		}

		return category;
	}

	public boolean matches(final String query) {
		final String tag = String.format("%s %s %s", getName(), getDescription(), getAuthors()).toLowerCase();
		return tag.contains(query.toLowerCase());
	}

	@Override
	public String toString() {
		return getName().toLowerCase();
	}

	public static ScriptDefinition fromMap(final Map<String, String> data) {
		final String name = data.containsKey("name") ? data.get("name") : null;
		final String id = data.containsKey("id") ? data.get("id") : null;
		final String description = data.containsKey("description") ? data.get("description") : null;
		final String website = data.containsKey("website") ? data.get("website") : null;
		final boolean hidden = data.containsKey("hidden") ? IniParser.parseBool(data.get("hidden")) : false;
		final String[] authors = data.containsKey("authors") ? data.get("authors").split(",") : new String[]{};
		double version = 1d;

		if (data.containsKey("version")) {
			try {
				version = Double.parseDouble(data.get("version"));
			} catch (final NumberFormatException ignored) {
			}
		}

		return name == null || name.isEmpty() ? null : new ScriptDefinition(name, id, description, version, authors, website, hidden);
	}
}
