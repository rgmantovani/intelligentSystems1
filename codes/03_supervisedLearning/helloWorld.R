# carregando o pacote ggplot2
library(ggplot2)

# acessando o dataset
mpg
View(mpg)

# contando numero de linhas do dataset
nrow(mpg)

#contando numero de colunas do dataset
ncol(mpg)

# principais caracteristicas do dataset
summary(mpg)

# acessar as colunas
# ... via indices
mpg[,1]

# ou nome
mpg$model

# acessando varias colunas 
#mpg[ , 1:4]
#mpg[, c(1,5,6,11)]

# acessar linha
mpg[1,]

# varias linhas 
# mpg[1:5, ]
#mpg[c(1,30,40,50), ]

#Para saber o que significa, nesse caso podemos ler o help
?mpg

# grafico para ver valores de algumas variaveis
plot(x = mpg$hwy, y = mpg$cyl)

#usando ggpplot
ggplot(mpg, aes(x = hwy, y = cyl)) + geom_point() + theme_bw()

# o que acontece se plotarmos class x drv
ggplot(mpg, aes(x = class, y = drv)) + geom_point() + theme_bw()

# Por que esse grafico nao seria util?
  
