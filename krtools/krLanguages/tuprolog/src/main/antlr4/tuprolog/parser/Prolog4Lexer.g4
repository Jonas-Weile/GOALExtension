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
 * 
 * 
 * The lexer grammar for the agent programming language GOAL lists all tokens used in the parser grammar
 * for the programming language. It is also used by the parser grammar for tests (test2g files).
 */
lexer grammar Prolog4Lexer;



tokens{ HIDDEN }

IF		: ':-';
QF		: '?-';
SLBR	: '[';
SRBR	: ']';
DOT		: '.';
COMMA	: ',';
LBR		: '(';
RBR		: ')';
CLBR	: '{';
CRBR	: '}';

BAR		: '|';
LARROW	: '-->';
ARROW	: '->';
STARARROW: '*->';
EQ		: '=';
NOTEQ	: '\\=';
IDENTICAL	: '==';
NOTIDENTICAL	: '\\==';
COLON	: ':';
SEMICOLON	: ';';

AT			: '@';
ALPHALT		: '@<';
ALPHALE		: '@=<';
ALPHAGT		: '@>';
ALPHAGE		: '@>=';
ALPHAEQ		: '=@=';
UNIV		: '=..';
IS			: 'is';

AND			: '/\\';
OR			: '\\/';

EQUAL		: '=:=';
NOTEQUAL	: '=\\=';
LT			: '<';
LT2			: '<<';
LE			: '=<';
GT			: '>';
GT2			: '>>';
GE			: '>=';
UNEQUAL		: '><';

UP			: '^';

PLUS		: '+';
MINUS		: '-';
STAR		: '*';
STAR2		: '**';
SLASH		: '/';
SLASH2		: '//';
BACKSLASH	: '\\';

REM			: 'rem';
MOD			: 'mod';
XOR			: 'xor';
RDIV		: 'rdiv';

NEGATION	: '\\+';


/*  We have a NUMBER token, while the ISO spec has a FLOAT and a NUMBER token.
  However antlr can not determine properly whether a DOT is a decimal dot or a float number dot if
  we follow the ISO.
  If necessary, we will in post processing determine whether the number is float or integer. 
*/ 
NUMBER  // 6.4.4. + 6.4.5
    // We follow the ISO convention: you must use the dot and at least a digit if you want to use E notation.
  :   // HACK to get around ANTLR issues. 
      // ANTLR will incorrectly determine for itself whether REST_OF_FLOAT is following
      // if we would write "REST_OF_FLOAT?", using only the "." as the trigger.
      // the break will stop the parser right in its tracks and return the INTEGERCONSTANT parsed so far  
      // We still need to put REST_OF_FLOAT optional, otherwise a plain "0" will not parse, because
      // the ANT predictor predicts that the first rule will *not* fit (because there is NO '.')
      // and it will try only the 0x 0b etc cases which of course fail also.    
      
      // CHECK Is this now working with ANTLR4?
    INTEGERCONSTANT REST_OF_FLOAT?  // accept either FLOAT or INTEGER
  | BINARYCONSTANT
  | OCTALCONSTANT
  | HEXADECIMALCONSTANT
  ;


// Floating point number (used in test2g file).
fragment 
FLOAT	: (PLUS | MINUS)? (DIGIT+ (DOT DIGIT+)?) | (DOT DIGIT+)
		;
		
fragment 
REST_OF_FLOAT
  :  '.' INTEGERCONSTANT EXPONENT?
  ;

fragment EXPONENT// 6.4.5
  :   ('e' | 'E') ('+' | '-')? INTEGERCONSTANT
  ;

NAME  // 6.4.2 (graphic tokens, semicolon token nor quoted tokens (of the form '...') have been included; see also below).
  : ('a'..'z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*  // 6.4.2 (identifier)
  | '!'           // 6.4.2 (cut token)
  ;

VARIABLE// 6.4.3
  : ('A'..'Z' | '_') (ALPHACHAR | DIGIT |'_')*
  ;


fragment 
INTEGERCONSTANT  // 6.4.4
  :     (DIGIT)+
  ;
  
fragment BINARYCONSTANT   // 6.4.4
  : '0b' ('0' | '1')+
  ;

fragment OCTALCONSTANT    // 6.4.4
  : '0o' ('0'..'7')+
  ;

fragment HEXADECIMALCONSTANT  // 6.4.4
  : '0x' ('0'..'9' | 'a'..'f' | 'A'..'F')+
  ;

fragment CHAR // 6.5. many characters but not quotes. 
  : ALPHACHAR | DIGIT | '_'   // alpha numeric char
  | '#' | '$' | '&' | '*' | '+' | '-' | '.' | '/' | ':' | '<' | '=' | '>' | '?' | '@' | '^' | '~' // graphic char
  | '!' | '(' | ')' | ',' | ';' | '[' | ']' | '{' | '}' | '|' | '%' // solo char
  |  WHITESPACECHAR // layout characters. See my comments at WHITESPACE.
  | '\\' // meta char; quote chars are explicitly handled in definition of string token
  | ESCAPE_SEQUENCE
  ;

fragment ALPHACHAR
  : 'A'..'Z' | 'a'..'z'
  ;

fragment DIGIT
  :     '0'..'9'
  ;
  
STRING  // Compare 6.4.2 (quoted char); in contrast with 6.5.4 new line chars are allowed in strings
  : '\'' (CHAR | '\'\'' | '"' | '`' )* '\'' // single quoted string
  | '"' (CHAR | '""' | '\'' | '`' )* '"'    // double quoted string
  | '`' (CHAR | '``' | '\'' | '"' )* '`'    // back quoted string
  ; 

fragment ESCAPE_SEQUENCE: '\\' (META_CHAR | SYMBOLIC_CONTROL_CHAR) ;  

fragment META_CHAR: '\\' | '\'' | '"' | '`'; // 6.5.5

fragment SYMBOLIC_CONTROL_CHAR: 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | 'x'; // 6.4.2.1

// Cf. 6.4.8: The end char '.' must be followed by a layout text; 
// The footnote there also allows comments; we are more generous also allowing /* there
ENDTOKEN   : '.' ( WHITESPACECHAR | COMMENTCHARS | EOF );

// Layout text. Note that comments and whitespace are handled slightly differently than the Standard does.
// See also remark (1) in list of deviations below.
COMMENT   : COMMENTCHARS -> skip ; 
    // skip() means that COMMENT can be placed anywhere.
    // I think that's not conform the standard?
   
fragment COMMENTCHARS:  '%'(~('\n'|'\r'))*                // 6.4.1 (single line comment)
        | '/*' .*? '*/'  // 6.4.1 (bracketed comment)
        ;

WHITESPACE  : WHITESPACECHAR+  -> skip; // 6.4.1 (layout text)//FIXME channel=HIDDEN
    // because skip() is called, WHITESPACE never will appear as a token.
    // hence you can not refer to WHITESPACE in the parser

fragment WHITESPACECHAR: (' '|'\t'|'\f'|'\r'|'\n') ; // non ISO: see comments.

