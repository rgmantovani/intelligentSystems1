# carregar os pacotes necessarios
library(OpenML)
library(mlr)
library(ggplot2)
library(PMCMR)

# Obter varios datasets
data1 = getOMLDataSet(data.id=61)
dataset1 = data1$data

data2 = getOMLDataSet(data.id=1557)
dataset2 = data2$data

data3 = getOMLDataSet(data.id=1499)
dataset3 = data3$data

data4 = getOMLDataSet(data.id=54)
dataset4 = data4$data

# Criar tarefas de classificacao, uma para cada dataset
task1 = makeClassifTask(id =  data1$desc$name, data = dataset1, target = "class")
task2 = makeClassifTask(id =  data2$desc$name, data = dataset2, target = "Class")
task3 = makeClassifTask(id =  data3$desc$name, data = dataset3, target = "Class")
task4 = makeClassifTask(id =  data4$desc$name, data = dataset4, target = "Class")
tasks = list(task1, task2, task3, task4)

# Criando uma lista de algoritmos (learners)
lrns = list(
  makeLearner("classif.lda", id = "lda"),
  makeLearner("classif.svm", id = "svm"),
  makeLearner("classif.rpart", id = "rpart"),
  makeLearner("classif.randomForest", id = "randomForest")
)

# Criando uma estrategia de validacao
# rdesc = makeResampleDesc("RepCV", folds = 10, reps = 10, stratify = TRUE)
rdesc = makeResampleDesc("CV", iters = 10, stratify = TRUE)

# Definindo as medidas de desempenho (avaliacao)
me = list(acc, bac)

# executando os experimentos
bmr = benchmark(lrns, tasks, rdesc, me, show.info = TRUE)
print(bmr)

# Plotando os resultados
plotBMRSummary(bmr = bmr)

plotBMRRanksAsBarChart(bmr = bmr)

plotBMRBoxplots(bmr, measure = bac, style = "box", pretty.names = FALSE, 
  order.lrn = getBMRLearnerIds(bmr)) + aes(color = learner.id) +
  theme(strip.text.x = element_text(size = 8))

plotBMRBoxplots(bmr, measure = bac, style = "violin", pretty.names = FALSE, 
  order.lrn = getBMRLearnerIds(bmr)) + aes(color = learner.id) +
  theme(strip.text.x = element_text(size = 8))

# Calcular teste estatistico e verificar a significancia dos resultados
g = generateCritDifferencesData(bmr, p.value = 0.05, test = "nemenyi")
plotCritDifferences(g) + coord_cartesian(xlim = c(-1,5), ylim = c(0,2))
#+ scale_colour_manual(values = c("lda" = "black", "svm" = "red", "rpart" = "blue", "randomForest" = "green"))
