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

/**
 * <p>
 * Observer of a debugger. A debug observer receives debug events from the
 * debuggers it is subscribed to, by call backs to
 * {@link #notifyBreakpointHit(DebugEvent)}. A default implementation is
 * available in the abstract class {@link MyDebugObserver}.
 * </p>
 * <p>
 * The owner of this observer needs to use
 * {@link ObservableDebugger#subscribe(DebugObserver, Channel)} and
 * {@link ObservableDebugger#unsubscribe(DebugObserver)} the observer with one
 * or more debuggers. Also it needs to enable the events it wishes to observe,
 * see {@link ObservableDebugger#addPause(DebugObserver, Channel)} and
 * {@link ObservableDebugger#subscribe(DebugObserver, Channel)}
 * </p>
 */
public interface DebugObserver {

	/**
	 * Returns the name of the debug observer. Must contain unique name, otherwise
	 * subscribe will throw exception.
	 *
	 * @return name of the debug observer.
	 */
	String getObserverName();

	/**
	 * <p>
	 * Handles debug events received from the debugger. A debugger calls this method
	 * whenever a debug event that needs to be reported occurs.
	 * </p>
	 * <p>
	 * IMPORTANT: this method is called by the thread that is being debugged. The
	 * method should return immediately to not block the calling thread. For pausing
	 * or stepping the (agent) thread that is being debugged, the corresponding
	 * debugger methods should be used.
	 * </p>
	 *
	 * @param event
	 *            debug event received from debugger.
	 * @return boolean if any notifier return false, the related debug event will be
	 *         disregarded by the actual debugger
	 */
	boolean notifyBreakpointHit(DebugEvent event);

	/**
	 * We DO use equals on DebugObservers. But the default for equals is: an object
	 * is only equal to itself. And this should work for normal debug observers.
	 */

}
