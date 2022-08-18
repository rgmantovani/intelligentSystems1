#
# Para testar
#
#	m = aco(matrices, going, coming, numberOfAnts=100, iterations=200, nestId=1, foodId=5)
#
#

generateMatrices <- function(numberOfVertices = 5, 
			probabilityOfInfinity = 0.5,
			eta = 0.000001) {

	dist     = matrix(0, nrow=numberOfVertices, 
			ncol=numberOfVertices)
	currpher = matrix(eta, nrow=numberOfVertices, 
			ncol=numberOfVertices)

	for (i in 1:(numberOfVertices-1)) {
		for (j in (i+1):numberOfVertices) {
			if (runif(min=0, max=1, n=1) 
				< probabilityOfInfinity) {
				dist[i, j] = dist[j, i] = Inf
			} else {
				dist[i, j] = dist[j, i] = 
					runif(min=1, max=1000, n=1)
			}
		}
	}

	ret = list()
	ret$dist = dist
	ret$currpher = currpher

	ret
}

walk <- function(ants, matrices, nestid, foodid, goingMatrix, comingMatrix, deltaT = 0.1) {

	newPheromone = matrix(0, nrow=nrow(matrices$currpher), 
			ncol=ncol(matrices$currpher))

	# formiga esta indo ou voltando?
	#	criar um vetor para avisar
	#	quando chegar no destino, trocar esse bit
	
	for (i in 1:length(ants$position)) {

		if (ants$direction[i] == 0) { # indo
			mydirection = goingMatrix[ants$position[i],]
		} else if (ants$direction[i] == 1) { # voltando
			mydirection = comingMatrix[ants$position[i],]
		}

		# obter informacoes
		mydistances = matrices$dist[ants$position[i],]
		mycurrpher = matrices$currpher[ants$position[i],]

		possibleIds = which(mydirection == 1)

		T = mycurrpher[possibleIds] / mydistances[possibleIds]
		probabilities = T / sum(T)

		if (length(probabilities) == 1) {
			idnext = possibleIds[1]
		} else {
			idnext = sample(possibleIds, prob=probabilities, size=1)
		}

		# contabilizar o feromonio novo devido ao
		#	caminhamento das formigas
		newPheromone[ants$position[i], idnext] = deltaT
		newPheromone[idnext, ants$position[i]] = deltaT

		cat(paste("ant ", i, " from ", ants$position[i], " to ", idnext, "\n"))

		# atualizar a posicao da formiga
		ants$position[i] = idnext

		if (idnext == nestid) {
			ants$direction[i] = 0 # indo
		} else if (idnext == foodid) {
			ants$direction[i] = 1 # voltando
		}
	}

	ret = list()
	ret$ants = ants
	ret$matrices = matrices
	ret$newPheromone = newPheromone

	ret
}

update <- function(matrices, newPheromone, evaporation = 0.1) {

	matrices$currpher = matrices$currpher * (1-evaporation) +
			newPheromone

	matrices
}

test <- function(m) {

	if (sum(rowSums(m) == 0) > 0 ||
		sum(colSums(m) == 0) > 0) {
		return (FALSE)
	}

	return (TRUE)
}

generateDirectionMatrix <- function(matrices, numberOfVertices, directionProbability) {

	m = matrix(0, nrow=numberOfVertices, ncol=numberOfVertices)

	while (test(m) == F) {

		print("Trying to generate a feasible graph")

		for (i in 1:numberOfVertices) {
			for (j in 1:numberOfVertices) {
				if (i != j & is.infinite(matrices$dist[i, j]) == F) {
					if (runif(min=0, max=1, n=1) < directionProbability) {
						m[i, j] = 1
					} else {
						m[i, j] = 0
					}
				}
			}
		}
	}

	m
}

printPath <- function(matrices, goingMatrix, comingMatrix, nestId, foodId) {
	print("Distances")
	print(matrices$dist)

	print("Going Matrix")
	print(goingMatrix)

	print("Coming Matrix")
	print(comingMatrix)

	print("Pheronome Matrix")
	print(matrices$currpher)

	idnext = nestId
	print("Going Path")
	cat(paste(idnext, "\n"))

	# considerar a goingMatrix
	while (idnext != foodId) {

		possibleIds = which(goingMatrix[idnext,] == 1)
		probs = matrices$currpher[idnext, possibleIds] / matrices$dist[idnext, possibleIds]

		idnext = possibleIds[which(max(probs) == probs)]

		cat(paste(idnext, "\n"))
	}

	idnext = foodId
	print("Coming-Back Path")
	cat(paste(idnext, "\n"))

	# considerar a comingMatrix
	while (idnext != nestId) {

		possibleIds = which(comingMatrix[idnext,] == 1)
		probs = matrices$currpher[idnext, possibleIds] / matrices$dist[idnext, possibleIds]

		idnext = possibleIds[which(max(probs) == probs)]

		cat(paste(idnext, "\n"))
	}

}

aco <- function(matrices = NULL, goingMatrix = NULL, 
		comingMatrix = NULL,
		numberOfVertices = 5, probabilityOfInfinity = 0.5,
		numberOfAnts = 100, nestId = 1, foodId = 5,
		deltaT = 0.5, evaporation = 0.1, 
		directionProbability = 0.5, iterations = 100) {

	# matrices
	if (is.null(matrices) == T) {
		matrices = 
		    generateMatrices(numberOfVertices, probabilityOfInfinity)
	}

	# generating the graph
	if (is.null(goingMatrix) == T) {
		goingMatrix = generateDirectionMatrix(matrices, numberOfVertices, directionProbability)
	}

	if (is.null(comingMatrix) == T) {
		comingMatrix = generateDirectionMatrix(matrices, numberOfVertices, directionProbability)
	}

	# ants
	ants = list()
	ants$position  = rep(nestId, numberOfAnts)
	ants$direction = rep(0, numberOfAnts)

	# iteracoes
	for (i in 1:iterations) {

		cat(paste("Running iteration ", i, "\n"))

		# walking...
		ret = walk(ants, matrices, nestId, foodId, goingMatrix, comingMatrix, deltaT)
		ants = ret$ants
		matrices = ret$matrices
		newPheromone = ret$newPheromone

		# updating pheromone
		matrices = update(matrices, newPheromone, evaporation)
	}

	# mostrar o caminho percorrido por meio da matrix de feromonio
	printPath(matrices, goingMatrix, comingMatrix, nestId, foodId)

	return (matrices)
}
