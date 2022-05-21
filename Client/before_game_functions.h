#ifndef BEFORE_GAME_FUNCTIONS_C
#define BEFORE_GAME_FUNCTIONS_C

#include "utils.h"

void recv_GAMES(int tcpsocket_fd, uint8_t *games, uint8_t *n);
void send_GAME_request(int tcpsocket_fd, uint8_t *games, uint8_t *n);
void send_NEWPL_request(int tcpsocket_fd, char *username, char *port, int *in_game);
void send_REGIS_request(int tcpsocket_fd, char *username, char *port, uint8_t m, int *in_game);
void send_UNREG_request(int tcpsocket_fd, int *in_game);
void send_SIZE_request(int tcpsocket_fd, uint8_t m);
void send_LIST_request(int tcpsocket_fd, uint8_t m);

#endif // BEFORE_GAME_FUNCTIONS_C