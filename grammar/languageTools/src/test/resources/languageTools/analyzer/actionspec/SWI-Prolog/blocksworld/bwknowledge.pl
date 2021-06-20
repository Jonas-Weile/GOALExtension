:- dynamic(on/2).

% only blocks can be on top of another object.
block(X) :- on(X, _).
% a block is clear if nothing is on top of it.
clear(X) :- block(X), not( on(_, X) ).
% the table is always clear.
clear(table).
% a tower is any non-empty stack of blocks that sits on the table.
tower([X]) :- on(X, table).
tower([X, Y| T]) :- on(X, Y), tower([Y| T]).
% a block is above another block if it sits on top of that block or
% it sits on another block that is above that block
above(X, Y) :- on(X, Y).
above(X, Y) :- on(X, Z), above(Z, Y).