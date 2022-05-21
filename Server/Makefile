JFLAGS= -g
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	src/Server/Player.java \
	src/Server/Game.java \
	src/Server/Ghost.java \
	src/Server/Labyrinth.java \
	src/Server/Server.java \
	src/Server/Utils.java \
	src/Server/ServerImpl.java\
	src/Server/PlayerHandler.java

default: classes
classes:$(CLASSES:.java=.class)

clean:
	$(RM) *.class
