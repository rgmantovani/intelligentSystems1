# -------------------------------------------------------------------------------------------------
# import required packages/libraries
# -------------------------------------------------------------------------------------------------

# -------------------------------------------------------------------------------------------------
# A class for a Genetic Algorithm 
# -------------------------------------------------------------------------------------------------

class GeneticAlgorithm:
    
    # attributes
    population = []
    # number of Generations to execute
    numberOfGenerations = 100
    # stop GA execution is no improvement is observed after some generations
    stopEarlyCriteria = 10
    # population size
    populationSize = 100
    # mutation rate
    mutationRate = 0.05
    # best individual(s) returned after the GA is executed
    bestIndividual = []
    # best fitness value(s) obtained by the best individuals 
    bestFitness = []
    # elitism?
    
    # constructor
    def __init__(self):
        pass
    
    # generate the initial population
    def generateInitialPopulation(self):
        pass   
    
    # fitness function to evaluate an individual
    def fitnessFunction(self):
        pass
    
    # receive an individual and evaluate its fitness
    def evaluateIndividual(self):
        pass 
    
    # given a population, selects two parents for crossover
    def selectParents(self):
        pass
    
    # given two parents, generate two children recombining them
    def generateChildren(self):
        pass
    
    # selects an individual and apply a mutation
    def mutationOperator(self):
        pass

    # run GA
    def execute(self):
        pass
    
# -------------------------------------------------------------------------------------------------
# -------------------------------------------------------------------------------------------------