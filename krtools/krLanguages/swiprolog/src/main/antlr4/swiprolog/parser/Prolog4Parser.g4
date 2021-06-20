/*
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

/**
 * This is an ANTLR 4 grammar for the Prolog language.
 *
 * <p>We have followed the ISO/IEC 13211-1 International Standard for Prolog in most cases, but not always.
 * Existing Prolog implementations do not completely conform to the Standard, and, in practice, some
 * compromises have to be made (see below). Other reasons for deviating have been more pragmatically
 * motivated: we wanted to keep our grammar simple (and accordingly choose simple solutions to resolve
 * certain issues) and we did not want our grammar to support certain options that quickly lead to
 * unreadable code (e.g., using graphic tokens as predicate names). For a list of deviations, please see
 * the end of this file. For those who would like to conform more closely to the Standard, this grammar
 * may provide a starting point.</p>
 *
 * <p>All references below of the form "6.3.4" refer to clauses of the International Standard for Prolog.</p>
 *
 */

parser grammar Prolog4Parser;

options{ tokenVocab=Prolog4Lexer; }



/*---------------------------------------------------------------------
 * PARSER RULES
 *---------------------------------------------------------------------*/
 
/**
 * Parses a Prolog file.
 * <p>
 * Expects Prolog program with common Prolog syntax,
 * but requires the EOF token after the last clause or directive.
 * </p> 
 */
prologfile
    : prologtext EOF
    ;
    
prologtext
  : directiveorclause*
  ;
  
directiveorclause 
  : directive 
  | clause 
  ;

directive // 6.2.1.1
  : ':-' term1200 ENDTOKEN
  ;

clause // 6.2.1.2
  : term1200 ENDTOKEN
  ;

arglist // 6.3.3
  : expression ( ',' arglist )?
  ;

possiblyEmptyConjunct
  : term1000?
  ;
  
possiblyEmptyDisjunct
  : term1100?
  ;
  
expression // 6.3.3.1
  : term900
  ;

listterm // 6.3.5
  : '[' items? ']'
  ;

items // 6.3.5 ; we use the prolog "." functor to build items list.
  : expression
      (
        (',' items) 
        | ( BAR ( listterm | VARIABLE ) )
      )?
  ;

prefixoperator   // compensates for absence of names derived from graphic token.
  : prefixop LBR expression COMMA expression RBR
  ;

/* Note that ',' cannot be used as prefix operator since the comma character is not a graphic token
 char (cf. 6.4.2, 6.5.1).
 In addition, we do not allow ':-' as prefix operator to avoid ambiguity and allow '-' 
 only as unary prefix operator (see term200 below for the 'infix' version.)
*/
prefixop
  :  LARROW | SEMICOLON | ARROW | EQ | NOTEQ | IDENTICAL | NOTIDENTICAL | ALPHALT | ALPHALE | ALPHAGT | ALPHAGE | UNIV | IS |
    EQUAL | NOTEQUAL | LT | LE | GT | GE | PLUS | AND | OR | STAR | SLASH | SLASH2 | REM |
    MOD | LT2 | GT2 | STAR2 | UP ;

/* 
 * Prolog terms have been defined using the grammar design pattern in Terence Parr, 2007, The Definitive ANTLR
 * Reference p. 61, 275. Below the numbers after 'term' indicate the operator precedence (see the ISO Standard
 * for more information).
 */
term0
  : NUMBER  
        // 6.3.1.1 (The Standard supports negative numbers explicitly; 
        // using the '-/1' operator these are covered here as well; see term200 below). 
  | NAME ( '(' a=arglist ')' )? // 6.3.1.3 and 6.3.3;
  | VARIABLE // 6.3.2
  | STRING // Compare 6.4.2.1
  | LBR term1200 RBR // 6.3.4.1
  | CLBR term1200 CRBR // 6.3.6
  | listterm
  | prefixoperator
  ;

term50
  : term0 (':' term0)?
  ;

term100
  : term50 ('@' term50)?
  ;

term200 // '=' especially allowed for include(=(a), ...,...)
  : (op = '-' | op= '\\' | op='=' ) term200 
  | term100  ( (op= '^' term200) | (op= '**' term100) )?
  ;

term400
// Operators *, /, //, rem, mod, <<, and >> are left-associative
// rdiv is SWI prolog specific.
  : term200 term400b*
  ;

term400b
  : op=('*'  | '/' | '//' | 'rem' | 'mod' | 'rdiv' | 'gcd' | 'lcm' | '<<' | '>>')  term200
  ;
  
term500  // Operators +, -, /\, and \/ are left-associative
// 'xor' and '><' are SWI specific.
  : term400 term500b*
  ;
  
term500b
  : op=('+' | '-' | '/\\' | '\\/' | 'xor' | '><')  term400
  ;

term700
  : 
      term500
       ( 
         op= (	'=' | '\\=' | '==' | '\\==' | '@<' | '@=<' | 
			'@>' | '@>=' | '=@='| '=..' | 'is' | '=:=' | '=\\=' |
        	'<' | '=<' | '>' | '>=') 
         term500 
       )?
  ;

term900 // CHECK UNKNOWN OPERATOR, NOT IN TABLE ON ISO-SPEC p.13 ?
  : term700
  | op = '\\+' term900 
  ;

term1000
  : term900  (op=',' term1000)?
  ;

term1050
  : t1 = term1000  ( op=('*->' | '->') term1050  )?
  ;

term1100 
  : term1050  (op=';' term1100)?
  ;

term1105
  :  term1100  (op='|' term1105)?
  ; 
  
term1150 // NOT ISO, only SWI
  : term1105
  |	op=DYNAMIC term1000
  ;
  
  
term1200
  : term1150  ( ( op=':-' | op='-->')  term1150)?
  | op='?-'  term1150
  ;


/* Deviations from the ISO/IEC 13211-1 International Standard for Prolog.
 1. White space and comments can be used more freely than the Standard allows.
  Since comments are "layout text" (6.4.1) and in the Standard layout text is used to resolve two
  ambiguities [(i) whether a dot is a graphic or end token; and (ii) whether an atom followed by an open
  token is a functor or an operator] these ambiguities have been resolved somewhat differently:
  - by explicitly defining an end token to include at least one white space character (though we
    would have preferred to drop this requirement, existing Prolog implementations would not accept
    Prolog texts accepted by this grammar)
  - by restricting name tokens: graphic tokens are not allowed as names; see also (2) below.
  We also allow the /* directly behind an ENDTOKEN, in line with the more freely use of layout text
  mentioned above.
 2. Graphic tokens are not allowed as atoms (graphic chars are only allowed as part of strings).
  The Standard allows the use of graphic tokens such as '/*^' as names (which serve as identifiers 7.1.4,
  e.g., for predicates, functors, and constants). This leads to certain obvious issues that have to be
  settled more or less ad hoc, even in the Standard itself (e.g. by disallowing the use of ':-/1' as
  principal functor in a Prolog clause (6.2.1.2) or the use of '/*' at the beginning of a graphic token
  (since it is also used to indicate the start of a comment; cf. 6.4.2; see also (1) above). We also
  believe that the use of graphic tokens as names quickly leads to unreadable code.
 3. Operators are not allowed as atoms (they are allowed as part of strings).
  The standard allows operators as atoms (which then are terms with priority 1201; cf. 6.3.1.3). Among
  others this allows expressions such as 'f(:-, ;, [:-, :-|:-])' (cf. 6.3.3.1). Since atoms are supposed to
  serve as identifiers (7.1.4), remarks like those made above (2) regarding the liberal use of graphic
  characters apply to this case as well.
  Instead, in this grammar, operators with associated priorities have been handled explicitly ("hard coded")
  using the precedence design pattern discussed in Terence Parr, 2007, The Definitive ANTLR Reference, The
  Pragmatic Bookshelf, p. 61, 275.
  As a result, different from the Standard 6.3.4.4 note 4, it is not possible to redefine operator
  precedences by means of the operator 'op/3'.
 4. '[]' and '{}' are not allowed as atoms.
  In this grammar '[]' denotes the empty list which can be used as an argument in a compound term. '[]'
  cannot be used otherwise, and the 'empty list' is covered by the grammar rule for lists (see nonterminal
  'listterm').
  The Standard introduces a notational inconsistency, since it declares that whereever the phrase
  '{}(...)' is used one can equivalently use '{...}' but '[](...)' cannot be replaced equivalently with
  '[...]' (instead one has to use '.(...,...)' notation (cf. 6.3.3, 6.3.5 and 6.3.6).
  Note that '[]' nor '{}' can be treated as operators according to the Standard (6.3.4.3 note 3).
 5. Quoted tokens are not allowed as atoms (instead they are used to define a string token).
  The ISO Standard 6.4.2 states that if the (allowed) character sequence appearing within the quoted token
  is a valid atom without quotes "the quoted token shall denote that atom", i.e. can equivalently be
  replaced by the atom without quotes. E.g. "'abc'" and "abc" denote the same atom.
  Because of (2-5) this grammar does not explicitly introduce a non-terminal to denote atoms (6.3.1.3).
 6. Predefined unary operator ':-' (cf. ISO Standard 6.3.4.4).
  The unary operator ':-' is only allowed in a directive term (see nonterminal 'directive' above).
 7. After a head tail separator '|' a listterm is required.
  Different from the Standard 6.3.5 the grammar does not allow an arbitrary expression after a head tail
  separator but requires a listterm at that position instead. The Standard does not recognize a list as a
  type (cf. 7.1) but the restriction in the grammar on list tails makes sense and seems useful.
  The liberal approach which allows an arbitrary term after a head tail separator leads to issues which
  are not easily solved. In the Standard it appears that the head tail separator is only used as a syntactic
  device (cf. 7.10.6). In existing Prolog implementations, the built-in member function handles a "list" of
  the form '[a,b|c]' as the list '[a]', whereas the append function returns 'no' when attempting to append
  any list to '[a,b|c]' which suggests lists of the form '[a,b|c]' have limited use.
  Note that the setup used here facilitates identifying various basic types including integers, floats,
  strings, as well as lists. This allows for a limited implementation of type checking at compile time.
  TODO insert text about variables. 
 8. Character code lists have not been included in this grammar (cf. Standard 6.3.7).
 9. ANTLR precludes the use of (automatically) generated lexer tokens such as 'is', 'rem' and 'mod' as
  e.g. constant names. They are treated as 'reserved keywords' and cannot be (re)used as identifiers.
10. Strings have only limited support. Newlines are allowed within strings. No support for back quoted strings (`...`).
  escape sequence chars limited support, eg for \" \` \n \r etc.
  But no support for exotic number notations.
11. It is not clear from the ISO standard how to  parse and print curly bracket notation (eg {1,2,3}). 
  It is not even clear to me whether {} is a good term, I think it is not but SWI still accepts it as term. 
12. dynamic/1 is a fx operator as it is defined in SWI Prolog (but not in the ISO). 

 * Additional remarks.
 - As noted by others, existing Prolog implementations do not completely conform to the ISO/IEC 13211-1 standard.
   See for a discussion of the standard e.g. Roberto Bagnara, Is the ISO Prolog Standard Taken Seriously?, in:
   The Association for Logic Programming Newsletter 12, 1 (February 1999), pp. 10-12.
   A few examples to illustrate some of the deviations in practice:
   - Existing Prolog implementations allow clauses such as 'p :- !, q' whereas the Standard requires brackets
     around the cut operator (i.e. only 'p :- (!), q' is valid; cf. 6.3.3 and [Bagnara, 1999] above).
   - SWI Prolog (http://www.swi-prolog.org/) allows compound terms with argument terms that have priority>=999
     (e.g. a term1200 can be an argument (the Standard only allows terms with priority<999; cf. 6.3.3.1).
   - The predefined operator table (Figure 9.2) in: Pierre Deransart, AbdelAli Ed-Dbali, Laurent Cervoni, 1996,
     Prolog: The Standard - Reference Manual, Springer-Verlag differs from the ISO Standard Table 5, p. 16.
   - Parsing priority issues in SWI Prolog parses. For instance -4**4 parses as (-4)**4. This is incorrect,
    because - is an operator of priority 200 and ** (which is xfx of prio 200) can only take operands of
     priority less than 200. The only correct way to parse -4**4 is as -(4**4) which would give -256.
   SWI will parse as expected if you write "- 4 ** 4" (with whitespace before 4).
   Also note that SWI will PRINT OUT (-4)**4 as -4**4 which does in turn NOT parse as (-4)**4 !!
  In TUProlog similar issues play but the details are different; it always incorrectly parses -4**4 as (-4)**4, 
  irrespective of spaces after the '-'. 
   - gprolog (http://www.gprolog.org/manual/html_node/gprolog037.html)
   seems to have a number of non-iso operators:
    ':' as a priority 600 xfy operator, 
    '+' priority 200 yfx 
    '\+' fy at priority 900.
*/
