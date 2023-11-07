
source("AG.R")

set.seed(42)
N = 1000

numeroGeracoes = 50

problema = sample(x = -1000:1000, size = N,replace = TRUE)

solucao = algoritmoGenetico(problema = problema, numeroGeracoes = numeroGeracoes, 
	tamanhoPopulacao=100, taxaMutacao = 1/N)

# -------------------------------
# plot da melhor solucao e media por geracao
# -------------------------------

library(ggplot2)
ids = 0:numeroGeracoes
df = cbind(ids, solucao$melhorFitnessPopulacao, solucao$mediaFitnessPopulacao)
df = data.frame(df)
colnames(df) = c("geracao", "melhor", "media")
df.melted =  melt(df, id.vars = 1)

 # geracao variable  value

g = ggplot(df.melted, aes(x = geracao, y = value, group = variable, 
	color = variable, shape = variable)) + geom_point() + geom_line()
g 

