/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
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

package tuprolog.language;

import java.util.LinkedHashSet;
import java.util.Set;

import krTools.language.Var;
import krTools.parser.SourceInfo;

/**
 * A Prolog variable.
 */
public class PrologVar extends PrologTerm implements Var {
	/**
	 * Creates a variable.
	 *
	 * @param var
	 *            A JPL variable.
	 * @param info
	 *            A source info object.
	 */
	public PrologVar(alice.tuprolog.Var var, SourceInfo info) {
		super(var, info);
	}

	/**
	 * Returns JPL variable.
	 */
	public alice.tuprolog.Var getVariable() {
		return (alice.tuprolog.Var) getTerm();
	}

	@Override
	public boolean isVar() {
		return true;
	}

	/**
	 * An underscore is an anonymous Prolog variable. Note that variables that
	 * *start* with _ are not anonymous.
	 *
	 * @return {@code true} if variable is anonymous, {@code false} otherwise.
	 */
	public boolean isAnonymous() {
		return getTerm() instanceof alice.tuprolog.Var && ((alice.tuprolog.Var) getTerm()).getName().equals("_");
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public Set<Var> getFreeVar() {
		LinkedHashSet<Var> set = new LinkedHashSet<>(1);
		set.add(this);
		return set;
	}

	@Override
	public Var getVariant(Set<Var> usedNames) {
		String name = (getTerm() instanceof alice.tuprolog.Var) ? ((alice.tuprolog.Var) getTerm()).getName() : "";
		SourceInfo theinfo = getSourceInfo();

		int n = 1;
		Var newVar;
		do {
			newVar = new PrologVar(new alice.tuprolog.Var(name + "_" + n), theinfo);
			n++;
		} while (usedNames.contains(newVar));

		return newVar;
	}
}