# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

generationBased = function(dist, k.childreen = 10,
	ncities = nrow(dist), population.size = 100, ngenerations = 1000) {

	# populacao de N individuos
	population = NULL
	fitness = rep(0, population.size)

	for (i in 1:population.size) {
		candidate  = sample(seq(1, ncities), size = ncities)
		population = rbind(population, candidate)
		fitness[i] = fitness.fn(candidate.solution = candidate,
			distance.matrix =  dist)
	}

	avgFitness = c()
	sdFitness  = c()

	for (i in 1:ngenerations) {

		child.population = NULL
		child.fitness = rep(0, k.childreen)

		for (j in 1:k.childreen) {

			pid = sample(seq(1, population.size), size=1)
			child = population[pid,]

			genes = sample(seq(1, ncities), size=2)
			aux   = child[genes[1]]
			child[genes[1]] = child[genes[2]]
			child[genes[2]] = aux

			child.fitness[j] = fitness.fn(candidate.solution =
				child, distance.matrix = dist)
			child.population = rbind(child.population, child)
		}

		for (j in 1:k.childreen) {

			selected = sample(seq(1, population.size), size = 1)
			if (child.fitness[j] > fitness[selected]) {
				population[selected,] =	child.population[j,]
				fitness[selected]     = child.fitness[j]
			}
		}

		avgFitness = c(avgFitness, mean(fitness))
		sdFitness  = c(sdFitness, sd(fitness))
		cat("Geracao ", i, ":", mean(fitness), " - ", sd(fitness), "\n")
	}

	last.population = cbind(population, fitness)
	ret = list(avgFitness = avgFitness, sdFitness = sdFitness,
		population = last.population)

	return(ret)
}

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------
