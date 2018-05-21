# RMSP

Uruchomienie MongoDB

1. Pobierz i zainstaluj MongoDB: https://www.mongodb.org/downloads

2. W linii komend uruchom: mongod.exe (pod linuxem pewnie ./mongod)

3. Gotowe!


Konfiguracja rCallera:

1. pobierz i zainstaluj R 
https://www.r-project.org/

2. w consoli R pobierz biblioteke Runiversal (aby RCaller w ogole mogl dzialac) poleceniem:
install.packages("Runiversal", repos=c("http://cran.rstudio.com"),dep=T)

3. podmien sciezki w klasie RCallerCreator

4. Pobierz pakiety wpisujac komendy:
  install.packages("forecast", repos=c("http://cran.rstudio.com"),dep=T)
  install.packages("fGarch", repos=c("http://cran.rstudio.com"),dep=T)
  install.packages("tseries", repos=c("http://cran.rstudio.com"),dep=T)
	install.packages("randtests", repos=c("http://cran.rstudio.com"),dep=T)
