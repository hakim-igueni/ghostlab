#include "utils.h"
#include "during_game_functions.h"

int send_START_request(int tcpsocket_fd, int *udpsocket_fd, uint16_t *x, uint16_t *y, uint16_t *p, int *in_game)
{
    if (*in_game == 0)
    {
        printf("Vous n'êtes pas dans une partie pour commencer.\n");
        return -1;
    }
    // Envoie du message START pour commencer la partie
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "START***");
    printf("[START] Le message à envoyer au serveur : %s\n", buffer);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[START] send");
        exit(EXIT_FAILURE);
    }

    recv_WELCO(tcpsocket_fd, udpsocket_fd);
    recv_POSIT(tcpsocket_fd, x, y);
    return 0;
}

void recv_WELCO(int tcpsocket_fd, int *udpsocket_fd)
{
    // Recevoir le message de bienvenue sous la forme "WELCO␣m␣h␣w␣f␣ip␣port***"
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    printf("[WELCO] Attente de la réponse du serveur...\n");
    int received_bytes = recv(tcpsocket_fd, buffer, 40, 0);
    if (received_bytes == -1)
    {
        perror("[WELCO] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    printf("[WELCO] La réponse du serveur : %s\n", buffer);
    uint16_t h, w;
    uint8_t m = (uint8_t)buffer[6];
    h = le_to_ho(buffer, 8);
    w = le_to_ho(buffer, 11);
    uint8_t f = (uint8_t)buffer[14];
    char ip[16];
    strncpy(ip, buffer + 16, 15);
    // supprimer les # à la fin de l'ip
    char *p = strchr(ip, '#');
    if (p != NULL)
    {
        *p = '\0';
    }
    uint16_t port = (uint16_t)strtol(buffer + 32, NULL, 10);

    printf("WELCO␣m␣h␣w␣f␣ip␣port*** : m=%d h=%d  w=%d  f=%d ip=%s port=%d \n", m, h, w, f, ip, port);
    // printf("Buffer[38] = %c", buffer[38]);
    // s'abonner à l'adresse ip recu
    *udpsocket_fd = socket(PF_INET, SOCK_DGRAM, 0);
    int ok = 1;
    int r = setsockopt(*udpsocket_fd, SOL_SOCKET, SO_REUSEADDR, &ok, sizeof(ok));
    if (r == -1)
    {
        perror("[WELCO] setsockopt");
        exit(EXIT_FAILURE);
    }
    struct sockaddr_in address_sock;
    address_sock.sin_family = AF_INET;
    address_sock.sin_port = htons(port);
    address_sock.sin_addr.s_addr = htonl(INADDR_ANY);
    r = bind(*udpsocket_fd, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    if (r == -1)
    {
        perror("[WELCO] bind");
        exit(EXIT_FAILURE);
    }
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr = inet_addr(ip);
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);
    r = setsockopt(*udpsocket_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));
}
// 18.82
void recv_POSIT(int tcpsocket_fd, uint16_t *x, uint16_t *y)
{
    // Recevoir la position du joueur sous la forme "POSIT id x y***"
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 25, 0);
    if (received_bytes == -1)
    {
        perror("[POSIT] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';

    // extraire les informations
    char id[9];
    strncpy(id, buffer + 6, 8);
    // TODO:Verifier si id correspond au joueur, est-ce utile d'extraire id ?

    // Mettre à jour la position du client
    *x = (uint16_t)strtol(buffer + 15, NULL, 10);
    *y = (uint16_t)strtol(buffer + 18, NULL, 10);

    printf("[POSIT] La réponse du serveur : %s\n", buffer);
    printf("[POSIT] id : %s, x : %d, y : %d\n", id, *x, *y);
}

void recv_MOVE(int tcpsocket_fd, uint16_t *x, uint16_t *y, uint16_t *p)
{
    /* Recevoir la réponse du serveur sous la forme "MOVE! x y***"
    ou sous la forme "MOVEF x y p***" si le joueur a capturé un fantome */
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);

    // quand on capte un fantome, on reçoit 20 caractères, pas 16,
    // donc on recoit d'abord les 5 premiers caractères pour savoir si
    // on a capturé un fantome ou pas

    int received_bytes = recv(tcpsocket_fd, buffer, 5, 0);
    if (received_bytes == -1)
    {
        perror("[MOVE] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    printf("[MOVE] La réponse du serveur : %s\n", buffer);
    if (buffer[4] == 'F')
    {
        // si le joueur a capturé un fantome, on met à jour le nombre de points
        // Le joueur a capturé un fantome
        // On reçoit la suite sous la forme " x y p***"
        received_bytes += recv(tcpsocket_fd,
                               buffer + 5, 15, 0);
        if (received_bytes == -1)
        {
            perror("[MOVE] read");
            exit(EXIT_FAILURE);
        }
        printf("[MOVE] Le joueur a capturé un fantome\n");
        *p = (uint16_t)strtol(buffer + 12, NULL, 10);
    }
    else if (buffer[4] == '!')
    {
        // Le joueur n'a pas capturé un fantome
        // On reçoit la suite sous la forme " x y***"
        received_bytes += recv(tcpsocket_fd,
                               buffer + 5, 11, 0);
        if (received_bytes == -1)
        {
            perror("[MOVE] read");
            exit(EXIT_FAILURE);
        }
        printf("[MOVE] Le joueur n'a pas capturé un fantome\n");
        printf("[MOVE] buffer = %s\n", buffer);
    }
    else
    {
        printf("[MOVE] Erreur de réception\n");
        exit(EXIT_FAILURE);
    }
    *x = (uint16_t)strtol(buffer + 6, NULL, 10);
    *y = (uint16_t)strtol(buffer + 9, NULL, 10);
}

void send_UPMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p)
{
    // On passe en paramètre x et y pour recuperer la nouvelle position du joueur
    // Envoie du message "UPMOV d***"" pour déplacer le joueur vers le haut
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "UPMOV %s***", d);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[UPMOV] send");
        exit(EXIT_FAILURE);
    }
    printf("sent_bytes : %d\n", sent_bytes);
    // Recevoir la réponse du serveur sous la forme "MOVE! x y***"
    recv_MOVE(tcpsocket_fd, x, y, p);
}

void send_DOMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p)
{
    // On passe en paramètre x et y pour recuperer la nouvelle position du joueur
    // Envoie du message "DOMOV d***"" pour déplacer le joueur vers le haut
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "DOMOV %s***", d);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[DOMOV] send");
        exit(EXIT_FAILURE);
    }
    // Recevoir la réponse du serveur sous la forme "MOVE! x y***"
    recv_MOVE(tcpsocket_fd, x, y, p);
}

void send_LEMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p)
{
    // On passe en paramètre x et y pour recuperer la nouvelle position du joueur
    // Envoie du message "LEMOV d***"" pour déplacer le joueur vers le haut
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "LEMOV %s***", d);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[LEMOV] send");
        exit(EXIT_FAILURE);
    }
    // Recevoir la réponse du serveur sous la forme "MOVE! x y***"
    recv_MOVE(tcpsocket_fd, x, y, p);
}

void send_RIMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p)
{
    // On passe en paramètre x et y pour recuperer la nouvelle position du joueur
    // Envoie du message "RIMOV d***"" pour déplacer le joueur vers le haut
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "RIMOV %s***", d);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[RIMOV] send");
        exit(EXIT_FAILURE);
    }
    // Recevoir la réponse du serveur sous la forme "MOVE! x y***"
    recv_MOVE(tcpsocket_fd, x, y, p);
}

void send_GLIS_request(int tcpsocket_fd)
{
    // Demander la liste des joueurs en envoyant "GLIS?***"
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "GLIS?***");
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[GLIS] send");
        exit(EXIT_FAILURE);
    }

    // Recevoir la réponse du serveur sous la forme "GLIS! s***"
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 10, 0);
    if (received_bytes == -1)
    {
        perror("[GLIS] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    uint8_t s = (uint8_t)buffer[6];
    printf("[GLIS] Le nombre de joueurs connectés est %d\n", s);

    // Recevoir les joueurs connectés sous la forme "GPLYR id x y p***"
    for (int i = 0; i < s; i++)
    {
        memset(buffer, 0, BUFFER_SIZE);
        received_bytes = recv(tcpsocket_fd,
                              buffer, 30, 0);
        if (received_bytes == -1)
        {
            perror("[GPLYR] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        printf("[GPLYR] %s\n", buffer);
        char id[9];
        strncpy(id, buffer + 6, 8);
        uint16_t x, y, p;
        x = (uint16_t)strtol(buffer + 14, NULL, 10);
        y = (uint16_t)strtol(buffer + 17, NULL, 10);
        p = (uint16_t)strtol(buffer + 20, NULL, 10);
        printf("[GPLYR] id: %s, x: %d, y: %d, p: %d\n", id, x, y, p);
    }
}

void send_MALL_request(int tcpsocket_fd, char *mess)
{
    // Envoyer le message "MALL? mess***"
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "MALL? %s***", mess);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[MALL] send");
        exit(EXIT_FAILURE);
    }

    // Recevoir la réponse du serveur sous la forme "MALL!***"
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 8, 0);
    if (received_bytes == -1)
    {
        perror("[MALL] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    printf("[MALL] %s\n", buffer);
}

void send_SEND_request(int tcpsocket_fd, char *id, char *mess)
{
    // envoyer le message "SEND? id mess***"
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "SEND? %s %s***", id, mess);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[SEND] send");
        exit(EXIT_FAILURE);
    }

    // Recevoir la réponse du serveur sous la forme "SEND!***" si l'envoi a bien eu lieu, "NSEND***" sinon
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 8, 0);
    if (received_bytes == -1)
    {
        perror("[SEND] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    if (strncmp(buffer, "SEND!", 5) == 0)
    {
        printf("[SEND] Le message a bien été envoyé : %s\n", buffer);
    }
    else if (strncmp(buffer, "NSEND", 5) == 0)
    {
        printf("[SEND] Le message n'a pas pu être envoyé : %s\n", buffer);
    }
    else
    {
        printf("[SEND] Erreur : %s\n", buffer);
    }
}

void send_IQUIT(int tcpsocket_fd)
{
    // Envoie du message "IQUIT***" pour quitter la partie
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "IQUIT***");
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[IQUIT] send");
        exit(EXIT_FAILURE);
    }
    // recevoir la réponse du serveur sous la forme "GOBYE***"
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 8, 0);
    if (received_bytes == -1)
    {
        perror("[IQUIT] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    if (strncmp(buffer, "GOBYE", 5) == 0)
    {
        printf("[IQUIT] %s\n", buffer);
    }
    else
    {
        printf("[IQUIT] Erreur : %s\n", buffer);
    }
    close(tcpsocket_fd);
}

void *recv_UDP_manuel(void *arg)
{
    struct infos_player infos_player = *((struct infos_player *)arg);
    if (infos_player.in_game == 0)
    {
        printf("[UDP] Vous n'êtes pas dans une partie\n");
        return NULL;
    }
    char id[9];
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    while (1)
    {
        memset(buffer, 0, BUFFER_SIZE);
        // Recevoir les messages UDP
        int received_bytes = recv(infos_player.udpsocket_fd, buffer, 230, 0);
        if (received_bytes == -1)
        {
            perror("[UDP] read");
            exit(EXIT_FAILURE);
        }
        if (strncmp(buffer, "GHOST", 5) == 0) // si le message est "GHOST␣x␣y+++"
        {
            uint16_t xf, yf;
            treat_GHOST(infos_player.udpsocket_fd, &xf, &yf, buffer);
        }
        else if (strncmp(buffer, "SCORE", 5) == 0) // si le message est "SCORE␣id␣p␣x␣y+++"
        {
            treat_SCORE(infos_player.udpsocket_fd, id, buffer);
        }
        else if (strncmp(buffer, "MESSA", 5) == 0) // si le message est "MESSA␣id␣mess+++"
        {
            treat_MESSA(infos_player.udpsocket_fd, buffer);
        }
        else if (strncmp(buffer, "MESSP", 5) == 0) // si le message est "MESSP␣id␣mess+++"
        {
            treat_MESSP(infos_player.udpsocket_fd, id, buffer);
        }
        else if (strncmp(buffer, "ENDGA", 5) == 0) // si le message est "ENDGA␣id␣p+++"
        {
            treat_ENDGA(infos_player.udpsocket_fd, buffer);
            break;
        }
        else
        {
            received_bytes += recv(infos_player.udpsocket_fd, buffer + 5, BUFFER_SIZE, 0);
            printf("[UDP] %s\n", buffer);
        }
    }
    return NULL;
}

void recv_UDP_auto(int udpsocket_fd, int tcpsocket_fd, uint16_t *xj, uint16_t *yj, uint16_t *p, int *in_game)
{
    char id[9];
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    while (1)
    {
        memset(buffer, 0, BUFFER_SIZE);
        // Recevoir les messages UDP
        int received_bytes = recv(udpsocket_fd, buffer, 230, 0);
        if (received_bytes == -1)
        {
            perror("[UDP] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        if (strncmp(buffer, "GHOST", 5) == 0) // si le message est "GHOST␣x␣y+++"
        {
            uint16_t xf, yf;
            treat_GHOST(udpsocket_fd, &xf, &yf, buffer);
            // Déplacer le joueur
            move_player(tcpsocket_fd, xj, yj, xf, yf, p);
        }
        else if (strncmp(buffer, "SCORE", 5) == 0) // si le message est "SCORE␣id␣p␣x␣y+++"
        {
            treat_SCORE(udpsocket_fd, id, buffer);
            // Envoyer un message au client dont l'id est id
            send_SEND_request(tcpsocket_fd, id, "Bsahtek");
        }
        else if (strncmp(buffer, "MESSA", 5) == 0) // si le message est "MESSA␣id␣mess+++"
        {
            treat_MESSA(udpsocket_fd, buffer);
        }
        else if (strncmp(buffer, "MESSP", 5) == 0) // si le message est "MESSP␣id␣mess+++"
        {
            treat_MESSP(udpsocket_fd, id, buffer);
            // Répondre au client dont l'id est id
            send_SEND_request(tcpsocket_fd, id, "Bien reçu");
        }
        else if (strncmp(buffer, "ENDGA", 5) == 0) // si le message est "ENDGA␣id␣p+++"
        {
            treat_ENDGA(udpsocket_fd, buffer);
            break;
        }
        else
        {
            received_bytes += recv(udpsocket_fd, buffer + 5, BUFFER_SIZE, 0);
            printf("[UDP] %s\n", buffer);
        }
    }
}

void treat_GHOST(int udpsocket_fd, uint16_t *xf, uint16_t *yf, char *buffer)
{
    // char buffer[BUFFER_SIZE];
    // memset(buffer, 0, BUFFER_SIZE);
    // int received_bytes = 5;
    // sprintf(buffer, "GHOST");
    // received_bytes += recv(udpsocket_fd, buffer + 5, 11, 0);
    // if (received_bytes == -1)
    // {
    //     perror("[treat_GHOST] read");
    //     exit(EXIT_FAILURE);
    // }
    // buffer[received_bytes] = '\0';
    printf("[treat_GHOST] %s\n", buffer);
    *xf = (uint16_t)strtol(buffer + 6, NULL, 10);
    *yf = (uint16_t)strtol(buffer + 9, NULL, 10);
    printf("[treat_GHOST] GHOST␣x␣y+++ : x = %d, y = %d\n", *xf, *yf);
}

void treat_SCORE(int udpsocket_fd, char *id, char *buffer)
{
    strncpy(id, buffer + 6, 8);
    uint16_t x, y, p;
    p = (uint16_t)strtol(buffer + 15, NULL, 10);
    x = (uint16_t)strtol(buffer + 20, NULL, 10);
    y = (uint16_t)strtol(buffer + 24, NULL, 10);
    printf("[treat_SCORE] La reponse du serveur est : %s\n", buffer);
    printf("[treat_SCORE] SCORE␣id␣p␣x␣y+++ : id = %s, p = %d, x = %d, y = %d\n", id, p, x, y);
}

void treat_MESSA(int udpsocket_fd, char *buffer)
{
    char id[9];
    strncpy(id, buffer + 6, 8);
    char mess[200];
    strcpy(mess, buffer + 15);
    printf("[treat_MESSA] MESSA␣id␣mess+++ : id = %s, mess = %s\n", id, mess);
}

void treat_MESSP(int udpsocket_fd, char *id, char *buffer)
{
    strncpy(id, buffer + 6, 8);
    char mess[200];
    strcpy(mess, buffer + 15);
    printf("[treat_MESSP] MESSP␣id␣mess+++ : id = %s, mess = %s\n", id, mess);
}

void treat_ENDGA(int udpsocket_fd, char *buffer)
{
    char id[9];
    strncpy(id, buffer + 6, 8);
    uint16_t p;
    p = (uint16_t)strtol(buffer + 15, NULL, 10);
    printf("[treat_ENDGA] ENDGA␣id␣p+++ : id = %s, p = %d\n", id, p);
    if (close(udpsocket_fd) == -1)
    {
        perror("[treat_ENDGA] close");
        exit(EXIT_FAILURE);
    }
}

void move_player(int tcpsocket_fd, uint16_t *xj, uint16_t *yj, uint16_t xf, uint16_t yf, uint16_t *p)
{
    // uint16_t x, y;
    // x = *xj;
    // y = *yj;
    char d[4]; // déplacement
    if (xf > *xj)
    {
        complete_number((xf - *xj), d);
        send_RIMOV_request(tcpsocket_fd, d, xj, yj, p);
    }
    else if (xf < *xj)
    {
        complete_number((*xj - xf), d);
        send_LEMOV_request(tcpsocket_fd, d, xj, yj, p);
    }
    if (yf > *yj)
    {
        complete_number((yf - *yj), d);
        send_DOMOV_request(tcpsocket_fd, d, xj, yj, p);
    }
    else if (yf < *yj)
    {
        complete_number((*yj - yf), d);
        send_DOMOV_request(tcpsocket_fd, d, xj, yj, p);
    }
}