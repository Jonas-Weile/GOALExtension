:-dynamic fib/1.

fib(0, 0).
fib(X, Y) :- X > 0, fib(X, Y, _).
fib(1, 1, 0).
fib(X, Y1, Y2) :- X > 1, X1 is X - 1, fib(X1, Y2, Y3), Y1 is Y2 + Y3.