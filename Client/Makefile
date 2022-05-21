OBJS	= client.o before_game_functions.o during_game_functions.o utils.o
SOURCE	= client.c before_game_functions.c during_game_functions.c utils.c
HEADER	= before_game_functions.h during_game_functions.h utils.h
OUT	= client
CC	 = gcc
FLAGS	 = -g -c -Wall
LFLAGS	 = 

all: $(OBJS)
	$(CC) -g $(OBJS) -o $(OUT) $(LFLAGS)
	rm $(OBJS)

client.o: client.c
	$(CC) $(FLAGS) client.c 

before_game_functions.o: before_game_functions.c
	$(CC) $(FLAGS) before_game_functions.c 

during_game_functions.o: during_game_functions.c
	$(CC) $(FLAGS) during_game_functions.c 


clean:
	rm -f $(OBJS) $(OUT)
