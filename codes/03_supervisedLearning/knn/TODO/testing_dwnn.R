# source("myDwnn.r")


# TODO: adapt to work with our code

# dataset = read.table("../datasets/iris/iris4knn.dat")
# test = read.table("../datasets/iris/iris.knn.query")
#
# v = c()
#
# #teste do dwnn
# for(i in 1:nrow(test)){
#   query =  test[i,]
#   #ad = dwnnContinuous(dataset, query)
#   ad = dwnnDiscrete(dataset, query)
#   v = c(v,ad)
# }
#
# print("Saida")
# print(cbind(test, v))
