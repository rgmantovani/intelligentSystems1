# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

#gerar problema do caixeiro viajante aleatoriamente
make.distances = function(dmin, dmax, ncities) {

	dist = matrix(0, nrow=ncities, ncol=ncities)

	for (i in 1:(ncities-1)) {
		for (j in (i+1):ncities) {
			dist[i,j] = runif(dmin, dmax, n=1)
			dist[j,i] = dist[i,j]
		}
	}
	return(dist)
}


	# Caixeiro Viajante
	#	- vetor
	#	- matrix
	#	- arvores

	# vetor, 5 cidades -> c(1, 2, 3, 4, 5, 1)

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

# funcao de fitness
fitness.fn = function(candidate.solution, distance.matrix) {

	total = 0
	candidate.solution = c(candidate.solution, candidate.solution[1])

	for (i in 1:(length(candidate.solution)-1)) {
		total = total + distance.matrix[candidate.solution[i],
    candidate.solution[i+1]]
	}

	ret = (1/total) # -total # exp(-total)
	return(ret)
}

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------
