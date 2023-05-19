# -------------------------------------------------------

set.seed(42)
N = 100 # qtde de numeros que queremos separar
problema = sample(x = -1000:1000, size = N)

POP.SIZE = 10
GERACOES = 10 # -> 100

# -------------------------------------------------------
# -------------------------------------------------------

fitnessFunction = function(individual) {
	ids0 = which(individual == 0)
	fit = abs(sum(problema[ids0]) - sum(problema[-ids0]))
	return(fit)
}

# -------------------------------------------------------
# -------------------------------------------------------

tournamentSelection = function(population, fitness.values, k=3) {
	# selecionar aleatoriamente K individuos
	challengers = sample(x= 1:nrow(population), size = k)
	#print(challengers)
	id.min = which.min(fitness.values[challengers])
	#print(challengers[id.min])
	# retornar o melhor (menor valor de fitness)
	best = population[challengers[id.min],]
	return(best)
}

# -------------------------------------------------------
# -------------------------------------------------------

onePointCrossover = function(parentA, parentB) {
	CUTOFF = sample(x =(2:(N-1)), size = 1)
	child1 = c(parentA[1:CUTOFF], parentB[(CUTOFF+1):N])
	child2 = c(parentB[1:CUTOFF], parentA[(CUTOFF+1):N])
	children = list(child1 = child1, child2 = child2)
	return(children)
}

# -------------------------------------------------------
# -------------------------------------------------------

mutation = function(children, mut.prob = 0.01) {

	# aplicar mutação no child1
	for(i in 1:length(children$child1)) {
		prob = sample(x = 1:100, size = 1)
		if(prob <= (mut.prob*100)) {
			children$child1[i] = 1 - children$child1[i] 
		}
	}
	# aplicar mutação no child2
	for(i in 1:length(children$child2)) {
		prob = sample(x = 1:100, size = 1)
		if(prob <= (mut.prob*100)) {
			children$child2[i] = 1 - children$child2[i] 
		}
	}
	return(children)
}

# -------------------------------------------------------
# -------------------------------------------------------

#individuos = vetores binarios [N]
geneticAlgorithm = function(pop.size = POP.SIZE, 
	generations = GERACOES) {

	# 1. Gerar uma população aleatória (P) - ok
	population = matrix(0, nrow = POP.SIZE, ncol = N)
	fitness.values = array(0, dim = POP.SIZE)
	
	for(i in 1:POP.SIZE) {
		population[i,] = sample(x = c(0,1), size = N, replace = TRUE)
		# 2. Avalia o fitness dos individuos em P - ok
		fitness.values[i] = fitnessFunction(individual = population[i,])
	}
	
	# 3. Repetir, até que um critério de parada seja satisfeito
	
	for(k in 1:generations) {
		cat(" - Geração: ", k,  "\n")
	# 	 4. Repetir (POP.SIZE/2) vezes
		new.population = matrix(0, nrow = POP.SIZE, ncol = N)
		for(j in 1:(POP.SIZE/2)) {
	# 		a) selecionar 2 pais (pai 1, pai 2)
			parentA = tournamentSelection(population, fitness.values, k=3)
			parentB = tournamentSelection(population, fitness.values, k=3)

	#		b) fazer o crossover (filh0 1, filho 2)
			children = onePointCrossover(parentA, parentB)

	#       c) mutação (filho1), mutação no filho 2
			xmen = mutation(children, mut.prob = 0.01)
			idx1 = (2*j)-1
			idx2 = 2*j
	#       d) jogar em uma população Q
			new.population[idx1, ] = xmen$child1
			new.population[idx2, ] = xmen$child2
		}

	# 	 5. Substituir a população original pela nova (Q)
		population = new.population
	# 	 6. Avaliar o fitness da população nova (Q) 
		for(i in 1:POP.SIZE) {
			fitness.values[i] = fitnessFunction(individual = population[i,])
		}
		print(mean(fitness.values))
		print(min(fitness.values))
	}
	
	# retornar (melhor(es) individuos, populacao final)
}

# -------------------------------------------------------
# -------------------------------------------------------
