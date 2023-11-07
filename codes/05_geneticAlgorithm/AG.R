
# ============================================
fitness = function(individuo, problema) {
	ids  = which(individuo == 0)
	setA = problema[ids]
	setB = problema[-ids]
	valor.fitness = abs(sum(setA) - sum(setB))
	return(valor.fitness)
}
# ============================================

selecaoTorneio = function(populacao, fitnessPopulacao,
	tamanhoPopulacao, k=3) {

	competidores = sample(x=1:tamanhoPopulacao, size = 3)
	# print(competidores)
	# print(fitnessPopulacao[competidores])
	melhor = which.min(fitnessPopulacao[competidores])
	pai1 = populacao[competidores[melhor],]

	competidores = sample(x=1:tamanhoPopulacao, size = 3)
	# print(competidores)
	# print(fitnessPopulacao[competidores])
	melhor = which.min(fitnessPopulacao[competidores])
	pai2 = populacao[competidores[melhor],]

	pais = list(pai1 = pai1, pai2 = pai2)
	return(pais)
}

# ============================================
mutacaoPontual = function(individuo, taxaMutacao) {

	# taxa de Mutacao = 1/N = 0.1
	# se vai mutar ou nao?
	probMutacao = runif(min = 0, max = 1, n = 1)
	if(probMutacao < taxaMutacao) {
		# print("Mutou!")
		# qual posicao mutar
		posicao = sample(x = 1:length(individuo), size = 1)
		# print(posicao)
		individuo[posicao] = as.integer(!(individuo[posicao]))
	} 
	return(individuo)
}

# ============================================
crossoverUmPonto = function(pai1, pai2) {

	tamanho = length(pai1)
	# ponto de corte aleatorio
	p = sample(x=1:(tamanho - 1), size = 1)

	filho1 = c(pai1[1:p], pai2[(p+1):tamanho])
	filho2 = c(pai2[1:p], pai1[(p+1):tamanho])

	filhos = list(filho1 = filho1, filho2 = filho2)
	return(filhos)
}

# ============================================


algoritmoGenetico = function(problema, tamanhoPopulacao = 20, 
	numeroGeracoes = 10, funcaoFitness = fitness, 
	funcaoSelecao   = selecaoTorneio, 
	funcaoCrossover = crossoverUmPonto, 
	funcaoMutacao   = mutacaoPontual, taxaMutacao) {

	# Gerar vetores com informacao da execucao do algoritmo
	mediaFitnessPopulacao  = array(data = 0, dim  = numeroGeracoes)  
	melhorFitnessPopulacao = array(data = 0, dim  = numeroGeracoes)

	# Gerar uma populacao inicial
	populacao =  matrix(data = 0, nrow = tamanhoPopulacao, ncol = N)
	fitnessPopulacao = array(data = 0, dim  = tamanhoPopulacao)
	for(i in 1:tamanhoPopulacao) {
		populacao[i,] = sample(x=0:1, size = N, replace = TRUE)
		fitnessPopulacao[i] = fitness(individuo = populacao[i,], 
				problema = problema)
	} 	

	primeiraPopulacao = populacao
	# print(primeiraPopulacao)
	
	# (do-while)Verifica se o objetivo foi satisfeito (criterios de parada)
	for(geracao in 1:numeroGeracoes) {

		mediaFitnessPopulacao[geracao] = mean(fitnessPopulacao)  
		melhorFitnessPopulacao[geracao] = min(fitnessPopulacao)
		cat(" * Geracao: ", geracao, " - Media: ", mean(fitnessPopulacao) , 
			"Melhor: ", min(fitnessPopulacao),"\n")

		# Repetir (tamanhoPopulacao/2) vezes	
		novaPopulacao =  matrix(data = 0, nrow = tamanhoPopulacao, ncol = N)
		for(k in 1:(tamanhoPopulacao/2)) {
			pais = selecaoTorneio(populacao = populacao, 
				fitnessPopulacao = fitnessPopulacao, 
				tamanhoPopulacao = tamanhoPopulacao, k = 3)
	#        	gera os filhos A e B, usando pai A e pai B
			filhos = crossoverUmPonto(pai1 = pais$pai1, pai2 = pais$pai2)
	#        	aplicar operador de mutacao no filho A, e filho B
			filho1 = mutacaoPontual(individuo = filhos$filho1, taxaMutacao = 1/N)
			filho2 = mutacaoPontual(individuo = filhos$filho2, taxaMutacao = 1/N)

			novaPopulacao[2*k-1, ] = filho1
			novaPopulacao[2*k,   ] = filho2
		}
	#	    Subtitui populacao pai pela populacao filha
		populacao = novaPopulacao
		# Avaliar o fitness da populacao
		fitnessPopulacao = array(data = 0, dim  = tamanhoPopulacao)
		for(i in 1:tamanhoPopulacao) {
			fitnessPopulacao[i] = fitness(individuo = populacao[i,], 
				problema = problema)
		} 

	}

	# Adicionando os valores da ultima populacao
	mediaFitnessPopulacao =  c(mediaFitnessPopulacao, mean(fitnessPopulacao))  
	melhorFitnessPopulacao = c(melhorFitnessPopulacao, min(fitnessPopulacao))

	# melhor individuo ultima populacao
	obj = list(populacao = populacao, primeiraPopulacao = primeiraPopulacao,
		fitnessPopulacao = fitnessPopulacao, mediaFitnessPopulacao = mediaFitnessPopulacao, 
		melhorFitnessPopulacao = melhorFitnessPopulacao)
	return(obj)
}

# ============================================
