#                                          Rapport du projet

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

Elle représente un labyrinthe, cette classe à été adaptée du code dans le lien ci-dessous:

[Labyrinth]: https://bitbucket.org/c0derepo/prime-algo-maze-generation/src/master/src/common

Elle génère un labyrinthe pour chaque partie en utilisant l’algorithme de Prim, et place un nombre différent de fantômes dans chaque labyrinth, cette classe gère le déplacement des fantômes dans un intervalle de temps bien défini(chaque seconde, un fantôme est déplacé selon sa vitesse) dans le labyrinthe.Elle contient les méthodes qui permettent de déplacer un joueur dans le labyrinthe. 

##### La classe Utils:

Cette classe permet de vérifier la bonne syntaxe d’une requête et les caractéristiques pour chacun des champs contenus dans les messages .

