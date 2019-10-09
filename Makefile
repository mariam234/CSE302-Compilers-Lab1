ANTLR4_JAR := ./jars/antlr-4.7.2-complete.jar
ANTLR4_RUNTIME_JAR := ./jars/antlr4-runtime-4.7.2.jar

CLASSPATH := .:$(ANTLR4_JAR):$(ANTLR4_RUNTIME_JAR):

Main.class: $(wildcard *.java) BX0.g4
	java -cp $(CLASSPATH) -jar $(ANTLR4_JAR) BX0.g4
	javac -cp $(CLASSPATH) $(wildcard *.java)

%.exe: %.bx Main.class
	java -cp $(CLASSPATH) Main $(<)

.PHONY: clean
clean:
	rm -f *.class $(filter-out BX0.g4,$(wildcard BX0*))
	rm -f bx0_tests/*.exe bx0_tests/*.c bx0_tests/*.s bx1_tests/*.exe bx1_tests/*.c bx1_tests/*.s
