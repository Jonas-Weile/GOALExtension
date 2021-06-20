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
package goal.core.runtime.service.environmentport.environmentport.events;

/**
 * event to indicate new entity.
 */
public class NewEntityEvent extends EnvironmentEvent {
	/** Auto-generated serial version UID */
	private static final long serialVersionUID = 6387717146943403919L;
	private final String entity;
	private final String type;

	public NewEntityEvent(String entity, String type) {
		this.entity = entity;
		this.type = type;
	}

	/**
	 * @return The entity
	 */
	public String getEntity() {
		return this.entity;
	}

	/**
	 * @return The type of the new entity
	 */
	public String getType() {
		return this.type;
	}

}