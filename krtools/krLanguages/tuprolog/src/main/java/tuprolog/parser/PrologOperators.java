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

package tuprolog.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

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
	 * associativity (right-, left- or non-) of the operator. This has
	 * consequences for bracketing requirements. See the ISO/IEC 12331 manual
	 * for more details.
	 */
	public enum Fixity {
		NOT_OPERATOR, XFX, FX, XFY, YFX, FY, XF
	}

	/**
	 * The built-in ops. Users are NOT allowed to overwrite builtin ops. All
	 * operators that are built-in but not defined here are not known as
	 * built-in, and WE declare them dynamic if the user uses them, which should
	 * disable the built-in version.
	 */
	public static final Map<String, Integer> OP_PRIOS;

	public static final Hashtable<String, PrologOperators.Fixity> OPERATOR_SPECS;

	/**
	 * Protected operators are not supported.
	 */
	public static final Set<String> PROTECTED_OPS;

	static {
		OP_PRIOS = new HashMap<>();
		OPERATOR_SPECS = new Hashtable<>();
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

		// true, cut, fail, conjunction, ;_, not, and var operators.
		OP_PRIOS.put("true/0", 0);
		OP_PRIOS.put("false/0", 0);
		OP_PRIOS.put("!/0", 0);
		OP_PRIOS.put("fail/0", 0);
		OP_PRIOS.put("not/1", 0);
		// list operators.
		OP_PRIOS.put("is_list/1", 0);
		OP_PRIOS.put("is_set/1", 0);
		OP_PRIOS.put("list_to_set/2", 0);
		OP_PRIOS.put("length/2", 0);
		OP_PRIOS.put("last/2", 0);
		OP_PRIOS.put("sort/2", 0);
		OP_PRIOS.put("merge/3", 0);
		OP_PRIOS.put("delete/3", 0);
		OP_PRIOS.put("maplist/2", 0);
		OP_PRIOS.put("maplist/3", 0);
		OP_PRIOS.put("maplist/4", 0);
		OP_PRIOS.put("maplist/5", 0);
		OP_PRIOS.put("sublist/3", 0);
		OP_PRIOS.put("include/3", 0);
		OP_PRIOS.put("reverse/2", 0);
		OP_PRIOS.put("flatten/2", 0);
		OP_PRIOS.put("member/2", 0);

		OP_PRIOS.put("append/2", 0);
		OP_PRIOS.put("append/3", 0);
		OP_PRIOS.put("prefix/2", 0); // new in TU6.0.2
		OP_PRIOS.put("intersection/3", 0);
		OP_PRIOS.put("nth0/3", 0);
		OP_PRIOS.put("nth1/3", 0);
		OP_PRIOS.put("nth0/4", 0);
		OP_PRIOS.put("nth1/4", 0);
		OP_PRIOS.put("proper_length/2", 0); // new in TU6.0.2
		OP_PRIOS.put("max_list/2", 0);
		OP_PRIOS.put("min_list/2", 0);
		OP_PRIOS.put("nextto/3", 0);
		OP_PRIOS.put("numlist/3", 0);
		OP_PRIOS.put("permutation/2", 0);
		OP_PRIOS.put("select/3", 0);
		OP_PRIOS.put("select/4", 0); // new in TU6.0.2
		OP_PRIOS.put("selectchk/3", 0);
		OP_PRIOS.put("selectchk/4", 0); // new in TU6.0.2
		OP_PRIOS.put("subset/2", 0);
		OP_PRIOS.put("subtract/3", 0);
		OP_PRIOS.put("sumlist/2", 0);
		OP_PRIOS.put("union/3", 0);

		OP_PRIOS.put("same_length/2", 0);
		OP_PRIOS.put("sumlist/2", 0);
		OP_PRIOS.put("sumlist/2", 0);

		OP_PRIOS.put("exclude/3", 0);
		OP_PRIOS.put("partition/3", 0);

		// random.
		OP_PRIOS.put("random/1", 0);
		OP_PRIOS.put("random/3", 0); // new in TU6.0.2
		OP_PRIOS.put("setrand/1", 0); // new in TU6.0.2

		OP_PRIOS.put("random_between/3", 0); // new in TU6.0.2
		OP_PRIOS.put("random_member/2", 0); // new in TU6.0.2
		OP_PRIOS.put("random_perm2/4", 0); // new in TU6.0.2
		OP_PRIOS.put("random_permutation/2", 0); // new in TU6.0.2
		OP_PRIOS.put("random_select/3", 0); // new in TU6.0.2
		OP_PRIOS.put("randseq/3", 0); // new in TU6.0.2
		OP_PRIOS.put("randset/3", 0); // new in TU6.0.2

		// operators that quantify over variables.
		OP_PRIOS.put("setof/3", 0);
		OP_PRIOS.put("bagof/3", 0);
		OP_PRIOS.put("findall/3", 0);
		OP_PRIOS.put("aggregate/3", 0);
		OP_PRIOS.put("aggregate/4", 0);
		OP_PRIOS.put("aggregate_all/3", 0);
		OP_PRIOS.put("aggregate_all/4", 0);
		OP_PRIOS.put("forall/2", 0);
		// operators used within context of operators that quantify over
		// variables.
		OP_PRIOS.put("max/1", 0);
		OP_PRIOS.put("min/1", 0);
		OP_PRIOS.put("count/1", 0);
		OP_PRIOS.put("sum/1", 0);
		OP_PRIOS.put("set/1", 0);
		OP_PRIOS.put("bag/1", 0);
		// operators related to date and time.
		OP_PRIOS.put("time/3", 0);
		OP_PRIOS.put("get_time/1", 0);
		OP_PRIOS.put("convert_time/2", 0);
		OP_PRIOS.put("date/3", 0);
		OP_PRIOS.put("date/9", 0);
		OP_PRIOS.put("date_time_stamp/2", 0);
		OP_PRIOS.put("stamp_date_time/3", 0);
		OP_PRIOS.put("day_of_the_week/2", 0);

		// TU Prolog chapter 4.5: term type verification
		OP_PRIOS.put("var/1", 0);
		OP_PRIOS.put("nonvar/1", 0);
		OP_PRIOS.put("integer/1", 0);
		OP_PRIOS.put("float/1", 0);
		OP_PRIOS.put("rational/1", 0);
		OP_PRIOS.put("rational/3", 0);
		OP_PRIOS.put("number/1", 0);
		OP_PRIOS.put("atom/1", 0);
		OP_PRIOS.put("string/1", 0);
		OP_PRIOS.put("atomic/1", 0);
		OP_PRIOS.put("compound/1", 0);
		OP_PRIOS.put("ground/1", 0);
		OP_PRIOS.put("cyclic_term/1", 0);
		OP_PRIOS.put("acyclic_term/1", 0);

		// TU Prolog chapter 4.6: term comparison
		OP_PRIOS.put("compare/3", 0);
		OP_PRIOS.put("unify_with_occurs_check/2", 0);

		// TU prolog ch. 4.8: meta call predicates
		// call has variable num of args but up to 6 seems supported if I
		// understand the manual right
		OP_PRIOS.put("call/1", 0);
		OP_PRIOS.put("call/2", 0);
		OP_PRIOS.put("call/3", 0);
		OP_PRIOS.put("call/4", 0);
		OP_PRIOS.put("call/5", 0);
		OP_PRIOS.put("call/6", 0);

		OP_PRIOS.put("once/1", 0);
		OP_PRIOS.put("ignore/1", 0);
		OP_PRIOS.put("call_with_depth_limit/3", 0);
		OP_PRIOS.put("setup_call_cleanup/3", 0);
		OP_PRIOS.put("setup_call_catcher_cleanup/4", 0);
		OP_PRIOS.put("call_cleanup/2", 0);
		OP_PRIOS.put("call_cleanup/3", 0);

		// TU prolog ch. 4.9.3
		OP_PRIOS.put("print_message/2", 0);
		OP_PRIOS.put("print_message_lines/3", 0);
		OP_PRIOS.put("message_to_string/2", 0);

		// TU prolog 4.12
		OP_PRIOS.put("phrase/2", 0);
		OP_PRIOS.put("phrase/3", 0);

		// TU 6.0.2 new A.17
		OP_PRIOS.put("is_ordset/1", 0);
		OP_PRIOS.put("ord_empty/1", 0);
		OP_PRIOS.put("ord_seteq/2", 0);
		OP_PRIOS.put("list_to_ord_set/2", 0);
		OP_PRIOS.put("ord_intersect/2", 0);
		OP_PRIOS.put("ord_disjoint/2", 0);

		OP_PRIOS.put("ord_intersect/3", 0);
		OP_PRIOS.put("ord_intersection/2", 0);
		OP_PRIOS.put("ord_intersection/3", 0);
		OP_PRIOS.put("ord_intersection/4", 0);
		OP_PRIOS.put("ord_add_element/3", 0);
		OP_PRIOS.put("ord_del_element/3", 0);
		OP_PRIOS.put("ord_memberchk/2", 0);
		OP_PRIOS.put("ord_member/2", 0);
		OP_PRIOS.put("ord_subset/2", 0);
		OP_PRIOS.put("ord_subtract/3", 0);
		OP_PRIOS.put("ord_union/2", 0);
		OP_PRIOS.put("ord_union/3", 0);
		OP_PRIOS.put("ord_union/4", 0);
		OP_PRIOS.put("ord_symdiff/3", 0);

		// TU 6.0.2 new A.18
		OP_PRIOS.put("pairs_keys_values/3", 0);
		OP_PRIOS.put("pairs_values/2", 0);
		OP_PRIOS.put("pairs_keys/2", 0);
		OP_PRIOS.put("group_pairs_by_key/2", 0);
		OP_PRIOS.put("transpose_pairs/2", 0);
		OP_PRIOS.put("map_list_to_pairs/3", 0);

		// TU Prolog 4.16.1
		OP_PRIOS.put("open/4", 0);
		OP_PRIOS.put("alias/1", 0);
		OP_PRIOS.put("encoding/1", 0);
		OP_PRIOS.put("bom/1", 0);
		OP_PRIOS.put("eof_action/1", 0);
		OP_PRIOS.put("buffer/1", 0);
		OP_PRIOS.put("close_on_abort/1", 0);
		OP_PRIOS.put("lock/1", 0);
		OP_PRIOS.put("open/3", 0);
		OP_PRIOS.put("open_null_stream/1", 0);
		OP_PRIOS.put("close/1", 0);
		OP_PRIOS.put("close/2", 0);
		OP_PRIOS.put("stream_property/2", 0);
		OP_PRIOS.put("end_of_stream/1", 0);
		OP_PRIOS.put("eof_action/1", 0);
		OP_PRIOS.put("file_name/1", 0);
		OP_PRIOS.put("file_no/1", 0);
		OP_PRIOS.put("input/0", 0);
		OP_PRIOS.put("mode/1", 0);
		OP_PRIOS.put("newline/1", 0);
		OP_PRIOS.put("nlink/1", 0);
		OP_PRIOS.put("output/0", 0);
		OP_PRIOS.put("position/1", 0);
		OP_PRIOS.put("reposition/1", 0);
		OP_PRIOS.put("representation_errors/1", 0);
		OP_PRIOS.put("timeout/1", 0);
		OP_PRIOS.put("type/1", 0);
		OP_PRIOS.put("tty/1", 0);
		OP_PRIOS.put("current_stream/3", 0);
		OP_PRIOS.put("is_stream/1", 0);
		OP_PRIOS.put("set_stream_position/2", 0);
		OP_PRIOS.put("stream_position_data/3", 0);
		OP_PRIOS.put("seek/4", 0);
		OP_PRIOS.put("set_stream/2", 0);
		OP_PRIOS.put("set_prolog_IO/3", 0);

		// TU ch. 4.16.4
		OP_PRIOS.put("with_output_to/2", 0);

		// TU ch. 4.17
		OP_PRIOS.put("wait_for_input/3", 0);
		OP_PRIOS.put("byte_count/2", 0);
		OP_PRIOS.put("character_count/2", 0);
		OP_PRIOS.put("line_count/2", 0);
		OP_PRIOS.put("line_position/2", 0);

		// TU ch 4.18
		OP_PRIOS.put("nl/0", 0);
		OP_PRIOS.put("nl/1", 0);
		OP_PRIOS.put("put/1", 0);
		OP_PRIOS.put("put/2", 0);
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
		OP_PRIOS.put("ttyflush/0", 0);
		OP_PRIOS.put("get_byte/1", 0);
		OP_PRIOS.put("get_byte/2", 0);
		OP_PRIOS.put("get_code/1", 0);
		OP_PRIOS.put("get_code/2", 0);
		OP_PRIOS.put("get_char/1", 0);
		OP_PRIOS.put("get_char/2", 0);
		OP_PRIOS.put("get0/1", 0);
		OP_PRIOS.put("get0/2", 0);
		OP_PRIOS.put("get/1", 0);
		OP_PRIOS.put("get/2", 0);
		OP_PRIOS.put("peek_byte/1", 0);
		OP_PRIOS.put("peek_byte/2", 0);
		OP_PRIOS.put("peek_code/1", 0);
		OP_PRIOS.put("peek_code/2", 0);
		OP_PRIOS.put("peek_char/1", 0);
		OP_PRIOS.put("peek_char/2", 0);
		OP_PRIOS.put("skip/1", 0);
		OP_PRIOS.put("skip/2", 0);
		OP_PRIOS.put("get_single_char/1", 0);
		OP_PRIOS.put("at_end_of_stream/0", 0);
		OP_PRIOS.put("at_end_of_stream/1", 0);
		OP_PRIOS.put("copy_stream_data/3", 0);
		OP_PRIOS.put("copy_stream_data/2", 0);
		OP_PRIOS.put("read_pending_input/3", 0);

		// TU manual ch. 4.19
		OP_PRIOS.put("write_term/2", 0);
		OP_PRIOS.put("attributes/1", 0);
		OP_PRIOS.put("backquoted_string/1", 0);
		OP_PRIOS.put("character_escapes/1", 0);
		OP_PRIOS.put("ignore_ops/1", 0);
		OP_PRIOS.put("max_depth/1", 0);
		OP_PRIOS.put("module/1", 0);
		OP_PRIOS.put("numbervars/1", 0);
		OP_PRIOS.put("partial/1", 0);
		OP_PRIOS.put("portray/1", 0);
		OP_PRIOS.put("priority/1", 0);
		OP_PRIOS.put("quoted/1", 0);
		OP_PRIOS.put("write_term/3", 0);
		OP_PRIOS.put("write_canonical/1", 0);
		OP_PRIOS.put("write_canonical/2", 0);
		OP_PRIOS.put("write/1", 0);
		OP_PRIOS.put("write/2", 0);
		OP_PRIOS.put("writeq/1", 0);
		OP_PRIOS.put("writeq/2", 0);
		OP_PRIOS.put("print/1", 0);
		OP_PRIOS.put("print/2", 0);
		OP_PRIOS.put("read/1", 0);
		OP_PRIOS.put("read/2", 0);
		OP_PRIOS.put("read_clause/1", 0);
		OP_PRIOS.put("read_clause/2", 0);
		OP_PRIOS.put("read_term/2", 0);
		OP_PRIOS.put("comments/1", 0);
		OP_PRIOS.put("double_quotes/1", 0);
		OP_PRIOS.put("singletons/1", 0);
		OP_PRIOS.put("syntax_errors/1", 0);
		OP_PRIOS.put("subterm_positions/1", 0);
		OP_PRIOS.put("string_position/2", 0);
		OP_PRIOS.put("brace_term_position/3", 0);
		OP_PRIOS.put("list_position/4", 0);
		OP_PRIOS.put("term_position/5", 0);
		OP_PRIOS.put("term_position/1", 0);
		OP_PRIOS.put("variables/1", 0);
		OP_PRIOS.put("variable_names/1", 0);
		OP_PRIOS.put("read_term/3", 0);
		OP_PRIOS.put("read_history/6", 0);
		OP_PRIOS.put("prompt/2", 0);
		OP_PRIOS.put("prompt1/1", 0);

		// TU ch 4.21 analysing and constructing atoms
		OP_PRIOS.put("atom_codes/2", 0);
		OP_PRIOS.put("atom_chars/2", 0);
		OP_PRIOS.put("char_code/2", 0);
		OP_PRIOS.put("number_chars/2", 0);
		OP_PRIOS.put("number_codes/2", 0);
		OP_PRIOS.put("name/2", 0);
		OP_PRIOS.put("atom_concat/3", 0);

		// TU ch. 4.22 character properties
		OP_PRIOS.put("char_type/2", 0);
		OP_PRIOS.put("alnum/0", 0);
		OP_PRIOS.put("alpha/0", 0);
		OP_PRIOS.put("csym/0", 0);
		OP_PRIOS.put("csymf/0", 0);
		OP_PRIOS.put("ascii/0", 0);
		OP_PRIOS.put("white/0", 0);
		OP_PRIOS.put("cntrl/0", 0);
		OP_PRIOS.put("digit/0", 0);
		OP_PRIOS.put("digit/1", 0);
		OP_PRIOS.put("xdigit/1", 0);
		OP_PRIOS.put("graph/0", 0);
		OP_PRIOS.put("lower/0", 0);
		OP_PRIOS.put("lower/1", 0);
		OP_PRIOS.put("to_lower/1", 0);
		OP_PRIOS.put("upper/0", 0);
		OP_PRIOS.put("upper/1", 0);
		OP_PRIOS.put("to_upper/1", 0);
		OP_PRIOS.put("punct/0", 0);
		OP_PRIOS.put("space/0", 0);
		OP_PRIOS.put("end_of_file/0", 0);
		OP_PRIOS.put("end_of_line/0", 0);
		OP_PRIOS.put("newline/0", 0);
		OP_PRIOS.put("period/0", 0);
		OP_PRIOS.put("quote/0", 0);
		OP_PRIOS.put("paren/1", 0);
		OP_PRIOS.put("code_type/2", 0);

		OP_PRIOS.put("downcase_atom/2", 0);
		OP_PRIOS.put("upcase_atom/2", 0);

		OP_PRIOS.put("normalize_space/2", 0);

		OP_PRIOS.put("collation_key/2", 0);
		OP_PRIOS.put("locale_sort/2", 0);

		// TU manual 4.23: representing text in strings
		OP_PRIOS.put("string_to_atom/2", 0);
		OP_PRIOS.put("string_to_list/2", 0);
		OP_PRIOS.put("string_length/2", 0);
		OP_PRIOS.put("string_concat/3", 0);
		OP_PRIOS.put("sub_string/5", 0);

		// TU manual 4.25: character conversion
		OP_PRIOS.put("char_conversion/2", 0);
		OP_PRIOS.put("current_char_conversion/2", 0);

		// TU manual 4.26: arithmetic
		// Note that the infix operators are above.
		OP_PRIOS.put("between/3", 0);
		OP_PRIOS.put("sqrt/1", 0);
		OP_PRIOS.put("floor/1", 0);
		OP_PRIOS.put("ceiling/1", 0);
		OP_PRIOS.put("truncate/1", 0);
		OP_PRIOS.put("round/1", 0);
		OP_PRIOS.put("round/2", 0);
		OP_PRIOS.put("min/2", 0);
		OP_PRIOS.put("max/2", 0);
		OP_PRIOS.put("sign/1", 0);
		OP_PRIOS.put("abs/1", 0);
		OP_PRIOS.put("log/1", 0);
		OP_PRIOS.put("log10/1", 0);
		OP_PRIOS.put("exp/1", 0);

		OP_PRIOS.put("succ/2", 0);
		OP_PRIOS.put("plus/1", 0);
		OP_PRIOS.put("random/1", 0);
		OP_PRIOS.put("integer/1", 0);
		OP_PRIOS.put("float/1", 0);
		OP_PRIOS.put("rational/1", 0);
		OP_PRIOS.put("rationalize/1", 0);
		OP_PRIOS.put("float_fractional_part/1", 0);
		OP_PRIOS.put("float_integer_part/1", 0);
		OP_PRIOS.put("ceil/1", 0);

		OP_PRIOS.put("sin/1", 0);
		OP_PRIOS.put("cos/1", 0);
		OP_PRIOS.put("tan/1", 0);
		OP_PRIOS.put("asin/1", 0);
		OP_PRIOS.put("acos/1", 0);
		OP_PRIOS.put("atan/1", 0);
		OP_PRIOS.put("atan/2", 0);
		OP_PRIOS.put("powm/3", 0);
		OP_PRIOS.put("pi/0", 0);
		OP_PRIOS.put("e/0", 0);
		OP_PRIOS.put("epsilon/0", 0);
		OP_PRIOS.put("cputime/0", 0);
		OP_PRIOS.put("eval/1", 0); // use as in X is eval(2+2).
		OP_PRIOS.put("msb/1", 0);
		OP_PRIOS.put("lsb/1", 0);
		OP_PRIOS.put("popcount/1", 0);

		// TU ch. 4.28 misc arithmetic predicates
		OP_PRIOS.put("set_random/1", 0);
		OP_PRIOS.put("seed/1", 0);

		// TU 4.29 built-in list operations
		OP_PRIOS.put("memberchk/2", 0);
		OP_PRIOS.put("msort/2", 0);
		OP_PRIOS.put("keysort/2", 0);
		OP_PRIOS.put("predsort/3", 0);

		// TU 4.30 finding all solutions
		// all functions were already there

		// TU 4.32 formatted write
		OP_PRIOS.put("writeln/1", 0);
		OP_PRIOS.put("writef/1", 0);
		OP_PRIOS.put("writef/2", 0);
		OP_PRIOS.put("swritef/3", 0);
		OP_PRIOS.put("swritef/2", 0);
		OP_PRIOS.put("format/1", 0);
		OP_PRIOS.put("format/2", 0);
		OP_PRIOS.put("format/3", 0);
		OP_PRIOS.put("format_predicate/2", 0);
		OP_PRIOS.put("current_format_predicate/2", 0);

		// TU 4.33 terminal control
		// open issue, see #1123.

		// TU 4.34 dealing with time and date
		OP_PRIOS.put("date_time_value/3", 0);
		OP_PRIOS.put("format_time/3", 0);
		OP_PRIOS.put("format_time/4", 0);
		OP_PRIOS.put("parse_time/2", 0);

		// TU 4.35 file system interaction
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
		OP_PRIOS.put("extensions/1", 0);
		OP_PRIOS.put("relative_to/1", 0);
		OP_PRIOS.put("access/1", 0);
		OP_PRIOS.put("read/0", 0);
		OP_PRIOS.put("write/0", 0);
		OP_PRIOS.put("append/0", 0);
		OP_PRIOS.put("exist/0", 0);
		OP_PRIOS.put("none/0", 0);
		OP_PRIOS.put("file_type/1", 0);
		OP_PRIOS.put("txt/0", 0);
		OP_PRIOS.put("prolog/0", 0);
		OP_PRIOS.put("executable/0", 0);
		OP_PRIOS.put("qlf/0", 0);
		OP_PRIOS.put("directory/0", 0);
		OP_PRIOS.put("source/0", 0);
		OP_PRIOS.put("file_errors/1", 0);
		OP_PRIOS.put("error/0", 0);
		OP_PRIOS.put("fail/0", 0);
		OP_PRIOS.put("solutions/1", 0);
		OP_PRIOS.put("first/0", 0);
		OP_PRIOS.put("expand/1", 0);
		OP_PRIOS.put("verbose_file_search/0", 0);

		OP_PRIOS.put("is_absolute_file_name/1", 0);
		OP_PRIOS.put("file_name_extension/3", 0);
		OP_PRIOS.put("expand_file_name/2", 0);
		OP_PRIOS.put("prolog_to_os_filename/2", 0);
		OP_PRIOS.put("read_link/3", 0);
		OP_PRIOS.put("tmp_file/2", 0);
		OP_PRIOS.put("make_directory/1", 0);
		OP_PRIOS.put("delete_directory/1", 0);
		OP_PRIOS.put("working_directory/2", 0);
		OP_PRIOS.put("chdir/1", 0);

		// flag is not built-in because of Unreal. See #2109
		// OP_PRIOS.put("flag/3", 0);

		// TU6.1.1 Attribute manipulation predicates
		OP_PRIOS.put("get_attr/3", 0);
		OP_PRIOS.put("put_attr/3", 0);
	}

	static {
		// restrict clause creation: ISO section 8.9
		PROTECTED_OPS = new HashSet<>();
		PROTECTED_OPS.add("assert");
		PROTECTED_OPS.add("asserta");
		PROTECTED_OPS.add("assertz");
		PROTECTED_OPS.add("retract");
		PROTECTED_OPS.add("abolish");

		// restrict module handling
		PROTECTED_OPS.add(":");

		// new operator in TU6, not clear what it does but something 'execute'
		PROTECTED_OPS.add("^");

		// prolog file handling directives, ISO 7.4.2
		// PROTECTED_OPS.add("dynamic");
		PROTECTED_OPS.add("multifile");
		PROTECTED_OPS.add("discontiguous");
		PROTECTED_OPS.add("op");
		PROTECTED_OPS.add("char_conversion");
		PROTECTED_OPS.add("initialization");
		// protected_ops.add("include"); CHECK we should allow include/3 but
		// don't want include/1
		PROTECTED_OPS.add("ensure_loaded");

		// new in TU6
		PROTECTED_OPS.add("module_transparent");
		PROTECTED_OPS.add("meta_predicate");
		PROTECTED_OPS.add("thread_local");
		PROTECTED_OPS.add("volatile");

		// converting term to query: ISO section 7.6.2
		PROTECTED_OPS.add("catch");
		PROTECTED_OPS.add("throw");

		// new but not supported in TU6.0.2
		PROTECTED_OPS.add("autoload_path");
		PROTECTED_OPS.add("blob");
		PROTECTED_OPS.add("copy_predicate_clauses");
		PROTECTED_OPS.add("create_prolog_flag");
		PROTECTED_OPS.add("default_module");
		PROTECTED_OPS.add("directory_files");
		PROTECTED_OPS.add("in_pce_thread_sync");
		PROTECTED_OPS.add("instance");
		PROTECTED_OPS.add("message_line_element");
		PROTECTED_OPS.add("parse_time");
		PROTECTED_OPS.add("prolog_skip_frame");
		PROTECTED_OPS.add("prolog_stack_property");
		PROTECTED_OPS.add("public");
		PROTECTED_OPS.add("qcompile");
		PROTECTED_OPS.add("random_property");
		PROTECTED_OPS.add("set_end_of_stream");
		PROTECTED_OPS.add("set_module");
		PROTECTED_OPS.add("set_prolog_stack");
		PROTECTED_OPS.add("source_file_property");
		PROTECTED_OPS.add("stream_pair");
		PROTECTED_OPS.add("subsumes");
		PROTECTED_OPS.add("subsumes_term");
		PROTECTED_OPS.add("subsumes_chk");
		PROTECTED_OPS.add("term_subsumer");
		PROTECTED_OPS.add("thread_get_message");
		PROTECTED_OPS.add("tmp_file_stream");
		PROTECTED_OPS.add("unload_file");
		PROTECTED_OPS.add("var_number");
		PROTECTED_OPS.add("variant_sha1");
		PROTECTED_OPS.add("write_length");
		PROTECTED_OPS.add("assert_predicate_options");
		PROTECTED_OPS.add("assertion");
		PROTECTED_OPS.add("assertion_failed");
		PROTECTED_OPS.add("check_predicate_option");
		PROTECTED_OPS.add("check_predicate_options");
		PROTECTED_OPS.add("csv");
		PROTECTED_OPS.add("csv_read_file");
		PROTECTED_OPS.add("csv_read_file_row");
		PROTECTED_OPS.add("csv_write_file");
		PROTECTED_OPS.add("csv_write_stream");
		PROTECTED_OPS.add("cumulative");
		PROTECTED_OPS.add("current_option_arg");
		PROTECTED_OPS.add("current_predicate_option");
		PROTECTED_OPS.add("debug");
		PROTECTED_OPS.add("debug_message_context");
		PROTECTED_OPS.add("debug_print_hook");
		PROTECTED_OPS.add("debugging");
		PROTECTED_OPS.add("derive_predicate_options");
		PROTECTED_OPS.add("derived_predicate_options");
		PROTECTED_OPS.add("getrand");
		PROTECTED_OPS.add("is_ordset");
		PROTECTED_OPS.add("list_debug_topics");
		PROTECTED_OPS.add("max_member");
		PROTECTED_OPS.add("max_var_number");
		PROTECTED_OPS.add("maybe");
		PROTECTED_OPS.add("min_member");
		PROTECTED_OPS.add("nodebug");
		PROTECTED_OPS.add("numbervars");
		PROTECTED_OPS.add("opt_arguments");
		PROTECTED_OPS.add("opt_help");
		PROTECTED_OPS.add("opt_parse");
		PROTECTED_OPS.add("predicate_options");
		PROTECTED_OPS.add("read_term_from_chars");
		PROTECTED_OPS.add("retractall_predicate_options");
		PROTECTED_OPS.add("varnumbers");
		PROTECTED_OPS.add("atan2");
		PROTECTED_OPS.add("div");
		PROTECTED_OPS.add("random_float");
		// PROTECTED_OPS.add("flag");
	}

	// constructor is not allowed. Static utility method.
	private PrologOperators() {
	}

	/**
	 * Built-in operators are already defined in TU Prolog, and one should not
	 * try to redefine these by inserting or deleting these (even though TU
	 * Prolog allows redefining built-in operators, we do not consider this good
	 * practice).
	 * </p>
	 *
	 * @returns {@code true} if signature is built-in Prolog function
	 *          <em>and</em> is not a protected predicate, {@code false}
	 *          otherwise.
	 */
	public static boolean prologBuiltin(String signature) {
		return OP_PRIOS.containsKey(signature);
	}

	/**
	 * A number of Prolog operators have a special meaning and are not allowed
	 * to occur within queries, databases etc.
	 *
	 * @return {@code true} if label is protected operator, {@code false}
	 *         otherwise.
	 */
	public static boolean goalProtected(String name) {
		return PROTECTED_OPS.contains(name);
	}

	/**
	 * @param signature
	 *            is funcname+"/"+#arguments, eg "member/2"
	 * @return spec given signature, or null if no such signature. specification
	 *         is fx, fy, xfy, xfx, etc.
	 */
	public static PrologOperators.Fixity getFixity(String signature) {
		return OPERATOR_SPECS.get(signature);
	}

	/**
	 * @param signature
	 *            is funcname+"/"+#arguments, e.g. "member/2"
	 * @returns priority of given signature, or {@code null} if no such
	 *          signature.
	 */
	public static Integer getPriority(String signature) {
		return OP_PRIOS.get(signature);
	}

	/**
	 * Checks if given label is L-atom (see L-atom, ISO p.132 in sec.A.3.1).
	 * which refers to a concrete atom (identifier), see clause 6.1.2b see also
	 * ISO Prolog definition of Name in section 6.4.2.
	 *
	 * @return {@code true} if label is predication.
	 */
	public static boolean is_L_atom(String name) {
		// See ISO section 6.1.2b and 6.4.2 "Names"
		if (name.equals("[]")) {
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
			{
				return true;
			}
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
