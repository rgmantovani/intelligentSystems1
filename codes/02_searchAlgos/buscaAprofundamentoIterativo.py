execfile('buscaProfundidadeLimitada.py')

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

# Exemplo de uso com o mesmo grafo:
print("Caminho encontrado:", busca_aprofundamento_iterativo(grafo, 'A', 'F', 3))
# Saída:
# Tentando com limite: 0
# Tentando com limite: 1
# Tentando com limite: 2
# Caminho encontrado: ['A', 'C', 'F']