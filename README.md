BX0 to restricted C compiler
----------------------------

This is skeleton code for a compiler from the language BX0 to a
restricted subset of the C compiler.

This skeleton code is written in Java.

TODO: write the instruction selection and code generation passes
and add the suitable calls to it in Main.java.

The grammar of BX0 is in the file BX0.g4, form which the lexer/parser
combination is automatically generated using Antlr v4 (see below).

The abstract syntax tree is in Ast.java. The source language is
Ast.Source and the target language is Ast.Target.


Build Requirements
------------------

1. Antlr4 (http://antlr4.org), which is the parser generator
   recommended to use with Java. Specifically, get the following
   jars:
   - https://www.antlr.org/download/antlr-4.7.2-complete.jar
   - https://www.antlr.org/download/antlr-runtime-4.7.2.jar
2. A recent Java 8 (at least version 1.8.0_112)
3. To get the Makefile to work, you will need to change the
   variables ANTLR4_JAR and ANTLR4_RUNTIME_JAR at the top
   of the file to point to where you placed the above
   antlr jars.
