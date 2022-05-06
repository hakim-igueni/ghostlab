#ifndef BEFORE_GAME_FUNCTIONS_C
#define BEFORE_GAME_FUNCTIONS_C

#include "utils.h"

void recv_GAMES(int tcpsocket_fd, uint8_t *games, uint8_t *n);
void send_GAME_request(int tcpsocket_fd, uint8_t *games, uint8_t *n);
void send_NEWPL_request(int tcpsocket_fd, char *username, char *port);
void send_REGIS_request(int tcpsocket_fd, char *username, char *port, uint8_t m);
void send_UNREG_request(int tcpsocket_fd);
void send_SIZE_request(int tcpsocket_fd);
void send_LIST_request(int tcpsocket_fd);
void send_START_request(int tcpsocket_fd, int *udpsocket_fd, uint16_t *x, uint16_t *y);
void recv_WELCO(int tcpsocket_fd, int *udpsocket_fd);
void recv_POSIT(int tcpsocket_fd, uint16_t *x, uint16_t *y);

#endif // BEFORE_GAME_FUNCTIONS_C