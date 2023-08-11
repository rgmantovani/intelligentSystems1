library(OpenML)
library(mlr)
# library(parallelMap)

# Starting parallelization in mode=socket with cpus=2.
# parallelStartSocket(8)

# set the seed to make this experiment reproducible.
set.seed(123, "L'Ecuyer")

# Create a task
data = getOMLDataSet(data.id = 54)$data
task = makeClassifTask(data = data, target = "Class")

# Create a search space
# see
# helpLearner('classif.randomForest')
# helpLearnerParam('classif.randomForest')
ps = makeParamSet(
  makeIntegerParam("ntree", lower = 10, upper = 600),
  makeIntegerParam("mtry", lower = 1, upper = 10),
  makeIntegerParam("maxnodes", lower = 2, upper = 20),
  makeIntegerParam("nodesize", lower = 5, upper = 500)
)
print(ps)
cat("------------------------------------------------------------\n\n")

### Random search with 50 iterations
ctrl = makeTuneControlRandom(maxit = 50L)

# 10-fold CV
rdesc = makeResampleDesc("CV", iters = 10L, stratify = TRUE)

# metric balance error rate 
me = ber

# Performing the tuning
res = tuneParams("classif.randomForest", task = task, resampling = rdesc,
                 par.set = ps, measure = me, control = ctrl)
print(res)
cat("------------------------------------------------------------\n\n")

# Stopped parallelization. All cleaned up.
# parallelStop()

res_data = generateHyperParsEffectData(res, partial.dep = TRUE)
res_plt = plotHyperParsEffect(res_data, x = "iteration", y = "ber.test.mean",
                              plot.type = "line",
                              partial.dep.learn = 'regr.randomForest')
print(res_plt)
