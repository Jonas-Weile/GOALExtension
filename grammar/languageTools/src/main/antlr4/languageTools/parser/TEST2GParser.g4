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
 * The Test parser grammar describes the grammar rules for the tests for GOAL agent programs.
 */
parser grammar TEST2GParser;

@header{
/**
 * The parser grammar for tests for the agent programming language GOAL. The grammar imports the GOAL parser
 * grammar and uses the rules for <i>actions</i> and <i>mentalStateCondition</i>.
 * 
 * Operators and punctuation symbols are directly referenced in the grammar to enhance readability.
 * References to <i>tokens</i> thus are implicit in the grammar. The token labels can be found in the
 * lexer grammar GOALLexer.
 */
}

options{ tokenVocab=GOALLexer; }

import MOD2GParser;

test
	: useclause* option? moduletest* agenttest+ EOF
	;

// ID is the name of a module here
moduletest
	: TEST moduleref (',' moduleref)* WITH pre? in? post?
	;

moduleref
	: ID PARLIST?
	;

pre
	: PRE KR_BLOCK
	;
in
	: IN '{' testcondition* '}'
	;
post
	: POST KR_BLOCK
	;
	
testcondition
	: (temporaltest | reacttest) '.'
	;
temporaltest
	: (ALWAYS | NEVER | EVENTUALLY) testmsc
	;
reacttest
	: testmsc LEADSTO testmsc
	;

// ID is the name of an agent here
agenttest
	: ID (',' ID)* '{' testaction* '}'
	;

testaction
	: DO actioncombo runcondition? '.'
	;
runcondition
    : UNTIL testmsc
	;

testmsc
	: (msc (',' doneTest)?) | (doneTest (',' msc)?)
	;
doneTest
	: (DONE '(' action ')')
	| (NOT '(' DONE '(' action ')' ')')
	;