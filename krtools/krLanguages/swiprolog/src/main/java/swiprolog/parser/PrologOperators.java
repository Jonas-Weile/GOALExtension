/**
 * GOAL interpreter that facilitates developing and executing GOAL multi-agent
 * programs. Copyright (C) 2011 K.V. Hindriks, W. Pasman
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

package swiprolog.parser;

import java.util.HashMap;
import java.util.Map;

import org.jpl7.JPL;

import krTools.database.Database;

/**
 * A list of built-in operators of Prolog that are supported; these operators
 * should be treated as reserved keywords. The list also contains information
 * about priorities and the arity of the operators. Note that this list does
 * <b>not</b> include assert, retract, nor throw; these are explicitly excluded
 * because the KR interface itself should be used to update content in a Prolog
 * database (using, e.g.,
 * {@link Database#delete(krTools.language.DatabaseFormula)}).
 */
public final class PrologOperators {

	/**
	 * Fixity is a part of a prolog operator specification. The specifier is a
	 * mnemonic that defines the class (prefix, infix or postfix) and the
	 * associativity (right-, left- or non-) of the operator. This has consequences
	 * for bracketing requirements. See the ISO/IEC 12331 manual for more details.
	 */
	public enum Fixity {
		NOT_OPERATOR, XFX, FX, XFY, YFX, FY, XF
	}

	/**
	 * The built-in ops. Users are NOT allowed to overwrite builtin ops. All
	 * operators that are built-in but not defined here are not known as built-in,
	 * and thus not useable (i.e., will give not defined error in parsing).
	 */
	public static final Map<String, Integer> OP_PRIOS;

	public static final Map<String, PrologOperators.Fixity> OPERATOR_SPECS;

	static {
		OP_PRIOS = new HashMap<>();
		OPERATOR_SPECS = new HashMap<>();
		OP_PRIOS.put(":/2", 50);
		OPERATOR_SPECS.put(":/1", Fixity.XFX);
		OP_PRIOS.put("@/1", 100);
		OPERATOR_SPECS.put("@/1", Fixity.XFX);
		OP_PRIOS.put("-/1", 200);
		OPERATOR_SPECS.put("-/1", Fixity.FY);
		OP_PRIOS.put("\\/1", 200);
		OPERATOR_SPECS.put("\\/1", Fixity.FY);
		OP_PRIOS.put("**/2", 200);
		OPERATOR_SPECS.put("**/2", Fixity.XFX);
		OP_PRIOS.put("^/2", 200);
		OPERATOR_SPECS.put("^/2", Fixity.XFY);
		OP_PRIOS.put("*/2", 400);
		OPERATOR_SPECS.put("*/2", Fixity.YFX);
		OP_PRIOS.put("//2", 400);
		OPERATOR_SPECS.put("//2", Fixity.YFX);
		OP_PRIOS.put("///2", 400);
		OPERATOR_SPECS.put("///2", Fixity.YFX);
		OP_PRIOS.put("rem/2", 400);
		OPERATOR_SPECS.put("rem/2", Fixity.YFX);
		OP_PRIOS.put("rdiv/2", 400);
		OPERATOR_SPECS.put("rdiv/2", Fixity.YFX);
		OP_PRIOS.put("gcd/2", 400); // new in SWI8
		OPERATOR_SPECS.put("gcd/2", Fixity.YFX);
		OP_PRIOS.put("lcm/2", 400); // new in SWI8
		OPERATOR_SPECS.put("lcm/2", Fixity.YFX);
		OP_PRIOS.put("mod/2", 400);
		OPERATOR_SPECS.put("mod/2", Fixity.YFX);
		OP_PRIOS.put("<</2", 400);
		OPERATOR_SPECS.put("<</2", Fixity.YFX);
		OP_PRIOS.put(">>/2", 400);
		OPERATOR_SPECS.put(">>/2", Fixity.YFX);
		OP_PRIOS.put("+/2", 500);
		OPERATOR_SPECS.put("+/2", Fixity.YFX);
		OP_PRIOS.put("-/2", 500);
		OPERATOR_SPECS.put("-/2", Fixity.YFX);
		OP_PRIOS.put("/\\/2", 500);
		OPERATOR_SPECS.put("/\\/2", Fixity.YFX);
		OP_PRIOS.put("\\//2", 500);
		OPERATOR_SPECS.put("\\//2", Fixity.YFX);
		OP_PRIOS.put("xor/2", 500);
		OPERATOR_SPECS.put("xor/2", Fixity.YFX);

		OP_PRIOS.put("\\=/2", 700);
		OPERATOR_SPECS.put("\\=/2", Fixity.XFX);
		OP_PRIOS.put("is/2", 700);
		OPERATOR_SPECS.put("is/2", Fixity.XFX);
		OP_PRIOS.put("=:=/2", 700);
		OPERATOR_SPECS.put("=:=/2", Fixity.XFX);
		OP_PRIOS.put("=\\=/2", 700);
		OPERATOR_SPECS.put("=\\=/2", Fixity.XFX);
		OP_PRIOS.put("</2", 700);
		OPERATOR_SPECS.put("</2", Fixity.XFX);
		OP_PRIOS.put("=</2", 700);
		OPERATOR_SPECS.put("=</2", Fixity.XFX);
		OP_PRIOS.put(">/2", 700);
		OPERATOR_SPECS.put(">/2", Fixity.XFX);
		OP_PRIOS.put(">=/2", 700);
		OPERATOR_SPECS.put(">=/2", Fixity.XFX);
		OP_PRIOS.put("=../2", 700);
		OPERATOR_SPECS.put("=../2", Fixity.XFX);
		OP_PRIOS.put("==/2", 700);
		OPERATOR_SPECS.put("==/2", Fixity.XFX);
		OP_PRIOS.put("\\==/2", 700);
		OPERATOR_SPECS.put("\\==/2", Fixity.XFX);
		OP_PRIOS.put("@</2", 700);
		OPERATOR_SPECS.put("@</2", Fixity.XFX);
		OP_PRIOS.put("@=</2", 700);
		OPERATOR_SPECS.put("@=</2", Fixity.XFX);
		OP_PRIOS.put("=@=/2", 700);
		OPERATOR_SPECS.put("=@=/2", Fixity.XFX);
		OP_PRIOS.put("@>/2", 700);
		OPERATOR_SPECS.put("@>/2", Fixity.XFX);
		OP_PRIOS.put("@>=/2", 700);
		OPERATOR_SPECS.put("@>=/2", Fixity.XFX);
		OP_PRIOS.put("=/2", 700);
		OPERATOR_SPECS.put("=/2", Fixity.XFX);
		OP_PRIOS.put("=\\=/2", 700);
		OPERATOR_SPECS.put("=\\=/2", Fixity.XFX);

		OP_PRIOS.put("\\+/1", 900);
		OPERATOR_SPECS.put("\\+/1", Fixity.FY);
		OP_PRIOS.put(",/2", 1000);
		OPERATOR_SPECS.put(",/2", Fixity.XFY);

		OP_PRIOS.put("*->/2", 1050);
		OPERATOR_SPECS.put("*->/2", Fixity.XFY);
		OP_PRIOS.put("->/2", 1050);
		OPERATOR_SPECS.put("->/2", Fixity.XFY);

		OP_PRIOS.put(";/2", 1100);
		OPERATOR_SPECS.put(";/2", Fixity.XFY);

		OP_PRIOS.put("|/2", 1105);
		OPERATOR_SPECS.put("|/2", Fixity.XFY);

		OP_PRIOS.put(":-/1", 1200);
		OPERATOR_SPECS.put(":-/1", Fixity.FX);
		OP_PRIOS.put("?-/1", 1200);
		OPERATOR_SPECS.put("?-/1", Fixity.FX);
		OP_PRIOS.put(":-/2", 1200);
		OPERATOR_SPECS.put(":-/2", Fixity.XFX);
		OP_PRIOS.put("-->/2", 1200);
		OPERATOR_SPECS.put("-->/2", Fixity.XFX);

		// special case, to work around KR-133.
		OP_PRIOS.put("=/1", 0);

		// true, cut, fail, conjunction, ;_, not, and var operators.
		OP_PRIOS.put("true/0", 0);
		OP_PRIOS.put("false/0", 0);
		OP_PRIOS.put("!/0", 0);
		OP_PRIOS.put("fail/0", 0);
		OP_PRIOS.put("not/1", 0);
		// repeat/0 is not supported (don't want infinity in an agent)

		// operators used within context of operators that quantify over
		// variables.
		OP_PRIOS.put("max/1", 0);
		OP_PRIOS.put("min/1", 0);
		OP_PRIOS.put("count/1", 0);
		OP_PRIOS.put("sum/1", 0);
		OP_PRIOS.put("set/1", 0);
		OP_PRIOS.put("bag/1", 0);

		// SWI 4.3 - we do not need Prolog file loading predicates
		// SWI 4.4 - we do not need editor interface predicates

		// SWI 4.5 - Verify Type of a Term
		OP_PRIOS.put("var/1", 0);
		OP_PRIOS.put("nonvar/1", 0);
		OP_PRIOS.put("integer/1", 0);
		OP_PRIOS.put("float/1", 0);
		OP_PRIOS.put("rational/1", 0);
		OP_PRIOS.put("rational/3", 0);
		OP_PRIOS.put("number/1", 0);
		OP_PRIOS.put("atom/1", 0);
		OP_PRIOS.put("blob/2", 0); // new in SWI7
		OP_PRIOS.put("string/1", 0);
		OP_PRIOS.put("atomic/1", 0);
		OP_PRIOS.put("compound/1", 0);
		OP_PRIOS.put("callable/1", 0); // new in SWI7
		OP_PRIOS.put("ground/1", 0);
		OP_PRIOS.put("cyclic_term/1", 0);
		OP_PRIOS.put("acyclic_term/1", 0);

		// SWI 4.6 - Comparison and Unification of Terms
		OP_PRIOS.put("compare/3", 0);
		OP_PRIOS.put("unify_with_occurs_check/2", 0);
		OP_PRIOS.put("subsumes_term/2", 0); // new in SWI7
		OP_PRIOS.put("term_subsumer/3", 0); // new in SWI7
		OP_PRIOS.put("unifiable/2", 0); // new in SWI7

		// SWI 4.7 - Control Predicates
		// already defined at the top

		// SWI 4.8 - Meta-Call Predicates
		// call has variable num of args but up to 8 seems supported
		OP_PRIOS.put("call/1", 0);
		OP_PRIOS.put("call/2", 0);
		OP_PRIOS.put("call/3", 0);
		OP_PRIOS.put("call/4", 0);
		OP_PRIOS.put("call/5", 0);
		OP_PRIOS.put("call/6", 0);
		OP_PRIOS.put("call/7", 0);
		OP_PRIOS.put("call/8", 0);
		OP_PRIOS.put("apply/2", 0); // new in SWI7
		// not/1 is already defined
		OP_PRIOS.put("once/1", 0);
		OP_PRIOS.put("ignore/1", 0);
		OP_PRIOS.put("call_with_depth_limit/3", 0);
		OP_PRIOS.put("call_with_inference_limit/3", 0); // new in SWI7
		OP_PRIOS.put("setup_call_cleanup/3", 0);
		OP_PRIOS.put("setup_call_catcher_cleanup/4", 0);
		OP_PRIOS.put("call_cleanup/2", 0);
		OP_PRIOS.put("call_cleanup/3", 0);

		// SWI 4.9 - we do not support delimited continuations (coroutines)
		// SWI 4.10 - we do not support exception handling

		// SWI 4.11: Printing messages
		OP_PRIOS.put("print_message/2", 0);
		OP_PRIOS.put("print_message_lines/3", 0);
		OP_PRIOS.put("message_to_string/2", 0);
		// message hooks use modules so are not supported
		// version info is not needed either

		// SWI 4.12 - we do not support handling signals

		// SWI 4.13 - DCG Grammar rules
		OP_PRIOS.put("phrase/2", 0);
		OP_PRIOS.put("phrase/3", 0);
		OP_PRIOS.put("call_dcg/3", 0); // new in SWI7

		// SWI 4.14 - database is purposely restricted (handled by GOAL)
		// SWI 4.15 - idem for predicate properties
		// (dynamic/1 is the only directive that is supported; handled in
		// SemanticTools)

		// SWI 4.16 - program examination
		OP_PRIOS.put("current_atom/1", 0);
		OP_PRIOS.put("current_blob/2", 0);
		OP_PRIOS.put("current_functor/2", 0);
		OP_PRIOS.put("current_flag/1", 0);
		OP_PRIOS.put("current_key/1", 0);
		OP_PRIOS.put("current_predicate/1", 0);
		OP_PRIOS.put("current_predicate/2", 0);
		OP_PRIOS.put("predicate_property/2", 0);
		OP_PRIOS.put("dwim_predicate/2", 0);
		OP_PRIOS.put("clause/2", 0);
		OP_PRIOS.put("clause/3", 0);
		OP_PRIOS.put("nth_clause/3", 0);
		OP_PRIOS.put("clause_property/2", 0);

		// SWI 4.17 - Input and output
		OP_PRIOS.put("open/4", 0);
		OP_PRIOS.put("open/3", 0);
		OP_PRIOS.put("open_null_stream/1", 0);
		OP_PRIOS.put("close/1", 0);
		OP_PRIOS.put("close/2", 0);
		OP_PRIOS.put("stream_property/2", 0);
		OP_PRIOS.put("current_stream/3", 0);
		OP_PRIOS.put("is_stream/1", 0);
		OP_PRIOS.put("stream_pair/3", 0); // new in SWI7
		OP_PRIOS.put("set_stream_position/2", 0);
		OP_PRIOS.put("stream_position_data/3", 0);
		OP_PRIOS.put("seek/4", 0);
		OP_PRIOS.put("set_stream/2", 0);
		OP_PRIOS.put("set_prolog_IO/3", 0);
		OP_PRIOS.put("set_system_IO/3", 0); // new in SWI8
		OP_PRIOS.put("see/1", 0); // new in SWI7
		OP_PRIOS.put("tell/1", 0); // new in SWI7
		OP_PRIOS.put("append/1", 0); // new in SWI7
		OP_PRIOS.put("seeing/1", 0); // new in SWI7
		OP_PRIOS.put("telling/1", 0); // new in SWI7
		OP_PRIOS.put("seen/0", 0); // new in SWI7
		OP_PRIOS.put("told/0", 0); // new in SWI7
		OP_PRIOS.put("set_input/1", 0); // new in SWI7
		OP_PRIOS.put("set_output/1", 0); // new in SWI7
		OP_PRIOS.put("current_input/1", 0); // new in SWI7
		OP_PRIOS.put("current_output/1", 0); // new in SWI7
		OP_PRIOS.put("register_iri_scheme/3", 0); // new in SWI8
		OP_PRIOS.put("with_output_to/2", 0);
		// did not include fast binary I/O

		// SWI 4.18: Status of streams
		OP_PRIOS.put("wait_for_input/3", 0);
		OP_PRIOS.put("byte_count/2", 0);
		OP_PRIOS.put("character_count/2", 0);
		OP_PRIOS.put("line_count/2", 0);
		OP_PRIOS.put("line_position/2", 0);

		// SWI 4.19 - Primitive character I/O
		OP_PRIOS.put("nl/0", 0);
		OP_PRIOS.put("nl/1", 0);
		OP_PRIOS.put("put_byte/1", 0);
		OP_PRIOS.put("put_byte/2", 0);
		OP_PRIOS.put("put_char/1", 0);
		OP_PRIOS.put("put_char/2", 0);
		OP_PRIOS.put("put_code/1", 0);
		OP_PRIOS.put("put_code/2", 0);
		OP_PRIOS.put("tab/1", 0);
		OP_PRIOS.put("tab/2", 0);
		OP_PRIOS.put("flush_output/0", 0);
		OP_PRIOS.put("flush_output/1", 0);
		// ttyflush not supported
		OP_PRIOS.put("get_byte/1", 0);
		OP_PRIOS.put("get_byte/2", 0);
		OP_PRIOS.put("get_code/1", 0);
		OP_PRIOS.put("get_code/2", 0);
		OP_PRIOS.put("get_char/1", 0);
		OP_PRIOS.put("get_char/2", 0);
		OP_PRIOS.put("peek_byte/1", 0);
		OP_PRIOS.put("peek_byte/2", 0);
		OP_PRIOS.put("peek_code/1", 0);
		OP_PRIOS.put("peek_code/2", 0);
		OP_PRIOS.put("peek_char/1", 0);
		OP_PRIOS.put("peek_char/2", 0);
		OP_PRIOS.put("peek_string/3", 0); // new in SWI7
		OP_PRIOS.put("skip/1", 0);
		OP_PRIOS.put("skip/2", 0);
		OP_PRIOS.put("get_single_char/1", 0);
		OP_PRIOS.put("at_end_of_stream/0", 0);
		OP_PRIOS.put("at_end_of_stream/1", 0);
		OP_PRIOS.put("set_end_of_stream/1", 0); // new in SWI7
		OP_PRIOS.put("copy_stream_data/3", 0);
		OP_PRIOS.put("copy_stream_data/2", 0);
		OP_PRIOS.put("fill_buffer/1", 0); // new in SWI7
		OP_PRIOS.put("read_pending_codes/3", 0); // new in SWI7
		OP_PRIOS.put("read_pending_chars/3", 0); // new in SWI7

		// SWI 4.20 - Term reading and writing
		OP_PRIOS.put("write_term/2", 0);
		OP_PRIOS.put("write_term/3", 0);
		OP_PRIOS.put("write_length/3", 0); // new in SWI7
		OP_PRIOS.put("write_canonical/1", 0);
		OP_PRIOS.put("write_canonical/2", 0);
		OP_PRIOS.put("write/1", 0);
		OP_PRIOS.put("write/2", 0);
		OP_PRIOS.put("writeq/1", 0);
		OP_PRIOS.put("writeq/2", 0);
		OP_PRIOS.put("writeln/1", 0);
		OP_PRIOS.put("writeln/2", 0); // new in SWI7
		OP_PRIOS.put("print/1", 0);
		OP_PRIOS.put("print/2", 0);
		OP_PRIOS.put("portray/1", 0);
		OP_PRIOS.put("read/1", 0);
		OP_PRIOS.put("read/2", 0);
		OP_PRIOS.put("read_clause/2", 0);
		OP_PRIOS.put("read_term/2", 0);
		OP_PRIOS.put("read_term/3", 0);
		OP_PRIOS.put("read_term_from_atom/3", 0);
		// read_history and prompts not included (no user input possible)

		// SWI 4.21 - Analysing and Constructing Terms (new in SWI7)
		OP_PRIOS.put("functor/3", 0);
		OP_PRIOS.put("arg/3", 0);
		OP_PRIOS.put("compound_name_arity/3", 0);
		OP_PRIOS.put("compound_name_arguments/3", 0);
		OP_PRIOS.put("numbervars/3", 0);
		OP_PRIOS.put("numbervars/4", 0);
		OP_PRIOS.put("var_number/2", 0);
		OP_PRIOS.put("term_variables/2", 0);
		OP_PRIOS.put("nonground/2", 0);
		OP_PRIOS.put("term_variables/3", 0);
		OP_PRIOS.put("term_singletons/2", 0);
		OP_PRIOS.put("is_most_general_term/1", 0); // new in SWI8
		OP_PRIOS.put("copy_term/2", 0);
		// non-logical operators not supported

		// SWI 4.22 - Analysing and Constructing Terms
		OP_PRIOS.put("atom_codes/2", 0);
		OP_PRIOS.put("atom_chars/2", 0);
		OP_PRIOS.put("char_code/2", 0);
		OP_PRIOS.put("number_chars/2", 0);
		OP_PRIOS.put("number_codes/2", 0);
		OP_PRIOS.put("atom_number/2", 0); // new in SWI7
		OP_PRIOS.put("name/2", 0);
		OP_PRIOS.put("term_to_atom/2", 0); // new in SWI7
		OP_PRIOS.put("atom_concat/3", 0);
		OP_PRIOS.put("atomic_concat/3", 0); // new in SWI7
		OP_PRIOS.put("atomic_list_concat/2", 0); // new in SWI7
		OP_PRIOS.put("atomic_list_concat/3", 0); // new in SWI7
		OP_PRIOS.put("atomic_concat/3", 0); // new in SWI7
		OP_PRIOS.put("atom_length/2", 0); // new in SWI7
		OP_PRIOS.put("sub_atom/5", 0); // new in SWI7
		OP_PRIOS.put("sub_atom_icasechk/3", 0); // new in SWI7

		// SWI 4.23 - localization support not included

		// SWI 4.24 - character properties
		OP_PRIOS.put("char_type/2", 0);
		OP_PRIOS.put("code_type/2", 0);
		OP_PRIOS.put("downcase_atom/2", 0);
		OP_PRIOS.put("upcase_atom/2", 0);
		OP_PRIOS.put("normalize_space/2", 0); // new in SWI8
		// language-specific predicates not supported

		// SWI 4.25 - operators are defined at the top
		// SWI 4.26 - character conversion is not supported

		// SWI 4.27 - Arithmetic
		// Note that the infix operators are above.
		OP_PRIOS.put("between/3", 0);
		OP_PRIOS.put("succ/2", 0);
		OP_PRIOS.put("plus/3", 0);
		OP_PRIOS.put("divmod/3", 0); // new in SWI7
		OP_PRIOS.put("nth_integer_root_and_remainder/4", 0); // new in SWI7
		OP_PRIOS.put("float_class/2", 0); // new in SWI8
		OP_PRIOS.put("float_parts/4", 0); // new in SWI8
		OP_PRIOS.put("bounded_number/3", 0); // new in SWI8
		OP_PRIOS.put("div/2", 0); // new in SWI8
		OP_PRIOS.put("abs/1", 0);
		OP_PRIOS.put("sign/1", 0);
		OP_PRIOS.put("copysign/2", 0); // new in SWI7
		OP_PRIOS.put("nexttoward/2", 0); // new in SWI8
		OP_PRIOS.put("roundtoward/2", 0); // new in SWI8
		OP_PRIOS.put("max/2", 0);
		OP_PRIOS.put("min/2", 0);
		OP_PRIOS.put("random/1", 0);
		OP_PRIOS.put("random_float/0", 0);
		OP_PRIOS.put("round/1", 0);
		OP_PRIOS.put("integer/1", 0);
		OP_PRIOS.put("float/1", 0);
		OP_PRIOS.put("rational/1", 0);
		OP_PRIOS.put("rationalize/1", 0);
		OP_PRIOS.put("numerator/1", 0); // new in SWI8
		OP_PRIOS.put("denominator/1", 0); // new in SWI8
		OP_PRIOS.put("float_fractional_part/1", 0);
		OP_PRIOS.put("float_integer_part/1", 0);
		OP_PRIOS.put("truncate/1", 0);
		OP_PRIOS.put("floor/1", 0);
		OP_PRIOS.put("ceiling/1", 0);
		OP_PRIOS.put("ceil/1", 0);
		OP_PRIOS.put("sqrt/1", 0);
		OP_PRIOS.put("sin/1", 0);
		OP_PRIOS.put("cos/1", 0);
		OP_PRIOS.put("tan/1", 0);
		OP_PRIOS.put("asin/1", 0);
		OP_PRIOS.put("acos/1", 0);
		OP_PRIOS.put("atan/1", 0);
		OP_PRIOS.put("atan/2", 0);
		OP_PRIOS.put("atan2/2", 0); // new in SWI7
		OP_PRIOS.put("sinh/1", 0); // new in SWI7
		OP_PRIOS.put("cosh/1", 0); // new in SWI7
		OP_PRIOS.put("tanh/1", 0); // new in SWI7
		OP_PRIOS.put("asinh/1", 0); // new in SWI7
		OP_PRIOS.put("acosh/1", 0); // new in SWI7
		OP_PRIOS.put("atanh/1", 0); // new in SWI7
		OP_PRIOS.put("log/1", 0);
		OP_PRIOS.put("log10/1", 0);
		OP_PRIOS.put("exp/1", 0);
		OP_PRIOS.put("powm/3", 0);
		OP_PRIOS.put("lgamma/1", 0); // new in SWI7
		OP_PRIOS.put("erf/1", 0); // new in SWI7
		OP_PRIOS.put("erfc/1", 0); // new in SWI7
		OP_PRIOS.put("pi/0", 0);
		OP_PRIOS.put("e/0", 0);
		OP_PRIOS.put("epsilon/0", 0);
		OP_PRIOS.put("inf/0", 0); // new in SWI7
		OP_PRIOS.put("nan/0", 0); // new in SWI7
		OP_PRIOS.put("cputime/0", 0);
		OP_PRIOS.put("eval/1", 0);
		OP_PRIOS.put("msb/1", 0);
		OP_PRIOS.put("lsb/1", 0);
		OP_PRIOS.put("popcount/1", 0);
		OP_PRIOS.put("getbit/2", 0); // new in SWI7

		// SWI 4.28 - Misc arithmetic support predicates
		OP_PRIOS.put("set_random/1", 0);
		OP_PRIOS.put("random_property/1", 0); // new in SWI7
		OP_PRIOS.put("current_arithmetic_function/1", 0); // new in SWI7

		// SWI 4.29 - Built-in list operations
		OP_PRIOS.put("[|]/2", 0);
		OP_PRIOS.put("is_list/1", 0);
		OP_PRIOS.put("memberchk/2", 0);
		OP_PRIOS.put("length/2", 0);
		OP_PRIOS.put("sort/2", 0);
		OP_PRIOS.put("sort/4", 0); // new in SWI7
		OP_PRIOS.put("msort/2", 0);
		OP_PRIOS.put("keysort/2", 0);
		OP_PRIOS.put("predsort/3", 0);

		// SWI 4.30 & 4.31 - Finding all Solutions to a Goal
		OP_PRIOS.put("findall/3", 0);
		OP_PRIOS.put("findall/4", 0); // new in SWI7
		OP_PRIOS.put("findnsols/4", 0); // new in SWI7
		OP_PRIOS.put("findnsols/5", 0); // new in SWI7
		OP_PRIOS.put("bagof/3", 0);
		OP_PRIOS.put("setof/3", 0);
		OP_PRIOS.put("forall/2", 0);

		// SWI 4.32 - Formatted write
		OP_PRIOS.put("format/1", 0);
		OP_PRIOS.put("format/2", 0);
		OP_PRIOS.put("format/3", 0);
		OP_PRIOS.put("format_predicate/2", 0);
		OP_PRIOS.put("current_format_predicate/2", 0);

		// SWI 4.33 - we do not support global variables
		// SWI 4.34 - terminal control is not needed

		// SWI 4.35.2 - Dealing with time and date
		// (other OS predicates are not supported)
		OP_PRIOS.put("get_time/1", 0);
		OP_PRIOS.put("stamp_date_time/3", 0);
		OP_PRIOS.put("date_time_stamp/2", 0);
		OP_PRIOS.put("date_time_value/3", 0);
		OP_PRIOS.put("format_time/3", 0);
		OP_PRIOS.put("format_time/4", 0);
		OP_PRIOS.put("parse_time/2", 0);
		OP_PRIOS.put("parse_time/3", 0); // new in SWI7
		OP_PRIOS.put("day_of_the_week/2", 0);

		// SWI 4.36 - File System Interaction
		OP_PRIOS.put("access_file/2", 0);
		OP_PRIOS.put("exists_file/1", 0);
		OP_PRIOS.put("file_directory_name/2", 0);
		OP_PRIOS.put("file_base_name/2", 0);
		OP_PRIOS.put("same_file/2", 0);
		OP_PRIOS.put("exists_directory/1", 0);
		OP_PRIOS.put("delete_file/1", 0);
		OP_PRIOS.put("rename_file/2", 0);
		OP_PRIOS.put("size_file/2", 0);
		OP_PRIOS.put("time_file/2", 0);
		OP_PRIOS.put("absolute_file_name/2", 0);
		OP_PRIOS.put("absolute_file_name/3", 0);
		OP_PRIOS.put("is_absolute_file_name/1", 0);
		OP_PRIOS.put("file_name_extension/3", 0);
		OP_PRIOS.put("directory_files/2", 0); // new in SWI7
		OP_PRIOS.put("expand_file_name/2", 0);
		OP_PRIOS.put("prolog_to_os_filename/2", 0);
		OP_PRIOS.put("read_link/3", 0);
		OP_PRIOS.put("tmp_file_stream/3", 0); // new in SWI7
		OP_PRIOS.put("make_directory/1", 0);
		OP_PRIOS.put("delete_directory/1", 0);
		OP_PRIOS.put("working_directory/2", 0);
		OP_PRIOS.put("chdir/1", 0);

		// SWI 4.37 - top-level manipulation not supported
		// SWI 4.38 - user interaction protocol not supported
		// SWI 4.39 - debugging predicates are not supported
		// SWI 4.40 - runtime statistics not supported (at prolog level)
		// SWI 4.41 - profiling not supported (at prolog level)
		// SWI 4.42 - manual memory management not supported
		// SWI 4.43 - Windows DDE interface not supported

		// SWI 4.44 - Miscellaneous (new in SWI7)
		OP_PRIOS.put("dwim_match/2", 0);
		OP_PRIOS.put("dwim_match/3", 0);
		OP_PRIOS.put("wildcard_match/2", 0);
		OP_PRIOS.put("wildcard_match/3", 0); // new in SWI8
		// sleep not supported at prolog level

		// SWI 5.2.1 - Predicates that operate on strings (new in SWI7)
		OP_PRIOS.put("atom_string/2", 0);
		OP_PRIOS.put("number_string/2", 0);
		OP_PRIOS.put("term_string/2", 0);
		OP_PRIOS.put("term_string/3", 0);
		OP_PRIOS.put("string_chars/2", 0);
		OP_PRIOS.put("string_codes/2", 0);
		OP_PRIOS.put("text_to_string/2", 0);
		OP_PRIOS.put("string_length/2", 0);
		OP_PRIOS.put("string_code/3", 0);
		OP_PRIOS.put("get_string_code/3", 0);
		OP_PRIOS.put("string_concat/3", 0);
		OP_PRIOS.put("split_string/4", 0);
		OP_PRIOS.put("sub_string/5", 0);
		OP_PRIOS.put("atomics_to_string/2", 0);
		OP_PRIOS.put("atomics_to_string/3", 0);
		OP_PRIOS.put("string_upper/2", 0);
		OP_PRIOS.put("string_lower/2", 0);
		OP_PRIOS.put("read_string/3", 0);
		OP_PRIOS.put("read_string/5", 0);
		OP_PRIOS.put("open_string/2", 0);

		// SWI 5.4 - Dicts not supported at the moment

		// SWI A.1 - Aggregation operators
		OP_PRIOS.put("aggregate/3", 0);
		OP_PRIOS.put("aggregate/4", 0);
		OP_PRIOS.put("aggregate_all/3", 0);
		OP_PRIOS.put("aggregate_all/4", 0);
		OP_PRIOS.put("foreach/2", 0); // new in SWI7
		OP_PRIOS.put("free_variables/4", 0); // new in SWI7

		// SWI A.2 - ANSI consoles not supported

		// SWI A.3 - Apply predicates on a list
		OP_PRIOS.put("include/3", 0);
		OP_PRIOS.put("exclude/3", 0);
		OP_PRIOS.put("partition/4", 0);
		OP_PRIOS.put("partition/5", 0); // new in SWI7
		OP_PRIOS.put("maplist/2", 0);
		OP_PRIOS.put("maplist/3", 0);
		OP_PRIOS.put("maplist/4", 0);
		OP_PRIOS.put("maplist/5", 0);
		OP_PRIOS.put("convlist/3", 0); // new in SWI7
		OP_PRIOS.put("foldl/4", 0); // new in SWI7
		OP_PRIOS.put("foldl/5", 0); // new in SWI7
		OP_PRIOS.put("foldl/6", 0); // new in SWI7
		OP_PRIOS.put("foldl/7", 0); // new in SWI7
		OP_PRIOS.put("scanl/4", 0); // new in SWI7
		OP_PRIOS.put("scanl/5", 0); // new in SWI7
		OP_PRIOS.put("scanl/6", 0); // new in SWI7
		OP_PRIOS.put("scanl/7", 0); // new in SWI7

		// SWI A.4 - Association lists not supported at the moment
		// SWI A.5 - Event notifications (for GUIs) not supported
		// SWI A.6 - Not supported as better native predicates are available
		// SWI A.7 - Consistency checking not supported
		// SWI A.8/A.9/A.10 - Constraint programming not supported

		// SWI A.11 - CSV processing
		OP_PRIOS.put("csv_read_file/2", 0);
		OP_PRIOS.put("csv_read_file/3", 0);
		OP_PRIOS.put("csv_read_stream/3", 0);
		OP_PRIOS.put("csv/1", 0);
		OP_PRIOS.put("csv/2", 0);
		OP_PRIOS.put("csv_read_file_row/3", 0);
		OP_PRIOS.put("csv_read_row/3", 0);
		OP_PRIOS.put("csv_options/2", 0);
		OP_PRIOS.put("csv_write_file/2", 0);
		OP_PRIOS.put("csv_write_file/3", 0);
		OP_PRIOS.put("csv_write_stream/3", 0);

		// SWI A.12/A.13 - DCGs not supported
		// SWI A.14 - Not supported as it is only a replacement format/3
		// SWI A.15 - Dictionaries are not supported at the moment
		// SWI A.16 - Error generation not supported

		// SWI A.17 - Unique identifiers (support added per SWI8)
		OP_PRIOS.put("gensym/2", 0);
		OP_PRIOS.put("reset_gensym/1", 0);
		OP_PRIOS.put("reset_gensym/0", 0);

		// SWI A.18 - Intercepts and signals not supported

		// SWI A.19 - External streams (support added per SWI8)
		OP_PRIOS.put("open_any/5", 0);
		OP_PRIOS.put("close_any/1", 0);

		// SWI A.20: Pretty printing (support added per SWI8)
		OP_PRIOS.put("portray_clause/1", 0);
		OP_PRIOS.put("portray_clause/2", 0);
		OP_PRIOS.put("portray_clause/3", 0);

		// SWI A.21: List Manipulation
		OP_PRIOS.put("member/2", 0);
		OP_PRIOS.put("append/3", 0);
		OP_PRIOS.put("append/2", 0);
		OP_PRIOS.put("prefix/2", 0);
		OP_PRIOS.put("select/3", 0);
		OP_PRIOS.put("selectchk/3", 0);
		OP_PRIOS.put("select/4", 0);
		OP_PRIOS.put("selectchk/4", 0);
		OP_PRIOS.put("nextto/3", 0);
		OP_PRIOS.put("delete/3", 0);
		OP_PRIOS.put("nth0/3", 0);
		OP_PRIOS.put("nth1/3", 0);
		OP_PRIOS.put("nth0/4", 0);
		OP_PRIOS.put("nth1/4", 0);
		OP_PRIOS.put("last/2", 0);
		OP_PRIOS.put("proper_length/2", 0);
		OP_PRIOS.put("same_length/2", 0);
		OP_PRIOS.put("reverse/2", 0);
		OP_PRIOS.put("permutation/2", 0);
		OP_PRIOS.put("flatten/2", 0);
		OP_PRIOS.put("max_member/2", 0); // new in SWI7
		OP_PRIOS.put("min_member/2", 0); // new in SWI7
		OP_PRIOS.put("sum_list/2", 0);
		OP_PRIOS.put("max_list/2", 0);
		OP_PRIOS.put("min_list/2", 0);
		OP_PRIOS.put("numlist/3", 0);
		OP_PRIOS.put("is_set/1", 0);
		OP_PRIOS.put("list_to_set/2", 0);
		OP_PRIOS.put("intersection/3", 0);
		OP_PRIOS.put("union/3", 0);
		OP_PRIOS.put("subset/2", 0);
		OP_PRIOS.put("subtract/3", 0);

		// SWI A.22 - Script entry points not supported
		// SWI A.23 - Non-backtrackable sets not supported
		// SWI A.24 - Web-browser interaction not supported
		// SWI A.25 - Occurs library not supported (see terms library)
		// SWI A.26 - Option lists not supported
		// SWI A.27 - Command line parsing not supported

		// SWI A.28: Ordered sets
		OP_PRIOS.put("is_ordset/1", 0);
		OP_PRIOS.put("ord_empty/1", 0);
		OP_PRIOS.put("ord_seteq/2", 0);
		OP_PRIOS.put("list_to_ord_set/2", 0);
		OP_PRIOS.put("ord_intersect/2", 0);
		OP_PRIOS.put("ord_disjoint/2", 0);
		OP_PRIOS.put("ord_intersection/2", 0);
		OP_PRIOS.put("ord_intersection/3", 0);
		OP_PRIOS.put("ord_intersection/4", 0);
		OP_PRIOS.put("ord_add_element/3", 0);
		OP_PRIOS.put("ord_del_element/3", 0);
		OP_PRIOS.put("ord_selectchk/2", 0); // new in SWI7
		OP_PRIOS.put("ord_memberchk/2", 0);
		OP_PRIOS.put("ord_subset/2", 0);
		OP_PRIOS.put("ord_subtract/3", 0);
		OP_PRIOS.put("ord_union/2", 0);
		OP_PRIOS.put("ord_union/3", 0);
		OP_PRIOS.put("ord_union/4", 0);
		OP_PRIOS.put("ord_symdiff/3", 0);

		// SWI A.29: Key-value lists
		OP_PRIOS.put("pairs_keys_values/3", 0);
		OP_PRIOS.put("pairs_values/2", 0);
		OP_PRIOS.put("pairs_keys/2", 0);
		OP_PRIOS.put("group_pairs_by_key/2", 0);
		OP_PRIOS.put("transpose_pairs/2", 0);
		OP_PRIOS.put("map_list_to_pairs/3", 0);

		// SWI A.30 - Persistency not supported
		// SWI A.31 - 'Pure I/O' not supported
		// SWI A.32 - Predicate options not supported
		// SWI A.33 - Just In Time Indexing not supported
		// SWI A.34 - Package management not supported
		// SWI A.35 - Cross-reference data collection not supported
		// SWI A.36 - Quasi quotation not supported

		// SWI A.37: Random numbers
		OP_PRIOS.put("random/1", 0);
		OP_PRIOS.put("random_between/3", 0);
		OP_PRIOS.put("random/3", 0);
		OP_PRIOS.put("setrand/1", 0);
		OP_PRIOS.put("getrand/1", 0); // new in SWI7
		OP_PRIOS.put("maybe/0", 0); // new in SWI7
		OP_PRIOS.put("maybe/1", 0); // new in SWI7
		OP_PRIOS.put("maybe/2", 0); // new in SWI7
		OP_PRIOS.put("random_perm2/4", 0);
		OP_PRIOS.put("random_member/2", 0);
		OP_PRIOS.put("random_select/3", 0);
		OP_PRIOS.put("randset/3", 0);
		OP_PRIOS.put("randseq/3", 0);
		OP_PRIOS.put("random_permutation/2", 0);

		// SWI A.38 - Readutil not supported at the moment (TODO: seems useful?)
		// SWI A.39 - Named fields not supported
		// SWI A.40 - Windows registry manipulation not supported
		// SWI A.41 - Setting management not supported

		// SWI A.42 - String utilities (support added per SWI8)
		OP_PRIOS.put("string/4", 0);
		OP_PRIOS.put("interpolate_string/4", 0);
		OP_PRIOS.put("string_lines/2", 0);
		OP_PRIOS.put("dedent_lines/3", 0);
		OP_PRIOS.put("indent_lines/3", 0);
		OP_PRIOS.put("indent_lines/4", 0);

		// SWI A.43 - Linear programming not supported
		// SWI A.44 - Modifying solution sequences not supported
		// SWI A.45 - Tabled execution not supported
		// SWI A.46 - Threading not supported
		// SWI A.47 - Thread management not supported
		// SWI A.48 - Unweighted graphs not supported
		// SWI A.49 - URLs not supported
		// SWI A.50 - Numbered terms not supported
		// SWI A.51 - Lambda expressions not supported
	}

	// constructor is not allowed. Static utility method.
	private PrologOperators() {
	}

	/**
	 * Built-in operators are already defined in SWI Prolog, and one should not try
	 * to redefine these by inserting or deleting these (even though SWI Prolog
	 * allows redefining built-in operators, we do not consider this good practice).
	 * </p>
	 *
	 * @returns {@code true} if signature is built-in Prolog function <em>and</em>
	 *          is not a protected predicate, {@code false} otherwise.
	 */
	public static boolean prologBuiltin(final String signature) {
		return OP_PRIOS.containsKey(signature);
	}

	/**
	 * @param signature is funcname+"/"+#arguments, eg "member/2"
	 * @return spec given signature, or null if no such signature. specification is
	 *         fx, fy, xfy, xfx, etc.
	 */
	public static PrologOperators.Fixity getFixity(final String signature) {
		return OPERATOR_SPECS.get(signature);
	}

	/**
	 * @param signature is funcname+"/"+#arguments, e.g. "member/2"
	 * @returns priority of given signature, or {@code null} if no such signature.
	 */
	public static Integer getPriority(final String signature) {
		return OP_PRIOS.get(signature);
	}

	/**
	 * Checks if given label is L-atom (see L-atom, ISO p.132 in sec.A.3.1). which
	 * refers to a concrete atom (identifier), see clause 6.1.2b see also ISO Prolog
	 * definition of Name in section 6.4.2.
	 *
	 * @return {@code true} if label is predication.
	 */
	public static boolean is_L_atom(final String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}
		// See ISO section 6.1.2b and 6.4.2 "Names"
		if (name.equals(JPL.LIST_NIL.name()) || name.equals(JPL.LIST_PAIR)) {
			return true;
		}
		if (name.equals("{}")) {
			return true;
		}
		if (name.matches("[a-z][_a-zA-Z0-9]*")) {
			return true; // ISO 6.5.2
		}

		// graphic token char
		if (name.matches("[\\\\\\#\\$\\&\\*\\+\\-\\.\\/\\:\\<\\=\\>\\?\\@\\^\\~]*")) {
			return true;
		}

		// single quoted char.. is hard, quick and dirty fix.
		if (name.matches(
				"\'[a-zA-Z0-9\\\\\\#\\$\\&\\*\\+\\-\\.\\/\\:\\<\\=\\>\\?\\@\\^\\~\\!\\(\\)\\,\\;\\[\\]\\{\\}\\|\\%]+\'")) {
			return true;
		}
		// things '"hallo"' up to scary things like `''``/:-->$`
		if (name.matches(";")) {
			return true;
		}
		if (name.matches("!")) {
			return true;
		}
		return false;
	}

}
