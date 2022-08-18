# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

# Modelo 01 - Steady State
steadyState = function(dist, ncities = nrow(dist),
	population.size = 100, ngenerations = 1000) {

	# cria uma populacao de N individuos
	population = NULL
	fitness = rep(0, population.size)

	# avalia a população inicial (fitness)
	for (i in 1:population.size) {
		candidate  = sample(seq(1, ncities), size = ncities)
		population = rbind(population, candidate)
		fitness[i] = fitness.fn(candidate.solution = candidate,
			distance.matrix = dist)
	}

	# to see population
	# cbind(population, fitness)

	# itera o algoritmo por N geracoes
	avgFitness = c()
	sdFitness  = c()

	for (i in 1:ngenerations) {

		# seleciona um pai aleatorio
		pid   = sample(seq(1, population.size), size=1)

		# cria um filho mudando o pai
		child = population[pid,]

		# seleciona dois genes (cidades) e inverte elas na cadeia
		genes = sample(seq(1, ncities), size=2)

		aux   = child[genes[1]]
		child[genes[1]] = child[genes[2]]
		child[genes[2]] = aux

		# avalia o fitness do filho criado
		child.fitness = fitness.fn(candidate.solution =
			child, distance.matrix = dist)

		# compara o novo filho c um individuo aleatorio da populacao
		selected = sample(seq(1, population.size), size = 1)

		# se o novo filho é melhor que o individuo, substitui na populacao
		if (child.fitness > fitness[selected]) {
			population[selected,] = child
			fitness[selected]     = child.fitness
		}

		# calcula as medidas de desempenho do algoritmo
		avgFitness = c(avgFitness, mean(fitness))
		sdFitness  = c(sdFitness, sd(fitness))
		cat("Geracao ", i, ":", mean(fitness), " - ", sd(fitness), "\n")
	}

	# TODO: save the optimization path
	last.population = cbind(population, fitness)
	ret = list(avgFitness = avgFitness, sdFitness = sdFitness,
		population = last.population)

	return(ret)
}

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------
