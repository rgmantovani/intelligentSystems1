
cost <- function(x) {
	x^2
}

pso <- function(numberOfParticles = 10, iterations = 100, omega = 0.9, phip = 0.7, phig = 0.3,
		lower = -100, upper = 100) {

	particles = rep(0, numberOfParticles)
	velocity  = rep(0, numberOfParticles)
	costlocalbest = rep(0, numberOfParticles)
	localbest = rep(0, numberOfParticles)
	globalbest = 0

	# inicializando particulas e seu custo inicial (melhor solucao ateh entao para a particula)
	for (i in 1: numberOfParticles) {
		particles[i] = runif(min=lower, max=upper, n=1)
		localbest[i] = particles[i]
		costlocalbest[i] = cost(particles[i])
	}

	# inicializando a melhor solucao global e a particula melhor
	idglobalcandidate = which(min(costlocalbest) == costlocalbest)[1]
	globalbest = particles[idglobalcandidate]

	#inicializando a velocidade das particulas
	for (i in 1: numberOfParticles) {
		velocity[i] = runif(min=-abs(upper-lower), max=abs(upper-lower), n=1)
	}

	for (i in 1:iterations) {

		# calcular velocidade e alterar posicao das particulas
		for (i in 1: numberOfParticles) {
			rp = runif(min=0, max=1, n=1)
			rg = runif(min=0, max=1, n=1)
			velocity[i] = velocity[i] * omega + phip * rp * (localbest[i] - particles[i])
				+ phig * rg * (globalbest - particles[i])
			particles[i] = particles[i] + velocity[i]

			# se posicao atual for melhor que alguma jah
			#	visitada pela particula, entaum fazer update
			if (cost(particles[i]) < costlocalbest[i]) {
				localbest[i] = particles[i]
				costlocalbest[i] = cost(particles[i])
			}
		}

		# verificar se alguma particula melhorou solucao global
		idglobalcandidate = which(min(costlocalbest) == costlocalbest)[1]
		if (particles[idglobalcandidate] < cost(globalbest)) {
			globalbest = particles[idglobalcandidate]
		}
	}

	print("Best")
	print(globalbest)
	cat(paste("Cost: ", cost(globalbest), "\n"))
}
