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
package goal.core.runtime;

import goal.util.Observer;

/**
 * interface to observe events from the {@link RuntimeManager}.
 */
public interface RuntimeEventObserver extends Observer<RuntimeManager<?, ?>, RuntimeEvent> {
	/**
	 * This function is called when a {@link RuntimeEvent} occurs. To be implemented
	 * by the observer.
	 *
	 * @param source
	 *            the {@link RuntimeManager} creating the event
	 * @param evt
	 *            the {@link RuntimeEvent} that happened.
	 */
	@Override
	public void eventOccured(RuntimeManager<?, ?> source, RuntimeEvent evt);
}
