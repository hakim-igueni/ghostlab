#ifndef DURING_GAME_FUNCTIONS_H
#define DURING_GAME_FUNCTIONS_H

#include "utils.h"

int send_START_request(int tcpsocket_fd, int *udpsocket_fd, uint16_t *x, uint16_t *y, uint16_t *p, int *in_game);
void recv_WELCO(int tcpsocket_fd, int *udpsocket_fd);
void recv_POSIT(int tcpsocket_fd, uint16_t *x, uint16_t *y);
void recv_MOVE(int tcpsocket_fd, uint16_t *x, uint16_t *y, uint16_t *p);
void send_UPMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_DOMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_LEMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_RIMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_GLIS_request(int tcpsocket_fd);
void send_MALL_request(int tcpsocket_fd, char *mess);
void send_SEND_request(int tcpsocket_fd, char *id, char *mess);
void send_IQUIT(int tcpsocket_fd);
void recv_UDP_auto(int udpsocket_fd, int tcpsocket_fd, uint16_t *xj, uint16_t *yj, uint16_t *p, int *in_game);
void *recv_UDP_manuel(void *arg);
void treat_GHOST(int udpsocket_fd, uint16_t *xf, uint16_t *yf);
void treat_SCORE(int udpsocket_fd, char *id);
void treat_MESSA(int udpsocket_fd);
void treat_MESSP(int udpsocket_fd, char *id);
void treat_ENDGA(int udpsocket_fd);
void move_player(int tcpsocket_fd, uint16_t *xj, uint16_t *yj, uint16_t xf, uint16_t yf, uint16_t *p);

#endif // DURING_GAME_FUNCTIONS_H