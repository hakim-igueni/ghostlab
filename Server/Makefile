JFLAGS= -g
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) -d . $<

CLASSES = \
	Server/Player.java \
	Server/Game.java \
	Server/Ghost.java \
	Server/Labyrinth.java \
	Server/Server.java \
	Server/Utils.java \
	Server/ServerImpl.java\
	Server/PlayerHandler.java

default: $(CLASSES:.java=.class)

clean:
	$(RM) Server/*.class
