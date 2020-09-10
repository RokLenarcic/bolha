# Bolha

Scraper za Bolho

Ali:

Download jar

https://github.com/RokLenarcic/bolha/releases/download/v0.0.2/bolha.jar

In pozenes z:

```
java -jar bolha.jar
```

plus opcije.

Ali pa pozenes s `clj` komando, 

instalacija https://www.clojure.org/guides/getting_started#_clojure_installer_and_cli_tools

```
clj -Sdeps '{:deps {bolha {:git/url "https://github.com/RokLenarcic/bolha.git" :sha "f5e69536457120c90d55c68235d1d1c9ada2212b"}}}' -m bolha
```

Ce opcije niso podane jih izpise. Primer opcij:

```
--host smtp.gmail.com --mail 'rok.lenarcic@gmail.com' --user rok.lenarcic --pass password --port 587 --tls --query lego
```
