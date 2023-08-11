library(OpenML)
library(mlr)
library(FSelector)
library(ggplot2)

# set the seed to make this experiment reproducible.
set.seed(123, "L'Ecuyer")

# Create a task
data = getOMLDataSet(data.id=1131)$data
data = data[-1] # removing the ID

cat("All features ....\n")
all.task = makeClassifTask(data = data, target = "Tissue")
print(all.task)
cat("-----------------------------------------------------\n\n")

# feature values
cat("Filtering ....\n")
fv = generateFilterValuesData(all.task, method = "information.gain")
print(fv)
cat("-----------------------------------------------------\n\n")

cat("4 best ....\n")
# Keep the 2 most important features
fil.abs.task = filterFeatures(all.task, fval=fv, abs=4)
print(fil.abs.task)
cat("-----------------------------------------------------\n\n")

cat("10% best ....\n")
# Keep the 25% most important features
fil.perc.task = filterFeatures(all.task, fval=fv, perc=0.10)
print(fil.perc.task)
cat("-----------------------------------------------------\n\n")

cat("threshold best ....\n")
# Keep all features with importance greater than 0.5
fil.thr.task = filterFeatures(all.task, fval=fv, threshold=0.5)
print(fil.thr.task)
cat("-----------------------------------------------------\n\n")

all.task$task.desc$id = 'all.task' 
fil.abs.task$task.desc$id = 'fil.abs.task' 
fil.perc.task$task.desc$id = 'fil.perc.task' 
fil.thr.task$task.desc$id = 'fil.thr.task' 
tasks = list(all.task, fil.abs.task, fil.perc.task, fil.thr.task)

# Create a list of learners
lrns = list(
  makeLearner("classif.knn", id = "knn"),
  makeLearner("classif.rpart", id = "rpart")
  # makeLearner("classif.randomForest", id = "randomForest")
)

### 10-fold cross validation
rdesc = makeResampleDesc("CV", iters = 10, stratify=TRUE)
me = list(bac)
bmr = benchmark(lrns, tasks, rdesc, me, show.info = TRUE)

rank_box = plotBMRBoxplots(bmr, measure = bac, style = "violin",
                           order.lrn = getBMRLearnerIds(bmr)) +
                           aes(color = learner.id) +
                           theme(strip.text.x = element_text(size = 8))
print(rank_box)
readline(prompt="Press enter: ")

print(bmr)
cat("--------------------------------------------------------\n\n")
