# ----------------------------------------------------------
# ----------------------------------------------------------

kNNDiscreto = function(training, query, k = 3) {

    features = training[ , -ncol(training)]
    target   = training[ , ncol(training)]

    # 1. calcular a distancia de query p todos as instancias
    # do treino
    distances = c()
    for(i in 1:nrow(features)) {
      distances[i] = sqrt(sum((features[i,] - query[-5])^2))
    }

    # 2. ordenar as distancias, e encontrar as K menores
    k.ids = order(distances)

    # 3. pegar a moda/maior numero de votos dos K mais proximos
    target.freq = target[k.ids[1:k]]

    # retorna o id da classe predita
    idClass = names(which.max(table(target.freq)))
    return(idClass)
}

# ----------------------------------------------------------
# ----------------------------------------------------------
