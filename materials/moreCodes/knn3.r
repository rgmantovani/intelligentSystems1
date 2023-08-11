
knn <- function(experiences, query, k = 3) {

	# qual o numero de atributos?
	nAttrs = ncol(experiences)-1

	# qual a coluna que contem a classe para cada instancia?
	class = ncol(experiences)

	# guardar as distancias neste vetor
	distance = c()

	# para cada instancia na base de experiencias
	for (i in 1:nrow(experiences)) {
		# computar a distancia entre os atributos da consulta (query)
		# e os atributos de cada instancia na base de conhecimento
		#	- Usando distancia Euclidiana (poderia ser outra)
		distance = c(distance,
		      sqrt(sum((experiences[i,1:nAttrs] - query)^2)))
	}

	# ordenando de maneira crescente (menor para maior)
	# e escolhendo as k instancias mais proximas
	instanceIndices = sort.list(distance, decreasing=FALSE)[1:k]

	# recuperando as classes das k instancias mais proximas
	candidates = experiences[instanceIndices, class]

	# quais classes ocorrem nessas k instancias mais proximas?
	occurrences = unique(candidates)
	total = rep(0, length(occurrences))

	# contabilizando (no vetor total) o numero de ocorrencias
	# de cada classe para os k vizinhos mais proximos
	for (j in 1:length(occurrences)) {
		total[j] = sum(occurrences[j] == candidates)
	}

	# ordenando de maneira decrescente (maior para menor) a fim
	# de selecionar a classe de maior ocorrencia considerando
	# os k vizinhos mais proximos
	idClass = occurrences[sort.list(total, decreasing=TRUE)[1]]

	idClass
}
