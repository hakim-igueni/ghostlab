#include "utils.h"
#include "before_game_functions.h"

void recv_GAMES(int tcpsocket_fd, uint8_t *games, uint8_t *n)
{
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    // Recevoir le message "GAMES n***"
    int received_bytes = recv(tcpsocket_fd, buffer, 10, 0);
    if (received_bytes == -1)
    {
        perror("read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    printf("[recv_GAMES] Message recu: %s\n", buffer);
    // Assurer que le message commence par GAMES
    if (strncmp(buffer, "GAMES", 5) != 0)
    {
        printf("Erreur: Le message n'est pas du bon format\n");
        exit(EXIT_FAILURE);
    }

    // Récupération du nombre de parties
    uint8_t m, s;
    *n = (uint8_t)buffer[6];
    printf("[recv_GAMES] Nombre de parties : %d\n\n", *n);

    // Récupupération des parties
    for (int i = 0; i < *n; i++)
    {
        // Reinitiation du buffer
        memset(buffer, 0, BUFFER_SIZE);

        // Recevoir le message "OGAME m s***"
        // m le numéro de la partie et s le nombre de joueurs
        // TODO: Assurer que le message commence par OGAME et fini par ***
        received_bytes = recv(tcpsocket_fd,
                              buffer, 12, 0);
        if (received_bytes == -1)
        {
            perror("[recv_GAMES] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        // printf("Le message envoyé par le serveur : %s\n", buffer);
        m = (uint8_t)buffer[6];
        s = (uint8_t)buffer[8];
        games[i] = m;
        printf("[recv_GAMES] Partie %d : %d joueurs\n", m, s);
    }
}

void send_GAME_request(int tcpsocket_fd, uint8_t *games, uint8_t *n)
{
    char buffer[BUFFER_SIZE];
    // Envoi du message "GAME?***"
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "GAME?***");
    printf("[GAME] Le message à envoyer au serveur : %s\n", buffer);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[GAME] send");
        exit(EXIT_FAILURE);
    }
    // Récupération des parties
    recv_GAMES(tcpsocket_fd, games, n);
}

void send_NEWPL_request(int tcpsocket_fd)
{
    // Envoi du message "NEWPL id port***" pour créer une nouvelle partie
    // TODO: Generer les username aléatoirement
    char *username = "username";
    char *port = "2121";
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "NEWPL %s %s***", username, port);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[NEWPL] send");
        exit(EXIT_FAILURE);
    }

    // Recevoir la reponse du serveur
    // Supposons que le serveur nous renvoie le message "REGOK m***"
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 10, 0);
    if (received_bytes == -1)
    {
        perror("[NEWPL] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    if (strncmp(buffer, "REGOK", 5) == 0)
    {
        uint8_t m = (uint8_t)buffer[6];
        printf("[NEWPL] L'identifiant de la nouvelle partie est %d\n", m);
    }
    else
    {
        puts(buffer);
        printf("[NEWPL] L'inscription n'est pas prise en compte\n");
        exit(EXIT_FAILURE);
    }
}

void send_REGIS_request(int tcpsocket_fd, char *username, char *port, uint8_t m)
{ // TODO: use unsigned char instead of char everywhere
    // Envoi du message "REGIS id port m***" pour rejoindre une partie
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "REGIS %s %s %c***", username, port, m);
    printf("[REGIS] Le message à envoyer au serveur : %s\n", buffer);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[REGIS] send");
        exit(EXIT_FAILURE);
    }
    // Recevoir la reponse du serveur
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 5, 0);
    if (received_bytes == -1)
    {
        perror("[REGIS] read");
        exit(EXIT_FAILURE);
    }
    if (strncmp(buffer, "REGOK", 5) == 0)
    {
        printf("[REGIS] L'inscription est prise en compte\n");
        received_bytes += recv(tcpsocket_fd, buffer + 5, 5, 0);
        if (received_bytes == -1)
        {
            perror("[REGIS] read");
            exit(EXIT_FAILURE);
        }
        u_int8_t m_char = (u_int8_t)buffer[6];
        if (m_char == m)
        {
            printf("[REGIS] L'inscription est faite dans la partie %d\n", m);
        }
        else
        {
            printf("[REGIS] L'inscription n'est pas faite dans la partie %d\n", m);
        }
    }
    else
    {
        puts(buffer);
        printf("[REGIS] L'inscription n'est pas prise en compte\n");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    printf("[REGIS] La réponse du serveur : %s\n", buffer);
}

void send_UNREG_request(int tcpsocket_fd)
{
    // Envoi du message "UNREG***" pour quitter une partie
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    sprintf(buffer, "UNREG***");
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[UNREG] send");
        exit(EXIT_FAILURE);
    }
    // Recevoir la reponse du serveur
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 5, 0);
    if (received_bytes == -1)
    {
        perror("[UNREG] read");
        exit(EXIT_FAILURE);
    }
    if (strncmp(buffer, "UNROK", 5) == 0)
    {
        // recevoir la suite du message "UNROK m***" càd " m***"
        received_bytes += recv(tcpsocket_fd, buffer + 5, 5, 0);
        if (received_bytes == -1)
        {
            perror("[UNREG] read");
            exit(EXIT_FAILURE);
        }
        uint8_t m = (uint8_t)buffer[6];
        printf("[UNREG] Le joueur s'est désinscri de la partie %d\n", m);
    }
    buffer[received_bytes] = '\0';
    printf("[UNREG] La réponse du serveur : %s\n", buffer);
}

void send_SIZE_request(int tcpsocket_fd)
{
    // Envoi du message "SIZE? m***" pour connaitre la taille de la grille
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    char m = '1';
    sprintf(buffer, "SIZE? %c***", m);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[SIZE] send");
        exit(EXIT_FAILURE);
    }
    // Recevoir la reponse du serveur sous la forme "SIZE! m h w***"
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 16, 0);
    if (received_bytes == -1)
    {
        perror("[SIZE] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    printf("[SIZE] La réponse du serveur : %s\n", buffer);
}

void send_LIST_request(int tcpsocket_fd)
{
    // Envoi du message "LIST? m***" pour connaitre la liste des joueurs
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    char m_char = '1';
    sprintf(buffer, "LIST? %c***", m_char);
    int sent_bytes = send(tcpsocket_fd, buffer, strlen(buffer), 0);
    if (sent_bytes == -1)
    {
        perror("[LIST] send");
        exit(EXIT_FAILURE);
    }
    // Recevoir la reponse du serveur sous la forme "LIST! m s***"
    memset(buffer, 0, BUFFER_SIZE);
    int received_bytes = recv(tcpsocket_fd, buffer, 12, 0);
    if (received_bytes == -1)
    {
        perror("[LIST] read");
        exit(EXIT_FAILURE);
    }
    buffer[received_bytes] = '\0';
    // uint8_t m = (uint8_t)buffer[6];
    uint8_t s = (uint8_t)buffer[8];
    for (int i = 0; i < s; i++)
    {
        // recevoir les joueurs sous la forme "PLAYR id***"
        memset(buffer, 0, BUFFER_SIZE);
        received_bytes = recv(tcpsocket_fd, buffer, 17, 0);
        if (received_bytes == -1)
        {
            perror("[LIST] read");
            exit(EXIT_FAILURE);
        }
        buffer[received_bytes] = '\0';
        printf("[LIST] Le joueur %d est %s\n", i + 1, buffer);
    }
}

void send_START_request(int tcpsocket_fd)
{
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
    // Comment va se dérouler la partie ?
}