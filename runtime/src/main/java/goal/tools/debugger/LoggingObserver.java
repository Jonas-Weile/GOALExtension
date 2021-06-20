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
package goal.tools.debugger;

import java.util.logging.Level;

import events.Channel;
import goal.preferences.DebugPreferences;
import goal.tools.logging.InfoLog;
import goal.tools.logging.StringsLogRecord;

/**
 * This is a default observer for Debugger that forwards breakpoint hit info to
 * the {@link InfoLog}.
 */
public class LoggingObserver implements DebugObserver {
	private final ObservableDebugger debugger;

	/**
	 * By calling this, an observer is attached to given debugger. It forwards
	 * breakpoint hit info into the {@link InfoLog} log.
	 *
	 * @param debugger
	 *            The current debugger.
	 */
	public LoggingObserver(ObservableDebugger debugger) {
		this.debugger = debugger;
	}

	/**
	 * Subscribe to all channels that were selected for viewing by the user.
	 */
	public void subscribe() {
		for (Channel channel : Channel.values()) {
			if (DebugPreferences.getChannelState(channel).canView()) {
				this.debugger.subscribe(this, channel);
				if (!Channel.getConditionalChannel(channel).equals(channel)) {
					this.debugger.subscribe(this, Channel.getConditionalChannel(channel));
				}
			}
		}
	}

	@Override
	public String getObserverName() {
		return "LoggingObserver";
	}

	@Override
	public boolean notifyBreakpointHit(DebugEvent event) {
		String string = event.toString();
		if (event.getChannel() == Channel.GOAL_ACHIEVED) {
			// FIXME: this is very hacky...
			string.replace("dropped", "achieved and removed");
		}
		new StringsLogRecord(Level.INFO, string).emit();
		return true;
	}
}
