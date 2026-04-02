# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------

def busca_profundidade_limitada(grafo, inicio, objetivo, limite):
    """
    Realiza busca em profundidade limitada.
    """
    # Pilha armazena: (nó atual, profundidade atual, caminho)
    pilha = [(inicio, 0, [inicio])]
    visitados = set()

    while pilha:
        (no, profundidade, caminho) = pilha.pop()
        
        if no == objetivo:
            return caminho
        
        # Se ainda não atingiu o limite, expande os filhos
        if profundidade < limite:
            for vizinho in grafo.get(no, []):
                if vizinho not in caminho: # Evita ciclos
                    pilha.append((vizinho, profundidade + 1, caminho + [vizinho]))
                    
    return None # Objetivo não encontrado dentro do limite

# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------

def busca_aprofundamento_iterativo(grafo, inicio, objetivo, max_profundidade):
    """
    Realiza busca em profundidade com aprofundamento iterativo.
    """
    for limite in range(max_profundidade + 1):
        print(f"Tentando com limite: {limite}")
        resultado = busca_profundidade_limitada(grafo, inicio, objetivo, limite)
        if resultado:
            return resultado
    return None

# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------

# Exemplo de uso:
grafo = {
    'A': ['B', 'C'],
    'B': ['D', 'E'],
    'C': ['F'],
    'D': [],
    'E': ['F'],
    'F': []
}

# Busca com limite 1 (não acha F)
print(busca_profundidade_limitada(grafo, 'A', 'F', 1)) # None

# Busca com limite 2 (acha F)
print(busca_profundidade_limitada(grafo, 'A', 'F', 2)) # ['A', 'C', 'F']

# Exemplo de uso com o mesmo grafo:
print("Caminho encontrado:", busca_aprofundamento_iterativo(grafo, 'A', 'F', 3))
# Saída:
# Tentando com limite: 0
# Tentando com limite: 1
# Tentando com limite: 2
# Caminho encontrado: ['A', 'C', 'F']

# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------