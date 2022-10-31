library(OpenML)
library(mlr)

# set the seed to make this experiment reproducible.
set.seed(123, "L'Ecuyer")

# List of all the available base learners
base.learners = list(
  makeLearner("classif.svm"),
  makeLearner("classif.randomForest"),
  makeLearner("classif.knn"),
  makeLearner("classif.naiveBayes"),
  makeLearner("classif.J48"),
  makeLearner("classif.rpart")
)

# Creates a mixed hyperspace with all the base learners
lrn = makeModelMultiplexer(base.learners)

# Defines the hyperparameter space considering options from different base learners
ps = makeModelMultiplexerParamSet(lrn,
  makeNumericParam(id = "gamma", lower = -12, upper = 12, trafo = function(x) 2^x),
  makeIntegerParam(id = "ntree", lower = 1L, upper = 500L),
  makeIntegerParam(id = "nodesize", lower = 1, upper = 20),
  makeIntegerParam(id = "k", lower = 1, upper = 50, default = 7),
  makeIntegerParam(id = "M", default = 2L, lower = 1L, upper = 50L),
  makeIntegerParam(id = "minsplit", lower = 1, upper = 8, trafo = function(x) 2^x),
  makeIntegerParam(id = "minbucket", lower = 0, upper = 8, trafo = function(x) 2^x)
)
print(ps)

# resampling strategy for tuning
rdesc = makeResampleDesc("CV", iters = 2L)

# ------------------------------------------------------
# single criteria optimization
# ctrl = makeTuneControlRandom(maxit = 10) # Random Search
# ctrl = makeTuneControlIrace(maxExperiments = 200L) # Irace

# res = tuneParams(lrn, iris.task, rdesc, par.set = ps, control = ctrl, s
# measure = ber, how.info = TRUE)
# ------------------------------------------------------

# multicriteria optimization
ctrl = makeTuneMultiCritControlRandom(maxit = 50L)

# Performing the tuning
res = tuneParamsMultiCrit(lrn, task = iris.task,
  resampling = rdesc, par.set = ps,
  measures = list(ber, timeboth), control = ctrl, show.info = TRUE)

g = plotTuneMultiCritResult(res, col = 'selected.learner')
g
