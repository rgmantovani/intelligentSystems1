#----------------------------------------------------------------------------------------
# Implementação de dwnn - Distance-Weighted Nearest Neighbors
#----------------------------------------------------------------------------------------
#----------------------------------------------------------------------------------------
# DWNN Continuo (Regressão)
# Parametros:
#   - dataset: dados a serem trabalhados
#   - query: dado a ser "classificado"
#   - k: numero de instancias a serem verificadas
#----------------------------------------------------------------------------------------

dwnnContinuous = function(dataset, query, k = 3){

  answer = ncol(dataset)
  nAttrs = ncol(dataset) - 1

  #calcular a distancia de query para cada padrao do dataset
  distance = rep(0, nrow(dataset))
  for(i in 1:nrow(dataset)){
    distance[i] = sqrt(sum((dataset[i,1:nAttrs] - query)^2))
  }

  #calcula os pesos para cada instancia
  weights = 1/distance;

  #retornar os k mais proximos
  idx = sort.list(distance)[1:k]

  #se existe algum exemplo identifico a query, retorno a classe dele
  if(distance[idx[1]] == 0){
      idClass = dataset[idx[1],answer]
      return (idClass)
  }

  candidates = dataset[idx, answer]
  w = weights[idx]
  idClass = sum(candidates * w) / sum(w)

  return (idClass)

}

#----------------------------------------------------------------------------------------
# DWNN Discreto (Classificação)
# Parametros:
#   - dataset: dados a serem trabalhados
#   - query: dado a ser "classificado"
#   - k: numero de instancias a serem verificadas
#----------------------------------------------------------------------------------------

dwnnDiscrete = function(dataset, query, k = 3){

  answer = ncol(dataset)
  nAttrs = ncol(dataset) - 1

  # calcular a distancia de query para cada padrao do dataset
  distance = rep(0, nrow(dataset))
  for(i in 1:nrow(dataset)){
    distance[i] = sqrt(sum((dataset[i,1:nAttrs] - query)^2))
  }

  # calcular os pesos para cada instancia
  weights = 1/distance;

  # retornar os k mais proximos
  idx = sort.list(distance)[1:k]
  candidates = dataset[idx, answer]
  w = weights[idx]

  # calcular das distancias para cada classe
  total = rep(0, nrow(unique(dataset[answer])))

  # somando os pesos de cada instancia para cada classe
  for(k in 1:length(candidates)){
    index = candidates[k]
    total[index] = total[index]+ w[k]
  }

  # retornando o indice da classe com maior soma
  idClass = sort.list(total, dec = T)[1]

  return (idClass)
}

#----------------------------------------------------------------------------------------
# DWNN Contínuo com Função Radial (Regressão)
# Parametros:
#   - dataset: dados a serem trabalhados
#   - query: dado a ser "classificado"
#   - k: numero de instancias a serem verificadas
#   - sigma: desvio padrao da funcao de base radial
#----------------------------------------------------------------------------------------

dwnnRbfContinuous = function(dataset, query, k = 3, sigma = 1){

  answer = ncol(dataset)
  nAttrs = ncol(dataset) - 1

  #calcular a distancia de query para cada padrao do dataset
  distance = rep(0, nrow(dataset))
  for(i in 1:nrow(dataset)){
    distance[i] = sqrt(sum((dataset[i,1:nAttrs] - query)^2))
  }

  #calcula os pesos para cada instancia usando uma funcao de base radial
  weights = exp(-(distance^2)/2*sigma^2)

  #retornar os k mais proximos
  idx = sort.list(distance)[1:k]

  #soma ponderada dos candidatos
  candidates = dataset[idx, answer]
  w = weights[idx]

  idClass = sum(candidates * w) / sum(w)

  #retorna a classe
  return (idClass)
}

#----------------------------------------------------------------------------------------
# DWNN Discreto com Função Radial (Classificação)
# Parametros:
#   - dataset: dados a serem trabalhados
#   - query: dado a ser "classificado"
#   - k: numero de instancias a serem verificadas
#   - sigma: desvio padrao da funcao de base radial
#----------------------------------------------------------------------------------------

dwnnRbfDiscrete = function(dataset, query, k = 3, sigma = 1){

  answer = ncol(dataset)
  nAttrs = ncol(dataset) - 1

  #calcular a distancia de query para cada padrao do dataset
  distance = rep(0, nrow(dataset))
  for(i in 1:nrow(dataset)){
    distance[i] = sqrt(sum((dataset[i,1:nAttrs] - query)^2))
  }

  #calcula os pesos para cada instancia usando uma funcao de base radial
  weights = exp(-(distance^2)/2*sigma^2)

  #retornar os k mais proximos
  idx = sort.list(distance)[1:k]
  candidates = dataset[idx, answer]
  w = weights[idx]

  #calcula das distancias para cada classe
  total = rep(0, nrow(unique(dataset[answer])))

  # somando os pesos de cada instancia para cada classe
  for(k in 1:length(candidates)){
    index = candidates[k]
    total[index] = total[index]+ w[k]
  }

  # retornando o indice da classe com maior soma
  idClass = sort.list(total, dec= T)[1]

  return (idClass)
}

#----------------------------------------------------------------------------------------
#----------------------------------------------------------------------------------------
