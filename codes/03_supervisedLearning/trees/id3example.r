#################################################################################
# Uso do ID3
#################################################################################

source("id3.r")


folder <- "/home/rgmantovani/Documents/Doutorado/Disciplinas/MachineLearning/codes/datasets/tenis/"
dataset <- read.table(file = paste(folder, "tenis.dat",sep=''), header=TRUE);

# arrayBaseData <- read.table("train.csv", sep = ' ' , header=TRUE);
# arrayTestData <- read.table("test.csv" , sep = ',' , header=TRUE);

#print(dataset[])
data <- dataset[,-1]
# browser()
#print(data)

tree <- id3(data)
#print(tree)

example <- c("Chuvoso", "Quente", "Alta", "Fraco")

print(id3_test(tree, example))

#################################################################################
#################################################################################
