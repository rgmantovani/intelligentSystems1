pai(josé, adão).
pai(adão, lucas).
pai(matheus, joaquin).
mae(ana, celia).

avo(X,Z) :- pai(X, Y), pai(Y, Z).

%avo(X, lucas)


