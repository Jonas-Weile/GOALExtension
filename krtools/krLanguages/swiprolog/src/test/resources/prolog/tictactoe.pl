pos(Pos) :- between(1, 9, Pos).
free(Pos) :- pos(Pos, empty).
free(List, Pos) :- member(Pos, List), pos(Pos,empty).
corner(1).
 corner(3).
 corner(7).
 corner(9).
 center(5).
line(A, B, C) :- pos(A), pos(B), pos(C), B is A+1, C is B+1, 0 is C mod 3.
line(A, B, C) :- pos(A), pos(B), pos(C), B is A+3, C is B+3.
line(1, 5, 9).
 line(3, 5, 7).
winning_line(Player,A,B,C) :- me(P), line(A, B, C), pos(A, P), pos(B, P), pos(C, P).
winning_move(Player, Pos) :- line(A, B, C), pos(A, Pa), pos(B, Pb), pos(C, Pc),count([Pa,Pb,Pc], Player, 2), free([A, B, C], Pos).
possible_winning_move(Player, [A, B, C], Pos) :- line(A, B, C), pos(A, Pa), pos(B, Pb), pos(C, Pc),count([Pa,Pb,Pc], Player, 1), count([Pa,Pb,Pc], empty, 2), free([A, B, C], Pos).
fork(Player, Pos) :- possible_winning_move(Player, [A, B, C], Pos),possible_winning_move(Player, [D, E, F], Pos), intersection([A, B, C], [D, E, F], L), not(length(L, 3)).
count([], A, 0).
count([A|T], A, C) :- count(T, A, TC), C is TC+1.
count([B|T], A, C) :- not(A=B), count(T, A, C).
opponent(x) :- me(o).
opponent(o) :- me(x).
lastitem.