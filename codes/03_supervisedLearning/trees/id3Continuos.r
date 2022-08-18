#id3Continuous

source("id3.r")

folder.iris <- "/home/rgmantovani/Documents/Doutorado/Disciplinas/MachineLearning/codes/datasets/iris/"
dataset.iris <- read.table(file = paste(folder.iris, "iris.data",sep=''), sep=",", header=FALSE);

folder.tenis <- "/home/rgmantovani/Documents/Doutorado/Disciplinas/MachineLearning/codes/datasets/tenis/"
dataset.tenis <- read.table(file = paste(folder.tenis, "tenis.dat",sep=''), header=TRUE);


#print(dataset)
 
# browser()

disc.tenis <- discretize(dataset.tenis[,-1], 4)

disc.iris <- discretize(dataset.iris, 7)

#View(disc.tenis)
#browser()
#print(disc.tenis)
#browser()
#print("discretização ok ...")


#id3(dataset.tenis[,-1])
#id3(disc.tenis)

id3(disc.iris)

