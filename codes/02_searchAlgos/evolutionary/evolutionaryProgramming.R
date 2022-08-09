# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

evolutionaryProgramming = function(dist.matrix, ncities = nrow(dist.matrix),
	pop.size = 100, ngenerations = 1000) {

	ret = list()
	ret$avgFitness = c()
	ret$sdFitness = c()

	# cria e avalia a populacao inicial (fitness)
	population = NULL
	fitness = rep(0, pop.size)
	for (i in 1:pop.size) {
		population = rbind(population,
			sample(seq(1, ncities), size=ncities))
		fitness[i] = fitness.fn(candidate.solution = population[i,],
			distance.matrix = dist.matrix)
	}

	# itera o algoritmo por N geracoes
	for (i in 1:ngenerations) {

		# vetor de fitness para os filhos
		childFitness = rep(0, pop.size)
		childPopulation = NULL

		# produzindo os filhos (cada pai produz um filho)
		for (j in 1:pop.size) {

			child = population[j,]

			# mutacao -> reprod assexuada
			ids = sample(seq(1, ncities), size=2)
			aux = child[ids[1]]
			child[ids[1]] = child[ids[2]]
			child[ids[2]] = aux

			# armazenar esse filho na população de filhos
			childPopulation = rbind(childPopulation, child)

			# calcular o fitness do filho
			childFitness[j] = fitness.fn(candidate.solution = child,
				distance.matrix = dist.matrix)
		}

		# unificar as duas populacaoes (pais, filhos)
		singlePopulation = rbind(population, childPopulation)
		singleFitness    = c(fitness, childFitness)

		# ordena populacao decrescentemente, seleciona os N melhores
		id = sort.list(singleFitness, decreasing=T)[1:pop.size]
		population = singlePopulation[id,]
		fitness = singleFitness[id]

		ret$avgFitness = c(ret$avgFitness, mean(fitness))
		ret$sdFitness = c(ret$sdFitness, sd(fitness))
		cat("Geracao ", i, ":", mean(fitness), " - ", sd(fitness), "\n")
	}

	return(ret)
}

# -------------------------------------------------------------------------------------------------
# ------------------------------------------------------------------------------------------------
