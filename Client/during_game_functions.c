#include "utils.h"
#include "during_game_functions.h"

void recv_WELCO(int tcpsocket_fd, int *udpsocket_fd)
{
    // Recevoir le message de bienvenue sous la forme "WELCO m h w f ip port***"
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    printf("[WELCO] Attente de la réponse du serveur...\n");
    int received_bytes = recv(tcpsocket_fd, buffer, 39, 0);
    if (received_bytes == -1)
    {
        perror("[WELCOME] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    printf("[WELCOME] La réponse du serveur : %s\n", buffer);
    // uint8_t m = (uint8_t)buffer[6];
    // uint16_t h = (uint16_t)strtol(buffer + 8, NULL, 10);
    // uint16_t w = (uint16_t)strtol(buffer + 11, NULL, 10);
    // uint8_t f = (uint8_t)buffer[14];
    char ip[16];
    strncpy(ip, buffer + 16, 15);
    // supprimer les # à la fin de l'ip
    char *p = strchr(ip, '#');
    if (p != NULL)
    {
        *p = '\0';
    }
    uint16_t port = (uint16_t)strtol(buffer + 32, NULL, 10);

    // s'abonner à l'adresse ip recu
    *udpsocket_fd = socket(PF_INET, SOCK_DGRAM, 0);
    int ok = 1;
    int r = setsockopt(*udpsocket_fd, SOL_SOCKET, SO_REUSEADDR, &ok, sizeof(ok));
    if (r == -1)
    {
        perror("[WELCOME] setsockopt");
        exit(EXIT_FAILURE);
    }
    struct sockaddr_in address_sock;
    address_sock.sin_family = AF_INET;
    address_sock.sin_port = htons(port);
    address_sock.sin_addr.s_addr = htonl(INADDR_ANY);
    r = bind(*udpsocket_fd, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    if (r == -1)
    {
        perror("[WELCOME] bind");
        exit(EXIT_FAILURE);
    }
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr = inet_addr(ip);
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);
    r = setsockopt(*udpsocket_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));

    printf("[WELCOME] La réponse du serveur : %s\n", buffer);
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
    *x = (uint16_t)strtol(buffer + 15, NULL, 10);
    *y = (uint16_t)strtol(buffer + 18, NULL, 10);

    printf("[POSIT] La réponse du serveur : %s\n", buffer);
}

void recv_MOVE(int tcpsocket_fd, uint16_t *x, uint16_t *y, uint16_t *p)
{
    /* Recevoir la réponse du serveur sous la forme "MOVE! x y***"
    ou sous la forme "MOVEF x y p***" si le joueur a capturé un fantome */
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    // TODO: quand on capte un fantome, on reçoit 20 caractères, pas 16,
    // donc on recoit d'abord les 5 premiers caractères pour savoir si
    // on a capturé un fantome ou pas

    int received_bytes = recv(tcpsocket_fd, buffer, 5, 0);
    if (received_bytes == -1)
    {
        perror("[MOVE] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    if (buffer[4] == 'F')
    {
        // TODO: si le joueur a capturé un fantome, on met à jour le nombre de points
        // Le joueur a capturé un fantome
        // On reçoit la suite sous la forme " x y p***"
        received_bytes = recv(tcpsocket_fd,
                              buffer, 15, 0);
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
        received_bytes = recv(tcpsocket_fd,
                              buffer, 11, 0);
        if (received_bytes == -1)
        {
            perror("[MOVE] read");
            exit(EXIT_FAILURE);
        }
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
    int received_bytes = recv(tcpsocket_fd, buffer, 9, 0);
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

void sens_SEND_request(int tcpsocket_fd, char *id, char *mess)
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
    printf("[SEND] %s\n", buffer);
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
    close(tcpsocket_fd);
}

void recv_UDP(int udpsocket_fd)
{
    // Recevoir les messages UDP
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(udpsocket_fd, buffer, 5, 0);
    if (received_bytes == -1)
    {
        perror("[UDP] read");
        exit(EXIT_FAILURE);
    }
    if (strncmp(buffer, "GHOST", 5) == 0) // si le message est "GHOST␣x␣y+++"
    {
        received_bytes += recv(udpsocket_fd, buffer + 5, 11, 0);
        if (received_bytes == -1)
        {
            perror("[UDP] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        uint16_t x, y;
        x = (uint16_t)strtol(buffer + 5, NULL, 10);
        y = (uint16_t)strtol(buffer + 8, NULL, 10);
        printf("[UDP] GHOST␣x␣y+++ : x = %d, y = %d\n", x, y);
    }
    else if (strncmp(buffer, "SCORE", 5) == 0) // si le message est "SCORE␣id␣p␣x␣y+++"
    {
        received_bytes += recv(udpsocket_fd, buffer + 5, 26, 0);
        if (received_bytes == -1)
        {
            perror("[UDP] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        char id[9];
        strncpy(id, buffer + 6, 8);
        uint16_t x, y, p;
        p = (uint16_t)strtol(buffer + 15, NULL, 10);
        x = (uint16_t)strtol(buffer + 20, NULL, 10);
        y = (uint16_t)strtol(buffer + 24, NULL, 10);
        printf("[UDP] SCORE␣id␣p␣x␣y+++ : id = %s, p = %d, x = %d, y = %d\n", id, p, x, y);
    }
    else if (strncmp(buffer, "MESSA", 5) == 0) // si le message est "MESSA␣id␣mess+++"
    {
        received_bytes += recv(udpsocket_fd, buffer + 5, 213, 0);
        if (received_bytes == -1)
        {
            perror("[UDP] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        char id[9];
        strncpy(id, buffer + 6, 8);
        char mess[200];
        strcpy(mess, buffer + 15);
        printf("[UDP] MESSA␣id␣mess+++ : id = %s, mess = %s\n", id, mess);
    }
    else if (strncmp(buffer, "MESSP", 5) == 0) // si le message est "MESSP␣id␣mess+++"
    {
        received_bytes += recv(udpsocket_fd, buffer + 5, 213, 0);
        if (received_bytes == -1)
        {
            perror("[UDP] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        char id[9];
        strncpy(id, buffer + 6, 8);
        char mess[200];
        strcpy(mess, buffer + 15);
        printf("[UDP] MESSP␣id␣mess+++ : id = %s, mess = %s\n", id, mess);
    }
    else if (strncmp(buffer, "ENDGA", 5) == 0) // si le message est "ENDGA␣id␣p+++"
    {
        received_bytes += recv(udpsocket_fd, buffer + 5, 16, 0);
        if (received_bytes == -1)
        {
            perror("[UDP] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        char id[9];
        strncpy(id, buffer + 6, 8);
        uint16_t p;
        p = (uint16_t)strtol(buffer + 15, NULL, 10);
        printf("[UDP] ENDGA␣id␣p+++ : id = %s, p = %d\n", id, p);
        // TODO: fermer la connexion UDP et deconnecter le joueur
    }
    else
    {
        printf("[UDP] %s\n", buffer);
    }
}