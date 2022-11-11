# ----------------------------------------------------------
# ----------------------------------------------------------

source("skeletonKNN.R")

data = iris

treino = data[ c(1:30, 51:80, 101:130),]
teste  = data[-c(1:30, 51:80, 101:130),]

# prever a classe dos elementos do conjunto de teste
preds = c()

aux = lapply(c(1, 3, 5, 7, 9, 13), function(k) {
  cat("Para K = ", k, "\n")
  for(i in 1:nrow(teste)) {
    query = teste[i, ]
    preds[i] = kNNDiscreto(training = treino, query = query, k = k)
  }
  conf.mat = table(as.factor(teste$Species), as.factor(preds))
  acc = sum(diag(conf.mat))/sum(conf.mat)
  print(acc)
  return(acc)
})

# preds - predicoes feitas pelo KNN p conjunto de teste
# 7 0.0.9666667

# ----------------------------------------------------------
# ----------------------------------------------------------
