# -------------------------------------------------------------
# -------------------------------------------------------------

grafo1 = { "A" : {"B" : 100, "C" : 125, "D" : 100, "E" : 75},
           "B" : {"A" : 100, "C" : 50, "D" : 75, "E" : 125},
           "C" : {"A" : 125, "B" : 50, "D" : 100, "E" : 75},
           "D" : {"A" : 100, "B" : 75, "C" : 100, "E" : 50},
           "E" : {"A" : 75, "B" : 125, "C" : 125, "D" : 50},
        }

# -------------------------------------------------------------
# -------------------------------------------------------------

grafo2 = { "A" : {"B" : 100, "C" : 125, "D" : 100},
           "B" : {"E" : 125, "F" : 125},
           "C" : {},
           "D" : {},
           "E" : {"H" : 75, "I" : 125,},
           "F" : {},
           "G" : {},
           "H" : {},
           "I" : {},
         }

# -------------------------------------------------------------
# -------------------------------------------------------------

grafo3 = { "A" : {"B" : 100, "C" : 125, "D" : 100},
           "B" : {"E" : 125, "F" : 125,},
           "C" : {"G" : 125, "H" : 125,},
           "D" : {"I" : 125, "J" : 125,},
           "E" : {"K" : 75, "L" : 125,},
           "F" : {"L" : 125, "M" : 125,},
           "G" : {"N" : 125,},
           "H" : {"O" : 125, "P" : 125,},
           "I" : {"P" : 125, "Q" : 125,},
           "J" : {"R" : 125,},
           "K" : {"S" : 125,},
           "L" : {"T" : 125,},
           "M" : {},
           "N" : {},
           "O" : {},
           "P" : {"U" : 125,},
           "Q" : {},
           "R" : {},
           "S" : {},
           "T" : {},
           "U" : {},
        }

# -------------------------------------------------------------
# -------------------------------------------------------------

def busca_retrocesso(grafo, inicial, objetivo):
  ciclo = False
  if inicial == objetivo:
    ciclo = True

  LE = [inicial]
  LNE = [inicial]
  BSS = []
  EC = inicial

  while len(LNE) != 0:
    print(f'LE: {LE}')
    print(f'LNE: {LNE}')
    print(f'BSS: {BSS}')
    print(f'EC: {EC}')

    if ciclo:
      if len(set(LE)) == len(grafo) and objetivo in list(grafo[EC].keys()):
        LE.insert(0, objetivo)
        return LE
    else:
      if EC == objetivo:
        return LE

    filhos_possiveis = len(list(grafo[EC].keys()))-sum(el in list(grafo[EC].keys()) for el in LE)
    print(f'N filhos {EC} possíveis: {filhos_possiveis}')
    if filhos_possiveis == 0:
      while len(LE) != 0 and EC == LE[0]:
        print(f'LE: {LE}')
        print(f'LNE: {LNE}')
        print(f'BSS: {BSS}')
        print(f'EC: {EC}')
        BSS.insert(0, EC)
        print(f'remove LE: {LE.pop(0)}')
        print(f'remove LNE: {LNE.pop(0)}')
        EC = LNE[0]

      LE.insert(0, EC)

    else:
      filhos = list(grafo[EC].keys())
      filhos.reverse()
      for filho in filhos:
        if filho not in BSS and filho not in LE and filho not in LNE:
          print(f"Adiciona filho: {filho}")
          LNE.insert(0, filho)

      if ciclo:
        EC = LNE.pop(0)
      else:
        EC = LNE[0]
      LE.insert(0, EC)

  return False

# -------------------------------------------------------------
# -------------------------------------------------------------
