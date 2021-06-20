/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools.program.agent.actions;

import krTools.KRInterface;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.parser.MOD2GParser;
import languageTools.program.agent.Module;

/**
 * Forces an exit from the current (non-anonymous) {@link Module}.
 * <p>
 * Modules create an (implicit) stack of contexts <module1, module2, module3,
 * ..., moduleN> when they are called, where 'moduleN' is the last module that
 * has been entered. Executing the exit-module action means that all anonymous
 * modules higher on the stack than the first non-anonymous module are exited as
 * well as the first non-anonymous module. Execution then continues in the
 * module one level lower in the stack.
 * </p>
 */
public class ExitModuleAction extends NonMentalAction {
	/**
	 * Creates an {@link ExitModuleAction} that forces an exit from the current
	 * (non-anonymous) module, i.e., the highest non-anonymous modules on the
	 * (implicit) module stack; all higher anonymous modules are also exited.
	 *
	 * @param kri
	 *            the {@link KRInterface}
	 */
	public ExitModuleAction(SourceInfo info) {
		super(ModuleValidator.getTokenName(MOD2GParser.EXITMODULE), info);
	}

	@Override
	public Action<Term> applySubst(Substitution substitution) {
		return this;
	}
}
