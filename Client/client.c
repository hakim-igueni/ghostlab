#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <unistd.h>

#define LOCALHOST "127.0.0.1"
#define SERVER_PORT 4444
#define BUFFER_SIZE 1024

void recv_GAMES(int socket_fd, uint8_t *games)
{
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    // Recevoir le message "GAMES n***"
    int received_bytes = recv(socket_fd, buffer, 10, 0);
    if (received_bytes == -1)
    {
        perror("read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';

    // Assurer que le message commence par GAMES
    if (strncmp(buffer, "GAMES", 5) != 0)
    {
        printf("Erreur: Le message n'est pas du bon format\n");
        exit(EXIT_FAILURE);
    }

    // Récupération du nombre de parties
    uint8_t n, m, s;
    n = (uint8_t)buffer[6];
    printf("Nombre de parties : %d\n\n", n);

    // Récupupération des parties
    for (int i = 0; i < n; i++)
    {
        // Reinitiation du buffer
        memset(buffer, 0, BUFFER_SIZE);

        // Recevoir le message "OGAME m s***"
        // m le numéro de la partie et s le nombre de joueurs
        // TODO: Assurer que le message commence par OGAME et fini par ***
        received_bytes = recv(socket_fd, buffer, 12, 0);
        if (received_bytes == -1)
        {
            perror("read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        // printf("Le message envoyé par le serveur : %s\n", buffer);
        m = (uint8_t)buffer[6];
        s = (uint8_t)buffer[8];
        games[i] = m;
        printf("Partie %d : %d joueurs\n", m, s);
    }
}

void send_GAME_request(int socket_fd, uint8_t *games)
{
    char buffer[BUFFER_SIZE];
    // Envoi du message "GAME?***"
    printf("\n[---Demande de la liste des parties---]\n");
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "GAME?***\n");
    printf("Le message à envoyer au serveur : %s\n", buffer);
    int sent_bytes = send(socket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("send");
        exit(EXIT_FAILURE);
    }
    // Récupération des parties
    recv_GAMES(socket_fd, games);
}

void send_NEWPL_request(int socket_fd)
{
    // Envoi du message "NEWPL id port***" pour créer une nouvelle partie
    // TODO: Generer les username aléatoirement
    char *username = "username";
    char *port = "2121";
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "NEWPL %s %s***", username, port);
    int sent_bytes = send(socket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[NEWPL] send");
        exit(EXIT_FAILURE);
    }

    // Recevoir la reponse du serveur
    // Supposons que le serveur nous renvoie le message "REGOK m***"
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(socket_fd, buffer, 10, 0);
    if (received_bytes == -1)
    {
        perror("read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    if (strncmp(buffer, "REGOK", 5) == 0)
    {
        uint8_t m = (uint8_t)buffer[6];
        printf("L'identifiant de la nouvelle partie est %d\n", m);
    }
    else
    {
        puts(buffer);
        printf("L'inscription n'est pas prise en compte\n");
        exit(EXIT_FAILURE);
    }
}

int main()
{
    // Déclaration des variables
    int sent_bytes, received_bytes;
    char buffer[BUFFER_SIZE];
    uint8_t n, m, s;
    uint8_t games[255];

    // Adresse de la socket client
    struct sockaddr_in socket_adr = {
        .sin_family = PF_INET,
        .sin_port = htons(SERVER_PORT)};
    if (inet_pton(AF_INET, LOCALHOST, &socket_adr.sin_addr) != 1)
    {
        perror("inet_pton");
        exit(EXIT_FAILURE);
    };

    // Création de la socket client
    int socket_fd = socket(PF_INET, SOCK_STREAM, 0);
    // gestion d'erreur
    // if (socket_fd == -1)
    // {
    //     perror("socket");
    //     exit(EXIT_FAILURE);
    // }

    // Connexion au serveur
    int connect_status = connect(socket_fd, (struct sockaddr *)&socket_adr, sizeof(socket_adr));
    if (connect_status == -1)
    {
        perror("connect");
        exit(EXIT_FAILURE);
    }

    printf("[---Connexion au serveur réussie---]\n");
    recv_GAMES(socket_fd, games);

    // -------------------Créer une nouvelle partie-----------------------
    printf("\n[---Création d'une nouvelle partie---]\n");
    send_NEWPL_request(socket_fd);
    sleep(10);
    // -------------------Demande de la liste des parties-----------------------
    printf("\n[---Demande de la liste des parties---]\n");
    send_GAME_request(socket_fd, games);
    sleep(20);
    //     // ------------------Rejoindre une partie-------------------------
    //     // Envoi du message "REGIS id port m***" pour rejoindre une partie
    //     printf("\n[---Rejoindre une partie---]\n");
    //     memset(buffer, 0, BUFFER_SIZE);
    //     sprintf(buffer, "REGIS %d %d %d***\n", 87, 2121, liste_parties->id_partie);
    //     printf("Le message à envoyer au serveur : %s\n", buffer);
    //     sent_bytes = send(socket_fd, buffer, strlen(buffer), 0);
    //     if (sent_bytes == -1)
    //     {
    //         perror("send");
    //         exit(EXIT_FAILURE);
    //     }

    //     // Recevoir la reponse du serveur
    //     memset(buffer, 0, BUFFER_SIZE);
    //     received_bytes = recv(socket_fd, buffer, BUFFER_SIZE, 0);
    //     if (received_bytes == -1)
    //     {
    //         perror("read");
    //         exit(EXIT_FAILURE);
    //     }
    //     buffer[received_bytes] = '\0';
    //     printf("La réponse du serveur : %s\n", buffer);

    //     // ------------------Se désinscrire d'une partie-------------------------
    //     // Envoi du message "UNREG***"
    //     printf("\n[---Se désinscrire d'une partie---]\n");
    //     memset(buffer, 0, BUFFER_SIZE);
    //     sprintf(buffer, "UNREG***\n");
    //     printf("Le message à envoyer au serveur : %s\n", buffer);
    //     sent_bytes = send(socket_fd, buffer, strlen(buffer), 0);
    //     if (sent_bytes == -1)
    //     {
    //         perror("send");
    //         exit(EXIT_FAILURE);
    //     }

    //     // Recevoir la reponse du serveur
    //     memset(buffer, 0, BUFFER_SIZE);
    //     received_bytes = recv(socket_fd, buffer, BUFFER_SIZE, 0);
    //     if (received_bytes == -1)
    //     {
    //         perror("read");
    //         exit(EXIT_FAILURE);
    //     }
    //     buffer[received_bytes] = '\0';
    //     printf("La réponse du serveur : %s\n", buffer);

    //     // -------------Demande de la taille du labyrinthe-------------------
    //     // Envoi du message "SIZE? m***"
    //     printf("\n[---Demande de la taille du labyrinthe---]\n");
    //     memset(buffer, 0, BUFFER_SIZE);
    //     sprintf(buffer, "SIZE? %d***\n", liste_parties->id_partie);
    //     printf("Le message à envoyer au serveur : %s\n", buffer);
    //     sent_bytes = send(socket_fd, buffer, strlen(buffer), 0);
    //     if (sent_bytes == -1)
    //     {
    //         perror("send");
    //         exit(EXIT_FAILURE);
    //     }

    //     // Recevoir la reponse du serveur
    //     memset(buffer, 0, BUFFER_SIZE);
    //     received_bytes = recv(socket_fd, buffer, BUFFER_SIZE, 0);
    //     if (received_bytes == -1)
    //     {
    //         perror("read");
    //         exit(EXIT_FAILURE);
    //     }
    //     buffer[received_bytes] = '\0';
    //     printf("La réponse du serveur : %s\n", buffer);

    //     // -------------Demande de la liste des joueurs-------------------
    //     // Envoi du message "LIST? m***"
    //     printf("\n[---Demande de la liste des joueurs---]\n");
    //     memset(buffer, 0, BUFFER_SIZE);
    //     sprintf(buffer, "LIST? %d***\n", liste_parties->id_partie);
    //     printf("Le message à envoyer au serveur : %s\n", buffer);
    //     sent_bytes = send(socket_fd, buffer, strlen(buffer), 0);
    //     if (sent_bytes == -1)
    //     {
    //         perror("send");
    //         exit(EXIT_FAILURE);
    //     }

    //     // Recevoir la reponse du serveur
    //     memset(buffer, 0, BUFFER_SIZE);
    //     received_bytes = recv(socket_fd, buffer, BUFFER_SIZE, 0);
    //     if (received_bytes == -1)
    //     {
    //         perror("read");
    //         exit(EXIT_FAILURE);
    //     }
    //     buffer[received_bytes] = '\0';
    //     printf("La réponse du serveur : %s\n", buffer);
    //     nb_joueurs = atoi(buffer + 7);
    //     struct list_players *liste_joueurs = NULL;

    //     for (int i = 0; i < nb_joueurs; i++)
    //     {
    //         memset(buffer, 0, BUFFER_SIZE);
    //         received_bytes = recv(socket_fd, buffer, 11, 0);
    //         if (received_bytes == -1)
    //         {
    //             perror("read");
    //             exit(EXIT_FAILURE);
    //         }
    //         buffer[received_bytes] = '\0';
    //         printf("Le message envoyé par le serveur : %s\n", buffer);
    //         liste_joueurs = add_player(atoi(buffer + 5), liste_joueurs);
    //     }
    //     afficher_liste_players(liste_joueurs);

    //     // -------------Demande de la liste des parties-------------------

    //     // Recevoir la reponse du serveur
    //     memset(buffer, 0, BUFFER_SIZE);
    //     received_bytes = recv(socket_fd, buffer, BUFFER_SIZE, 0);
    //     if (received_bytes == -1)
    //     {
    //         perror("read");
    //         exit(EXIT_FAILURE);
    //     }
    //     buffer[received_bytes] = '\0';
    //     printf("La réponse du serveur : %s\n", buffer);
    //     n = atoi(buffer + 6);

    //     for (int i = 0; i < n; i++)
    //     {
    //         // Reinitiation du buffer
    //         memset(buffer, 0, BUFFER_SIZE);

    //         // Recevoir le message "OGAMES m s***"
    //         // m le numéro de la partie et s le nombre de joueurs
    //         received_bytes = recv(socket_fd, buffer, BUFFER_SIZE, 0);
    //         if (received_bytes == -1)
    //         {
    //             perror("read");
    //             exit(EXIT_FAILURE);
    //         }
    //         buffer[received_bytes] = '\0';
    //         printf("Le message envoyé par le serveur : %s\n", buffer);
    //         id_partie = atoi(buffer + 7);
    //         nb_joueurs = atoi(buffer + 9);
    //         liste_parties = add_partie(id_partie, nb_joueurs, liste_parties);
    //     }

    //     // Afficher la liste des parties
    //     afficher_liste_parties(liste_parties);

    //     close(socket_fd);
}