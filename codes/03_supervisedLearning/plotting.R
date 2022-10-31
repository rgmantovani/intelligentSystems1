# carrega o pacote ggplot2
library(ggplot2)

#mostra o dataset
 mpg
View(mpg)
?mpg
 
# plotando scatter plot com x = mpg$displ e y = mph$hwy
ggplot(data = mpg) + geom_point(mapping = aes(x = displ, y = hwy))

# adicionando a cor das classes
ggplot(data = mpg) + 
  geom_point(mapping = aes(x = displ, y = hwy, colour = class))

# tamanho dos pontos
ggplot(data = mpg) + 
  geom_point(mapping = aes(x = displ, y = hwy, size = class))

# tamanho dos pontos + cores
ggplot(data = mpg) + 
  geom_point(mapping = aes(x = displ, y = hwy, colour = class, size = class))

# shape -> forma dos pontos
# alpha -> transparencia

# grafico de barras
ggplot(data = mpg) + geom_bar(mapping = aes(x = fl, fill = fl))

# grafico de densidade
ggplot(data = mpg) +
  geom_density(mapping = aes(x = displ, group = class, colour = class, fill = class))

# histograma
ggplot(data = mpg) + geom_histogram(mapping = aes(x = displ), bins = 15)

# boxplot
ggplot(data = mpg) +
  geom_boxplot(mapping = aes(x = class, y = displ, group = class))
