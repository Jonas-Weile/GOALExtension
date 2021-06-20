:-dynamic result/1,
	receivedInvitation/2,  % invitations that we received.
	sentInvitation/2, % invitations that we sent
	receivedAccept/2, % accepts that we received
	meeting/4. % meetings that all agreed on.

% meeting conflicts if there is a scheduled meeting that overlaps in time and shares at least one attendee.
% case 1: an existing meeting starts after the meeting being examined begins and begins before that meeting ends. 
meeting_conflict(Date, Time, Duration, Attendees) :- 
	meeting(Date2, Time2, Dur2, Att2), % there is some other meeting.
	not(intersection(Attendees,Att2,[])), % that shares one or more attendees, i.e. the intersection is *not* empty.
	before(Date, Time, Date2, Time2),
	ends(Date, Time, Duration, End_date, End_time),
	before(Date2, Time2, End_date, End_time).

% case 2: an existing meeting starts before the meeting being examined begins and ends after that meeting begins.
meeting_conflict(Date, Time, Duration, Attendees) :- 
	meeting(Date2, Time2, Dur2, Att2), % there is some other meeting.
	not(intersection(Attendees,Att2,[])), % that shares one or more attendees, i.e. the intersection is *not* empty.
	before(Date2, Time2, Date, Time),
	ends(Date2, Time2, Dur2, End_date, End_time),
	before(Date, Time, End_date, End_time).
	
% one date is before another if:
before(date(D1, M1, Y1), time(Hr1,Min1), date(D2, M2, Y2), time(Hr2,Min2)) :- Y1 < Y2. % it ocurs in an earlier year 
before(date(D1, M1, Y1), time(Hr1,Min1), date(D2, M2, Y2), time(Hr2,Min2)) :- Y1 = Y2, M1 < M2. % it occurs in an earlier month
before(date(D1, M1, Y1), time(Hr1,Min1), date(D2, M2, Y2), time(Hr2,Min2)) :- Y1 = Y2, M1 = M2, D1 < D2. % etc
before(date(D1, M1, Y1), time(Hr1,Min1), date(D2, M2, Y2), time(Hr2,Min2)) :- Y1 = Y2, M1 = M2, D1 = D2, Hr1 < Hr2.
before(date(D1, M1, Y1), time(Hr1,Min1), date(D2, M2, Y2), time(Hr2,Min2)) :- Y1 = Y2, M1 = M2, D1 = D2, Hr1 = Hr2, Min1 < Min2.

% computes the end date and time given a starting date, time and duration.
ends(date(D, M, Y), time(H:Min), duration(H_duration:M_duration), date(D_end, M_end, Y_end), time(H_end:Min_end)) :- 
	Hours is H + H_duration, Mins is Min + M_duration, 
	date_time_stamp(date(Y, M, D, Hours, Mins, 0, 0, cet, true), Stamp), 
	stamp_date_time(Stamp, date(Y_end, M_end, D_end, H_end, Min_end, _, _, _, _), 0).
	
% additional predicate definitions
del([], X, []).
del([X|T], X, T).
del([H|T], X, L) :- not(H = X), del(T, X, L1), L = [H|L1].
