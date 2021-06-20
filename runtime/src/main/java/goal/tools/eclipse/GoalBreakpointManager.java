/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.tools.eclipse;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;

import goal.tools.debugger.BreakPoint;

public final class GoalBreakpointManager {
	private final static String DELIMITER = "|";
	private final static Map<File, GoalBreakpointManager> breakManagers = new ConcurrentHashMap<>();
	private final File file;
	private final Map<Integer, BreakPoint> breakpoints;

	public static void clearManagers() {
		breakManagers.clear();
	}

	public static GoalBreakpointManager getOrCreateGoalBreakpointManager(final File f) {
		final GoalBreakpointManager existing = breakManagers.get(f);
		if (existing == null) {
			return new GoalBreakpointManager(f);
		} else {
			return existing;
		}
	}

	private GoalBreakpointManager(final File f) {
		this.file = f;
		this.breakpoints = new ConcurrentHashMap<>();
		breakManagers.put(f, this);
	}

	public File getFile() {
		return this.file;
	}

	public void addBreakpoint(final int line, final BreakPoint.Type type) {
		this.breakpoints.put(line, new BreakPoint(this.file, line, type));
	}

	public void removeBreakpoint(final int line) {
		this.breakpoints.remove(line);
	}

	public Set<BreakPoint> getBreakPoints() {
		return ImmutableSet.copyOf(this.breakpoints.values());
	}

	@Override
	public String toString() {
		try {
			final StringBuffer buffer = new StringBuffer();
			buffer.append(this.file.getCanonicalPath()).append(DELIMITER).append(this.breakpoints.size());
			for (final int i : this.breakpoints.keySet()) {
				final BreakPoint point = this.breakpoints.get(i);
				buffer.append(DELIMITER).append(point.getLine()).append(DELIMITER).append(point.getType().name());
			}
			return buffer.toString();
		} catch (IOException e) {
			return ""; // TODO
		}
	}

	public static String saveAll() {
		final StringBuffer buffer = new StringBuffer();
		for (final GoalBreakpointManager manager : breakManagers.values()) {
			buffer.append(DELIMITER).append(DELIMITER).append(manager.toString());
		}
		return buffer.toString();
	}

	public static void loadAll(final String input) {
		clearManagers();
		for (final String s1 : input.split("\\" + DELIMITER + "\\" + DELIMITER)) {
			final String s2 = s1.trim();
			if (!s2.isEmpty()) {
				final String[] s = s2.split("\\" + DELIMITER);
				final File f = new File(s[0]);
				final GoalBreakpointManager manager = getOrCreateGoalBreakpointManager(f);
				final int size = Integer.parseInt(s[1]) * 2;
				for (int i = 2; i <= (size + 1); i += 2) {
					manager.addBreakpoint(Integer.parseInt(s[i]), BreakPoint.Type.valueOf(s[i + 1]));
				}
			}
		}
	}
}