JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
# $(JC) $(JFLAGS) -d $(@D) $(<)
	$(JC) $(JFLAGS) $*.java


CLASSES = Game.java Ghost.java Labyrinth.java Player.java Server.java Utils.java WelcomePlayerService.java


default: classes


classes: $(CLASSES:.java=.class)


clean:
	rm -f *.class
