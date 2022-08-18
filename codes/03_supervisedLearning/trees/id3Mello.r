
dataEntropy <- function(dataset) {

	# Se restou apenas um exemplo, entropia = 0
	if (is.vector(dataset)) {
		return (0)
	}

	# calcular entropia dos dados
	answer = ncol(dataset)
	options = unique(dataset[,answer])

	counting = c()
	for (i in 1:length(options)) {
		counting = c(counting,
			     sum(dataset[,answer] == options[i]))
	}

	prob = counting / sum(counting)
	H = -sum(prob[!is.nan(prob)] * log2(prob[!is.nan(prob)]))

	H
}

informationGain <- function(dataset, attrId) {

	E_S = dataEntropy(dataset)

	# calcular o ganho
	# Panorama = c(Nub, Chuv, Ens)
	options = unique(dataset[,attrId])

	sum = 0
	for (i in 1:length(options)) {
		proportion = 
		   sum(dataset[,attrId] == options[i]) / nrow(dataset)
		rowIds = which(dataset[,attrId] == options[i])
		E_S_A_v = dataEntropy(dataset[rowIds,])
		sum  = sum + proportion * E_S_A_v
	}

	IG = E_S - sum

	IG
}

id3_test <- function(node, example) {

	if (node$selectedAttrId == -1) {
		return(node$answer)
	}
	
	value = example[node$selectedAttrId]
	childId = which(node$options == value)
	id3_test(node$children[[childId]], example)
}

# algoritmo de arvores de decisao (Quinlan)
id3 <- function(dataset) {

	dataset = as.matrix(dataset)

	root = list()
	root$data = dataset
	nAttrs = ncol(root$data)-1
	root$eligibleAttrs = seq(1, nAttrs)

	root = id3_recursive(root)

	root
}

id3_recursive <- function(node) {

	selectedAttrId = -1
	maxGain = 0

	# computando o ganho de informacao
	for (i in 1:length(node$eligibleAttrs)) {
		attrId = node$eligibleAttrs[i]
		gain = informationGain(node$data, attrId)
		if (gain > maxGain) {
			selectedAttrId = attrId
			maxGain = gain
		}
	}

	# definindo qual atributo foi selecionado
	node$selectedAttrId = selectedAttrId

	# condicao de parada do algoritmo, pois entropia = 0
	if (node$selectedAttrId == -1) {
		answerId = ncol(node$data)
		node$answer = unique(node$data[,answerId])[1]
		return (node)
	}

	# definindo as opcoes ou valores para aquele atributo que
	#	permitem definir os filhos
	node$options = unique(node$data[, node$selectedAttrId])
	node$children = list()

	for (i in 1:length(node$options)) {
		# montando arvore para filho
		newnode = list()
		thisOption = node$options[i]
		rowIds = which(node$data[,node$selectedAttrId] == thisOption)

		# realizando um corte nos exemplos
		newnode$data = node$data[rowIds,]
		newnode$eligibleAttrs = setdiff(node$eligibleAttrs, node$selectedAttrId)

		node$children[[i]] = newnode
	}

	for (i in 1:length(node$options)) {
		node$children[[i]] = id3_recursive(node$children[[i]])
	}

	return (node)
}
