# Nesse exemplo usaremos o dataset "dresses-sales", armazenado no OpenML
# Este dataset descreve o sono de 62 especies de mamiferos
# Para manipula-lo usaremos o correspondente id no repositorio (510)
# https://www.openml.org/d/510

# carregar os pacotes
library(OpenML)      # para acessar e obter o dataset
library(mlr)         # para manipularmos os dados    
library(ggplot2)     # para visualizar o dataset

# loading dataset usando o pacote OpenML
data = getOMLDataSet(data.id = 510) 

# tambem poderia ser
# data = OpenML::getOMLDataSet(data.id = 510)

# o objeto "data" retorna muita informacao, vamos pegar apenas o dataset no slot $data
dataset = data$data 
View(dataset)

# Verificar os problemas que podemos ter com os dados
# 1 - atributos (features) desnecessarios -> ids
teste = apply(dataset, 2, unique)
unlist(lapply(teste, length))

# 'species' e um atributo do tipo identificador, 
# ele nao adiciona nada na resolucao do problema
ggplot(data = dataset) + 
  geom_point(mapping = aes(x = species, y = overall_danger_index)) + 
  theme(axis.text.x = element_text(angle = 90, hjust = 1)) 

# remove o id (todos as colunas menos a 1)
dataset = dataset[,-1]
View(dataset)

# 2 - verificar valores ausentes (NA)
any(is.na(dataset))
unlist(lapply(dataset, function(x) any(is.na(x))))

# quais os tipos dos dados de cada coluna
unlist(lapply(dataset, class ))

# coluna com dados ausentes
# Ex: slow_wave, gestation_time
dataset$gestation_time
dataset$slow_wave

# solucao simples: numericos --> mediana da coluna
obj = mlr::impute(obj = dataset, target = "overall_danger_index", 
             classes = list(numeric = imputeMedian()))

# Outras formas:
#imputeMax()             // imputa o maior valor
#imputeMean()            // imputa a media   
#imputeConstant(valor)        // impute um valor constante
#imputeMin()             // imputa o menor valor  
#imputeMode()            // imputa a moda (elemento mais frequente)

# checar se ainda tem valores ausentes (NAs)
new.dataset = obj$data
any(is.na(new.dataset))

# comparar os valores de antes e depois para o atributo 'slow_wave'
rbind(dataset$slow_wave, new.dataset$slow_wave)

# plotar o boxplot dos atributos
ggplot(data = new.dataset) + 
  geom_boxplot(mapping = aes(x = overall_danger_index, 
     y = slow_wave, group = overall_danger_index))

# 3 - Remover atributos correlacionados
View(new.dataset)

aux = new.dataset[,-ncol(new.dataset)]
df = cor(data.matrix(aux))
hc = caret::findCorrelation(df, cutoff = 0.80, verbose = FALSE)
new.dataset = new.dataset[, -c(hc)]
View(new.dataset)

# 4 - Normalizar os dados, deixando eles na mesma escala
norm.dataset = normalizeFeatures(obj = new.dataset, 
  target = "overall_danger_index", method = "standardize")

ggplot(data = norm.dataset) + geom_boxplot(mapping = aes(x = overall_danger_index, 
  y = slow_wave, group = overall_danger_index))

