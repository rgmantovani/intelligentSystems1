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

  # calcular a distancia de query para cada padrao do dataset
  # calcular os pesos para cada instancia
  # retornar os k mais proximos
  # se existe algum exemplo identifico a query, retorno a classe dele
  # senão, computa a media ponderada dos mais proximos
  # ...
}

#----------------------------------------------------------------------------------------
# DWNN Discreto (Classificação)
# Parametros:
#   - dataset: dados a serem trabalhados
#   - query: dado a ser "classificado"
#   - k: numero de instancias a serem verificadas
#----------------------------------------------------------------------------------------

dwnnDiscrete = function(dataset, query, k = 3){

  # calcular a distancia de query para cada padrao do dataset
  # calcular os pesos para cada instancia
  # retornar os k mais proximos
  # calcular das distancias para cada classe
  # somando os pesos de cada instancia para cada classe
  # retornando o indice da classe com maior soma
  # ...
}

#----------------------------------------------------------------------------------------
#----------------------------------------------------------------------------------------
