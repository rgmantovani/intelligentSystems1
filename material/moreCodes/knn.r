
knn <- function(experiences, query, k = 3) {

	nAttrs = ncol(experiences)-1
	class = ncol(experiences)

	distance = c()

	for (i in 1:nrow(experiences)) {
		distance = c(distance,
		      sqrt(sum((experiences[i,1:nAttrs] - query)^2)))
	}

	instanceIndices = sort.list(distance)[1:k]
	candidates = experiences[instanceIndices, class]

	occurrences = unique(candidates)
	total = rep(0, length(occurrences))

	for (j in 1:length(occurrences)) {
		total[j] = sum(occurrences[j] == candidates)
	}

	idClass = occurrences[sort.list(total, dec=T)[1]]

	idClass
}
