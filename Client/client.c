#include "utils.h"
#include "before_game_functions.h"
#include "during_game_functions.h"
// ------------------- LES MESSAGES TCP -------------------   //
/* Dans cette partie la socket qu'on spécifie est celle du serveur*/

// TODO: verfier a chaque fois que le message recu n'est pas GOBYE

//-----------------LES MESSAGE UDP-----------------//
/* Dans cette partie on spécifie la socket de la partie*/

int main()
{
    // Déclaration des variables
    // int sent_bytes, received_bytes;
    // char buffer[BUFFER_SIZE];
    // uint8_t n, m, s;
    uint8_t games[255];
    uint8_t n;
    int udpsocket_fd;
    char buffer[BUFFER_SIZE];

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
    int tcpsocket_fd = socket(PF_INET, SOCK_STREAM, 0);
    if (tcpsocket_fd == -1)
    {
        perror("socket");
        exit(EXIT_FAILURE);
    }

    // Connexion au serveur
    int connect_status = connect(tcpsocket_fd, (struct sockaddr *)&socket_adr, sizeof(socket_adr));
    if (connect_status == -1)
    {
        perror("connect");
        exit(EXIT_FAILURE);
    }

    printf("[---Connexion au serveur réussie---]\n");
    recv_GAMES(tcpsocket_fd, games, &n);

    // -------------------Créer une nouvelle partie------------------------
    printf("\n[---Création d'une nouvelle partie---]\n");
    send_NEWPL_request(tcpsocket_fd, "username", "2121");
    // sleep(10);

    // -------------------Demande de la liste des parties------------------
    printf("\n[---Demande de la liste des parties---]\n");
    send_GAME_request(tcpsocket_fd, games, &n);
    // sleep(20);

    // ------------------Se désinscrire d'une partie-----------------------
    printf("\n[---Se désinscrire d'une partie---]\n");
    send_UNREG_request(tcpsocket_fd);
    // sleep(10);

    // -------------------Demande de la liste des parties------------------
    printf("\n[---Demande de la liste des parties---]\n");
    send_GAME_request(tcpsocket_fd, games, &n);

    // ------------------Rejoindre une partie------------------------------
    printf("\n[---Rejoindre une partie---]\n");
    send_REGIS_request(tcpsocket_fd, "usernxme", "2131", games[0]);
    // sleep(10);

    // -------------------Demande de la liste des parties------------------
    printf("\n[---Demande de la liste des parties---]\n");
    send_GAME_request(tcpsocket_fd, games, &n);

    // -------------Demande de la taille du labyrinthe-------------------
    // printf("\n[---Demande de la taille du labyrinthe---]\n");
    // send_SIZE_request(tcpsocket_fd);
    // sleep(10);

    // -------------Demande de la liste des joueurs-------------------
    printf("\n[---Demande de la liste des joueurs---]\n");
    send_LIST_request(tcpsocket_fd);
    // sleep(10);

    // Start the game
    printf("\n[---Début de la partie---]\n");
    send_START_request(tcpsocket_fd);
    recv_WELCO(tcpsocket_fd, &udpsocket_fd);
    memset(buffer, 0, BUFFER_SIZE);
    recv(udpsocket_fd, buffer, BUFFER_SIZE, 0);
    printf("[---Message recu après l'abonnement---]\n");
    printf("%s\n", buffer);
    // sleep(10);

    //

    close(tcpsocket_fd);
}