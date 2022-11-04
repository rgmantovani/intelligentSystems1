# carregando os pacotes necessarios
library(OpenML)
library(mlr)
library(ggplot2)

# Obter o dataset do OpenML
data_obj = getOMLDataSet(data.id=61)
dataset = data_obj$data

# Criar uma tarefa de classificacao
task = makeClassifTask(data = dataset, target = "class")

# Criar uma lista de algoritmos (learners)
lrns = list(
  makeLearner("classif.lda", id = "lda"),                    # LDA - algoritmo linear
  makeLearner("classif.svm", id = "svm"),                    # SVM
  makeLearner("classif.rpart", id = "rpart"),                # DT  - arvore de decisao
  makeLearner("classif.randomForest", id = "randomForest")   # RF  - random Forest 
)

# Definir o processo de validacao cruzada (10 particoes)
#rdesc = makeResampleDesc("CV", iters = 10, stratify = TRUE)
rdesc = makeResampleDesc(method = "RepCV", stratify = TRUE, rep = 10, folds = 10)

# Definir medidas de avaliacao
me = list(acc, bac)

# Rodar os algoritmos na tarefa definida
bmr = benchmark(learners = lrns, tasks = task, resamplings = rdesc, 
  measures = me, show.info = TRUE)
print(bmr)

# Plotar os resultados (boxplots)
plotBMRBoxplots(bmr, measure = bac, style = "violin",
  order.lrn = getBMRLearnerIds(bmr)) + aes(color = learner.id)

plotBMRBoxplots(bmr, measure = bac, style = "box",
  order.lrn = getBMRLearnerIds(bmr)) +
  aes(color = learner.id) 

# demais plots?
