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
 * The lexer grammar for the agent programming language GOAL lists all tokens used in the parser grammar
 * for the programming language. It is also used by the parser grammar for tests (test2g files).
 */
lexer grammar GOALLexer;

@members {
	// This variable decides when to enter KR parsing and when not.
	// if set to TRUE, it tells parser to NOT enter KR parsing.
	boolean stayInDefault = false;
}

tokens{ HIDDEN }

// Modules
USE				: 'use';
AS				: 'as';
MODULE			: 'module';
ACTIONSPEC		: 'actionspec';
PLANNER			: 'planner';
MAS				: 'mas';
FOCUS			: 'focus';
NONE			: 'none';
NEW				: 'new';
FILTER			: 'filter';
SELECT			: 'select';
EXITMODULE		: 'exit-module'; // built-in action but up here because of next token
EXIT			: 'exit';
ALWAYS			: 'always';
NEVER			: 'never';
NOGOALS			: 'nogoals';
NOACTION		: 'noaction';
LEARNBEL		: 'learnbel';
LEARNGOAL		: 'learngoal';

// Use cases
KNOWLEDGE		: 'knowledge';
BELIEFS			: 'beliefs';
GOALS			: 'goals';

// Options, macros
ORDER			: 'order';
LINEARALL		: 'linearall';
LINEARALLRANDOM	: 'linearallrandom';
LINEAR			: 'linear';
LINEARRANDOM	: 'linearrandom';
RANDOMALL		: 'randomall';
RANDOM			: 'random';
ADAPTIVE		: 'adaptive';
DEFINE			: 'define';

// Rule tokens
IF				: 'if';
THEN			: 'then';
FORALL			: 'forall';
DO				: 'do';
LISTALL			: 'listall'		-> pushMode(VAR_PARAMETERS);
LTRARROW		: '->';
RTLARROW		: '<-';

// Mental state operators
BELIEF_OP		: 'bel';
GOAL_OP			: 'goal';		// GOAL_OP because we cannot have same token as parser grammar name.
AGOAL_OP		: 'a-goal';
GOALA_OP		: 'goal-a';
PERCEPT_OP		: 'percept';
SENT_OP			: 'sent';
SENT_IND_OP		: 'sent:';
SENT_INT_OP		: 'sent?';
SENT_IMP_OP		: 'sent!';

NOT				: 'not'			{ stayInDefault = true; };
TRUE			: 'true';

// Built-in actions
ADOPT			: 'adopt';
DROP			: 'drop';
INSERT			: 'insert';
DELETE			: 'delete';
LOG				: 'log';
PRINT			: 'print';
SEND			: 'send';
SEND_IND		: 'send:';
SEND_INT		: 'send?';
SEND_IMP		: 'send!';
SLEEP			: 'sleep';
UNSUBSCRIBE		: 'unsubscribe';
SUBSCRIBE		: 'subscribe';
STARTTIMER		: 'starttimer';
CANCELTIMER		: 'canceltimer';

// Selector expressions
ALL				: 'all';
ALLOTHER		: 'allother';
SELF			: 'self';
SOME			: 'some';
SOMEOTHER		: 'someother';
THIS			: 'this';

// Action specification tokens
EXTERNAL		: 'external';
INTERNAL		: 'internal';
PRE				: 'pre'			-> pushMode(KRBLOCK);
POST			: 'post'		-> pushMode(KRBLOCK);
POSTADD			: 'post+'		-> pushMode(KRBLOCK);
POSTDEL			: 'post-'		-> pushMode(KRBLOCK);
WITH			: 'with';

// Planner tokens
METHOD			: 'method';
OPERATOR		: 'operator';
TASK			: 'task';
SUBTASKS		: 'subtasks';

// Test tokens
TIMEOUT			: 'timeout';
TEST			: 'test';
IN				: 'in';
DONE			: 'done'		{ stayInDefault = true; };
EVENTUALLY		: 'eventually';
LEADSTO			: 'leadsto';
UNTIL		    : 'until';

fragment EscapedQuote: '\\"';
StringLiteral
	: '"' (EscapedQuote | ~[\r\n"])* '"'
	;

fragment EscapedSingleQuote: '\\\'';
SingleQuotedStringLiteral
	: '\'' (EscapedSingleQuote | ~[\r\n'])* '\''
	;

//  Plus, minus, equals, and punctuation tokens.
EQUALS	: '=';
MINUS	: '-';
PLUS	: '+';
DOT		: '.';
COMMA	: ',';
LBR		: '('		 { stayInDefault = false; };
RBR		: ')';
CLBR	: '{';
CRBR	: '}';
SLBR	: '[';
SRBR	: ']';

// GOAL identifiers

ID
	: (CHAR | SCORE) (CHAR | DIGIT | SCORE)*
	;

fragment CHAR	: [\p{Alpha}\p{General_Category=Other_Letter}];
fragment SCORE	: '_';
fragment DIGIT	: [\p{Digit}];

FLOAT
	: ('+' | '-')? (DIGIT+ '.' DIGIT+)
	| ('.' DIGIT+)
	;
	
INT
	: ('+' | '-')? DIGIT+
	;
	
// Parameter list of KR terms (anything between brackets, except for bracket following 'not' operator)
PARLIST
  :  { !stayInDefault }? '(' (  ~('(' | ')') | PARLIST )* ')'
  ;

// Comments
LINE_COMMENT	: '%' ~[\r\n]*		-> channel(HIDDEN);
BLOCK_COMMENT	: '/*' .*? '*/'		-> channel(HIDDEN);
// White space
WS				: WHITESPACECHAR+	-> channel(HIDDEN);
fragment WHITESPACECHAR: [\p{White_Space}];

// Knowledge representation code
mode KRBLOCK;
KR_BLOCK
  : '{' (  ~('{' | '}') | KR_BLOCK )* '}' -> popMode
  ;
KR_BLOCK_WS
  : WS -> type(WS), channel(HIDDEN)
  ;

// Lexer mode to recognize variable parameters, to allow for all kinds of styles of variable names;
// E.g., PDDL uses '?' to indicate start of variable.
mode VAR_PARAMETERS;
VAR		: (CHAR | DIGIT | PUNCTUATION)+
		;
VAR_PARAMETERS_RTLARROW	: RTLARROW -> type(RTLARROW), popMode;
VAR_PARAMETERS_RBR		: RBR -> type(RBR), popMode;
VAR_PARAMETERS_COMMA	: COMMA -> type(COMMA);
VAR_PARAMETERS_WS 		: WS -> type(WS), channel(HIDDEN);
// Something is wrong if we find anything else, but let's make sure we get out of this mode then again to avoid
// getting errors that say the rest of the input cannot be tokenized.
ERROR					: . -> popMode;

fragment PUNCTUATION: ('='|'-'|'+'|'.'|':'|'?'|'!'|'\''|'"'|'~'|'*'|'$'|'%'|'#'|'@'|'^'|'&'|'_'|'/');