
selectedPrototype <- function(object, prototypes) {
	
	dist = c()
	for (i in 1:nrow(prototypes)) {
		dist = c(dist, sqrt(sum((object - prototypes[i,])^2)))
	}

	closest = which.min(dist)[1]

	closest
}

kmeans <- function(dataset, k, epsilon = 0.001) {

	dataset = as.data.frame(dataset)

	# inicializar os k centroides (centros, prototipos)
	prototypes = dataset[sample(seq(1,nrow(dataset)), size=k),]
	prototypes = as.data.frame(prototypes)

	deviation = epsilon + 1
	while (deviation > epsilon) {
		deviation = 0
		# verificar quais objetos ou elementos estao proximos
		#	de cada um dos prototipos
		closest = c()
		for (i in 1:nrow(dataset)) {
			id = selectedPrototype(dataset[i,], prototypes)
			closest = c(closest, id)
		}

		# movimentacao dos prototipos
		for (i in 1:k) {
			rows = which(closest == i)

			if (length(rows) == 0) {
				prototypes[i,] = dataset[sample(seq(1,nrow(dataset)), size=1),]
			} else {
				nearest = as.data.frame(dataset[rows,])
				newPosition = colSums(nearest)/nrow(nearest)
				deviation = 
				    deviation +
				    sqrt(sum((newPosition - prototypes[i,])^2))
				prototypes[i,] = newPosition
			}
		}
	}

	ret = list()
	ret$prototypes = prototypes
	ret$closest = closest

	ret
}
