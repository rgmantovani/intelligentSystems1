

def geraFilhos(X):
    # descobrir a posicao do zero, e colocar e salvar em i e j (i,j)

    # avaliar as 4 possibilidades de movimento
    # 1 - cima
    filhos = []
    if(j-1 <= 0):
        filho1 = X
        filho1[i][j], filho1[i][j-1] = filho1[i][j-1],filho1[i][j]
        filhos.append(filho1)

    # 2 - direita
    if():
        filhos.append(filho2)
    # 3 - baixo
    if():
        filhos.append(filho3)
    # 4 - esquerda
    if():
        filhos.append(filho4)

    return (filhos)
