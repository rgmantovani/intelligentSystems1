
entropyMatrix <- function(adj) {
	prob = adj / rowSums(adj)
	H = -sum(prob[prob > 0] * log2(prob[prob > 0]))

	if (is.na(H)) {
		H = 0
	}

	return (H)
}

entropySeries <- function(series, nstates, eps = 0.000001) {

	sequence = seq(min(series), max(series)+eps, length=nstates+1)
	states = series

	# discretizar a serie temporal em estados
	for (i in 1:nstates) {
		idx = which(series >= sequence[i] & series < sequence[i+1])
		states[idx] = i
	}
	
	# criando uma matrix de adjacencia por transicao
	adj = matrix(0, nrow=nstates, ncol=nstates)
	entropy = c()
	for (i in 1:(length(states)-1)) {
		adj[states[i], states[i+1]] = 
			adj[states[i], states[i+1]] + 1

		entropy = c(entropy, entropyMatrix(adj))
	}

	ret = list()
	ret$series = series
	ret$states = states
	ret$entropy = entropy

	ret
}
