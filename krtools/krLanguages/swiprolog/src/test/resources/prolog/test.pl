% input to test our prolog parser.
:- dynamic(foo/0).

aap(3).
beer(4).
beer( 4, 1).
aap(10),beer(20),kat(30).
kat % NOTE You can add comments ANYwhere, seems broader than ISO.
		(3, 2,1). % NOTE You can add whitespace also anywhere. Also not ISO conform
aap + 2 . /*% NOTE You need a space between the 2 and the '.'.
		% SWI can work without that space. CHECK ISO compliance please. */
aap(X):-beer(X),kat(X,Y), Y<2 .
:-(1,2,3). 
:-((1 , (2 , 3))).
:-((1 , 2) , 3).
:-(aap,beer).
[].
[1,2,3,4,5].
[1,2,X].
[1,[2,1,0],[]].
[[],[[]]].
[aap,beer].
>=(0,1).
[>=(0,1), failure].
aap.
beer.
aap. beer. % works as expected. 
{1,2,3}. % NOT SURE from ISO what this SHOULD return: {}((1,(2,3))) or {}((1,2,3))
	% or is it the same anyway?
% some nasty terms from the inria suite! 
[>=(2 + floot(1),5), type_error(evaluable, floot/1)].
[(asserta((bar(X) :- X)), clause(bar(X), B)), [[B , call(X)]]].
[asserta((foo :- 4)), type_error(callable, 4)]. 

a < -b. % extra brackets will appear around -b
a < -b+c. % becomes (a < (-(b) + c))
a < -(b+c). % becomes (a < -((b + c)))
(a < -((b + c))). % this is as brackety as it will get.
1 .
-1 .
- -1 .
- - - -1 .
-- 1.
e-h.
a*b*c.
a*b.
a+b+c.
a;b;c;d.
a;b;c->d;e.
1+2+3+4+5 .
a+(b+c)+(d+e).
1+2*3+3*5/4*4 .
1+2.0.
((a+((b)))).
-e.
(-e).
e - f.
e -g.
a+(b-c)- g*(d- ( - e -f )).
:-(1,aap(2),beer).
:- a :- b. % Not sure, I guess it should parse as :- (a:-b) which it does. SWI fails on it.
%a :- b :- c. % correctly fails: should give priority clash
[catch(number_chars(A,L), error(instantiation_error, _), fail), failure].
display_io(on), 
		  run_bip('47
		'), 
		  display_io(off).
[(catch(true, C, write('something')), throw(blabla)), system_error]. 
[bagof(X,Y^((X=1;Y=1);(X=2,Y=2)),S), [[S , [1, _, 2]]]].
<(3*2,7-1). % note that < will print out in infix notation after parsing.
% Also note that '<' is not allowed anymore in our parser.
<(1.0,1).
<(2 + floot(1),5).
[abolish(foo/(-1)), domain_error(not_less_than_zero,-1)].
[abolish(5/2), type_error(atom,5)].
%,(true,false). % idem. This even crashes the parser!
[=\=(3 * 2,7 - 1), failure]. 
3 * 2 =\= 7 - 1 . %OK, =\= binds less tight than - and *.
-4**a. % OK, only way to parse is as -(4**a).
(-4)**a.
X is 2^3^4 .
X is (2^3)^4 .
a*b mod d. % (a*b) mod d
a*b mod d*e rem f.
a*b+c.
a*(b+c).
d+a*(b+c). 
a+b+c+d-e/\f. % should ((((a + b) + c) + d) - e) /\ f
a+b*c*d-d*e mod f+g-h rem i. % combined test of two left-recursive operators.
:- \ aap.
aap,beer,kat.
beer:-1,2;3,4;5,6,!.
beer :-	% the same but now after parsing and printing in SWI
        (   a,
            b
        ;   c,
            d
        ;   e,
            f, !
        ).
		at(agent,[0,0],0), % initial position.
		orientation(agent,0,0).
		done(T,forward):-(tried(T,forward),Tnext is T+1, percept([_,_,null,_,_],Tnext)), % no bump thgen forward succeeded
		 % This is giving name clashes all the time.... Now we need a triedtodo or whatever name you would like....
		(triedtodo(T,A):-(done(T,A))).
		triedtodo(T,A):-(tried(T,A)).
a:-b,c,d.
assert1.	
assert((a:-b,c)).
assert2.
range(L,I,H) :- (L < H, L1 is L+1, range(L1, I, H)).

abs(E-B, F). % ERR: parser FAILS (but gives warning)
abs(E- B, F). % ERR: parser ignores E- WITHOUT EVEN WARNING
abs(E - B, F). % Parses correctly. 
% NOTE, we DO require WHITESPACE after a dot:
the+end. 