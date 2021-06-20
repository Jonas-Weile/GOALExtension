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
 *
 * The grammar for module files for the GOAL agent programming language.
 * 
 * Operators and punctuation symbols are directly referenced in the grammar to enhance readability.
 * References to <i>tokens</i> thus are implicit in the grammar. The token labels can be found at the
 * end of the grammar.
 */
parser grammar MOD2GParser;

options{ tokenVocab=GOALLexer; }

module
	: (useclause)* (learnclause)* (option)* (macro)* MODULE ID PARLIST? '{' rules* '}' EOF
	;

useclause
	: USE ref (',' ref)* (AS usecase)? '.'
	;
	
learnclause
	: (LEARNBEL | LEARNGOAL) PARLIST '.'
	;
	
ref
	: string | (ID ('.' ID)*)
	;
	
usecase
	: KNOWLEDGE | BELIEFS | GOALS | MODULE | ACTIONSPEC | MAS | PLANNER
	;

option
	: exitoption | focusoption | orderoption | timeoutoption
	;

exitoption
	: EXIT '=' (value = ALWAYS | value = NEVER | value = NOGOALS | value = NOACTION) '.'
	;

focusoption
	: FOCUS '=' (value = NONE | value = NEW | value = SELECT | value = FILTER) '.'
	;

orderoption
	: ORDER '='
	  (value = LINEAR | value = LINEARALL | value = LINEARRANDOM | value = LINEARALLRANDOM | value = RANDOM | value = RANDOMALL | value = ADAPTIVE) '.'
	;
	
timeoutoption
	: TIMEOUT '=' (value = INT) '.'
	;
	
macro 
	: DEFINE ID PARLIST? AS msc '.'
	;
	
rules 
	: IF msc THEN ( actioncombo '.' | '{' rules* '}' )
	| FORALL msc DO ( actioncombo '.' | '{' rules* '}' )
	| LISTALL VAR RTLARROW msc DO ( actioncombo '.' | '{' rules* '}' )
	;
		
msc
	: mentalliteral (',' mentalliteral)*
	;
	
mentalliteral 
	: mentalatom | NOT '(' mentalatom ')' | TRUE | ID PARLIST?
	;
	
mentalatom  
	: (selector '.')? mentalop PARLIST
	;
	
mentalop 
	: BELIEF_OP | GOAL_OP | AGOAL_OP | GOALA_OP | PERCEPT_OP
	| SENT_OP | SENT_IND_OP | SENT_INT_OP | SENT_IMP_OP
	;
	
actioncombo
	: action ('+' action)*
	;
	
action 
	: ID PARLIST? | selectoraction | generalaction
	;

selectoraction 
	: (selector '.')?
		( op = INSERT PARLIST 
		| op = DELETE PARLIST
		| op = ADOPT PARLIST
		| op = DROP PARLIST
		| op = SEND PARLIST 
		| op = SEND_IND PARLIST
		| op = SEND_INT PARLIST
		| op = SEND_IMP PARLIST
		)
	;
	
generalaction
	: op = EXITMODULE
	| op = LOG PARLIST
	| op = PRINT PARLIST
	| op = SLEEP PARLIST
	| op = UNSUBSCRIBE PARLIST
	| op = SUBSCRIBE PARLIST
	| op = STARTTIMER PARLIST
	| op = CANCELTIMER PARLIST
	;
	
selector 
	: PARLIST
	| selectorop
	;

selectorop
	: ALL | ALLOTHER | SOME | SOMEOTHER | SELF | THIS
	;
	
string
	: (StringLiteral ('+' StringLiteral)*)
	| (SingleQuotedStringLiteral ('+' SingleQuotedStringLiteral)*)
	;