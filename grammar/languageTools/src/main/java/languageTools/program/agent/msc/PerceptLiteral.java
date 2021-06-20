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

package languageTools.program.agent.msc;

import java.util.Set;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.parser.MOD2GParser;
import languageTools.program.agent.selector.Selector;

public class PerceptLiteral extends MentalLiteral {

	public PerceptLiteral(boolean polarity, Selector selector, Query query, Set<String> signatures, SourceInfo info) {
		super(polarity, selector, query, signatures, info);
	}

	@Override
	public MentalLiteral applySubst(Substitution substitution) {
		return new PerceptLiteral(isPositive(), (getSelector() == null) ? null : getSelector().applySubst(substitution),
				(getFormula() == null) ? null : getFormula().applySubst(substitution), getUsedSignatures(),
				getSourceInfo());
	}

	@Override
	public String getOperator() {
		return ModuleValidator.getTokenName(MOD2GParser.PERCEPT_OP);
	}
}
