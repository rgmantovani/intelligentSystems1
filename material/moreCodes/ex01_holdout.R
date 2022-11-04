# Carregando os pacotes necessarios para o script funcionar
library(OpenML)
library(mlr)
library(ggplot2)
library(reshape2)

# Pegar o dataset iris do OpenML (id = 61)
dataObj = getOMLDataSet(data.id = 61)
dataset = dataObj$data

# Caracteristicas gerais do dataset
summary(dataset)
colnames(dataset)[ncol(dataset)] = "Species"

# Plotar a distribuicao de classes
ggplot(data = dataset) + geom_bar(aes(x = Species, 
  colour = Species, fill = Species))

# criar uma taerfa de classificacao
task = makeClassifTask(data = dataset, target = "Species")
print(task)

# Iniciar um algoritmo para classificar algorithm
#algo = makeLearner(cl = "classif.randomForest", predict.type = "prob")
algo = makeLearner(cl = "classif.rpart")
print(algo)

# Dividir os dados do dataset em treino e teste
rdesc = makeResampleDesc("Holdout", split = 2/3)

# Medidas de desempenho para avaliar os resultados
measures = list(acc, bac)

# Rodar o algoritmo na tarefa e coletar os resultados
result = resample(learner = algo, task = task, resampling = rdesc,
  measures = measures, show.info = TRUE)
result

# mostrando o resultado
print(result$aggr)

# Checar as predicoes obtidas pelo algoritmo
result$pred
pred = getRRPredictions(res = result)
print(pred)

# tabela predicoes
table(pred$data[,2:3])

# visualizar as predicoes usando um heatmp
aux = pred$data
df2 = melt(aux, id.vars =c(1,4,5))
df2$id = as.factor(df2$id)
ggplot(data = df2) + geom_tile(aes(x = id, y = variable, fill = value)) + 
  theme(axis.text.x = element_text(angle = 90, hjust = 1)) + 
  scale_fill_manual(values = c("blue", "black", "red"))
