package org.powerbot.concurrent.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StrategyGroup implements Iterable<Strategy> {
	private final List<Strategy> group;

	public StrategyGroup() {
		group = new ArrayList<Strategy>();
	}

	public StrategyGroup(final Strategy[] strategies) {
		this();
		Collections.addAll(group, strategies);
	}

	public StrategyGroup(final Collection<Strategy> collection) {
		this();
		group.addAll(collection);
	}

	public void group(final Strategy strategy) {
		group.add(strategy);
	}

	public boolean split(final Strategy strategy) {
		return group.remove(strategy);
	}

	@Override
	public Iterator<Strategy> iterator() {
		return group.iterator();
	}
}
