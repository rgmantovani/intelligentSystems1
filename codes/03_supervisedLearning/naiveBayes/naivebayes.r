

naive <- function(dataset, sample) {

	idClass = ncol(dataset)
	possible_hyp = unique(dataset[,idClass])
	answers = rep(0, length(possible_hyp))

	# argmax
	# para cada hipotese faca...
	for (j in 1:length(possible_hyp)) {

		# P(v_j) -> P(Não)
		answers[j] = 
		    sum(dataset[,idClass] == possible_hyp[j]) /
			nrow(dataset)

		# dada aquela hipotese, verifique os atributos
		rows = which(dataset[,idClass] == possible_hyp[j])
		part = dataset[rows,]
		prod = 1

		for (i in 1:length(sample)) {
			# P(a_1 | Não) * ... * P(a_n | Não)
			prod = prod * 
				sum(sample[i] == part[,i]) / nrow(part)
		}

		answers[j] = answers[j] * prod
	}

	answers = answers / sum(answers)

	ret = list()
	ret$possible_hyp = possible_hyp
	ret$answers = answers

	ret
}
