#ifndef BEFORE_GAME_FUNCTIONS_C
#define BEFORE_GAME_FUNCTIONS_C

#include "utils.h"

void recv_GAMES(int tcpsocket_fd, uint8_t *games);
void send_GAME_request(int tcpsocket_fd, uint8_t *games);
void send_NEWPL_request(int tcpsocket_fd);
void send_REGIS_request(int tcpsocket_fd, char *username, char *port, uint8_t m);
void send_UNREG_request(int tcpsocket_fd);
void send_SIZE_request(int tcpsocket_fd);
void send_LIST_request(int tcpsocket_fd);
void send_START_request(int tcpsocket_fd);

#endif // BEFORE_GAME_FUNCTIONS_C