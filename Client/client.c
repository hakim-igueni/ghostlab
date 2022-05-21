#include "utils.h"
#include "before_game_functions.h"
#include "during_game_functions.h"
// ------------------- LES MESSAGES TCP -------------------   //
/* Dans cette partie la socket qu'on spécifie est celle du serveur*/

// TODO: verfier a chaque fois que le message recu n'est pas GOBYE

//-----------------LES MESSAGE UDP-----------------//
/* Dans cette partie on spécifie la socket de la partie*/

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Usage: %s <SERVER_ADDRESS> <SERVER_PORT>\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    uint16_t server_port = (uint16_t)atoi(argv[2]);
    if (server_port == 0)
    {
        printf("Usage: %s <ADDRESS> <SERVER_PORT>\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    char *server_address = argv[1];
    // // Déclaration des variables
    // // int sent_bytes, received_bytes;
    uint8_t games[255];
    uint8_t n;
    uint16_t x, y;  // La postion du client
    uint16_t p = 0; // Le nombre de point
    char username[9];
    genarate_username(username, 8);
    char port[5];
    generate_port(port);
    int udpsocket_fd;
    // char buffer[BUFFER_SIZE];

    // Adresse de la socket client
    struct sockaddr_in socket_adr = {
        .sin_family = PF_INET,
        .sin_port = htons(server_port)};
    if (inet_pton(AF_INET, server_address, &socket_adr.sin_addr) != 1)
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

    // Scénario 1
    // -------------------Créer une nouvelle partie------------------------
    printf("\n[---Création d'une nouvelle partie---]\n");
    send_NEWPL_request(tcpsocket_fd, username, port);

    // -------------------Demande de la liste des parties------------------
    printf("\n[---Demande de la liste des parties---]\n");
    send_GAME_request(tcpsocket_fd, games, &n);

    // // // ------------------Se désinscrire d'une partie-----------------------
    // // printf("\n[---Se désinscrire d'une partie---]\n");
    // // send_UNREG_request(tcpsocket_fd);
    // // // sleep(10);

    // // // -------------------Demande de la liste des parties------------------
    // // printf("\n[---Demande de la liste des parties---]\n");
    // // send_GAME_request(tcpsocket_fd, games, &n);

    // // // ------------------Rejoindre une partie------------------------------
    // // printf("\n[---Rejoindre une partie---]\n");
    // // send_REGIS_request(tcpsocket_fd, "usernxme", "2131", games[0]);
    // // // sleep(10);

    // // // -------------------Demande de la liste des parties------------------
    // // printf("\n[---Demande de la liste des parties---]\n");
    // // send_GAME_request(tcpsocket_fd, games, &n);

    // // // -------------Demande de la taille du labyrinthe-------------------
    // // // printf("\n[---Demande de la taille du labyrinthe---]\n");
    // // // send_SIZE_request(tcpsocket_fd);
    // // // sleep(10);

    // // -------------Demande de la liste des joueurs-------------------
    // printf("\n[---Demande de la liste des joueurs---]\n");
    // send_LIST_request(tcpsocket_fd);
    // // sleep(10);

    // Start the game
    printf("\n[---Début de la partie---]\n");
    send_START_request(tcpsocket_fd, &udpsocket_fd, &x, &y, &p);
    // // TODO: Répondre aux messages UDP
    // // TODO: Répondre aux message GHOST (Partiellement)
    // // TODO: Régler les types uint16
    // memset(buffer, 0, BUFFER_SIZE);
    recv_UDP(udpsocket_fd, tcpsocket_fd, &x, &y, &p);
    // // sleep(10);

    // while (1)
    // {
    //     printf("Introduisez votre requete : ");
    //     fgets(buffer, BUFFER_SIZE, stdin);
    //     buffer[strlen(buffer) - 1] = '\0';

    //     printf("%s\n", buffer);
    // }

    close(tcpsocket_fd);
}