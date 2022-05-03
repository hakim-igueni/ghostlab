JFLAGS = -g
JC = javac
JVM=java



.SUFFIXES: .java .class

.java .class:
	$(JC) $(JFLAGS) $*.java


CLASSES = \
Player.java \
Game.java \
Ghost.java \
Labyrinth.java \
Server.java \
Utils.java \
WelcomePlayerService.java

MAIN = Server


default: classes


classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean:
	$(RM)*.class
