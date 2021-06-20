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
 * The grammar for ACT files for the GOAL agent programming language.
 */
parser grammar ACT2GParser;

options{ tokenVocab=GOALLexer; }

specifications
	: useclause* actionspec* EOF
	;

useclause
	: USE ref (',' ref)* AS KNOWLEDGE '.'
	;
	
ref
	: string | (ID ('.' ID)*)
	;

actionspec 
	: DEFINE ID PARLIST? asclause? WITH pre (post | (postdel? postadd))
	;
	
pre
	: PRE KR_BLOCK
	;
	
post
	: POST KR_BLOCK
	;
	
postadd
	: POSTADD KR_BLOCK
	;
	
postdel
	: POSTDEL KR_BLOCK
	;
	
asclause 
	: AS (INTERNAL | EXTERNAL)
	;	

string
	: (StringLiteral ('+' StringLiteral)*)
	| (SingleQuotedStringLiteral ('+' SingleQuotedStringLiteral)*)
	;