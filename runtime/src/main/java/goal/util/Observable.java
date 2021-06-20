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

/**
 * generic interface for observable functionality. Thread safe.
 *
 * @param <OBS>
 *            observer of OBJects coming from SRC
 * @param <SRC>
 *            the type of the event source of OBJs
 * @param <OBJ>
 *            the type of event objects created by this observable.
 */
public interface Observable<OBS extends Observer<SRC, OBJ>, SRC, OBJ> {

	/**
	 * Add a new observer. Nothing happens if observer already there.
	 *
	 * @param observer
	 */
	public void addObserver(OBS observer);

	/**
	 * Remove an observer. Nothing happens if observer not there.
	 *
	 * @param observer
	 */
	public void removeObserver(OBS observer);

	/**
	 * notify all our observers of some event. This notifies all observers available
	 * at the moment of the call. If the set of observers changes DURING the
	 * notifyAll handling, this change will be ignored.
	 */
	public void notifyObservers(SRC src, OBJ obj);
}
