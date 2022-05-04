#ifndef DURING_GAME_FUNCTIONS_H
#define DURING_GAME_FUNCTIONS_H

#include "utils.h"

void recv_WELCO(int tcpsocket_fd);
void recv_POSIT(int tcpsocket_fd, uint16_t *x, uint16_t *y);
void recv_MOVE(int tcpsocket_fd, uint16_t *x, uint16_t *y, uint16_t *p);
void send_UPMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_DOMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_LEMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_RIMOV_request(int tcpsocket_fd, char *d, uint16_t *x, uint16_t *y, uint16_t *p);
void send_GLIS_request(int tcpsocket_fd);
void send_MALL_request(int tcpsocket_fd, char *mess);
void sens_SEND_request(int tcpsocket_fd, char *id, char *mess);
void send_IQUIT(int tcpsocket_fd);

#endif