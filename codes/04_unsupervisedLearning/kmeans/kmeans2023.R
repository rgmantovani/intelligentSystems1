library(ggplot2)

SEED = 42

N = 35
set.seed(1)
x = runif(min = 0, max = 2, n= N)
set.seed(1+10)
y = runif(min = 0, max = 2, n= N)
df = data.frame(cbind(x,y))
K = 4
g = ggplot(df, aes(x = x, y = y)) + geom_point(size = 5)
# print(g)

# -------------------------
# -------------------------

euclidean = function(a, b) {
	return (sqrt(sum((a - b)^2)))
}

# -------------------------
# -------------------------

NashaKMeans = function(dataset, k = K) {

	# 1. Gerar k centroides
	cat("@ Gerando centroides: \n")
	set.seed(SEED)
	ids.centroides =  sample(x = 1:nrow(dataset), size = K)
	centroides = dataset[ids.centroides, ]
	print(centroides)
	
	# não considerando os pontos que ja foram escolhidos como centroides
	df = dataset[-ids.centroides,]	

	#iniciar os grupos
	grupos = as.list(as.data.frame(t(centroides))) 
	cat("@ Iniciando os grupos: \n")
	print(grupos)

	# 2. repetir para todos os exemplos
	for(i in 1:nrow(df)) {
		exemplo = df[i, ]
		# print(exemplo)
		cat("exemplo: ", unlist(exemplo))
		#  a. calcular distancia exemplo p centroides
		aux = lapply(1:nrow(centroides), function(k) {
		 	return(euclidean(centroides[k,], exemplo))
		})
		distances = unlist(aux)
		#  b. centroide mais perto 
		id.menor = which.min(distances)
		cat(" - Grupo: ", id.menor, "\n")
		#  c. add o exemplo no grupo do centroide mais perto
		 grupos[[id.menor]] = rbind(grupos[[id.menor]], exemplo)
		#  d. atualiza o centroide
		centroides[id.menor,] = colMeans(grupos[[id.menor]])
	} 	
	obj = list(grupos = grupos, centroides = centroides)
	return(obj)
}

# -------------------------
# -------------------------

ret = NashaKMeans(dataset = df, k = K)



aux = lapply(1:length(ret$grupos), function(i) {
	df = data.frame(ret$grupos[i])
	colnames(df) = c("x", "y")
	df$Grupos = i
	return(df)
})

df.kmeans = do.call("rbind", aux)

df.centroides = ret$centroides
df.centroides$Grupos = 5

df.full = rbind(df.kmeans, df.centroides)
df.full$Grupos = as.factor(df.full$Grupos)


g2 = ggplot(df.full, aes(x = x, y = y, colour = Grupos, 
	shape = Grupos)) + geom_point(size = 5)
g2
# ggsave(g2, filename = "teste1.pdf")


#TODO: avaliar se é bom ou ruim pela silhueta










# -------------------------
# -------------------------
