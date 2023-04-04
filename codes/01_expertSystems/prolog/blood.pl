% Doacao sangue
% --------------------------------

% Regras de doação
doar_para(o,o).
doar_para(o,a).
doar_para(o,b).
doar_para(o,ab).
doar_para(a,a).
doar_para(a,ab).
doar_para(b,b).
doar_para(b,ab).
doar_para(ab,ab).

% Doadores
doador(rafael, o).
doador(ana, a).
doador(jovem2, b).
doador(jovem3, ab).

% Pacientes (recebedores)
paciente(angelo, o).
paciente(madalena, a).
paciente(recb,b).
paciente(recab,ab).

% Regras
% verificar quais tipos de sangue são compativeis com um paciente
compativel(BLOODTYPE,PATIENT_NAME) :- paciente(PATIENT_NAME,Y), doar_para(BLOODTYPE,Y).

% verificar se os tipos sanguineos do doador e paciente sao compativeis
pode_doar(DONOR_NAME, PATIENT_NAME) :- doador(DONOR_NAME, X), compativel(X, PATIENT_NAME).
