# source("myDwnn.r")
#

# TODO: adapt to work with our code

# dataset = read.table("../datasets/iris/iris4knn.dat")
# test = read.table("../datasets/iris/iris.knn.query")
#
# v = c()
#
# #teste do knn
# for(i in 1:nrow(test)){
#   query =  test[i,]
#   #ad = dwnnRbfContinuous(dataset, query)
#   ad = dwnnRbfDiscrete(dataset, query)
#   v = c(v,ad)
# }
#
# print("Saida")
# print(cbind(test, v))
