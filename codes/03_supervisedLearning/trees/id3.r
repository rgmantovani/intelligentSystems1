#################################################################################
#entropia de uma matriz
#################################################################################
entropyMatrix <- function(adj) {
  prob = adj / rowSums(adj)
  H = -sum(prob[prob > 0] * log2(prob[prob > 0]))
  
  if (is.na(H)) {
    H = 0
  }
  
  return (H)
}

#################################################################################
#discretizar um data frame
#################################################################################
discretize <- function(dataset, nstates ,eps = 0.000001){

  series = dataset
  
  #para todas as colunas do dataset
  for(k in 1:ncol(dataset)){
  
      #se coluna nao é um factor
      if(class(series[,k]) != 'factor'){
        
        sequence = seq(min(series[,k]), max(series[,k])+eps, length=nstates+1)
        
        # discretizar a serie temporal em estados
        for (i in 1:nstates) {
          idx = which(series[,k] >= sequence[i] & series[,k] < sequence[i+1])
          series[idx,k] = i
        }
        #series[,k] <- factor(series[,k],levels=nstates)
        series[,k] <- as.factor(series[,k])
        
        }else{
        print("já é factor")
      }
  }
    
  return (series)
}
  
#################################################################################
# calcula a entropia dos dados
#################################################################################

dataEntropy <- function(dataset) {

	# Se restou apenas um exemplo, entropia = 0
	if (is.vector(dataset)) {
		return (0)
	}#else if{
  #  return (0)
	#}

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

	return (H)
}

#################################################################################
# Ganho de informação de um atributo
#################################################################################
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

	return (IG)
}

#################################################################################
# função de consulta do id3
#################################################################################
id3_test <- function(node, example) {

	if (node$selectedAttrId == -1) {
		return(node$answer)
	}
	
	value = example[node$selectedAttrId]
	childId = which(node$options == value)
	id3_test(node$children[[childId]], example)
}

#################################################################################
# algoritmo de arvores de decisao (Quinlan)
#################################################################################
id3 <- function(dataset) {

	dataset = as.matrix(dataset)

	root = list()
	root$data = dataset
	nAttrs = ncol(root$data)-1
	root$eligibleAttrs = seq(1, nAttrs)

	root = id3_recursive(root)

	return (root)
}

#################################################################################
#id3 recursiva, auxiliar
#################################################################################
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

#################################################################################
#fim de arquivo
#################################################################################