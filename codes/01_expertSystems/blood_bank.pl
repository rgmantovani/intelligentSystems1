%facts
%the 1st argument can donate to the 2nd argument
donate_to(o,o).
donate_to(o,a).
donate_to(o,b).
donate_to(o,ab).
donate_to(a,a).
donate_to(a,ab).
donate_to(b,b).
donate_to(b,ab).
donate_to(ab,ab).

%donor(name,bloodType)
donor(asyraf,a).
donor(naim,b).
donor(amir,ab).
donor(imran,o).

%donor has a disease
has(asyraf,hiv).
has(naim,hepatisis_b).
has(imran,tonsil).

%patient(name,bloodType)
patient(james,a).
patient(chong,b).
patient(muthu,o).
patient(drogba,ab).

%how many bags left for a particular blood type
stocks(a,10).
stocks(b,0).
stocks(ab,5).
stocks(o,38).

%if donor has these diseases, his blood will be unqualified to use
unqualified_disease(hiv).
unqualified_disease(hepatisis_b).
unqualified_disease(sifilis).

%rules
%to check what blood types are compatible with the patient’s blood type
compatibles(BLOODTYPE,PATIENT_NAME) :- patient(PATIENT_NAME,Y) , donate_to(BLOODTYPE,Y).

%to know what blood types are currently available according to the stocks of blood bag
currently_available(BLOODTYPE,PATIENT_NAME) :- compatibles(BLOODTYPE,PATIENT_NAME) , stocks(BLOODTYPE,QTY) , QTY>0.

%to test whether the patient’s blood type matches with the donor’s blood type
matches(DONOR_NAME,PATIENT_NAME) :- donor(DONOR_NAME,X) , compatibles(X,PATIENT_NAME).

%to see if a donor is qualified to donate
qualified_donor(DONOR_NAME) :- has(DONOR_NAME,DISEASE) , not(unqualified_disease(DISEASE)).

%to see if a donor’s blood type matches with the patient’s blood type and the donor is also qualified to donate
match_and_qualified(DONOR_NAME,PATIENT_NAME) :- matches(DONOR_NAME,PATIENT_NAME) , qualified_donor(DONOR_NAME).

%to determine the total bag of all blood types
total_bag(TOTAL) :- stocks(a,QTY_A),stocks(b,QTY_B),stocks(ab,QTY_AB),stocks(o,QTY_O),TOTAL is QTY_A + QTY_B + QTY_AB + QTY_O.

%to check which blood type has low stock (less than 10)
low_stocks(BLOODTYPE) :- stocks(BLOODTYPE,QTY),QTY < 10.
