# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

# mutation operator
mutation = function(child, mutation.prob) {

	if (runif(min=0, max=1, n=1) < mutation.prob) {
		ids = sample(seq(1, length(child)), size=2)
		aux = child[ids[1]]
		child[ids[1]] = child[ids[2]]
		child[ids[2]] = aux
	}
	return(child)
}

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

# crossover operator
pmx4students = function(father, mother, ncities) {

	# ponto de corte
	singlePoint = sample(seq(1, ncities-1), size=1)

	# divide a mae no ponto de corte
	mp1 = mother[1:singlePoint]
	mp2 = mother[(singlePoint+1):ncities]

	# divide o pai no ponto de corte
	fp1 = father[1:singlePoint]
	fp2 = father[(singlePoint+1):ncities]

	# cria dois novos filhos
	child1 = rep(0, ncities)
	child2 = rep(0, ncities)

	child1[1:singlePoint] = mp1
	child2[1:singlePoint] = fp1

	# para o primeiro filho
	interrogation_index = c()
	int = intersect(mp1, fp2)

	for (k in 1:length(int)) {
		position = which(fp2 == int[k])
		position = position + length(mp1)
		interrogation_index =	c(interrogation_index,position)
	}

	child1[interrogation_index] = setdiff(fp1, intersect(mp1, fp1))
	interrogation_index = which(child1 == 0)
	child1[interrogation_index] = setdiff(fp2, intersect(child1, fp2))

	# para o segundo filho
	interrogation_index = c()
	int = intersect(fp1, mp2)

	for (k in 1:length(int)) {
		position = which(mp2 == int[k])
		position = position + length(fp1)
		interrogation_index =	c(interrogation_index,position)
	}

	child2[interrogation_index] =	setdiff(mp1, intersect(fp1, mp1))
	interrogation_index = which(child2 == 0)
	child2[interrogation_index] =	setdiff(mp2, intersect(child2, mp2))

	# retorna os novos filhos
	ret = list(singlePoint = singlePoint, child1 = child1, child2 = child2)
	return(ret)
}

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

geneticAlgorithm = function(dist.matrix, selection = "random", ncities = nrow(dits.matrix),
  pop.size = 100, ngenerations = 1000, mutation.prob = 0.02, elitism = 0, k=2) {

	if (pop.size %% 2 != 0) {
		return ("#naumehpar")
	}

	# criando a populacao inicial
	population = NULL
	for (i in 1:pop.size) {
		chromosome = sample(seq(1, ncities), size=ncities)
		population = rbind(population, chromosome)
	}

	ret = list()
	ret$avgFitness  = c()
	ret$sdFitness   = c()
	ret$bestFitness = c()
	ret$lastPopulation

	# itera o algoritmo por N iteracoes
	for (i in 1:ngenerations) {

		# calculando o fitness da populacao corrente
		fitness = rep(0, pop.size)
		for (j in 1:pop.size) {
			fitness[j] = fitness.fn(candidate.solution = population[j,],
				distance.matrix = dist.matrix)
		}

		ret$avgFitness  = c(ret$avgFitness, mean(fitness))
		ret$sdFitness   = c(ret$sdFitness, sd(fitness))
		ret$bestFitness = c(ret$bestFitness, max(fitness))

		# produzir N filhos
		childPopulation = NULL
		childFitness = c()

		for (j in 1:(pop.size/2)) {

			# operador de selecao
			ids = NULL
			if (selection == "random") {
				ids = sample(seq(1, pop.size), size=2)
			} else if (selection == "roulette") {
				ids = sample(seq(1,pop.size), prob=fitness, size=2)
			} else if (selection == "ranking") {
				ranking = fitness
				ids = sort.list(ranking, dec=T)
				value = 1000
				for (k in 1:pop.size) {
					ranking[ids[k]] = value
					value = value / 2
				}
				ids = sample(seq(1,pop.size), prob=ranking, size=2)
			} else if (selection == "tournament") {
				kIds = sample(seq(1,pop.size), size=k)
				fids = sort.list(fitness[kIds], dec=T)[1:2]
				ids = kIds[fids]
			}

			# PMX crossover/cruzamento dos Pais
			# Partially Matched Crossover
			mother = population[ids[1],]
			father = population[ids[2],]
			childreen = pmx4students(mother = mother, father = father,
				ncities = ncities)

			# aplicando operador de mutação
			child1 = mutation(child = childreen$child1, mutation.prob = mutation.prob)
			child2 = mutation(child = childreen$child2, mutation.prob = mutation.prob)

			# add os novos filhos a populacao de filhos
			childPopulation = rbind(childPopulation, child1)
			childPopulation = rbind(childPopulation, child2)

			# computa o fitness dos filhos
			fitch1 = fitness.fn(candidate.solution = child1, distance.matrix = dist.matrix)
			fitch2 = fitness.fn(candidate.solution = child2, distance.matrix = dist.matrix)
			childFitness = c(childFitness, fitch1, fitch2)
		}

		# alterar a prob de mutacao?  Rechenberg (1965)
		if (mean(childFitness) > mean(fitness) && mutation.prob < 0.5) {
			mutation.prob = mutation.prob * 2
		} else {
			mutation.prob = mutation.prob / 2
		}

		if (elitism > 0) {
			bestIds = sort.list(fitness, dec=T)[1:elitism]
			bests = population[bestIds,]

			# escolha aleatoria dos filhos
			selectedChildreen = sample(seq(1, pop.size), size=elitism)
			childPopulation[selectedChildreen,] = bests
		}

		population = childPopulation
		cat("Geracao ", i, ":", mean(fitness), " - ", sd(fitness), "\n")
	}

	return(ret)
}

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------
