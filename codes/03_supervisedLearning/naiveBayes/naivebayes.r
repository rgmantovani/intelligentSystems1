# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------

naive = function(dataset, sample) {

	idClass = ncol(dataset)

	#possiveis hipoteses (valores de classes)
	possible_hyp = unique(dataset[,idClass])
	answers = rep(0, length(possible_hyp))

	# argmax - para cada hipotese faca...
	for (j in 1:length(possible_hyp)) {

		# computar a probabilidade do label (classe h). Ex: P(v_j) -> P(Não)
		answers[j] = sum(dataset[,idClass] == possible_hyp[j]) / nrow(dataset)

		# dada aquela hipotese, verifique os atributos
		# selectionando um subset de exemplos (subset), p fazer algumas contagens
		rows   = which(dataset[,idClass] == possible_hyp[j])
		subset = dataset[rows,]

		# calcular o produtório
		prod = 1
		for (i in 1:length(sample)) {
			# P(a_1 | Não) * ... * P(a_n | Não)
			prod = prod *sum(sample[i] == subset[,i]) / nrow(subset)
		}

		# computar a probabilidade condicional daquele valor de classe
		answers[j] = answers[j] * prod
	}

	# normalizar os valores de probabilidade obtidos
	answers = answers / sum(answers)

	# retornar as contagens, mas sem escolher qual é a classe (fazer isso externamente)
	ret = list(possible_hyp = possible_hyp, answers = answers)
	return(ret)

}

# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------
