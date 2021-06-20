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
 * The grammar for MAS files for the GOAL agent programming language.
 * 
 * Operators and punctuation symbols are directly referenced in the grammar to enhance readability.
 * References to <i>tokens</i> thus are implicit in the grammar. The token labels can be found at the
 * end of the grammar.
 */
grammar MAS2G;

tokens{ HIDDEN }

mas
	: environment? agent* policy EOF
	;

// Environment section

environment
	: USE ref AS ENVIRONMENT ( WITH initKeyValue (',' initKeyValue)* )? '.'
	;
	
ref
	: string | (ID ('.' ID)*)
	;

initKeyValue
	: ID '=' initExpr
	;

initExpr
	: constant
	| function
	| list
	;

constant
	: ID 
	| INT
	| FLOAT
	| string
	;

function
	: ID '(' initExpr (',' initExpr)* ')'
	;

list
	: '[' initExpr (',' initExpr)* ']'
	;

// Agent section

agent
	: DEFINE ID AS AGENT '{' useClause* '}'
	;
 
useClause
	: USE ref AS useCase '.'
	;

useCase
	: (INIT | EVENT | MAIN | SHUTDOWN) MODULE?
	;

// Launch Policy section

policy
	: LAUNCHPOLICY '{' launchRule* '}'
	;

launchRule
	: (WHEN entity)? LAUNCH instruction (',' instruction)* '.'
	;

entity
	: STAR | entitytype | entityname
	;
	
entitytype
	: TYPE '=' ID
	;
	
entityname
	: NAME '=' ID
	;

instruction
	: ID (WITH constraint (',' constraint)* )?
	;
	
constraint
	: nameconstraint | nrconstraint | maxconstraint | alpha | gamma | epsilon | decay
	;
	
nameconstraint
	: NAME '=' ID | NAME '=' STAR
	;
	
nrconstraint
	: NUMBER '=' INT
	;
	
maxconstraint
	: MAX '=' INT
	;
	
alpha
	: ALPHA '=' FLOAT
	;
	
gamma
	: GAMMA '=' FLOAT
	;
	
epsilon
	: EPSILON '=' FLOAT
	;
	
decay
	: DECAY '=' FLOAT
	;

string
	: (StringLiteral ('+' StringLiteral)*)
	| (SingleQuotedStringLiteral ('+' SingleQuotedStringLiteral)*)
	;

fragment EscapedQuote: '\\"';
StringLiteral
	: '"' (EscapedQuote | ~[\r\n"])* '"'
	;

fragment EscapedSingleQuote: '\\\'';
SingleQuotedStringLiteral
	: '\'' (EscapedSingleQuote | ~[\r\n'])* '\''
	;

// LEXER
INIT 			: 'init';
EVENT 			: 'event';
MAIN 			: 'main';
SHUTDOWN        : 'shutdown';
MODULE 			: 'module';
USE				: 'use';
ENVIRONMENT		: 'environment';
WITH			: 'with';
DEFINE			: 'define';
AS				: 'as';
AGENT			: 'agent';
WHEN			: 'when';
LAUNCH			: 'launch';
LAUNCHPOLICY	: 'launchpolicy';
TYPE			: 'type';
NAME			: 'name';
NUMBER			: 'number';
MAX				: 'max';
ALPHA   		: 'alpha';
GAMMA   		: 'gamma';
EPSILON			: 'epsilon';
DECAY			: 'decay';

EQUALS	: '=';
MINUS	: '-';
PLUS	: '+';
DOT		: '.';
COMMA	: ',';
LBR		: '(';
RBR		: ')';
CLBR	: '{';
CRBR	: '}';
SLBR	: '[';
SRBR	: ']';

ID
	: (LETTER | SCORE) (LETTER | DIGIT | SCORE)*
	;

fragment LETTER: [\p{Alpha}\p{General_Category=Other_Letter}];
fragment SCORE: '_';
fragment DIGIT: [\p{Digit}];

STAR : '*';

FLOAT
	: ('+' | '-')? (DIGIT+ '.' DIGIT+)
	| ('.' DIGIT+)
	;
	
INT
	: ('+' | '-')? DIGIT+
	;

// White space and comments.
LINE_COMMENT
	: '%' ~[\r\n]* -> channel(HIDDEN)
	;

BLOCK_COMMENT
	: '/*' .*? '*/' -> channel(HIDDEN)
	;

WS
	: WHITESPACECHAR+ -> skip
	;

fragment WHITESPACECHAR: [\p{White_Space}];