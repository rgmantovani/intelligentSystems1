selectCenter <- function(sample, centers, sigma, threshold) {
	
	id = -1

	if (is.null(centers))
		return (id)

	dist = c()
	for (i in 1:nrow(centers)) {
		dist = c(dist, sqrt(sum((centers[i,] - sample)^2)))
	}

	distClosest = min(dist)[1]
	id = which.min(dist)[1]

	act = exp(-distClosest^2 / (2*sigma^2))

	if (act < threshold)
		return (-1)

	return (id)
}

entropy <- function(ids, eps = 0.01) {

	nRbfs = length(unique(ids))
	adj = matrix(0, nrow=nRbfs, ncol=nRbfs)

	Htime = c()
	deltaHtime = c()
	oldH = 0

	for (i in 1:(length(ids)-1)) {

		# contagem das visitas
		from = i
		to = i+1
		fromState = ids[from]
		toState = ids[to]

		adj[fromState, toState] = adj[fromState, toState]+eps

		# calcular as probabilidades
		probs = adj / rowSums(adj)
		adj = probs

		lg = probs * log2(probs)
		H = -sum(lg[!is.nan(lg)])

		deltaHtime = c(deltaHtime, H - oldH)
		oldH = H

		Htime = c(Htime, H)
	}

	ret = list()
	ret$Htime = Htime
	ret$deltaHtime = deltaHtime

	ret
}

sonde <- function(dataset, alpha, sigma, threshold) {

	dataset = as.data.frame(dataset)
	centers = NULL

	closest = c()
	for (time in 1:nrow(dataset)) {

		cat(paste(time, " out of ", nrow(dataset), "\n"))
		
		sample = dataset[time,]
		id = selectCenter(sample, centers, sigma, threshold)

		if (id == -1) {
			centers = rbind(centers, sample)
			id = nrow(centers)
		} else {
			# se eu estiver proximo de um centro???
			centers[id,] = (1-alpha) * centers[id,] +
					alpha * sample
		}

		closest = c(closest, id)
	}

	#closest = c()
	#for (time in 1:nrow(dataset)) {
	#	sample = dataset[time,]
	#	closest = c(closest,
	#		   selectCenter(sample, centers, 
	#				sigma, threshold))
	#}

	ret = list()
	ret$centers = centers
	ret$closest = closest

	ret
}
