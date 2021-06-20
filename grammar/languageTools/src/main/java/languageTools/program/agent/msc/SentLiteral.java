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
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.selector.Selector;

public class SentLiteral extends MentalLiteral {
	private final SentenceMood mood;

	public SentLiteral(boolean polarity, Selector selector, Query query, SentenceMood mood, Set<String> signatures,
			SourceInfo info) {
		super(polarity, selector, query, signatures, info);
		this.mood = mood;
	}

	public SentenceMood getMood() {
		return this.mood;
	}

	@Override
	public String getOperator() {
		switch (this.mood) {
		case INDICATIVE:
			return ModuleValidator.getTokenName(MOD2GParser.SENT_IND_OP);
		case INTERROGATIVE:
			return ModuleValidator.getTokenName(MOD2GParser.SENT_INT_OP);
		case IMPERATIVE:
			return ModuleValidator.getTokenName(MOD2GParser.SENT_IMP_OP);
		default:
			return ModuleValidator.getTokenName(MOD2GParser.SENT_OP);
		}
	}

	@Override
	public MentalLiteral applySubst(Substitution substitution) {
		return new SentLiteral(isPositive(), (getSelector() == null) ? null : getSelector().applySubst(substitution),
				(getFormula() == null) ? null : getFormula().applySubst(substitution), getMood(), getUsedSignatures(),
				getSourceInfo());
	}
}
