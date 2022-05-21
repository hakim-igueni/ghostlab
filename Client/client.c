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
    // TODO: -m manuel
    // TODO: -a auto
    //  TODO: -h help

    // Déclaration des variables
    uint8_t games[255];
    uint8_t n;
    uint16_t x, y;  // La postion du client
    uint16_t p = 0; // Le nombre de point
    char username[9];
    genarate_username(username, 8);
    char port[5];
    generate_port(port);
    int udpsocket_fd;
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    uint8_t m;
    uint16_t d;
    char d_completed[4];
    char message[201];

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

    // // Scénario 1
    // // -------------------Créer une nouvelle partie------------------------
    // printf("\n[---Création d'une nouvelle partie---]\n");
    // send_NEWPL_request(tcpsocket_fd, username, port);

    // // -------------------Demande de la liste des parties------------------
    // printf("\n[---Demande de la liste des parties---]\n");
    // send_GAME_request(tcpsocket_fd, games, &n);

    // // // // ------------------Se désinscrire d'une partie-----------------------
    // // // printf("\n[---Se désinscrire d'une partie---]\n");
    // // // send_UNREG_request(tcpsocket_fd);
    // // // // sleep(10);

    // // // // -------------------Demande de la liste des parties------------------
    // // // printf("\n[---Demande de la liste des parties---]\n");
    // // // send_GAME_request(tcpsocket_fd, games, &n);

    // // // // ------------------Rejoindre une partie------------------------------
    // // // printf("\n[---Rejoindre une partie---]\n");
    // // // send_REGIS_request(tcpsocket_fd, "usernxme", "2131", games[0]);
    // // // // sleep(10);

    // // // // -------------------Demande de la liste des parties------------------
    // // // printf("\n[---Demande de la liste des parties---]\n");
    // // // send_GAME_request(tcpsocket_fd, games, &n);

    // // // // -------------Demande de la taille du labyrinthe-------------------
    // // // // printf("\n[---Demande de la taille du labyrinthe---]\n");
    // // // // send_SIZE_request(tcpsocket_fd);
    // // // // sleep(10);

    // // // -------------Demande de la liste des joueurs-------------------
    // // printf("\n[---Demande de la liste des joueurs---]\n");
    // // send_LIST_request(tcpsocket_fd);
    // // // sleep(10);

    // // Start the game
    // printf("\n[---Début de la partie---]\n");
    // send_START_request(tcpsocket_fd, &udpsocket_fd, &x, &y, &p);
    // recv_UDP_auto(udpsocket_fd, tcpsocket_fd, &x, &y, &p);
    // // // TODO: Répondre aux messages UDP
    // // // TODO: Répondre aux message GHOST (Partiellement)
    // // // TODO: Régler les types uint16
    // recv_UDP(udpsocket_fd, tcpsocket_fd, &x, &y, &p);
    // // sleep(10);

    while (1)
    {
        printf("1. GAME?***\n");
        printf("2. NEWPL id port***\n");
        printf("3. REGIS id port m***\n");
        printf("4. LIST? m***\n");
        printf("5. SIZE? m***\n");
        printf("6. UNREG***\n");
        printf("7. START***\n");
        printf("8. Aide et consignes\n");

        printf("\n[---Choix de l'action---]\n");
        int choice = 0;
        scanf("%d", &choice);
        switch (choice)
        {
        case 1:
            printf("\n[---Demande de la liste des parties---]\n");
            printf("1. GAME?***\n");
            send_GAME_request(tcpsocket_fd, games, &n);
            break;
        case 2:
            printf("\n[---Création d'une nouvelle partie---]\n");
            printf("2. NEWPL id port***\n");
            printf("id: ");
            fgets(username, 8, stdin);
            printf("port: ");
            fgets(port, 4, stdin);
            send_NEWPL_request(tcpsocket_fd, username, port);
            break;
        case 3:
            printf("\n[---Rejoindre une partie---]\n");
            printf("3. REGIS id port m***\n");
            printf("id: ");
            fgets(username, 8, stdin);
            printf("port: ");
            fgets(port, 4, stdin);
            printf("m: ");
            scanf("%d", &m);
            send_REGIS_request(tcpsocket_fd, username, port, m);
            break;
        case 4:
            printf("\n[---Demande de la liste des joueurs---]\n");
            printf("4. LIST? m***\n");
            printf("m: ");
            scanf("%d", &m);
            send_LIST_request(tcpsocket_fd, m);
            break;
        case 5:
            printf("\n[---Demande de la taille du labyrinthe---]\n");
            printf("5. SIZE? m***\n");
            printf("m: ");
            scanf("%d", &m);
            send_SIZE_request(tcpsocket_fd, m);
            break;
        case 6:
            printf("\n[---Se désinscrire d'une partie---]\n");
            printf("6. UNREG***\n");
            send_UNREG_request(tcpsocket_fd);
            break;
        case 7:
            printf("\n[---Début de la partie---]\n");
            send_START_request(tcpsocket_fd, &udpsocket_fd, &x, &y, &p);
            pthread_t thread;
            // TODO: TRAITER LES POINTEURS DE LA FONCTION
            pthread_create(&thread, NULL, recv_UDP_manuel, (void *)udpsocket_fd, (void *)tcpsocket_fd, (void *)&x, (void *)&y, (void *)&p);
            // TODO: afficher la listes des commandes et faire comme la premiere partie
            while (1)
            {
                printf("1. GLIS?***\n");
                printf("2. UPMOV d***\n");
                printf("3. DOMOV d***\n");
                printf("4. RIMOV d***\n");
                printf("5. LEMOV d***\n");
                printf("6. MALL? mess***\n");
                printf("7. SEND? id mess***\n");
                printf("8. IQUIT***\n");
                printf("9. Aide et consignes\n");

                printf("\n[---Choix de l'action---]\n");
                scanf("%d", &choice);
                switch (choice)
                {
                case 1:
                    printf("\n[---Demande de la liste des joueurs---]\n");
                    printf("1. GLIS?***\n");
                    send_GLIS_request(tcpsocket_fd);
                    break;
                case 2:
                    printf("\n[---Déplacement du joueur vers le haut---]\n");
                    printf("2. UPMOV d***\n");
                    printf("d: ");
                    scanf("%d", &d);
                    complete_number(d, d_completed);
                    send_UPMOV_request(tcpsocket_fd, d_completed, &x, &y, &p);
                    break;
                case 3:
                    printf("\n[---Déplacement du joueur vers le bas---]\n");
                    printf("3. DOMOV d***\n");
                    printf("d: ");
                    scanf("%d", &d);
                    complete_number(d, d_completed);
                    send_DOMOV_request(tcpsocket_fd, d_completed, &x, &y, &p);
                    break;
                case 4:
                    printf("\n[---Déplacement du joueur vers la droite---]\n");
                    printf("4. RIMOV d***\n");
                    printf("d: ");
                    scanf("%d", &d);
                    complete_number(d, d_completed);
                    send_RIMOV_request(tcpsocket_fd, d_completed, &x, &y, &p);
                    break;
                case 5:
                    printf("\n[---Déplacement du joueur vers la gauche---]\n");
                    printf("5. LEMOV d***\n");
                    printf("d: ");
                    scanf("%d", &d);
                    complete_number(d, d_completed);
                    send_LEMOV_request(tcpsocket_fd, d_completed, &x, &y, &p);
                    break;
                case 6:
                    printf("\n[---Envoyer un message pour tout les joueurs---]\n");
                    printf("6. MALL? mess***\n");
                    printf("mess: ");
                    fgets(message, 200, stdin);
                    send_MALL_request(tcpsocket_fd, message);
                    break;
                case 7:
                    printf("\n[---Envoyer un message à un joueur---]\n");
                    printf("7. SEND? id mess***\n");
                    printf("id: ");
                    fgets(username, 8, stdin);
                    printf("mess: ");
                    fgets(message, 200, stdin);
                    send_SEND_request(tcpsocket_fd, username, message);
                    break;
                case 8:
                    printf("\n[---Quitter la partie---]\n");
                    send_IQUIT(tcpsocket_fd);
                    // pthread_cancel(thread);
                    break;

                case 9:
                    printf("\n[---Aide et consignes---]\n",
                           "1. GLIS?*** : Demander la liste des joueurs de la partie\n",
                           "2. UPMOV d*** : Déplacer le joueur vers le haut avec un deplacement <d> qui doit être un nombre entre 0 et 999\n",
                           "3. DOMOV d*** : Déplacer le joueur vers le bas avec un deplacement <d> qui doit être un nombre entre 0 et 999\n",
                           "4. RIMOV d*** : Déplacer le joueur vers le bas avec un deplacement <d> qui doit être un nombre entre 0 et 999\n",
                           "5. LEMOV d*** : Déplacer le joueur vers le bas avec un deplacement <d> qui doit être un nombre entre 0 et 999\n",
                           "6. MALL? mess*** : Envoyer le message `mess` pour tout les joueurs\n",
                           "7. SEND? id mess*** : Envoyer le message `mess` pour tout le joueur `id`\n",
                           "8. IQUIT*** : Quitter la partie\n");
                }

                break;
            case 8:
                printf("\n[---Aide et consignes---]\n",
                       "1. GAME?*** : Demander la liste des parties non commencées\n",
                       "2. NEWPL id port*** : Créer une nouvelle partie, <id> est votre nom d'utilisateur qui doit être\n",
                       "exactement sur 8 caractères, <port> est le numéro de votre port UDP qui doit être entre 1024 et 9999\n"
                       "3. REGIS id port m*** : S'inscrire à une partie, <id> est votre nom d'utilisateur qui doit\n",
                       "être exactement sur 8 caractères, <port> est le numéro de votre port UDP, <m> est le numéro de la partie\n",
                       "4. LIST? m*** : Demander la liste des joueurs de la partie <m>\n",
                       "5. SIZE? m*** : Demander la taille du labyrinthe de la partie <m>\n",
                       "6. UNREG*** : Se désinscrire de la partie <m>\n",
                       "7. START*** : Commencer la partie\n");
                break;
            default:
                printf("\n[---Choix invalide---]\n");
                break;
            }
        }

        close(tcpsocket_fd);
    }