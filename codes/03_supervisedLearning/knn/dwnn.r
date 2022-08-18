# Radial-Basis Function Distance-Weighted Nearest Neighbors

euclidean <- function(x, q) {
	sqrt(sum((x-q)^2))
}

gaussianKernel <- function(dist, sigma) {
	exp(-dist^2/(2*sigma^2))
}

# Schefler, Statistics...
dwnn <- function(base, query, sigma = 1) {

	nAttrs = ncol(base)-1
	class = ncol(base)

	num = 0
	den = 0

	for (i in 1:nrow(base)) {
		dist = euclidean(base[i,1:nAttrs], query)
		w = gaussianKernel(dist, sigma)

		num = num + w * base[i, class]
		den = den + w
	}

	gox = num / den

	gox
}

test <- function(sigma=1) {
	b = read.table("../datasets/logistic/logistic-3.8.dat")
	test = read.table("../datasets/logistic/logistic-test.dat")
	ver = read.table("../datasets/logistic/logistic-test-verificar.dat")

	print(cbind(test, ver))

	input = test[1,]

	allResults = c()
	for (i in 1:10) {
		result = dwnn(b, input,sigma)
		allResults = c(allResults, result)
		input = result
	}

	ts.plot(cbind(ver))
	points(allResults, col=2)
}
