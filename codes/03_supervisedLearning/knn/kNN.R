#----------------------------------------------------------------------------------------
# Implementação de knn
#----------------------------------------------------------------------------------------

#----------------------------------------------------------------------------------------
# KNN Discreto (Classificação)
# Parametros:
#   - dataset: dados a serem trabalhados
#   - query: dado a ser "classificado"
#   - k: numero de instancias a serem verificadas
#----------------------------------------------------------------------------------------

knnDiscrete = function(dataset, query, k = 3){

  answer = ncol(dataset)
  nAttrs = ncol(dataset) - 1

  #calcular a distancia de query para cada padrao do dataset
  distance = rep(0, nrow(dataset))
  for(i in 1:nrow(dataset)){
    distance[i] = sqrt(sum((dataset[i,1:nAttrs] - query)^2))
  }

  #retornar os k mais proximos
  idx = sort.list(distance)[1:k]
  browser()
  candidates = dataset[idx, answer]

  occurrences = unique(candidates)
  total = rep(0, length(occurrences))

  for (j in 1:length(occurrences)) {
    total[j] = sum(occurrences[j] == candidates)
  }

  idClass = occurrences[sort.list(total, dec=T)[1]]

  return (idClass)
}

#----------------------------------------------------------------------------------------
# KNN Contínuo (Regressão)
# Parametros:
#   - dataset: dados a serem trabalhados
#   - query: dado a ser "classificado"
#   - k: numero de instancias a serem verificadas
#----------------------------------------------------------------------------------------

knnContinuous = function(dataset, query, k = 3){

  answer = ncol(dataset)
  nAttrs = ncol(dataset) - 1

  #calcular a distancia de query para cada padrao do dataset
  distance = rep(0, nrow(dataset))
  for(i in 1:nrow(dataset)){
    distance[i] = sqrt(sum((dataset[i,1:nAttrs] - query)^2))
  }

  #retornar os k mais proximos
  idx = sort.list(distance)[1:k]
  candidates = dataset[idx, answer]

  value = sum(candidates)/k
  return (value)
}

#----------------------------------------------------------------------------------------
#----------------------------------------------------------------------------------------
