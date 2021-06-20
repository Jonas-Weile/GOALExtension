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
package goal.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;

/**
 * Implements generic observable functionality. Thread safe.
 *
 * @param <OBS>
 *            observer of OBJects coming from SRC
 * @param <SRC>
 *            the type of the event source of OBJs
 * @param <OBJ>
 *            the type of event objects created by this observable.
 */
public class DefaultObservable<OBS extends Observer<SRC, OBJ>, SRC, OBJ> implements Observable<OBS, SRC, OBJ> {
	private final Set<OBS> observers = Collections.newSetFromMap(new ConcurrentHashMap<OBS, Boolean>());

	@Override
	public void addObserver(OBS observer) {
		this.observers.add(observer);
	}

	@Override
	public void removeObserver(OBS observer) {
		this.observers.remove(observer);
	}

	/**
	 * returns a static copy of the current list of observers.
	 *
	 * @return the current list of observers
	 */
	private Set<OBS> getObservers() {
		return ImmutableSet.copyOf(this.observers);
	}

	/**
	 * notify all our observers of some event. This notifies all observers
	 * available at the moment of the call. If the set of observers changes
	 * DURING the notifyAll handling, this change will be ignored.
	 * <p>
	 * IMPORTANT: notifyObservers should only be called by the class
	 * implementing this, and not by external users of the observable such as
	 * Observers. Can we enforce this?
	 */
	@Override
	public void notifyObservers(SRC src, OBJ obj) {
		for (OBS obs : getObservers()) {
			try {
				obs.eventOccured(src, obj);
			} catch (Throwable e) { // Callback protection
				new Warning(String.format(Resources.get(WarningStrings.FAILED_CALLBACK_1), obs.toString()), e).emit();
			}
		}
	}
}
