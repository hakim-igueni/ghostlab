#ifndef UTILS_H
#define UTILS_H

#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <time.h>

#define LOCALHOST "127.0.0.1"
#define SERVER_PORT 4444
#define BUFFER_SIZE 1024

void genarate_username(char *username, int size);
void generate_port(char *port);
void complete_number(uint16_t number, char *number_completed);

#endif