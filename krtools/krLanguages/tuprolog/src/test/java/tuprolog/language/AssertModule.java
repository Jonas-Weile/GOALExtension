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

/**
 * Test if inserting into module works ok. #3676 This also demonstrates that our
 * workaround works. The problem stems from JPL stripping off our module
 * references from our query The workaround is to not give JPL a term with ':'
 * as the top level functor.
 */
public class AssertModule {
	/*
	 * @Test public void assertIntoModule() { Query insert = new jpl.Query(
	 * "assert(:('owner:main:sippingbeer', sippingbeer))");
	 * insert.allSolutions();
	 *
	 * Query check = new jpl.Query("'owner:main:sippingbeer':sippingbeer");
	 * Hashtable[] result = check.allSolutions(); assertEquals(1,
	 * result.length);
	 *
	 * Query predicatesq = new jpl.Query(
	 * " 'owner:main:sippingbeer':(current_predicate(_,Pred), not(predicate_property(Pred, imported_from(_))), not(predicate_property(Pred, built_in)), strip_module(Pred,Module,Head), clause(Head,Body,_))"
	 * ); Hashtable[] preds = predicatesq.allSolutions(); assertEquals(0,
	 * preds.length); // This should have returned 1 as demonstrated below with
	 * the // workaround. // This demonstrates that JPL is FAILING our query.
	 *
	 * Query predicatesq1 = new jpl.Query(
	 * "true, 'owner:main:sippingbeer':(current_predicate(_,Pred), not(predicate_property(Pred, imported_from(_))), not(predicate_property(Pred, built_in)), strip_module(Pred,Module,Head), clause(Head,Body,_))"
	 * ); Hashtable[] preds1 = predicatesq1.allSolutions(); assertEquals(1,
	 * preds1.length); }
	 */
}