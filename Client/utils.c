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

void complete_number(uint16_t number, char *number_completed)
{
    if (number < 10)
    {
        sprintf(number_completed, "00%d", number);
    }
    else if (number < 100)
    {
        sprintf(number_completed, "0%d", number);
    }
    else if (number < 1000)
    {
        sprintf(number_completed, "%d", number);
    }
}

uint16_t le_to_ho(char *str, int pos)
{
    uint16_t h;
    h = str[pos];
    h += str[pos + 1] * 256;
    return h;
}