#include "utils.h"

void genarate_username(char *username, int size)
{
    srand(time(NULL));
    char *alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    int alphabet_size = strlen(alphabet);
    int i;
    for (i = 0; i < size; i++)
    {
        username[i] = alphabet[rand() % alphabet_size];
    }
    username[i] = '\0';
}

void generate_port(char *port)
{
    // TODO: verifier si le port est disponible
    srand(time(NULL));
    sprintf(port, "%d", rand() % (9999 - 1024) + 1024);
}