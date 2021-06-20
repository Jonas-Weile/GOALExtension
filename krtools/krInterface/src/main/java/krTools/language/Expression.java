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

package krTools.language;

import java.util.Set;

import krTools.parser.ParsedObject;

/**
 * An expression is any grammatically correct string of symbols of a knowledge
 * representation (KR) language. For example, an expression can be a constant
 * {@code a}, term {@code 1+2}, or formula or statement
 * {@code on(a,b), clear(table)}.
 *
 * <p>
 * The KR Interface has been designed to make minimal assumptions about the
 * language elements that are present in a KR language and does not make
 * assumptions about the syntax of expressions. The examples provided in the
 * previous paragraph have only been provided for purposes of clarification.
 * </p>
 *
 * <p>
 * The assumptions about expressions in the KR language that have been made are
 * the following:
 * <ul>
 * <li>each expression has a 'main operator': this may either be the expression
 * itself (e.g., in case of a constant or simple literal) or a functor (e.g.,
 * '+' or the logical operator 'not').</li>
 * <li>the language contains variables (though strictly speaking this is not
 * actually enforced; the methods {@link #isClosed}, {@link #isVar},
 * {@link #getFreeVar}, {@link #applySubst}, and {@link #mgu}, however, only
 * make sense if variables are present in the language.
 * </p>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Important</b>: {@link Expression} and other language elements such as
 * {@link Var} may be used in {@link Set}s and should implement
 * {@link java.lang.Object#hashCode} and {@link java.lang.Object#equals}.
 * </p>
 */
public interface Expression extends ParsedObject {
	/**
	 * @return String of the form "{operator name}/{arity}" where arity is
	 *         number of arguments associated with the operator.
	 */
	String getSignature();

	/**
	 * @return true if expression is a variable.
	 */
	boolean isVar();

	/**
	 * @return true if expression does not contain any free variables.
	 */
	boolean isClosed();

	/**
	 * @return all free variables in Expression.
	 */
	Set<Var> getFreeVar();

	/**
	 * Creates a new term by applying a substitution to this. This instantiates
	 * free variables that are bound to a term in the substitution by that term
	 * (or, only renames in case the substitution binds a variable to another
	 * one).
	 *
	 * @param substitution
	 *            A map that binds terms to variables.
	 * @return An instantiated expression.
	 */
	Expression applySubst(Substitution substitution);

	/**
	 * @return most general substitution (mgu) that makes this
	 *         {@link Expression} equal to the parameter expression expression
	 *         (unifies), or null if unification is impossible.
	 */
	Substitution mgu(Expression expression);
}
