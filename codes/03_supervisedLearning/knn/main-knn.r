source("myKnn.r")

dataset = read.table("../datasets/iris/iris4knn.dat")

test = read.table("../datasets/iris/iris.knn.query")

#teste do knn
for(i in 1:nrow(test)){
  
  print("Padrão:")
  query =  test[i,]
  print(query)
  ad = knnDiscrete(dataset, query)
  ac = knnContinuous(dataset, query)
  print("Saida")
  print(c(ad, ac))
  
}
