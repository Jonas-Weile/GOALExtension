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

package cognitiveSwiprolog;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cognitiveKr.CognitiveKR;
import krTools.KRInterface;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologExpression;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologUpdate;
import swiprolog.language.PrologVar;
import swiprolog.parser.SourceInfoObject;
import swiprolog.validator.SemanticTools;

/**
 * Implementation of {@link CognitiveKR} for SWI Prolog.
 */
public final class CognitiveSwiProlog extends CognitiveKR {
	public CognitiveSwiProlog(KRInterface kri) {
		super(kri);
	}

	@Override
	protected SourceInfo getSourceInfo(File source) throws IOException {
		return new SourceInfoObject(source.getCanonicalPath(), 0, 0, 0, 0);
	}

	@Override
	public Update visitGoalAdopt(String krFragment, SourceInfo info) throws ParserException {
		Update adopt = super.visitGoalAdopt(krFragment, info);
		if (adopt != null && !adopt.getDeleteList().isEmpty()) {
			// TODO: message(properties) system
			throw new ParserException("cannot adopt negative facts", info);
		}
		return adopt;
	}

	@Override
	public Update visitGoalDrop(String krFragment, SourceInfo info) throws ParserException {
		Update drop = super.visitGoalAdopt(krFragment, info);
		if (drop != null && !drop.getDeleteList().isEmpty()) {
			// TODO: message(properties) system
			throw new ParserException("cannot drop negative facts", info);
		}
		return drop;
	}

	@Override
	public Set<String> getDefinedSignatures(DatabaseFormula formula) throws ParserException {
		return SemanticTools.getDefinedSignatures(((PrologDBFormula) formula).getCompound(), formula.getSourceInfo());
	}

	@Override
	public Set<String> getDeclaredSignatures(DatabaseFormula formula) throws ParserException {
		return SemanticTools.getDeclaredSignatures(((PrologDBFormula) formula).getCompound(), formula.getSourceInfo());
	}

	@Override
	public List<Var> getAllVariables(Expression expression) throws ParserException {
		return getVars(expression);
	}

	private static List<Var> getVars(Expression expression) {
		List<Var> vars = new LinkedList<>();
		if (expression instanceof PrologVar) {
			PrologVar var = (PrologVar) expression;
			if (!var.isAnonymous()) {
				vars.add(var);
			}
		} else if (expression instanceof PrologCompound) {
			PrologCompound compound = (PrologCompound) expression;
			for (Term argument : compound) {
				vars.addAll(getVars(argument));
			}
		} else if (expression instanceof PrologDBFormula) {
			vars.addAll(getVars(((PrologDBFormula) expression).getCompound()));
		} else if (expression instanceof PrologQuery) {
			vars.addAll(getVars(((PrologQuery) expression).getCompound()));
		} else if (expression instanceof PrologUpdate) {
			vars.addAll(getVars(((PrologUpdate) expression).toQuery()));
		}
		return vars;
	}

	@Override
	public Set<String> getUsedSignatures(Expression expression) {
		return SemanticTools.getUsedSignatures((PrologExpression) expression);
	}
}