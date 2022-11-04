
euclidean <- function(x, q) {
	ret = sqrt(sum((x - q)^2))
	ret
}

knn <- function(base, query, k=3) {

	nAttrs = ncol(base)-1
	classAttr = ncol(base)

	distances = c()

	for (i in 1:nrow(base)) {
		distances = c(distances,
			      euclidean(base[i,1:nAttrs], query))
	}

	# ordenar as distancias
	ids = sort.list(distances)[1:k]
	classes = base[ids,classAttr]

	occurrences = unique(classes)
	total = c()

	for (i in 1:length(occurrences)) {
		total = c(total, sum(classes == occurrences[i]))
	}

	idClass = sort.list(total, decreasing=TRUE)[1]

	return (occurrences[idClass])
}
