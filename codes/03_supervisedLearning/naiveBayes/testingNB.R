source("naivebayes.R")

# --------------------------------------------------------
# Testando NB com dataset cageorico
# --------------------------------------------------------


dataset = read.csv("categorical_data.csv", header=TRUE)

# prever a classe dos elementos do exemplo teste
sample = c("Ensolarado", "Fria", "Alta", "Forte")
pred = naiveBayes(dataset = dataset[ ,-1], sample = sample)
print(pred)


# --------------------------------------------------------
# --------------------------------------------------------

data = iris
treino = data[ c(1:30, 51:80, 101:130),]
teste  = data[-c(1:30, 51:80, 101:130),]
preds = naiveBayes(dataset = treino, sample = teste[1,-5])
print(preds)

# --------------------------------------------------------
# --------------------------------------------------------
