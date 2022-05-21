#                                                           Rapport du projet

##                                                           "Jeu GHOSTLAB"

Le projet consiste à programmer un serveur pour le jeu GHOST LAB, ainsi que des clients dont le but de chacun des joueurs est d’attraper le max de fantômes possibles tout en faisant face à des contraintes(obstacles) pour cela nous avons implémenté un serveur en Java et un client en C.

### Coté Serveur:

Il est constitué de plusieurs classes:

##### La classe Server:

C’est la classe principale qui comporte la fonction main où un objet de type PlayerHandler est créé pour chaque joueur et celui-ci s’occupe de la communication avec le joueur.

##### La classe ServerImpl:

Représente la base de données du serveur

##### La classe Game:

Représente une partie (Game), composée de l’identifiant, l’adresse ip et le port de multidiffusion de la partie, un boolean qui dit si elle a commencé et un autre qui dit si elle est finie,le labyrinthe associé, la liste des joueurs de la partie ainsi que la liste des joueurs de la partie qui n’ont pas encore envoyé de START, à chaque fois qu’un joueur envoie START, il est supprimé de cette liste, la partie peut commencer dès que la liste est vide. Ajoute et supprime un joueur d’une partie.

##### La classe PlayerHandler:

Reçoit les requêtes d’un client, traite ces requêtes et répond à celles-ci, pour chaque client un thread est associé pour gérer la communication avec le serveur.

##### La classe Player: 

Représente un joueur, elle est constituée des informations sur celui-ci telle que sa position( le numéro de ligne et de colonne),l’identifiant ainsi que le portUdp du joueur, le score représentant le nombre de fantômes capturés. 

##### La classe Ghost:

Représente un fantôme, elle contient la position du fantôme(numéro de ligne et de colonne), sa vitesse de déplacement (comprise entre 1 et 10) ainsi que le score associé à celui-ci (score= 2*speed). 

##### La classe Labyrinth:

Elle représente un labyrinthe, cette classe à été adaptée du code dans le [lien](https://bitbucket.org/c0derepo/prime-algo-maze-generation/src/master/src/common). Elle génère un labyrinthe pour chaque partie en utilisant l’algorithme de Prim, et place un nombre différent de fantômes dans chaque labyrinth, cette classe gère le déplacement des fantômes dans un intervalle de temps bien défini(chaque seconde, un fantôme est déplacé selon sa vitesse) dans le labyrinthe.Elle contient les méthodes qui permettent de déplacer un joueur dans le labyrinthe. 

##### La classe Utils:

Cette classe permet de vérifier la bonne syntaxe d’une requête et les caractéristiques pour chacun des champs contenus dans les messages .

### Coté Client:

Il a été réparti en 4 fichiers c selon les besoins du joueur dans le jeu: 

##### before_game_functions:

Ce fichier contient l'ensemble des requetes que le joueur peut envoyer avant de commencer une partie (créer une nouvelle partie ou intégrer une partie déja existante) et les réponses reçus de la part du serveur ainsi que leurs traitements (vérification de la syntaxe des messages du serveur).

##### client:

Représente le fichier principal (le joueur), qui contient la fonction main où la connexion avec le serveur est établie grace à la création d'une socketTCP avec celui-ci. Un thread est créé et ecoute en UDP tout message UDP venant du serveur. 



##### during_game_functions:

Cette partie du client est composé de toutes les requetes (Se déplacer au sein du labyrinthe, avoir des informations sur la partie telle que le nombre de joueurs présents dans la partie, communiquer avec d'autres joueurs de la même partie, Abandonner la partie) qu'un joueur peut envoyer au serveur durant le déroulement du jeu ainsi que les réponses du serveur et leurs traitements. La partie commence lorsque tous les joueurs de celle-ci envoient un START. 

Des Méthodes de traitement ont été implémentées pour avoir un client automatique .

##### utils:

Comporte trois fonctions: l'une permet de générer l'identifiant d'un joueur en resepectant les caractéres alpha numériques, la deuxiéme permet de générer un numéro de port composé de 4 caractéres numériques et la troisiéme fonction permet de compléter les numéros par des 0 pour avoir des numéros codés sur le nombre d'octets spécifié dans le sujet du projet. 





