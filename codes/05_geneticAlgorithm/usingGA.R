# -------------------------------------------------------
# -------------------------------------------------------

set.seed(42)

source("geneticAlgorithm.R")

N = 100 # qtde de numeros que queremos separar
problema = sample(x = -1000:1000, size = N)

POP.SIZE = 100
GERACOES = 2000

teste = geneticAlgorithm(problem = problema, pop.size = POP.SIZE, 
	generations = GERACOES)

# -------------------------------------------------------
# -------------------------------------------------------

library(ggplot2)

df = cbind(1:length(teste$average.fitness), teste$average.fitness)
df = as.data.frame(df)
colnames(df) = c("generation", "avgFitness")

g = ggplot(df, aes(x = generation, y = avgFitness))
g = g + geom_point() + geom_line()
print(g)

# -------------------------------------------------------
# -------------------------------------------------------
