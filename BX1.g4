grammar BX1 ;

program: (vardecl)* | statement (';' statement)* ';'? ;

vardecl: 'var' VAR ('=' expr)? (',' VAR ('=' expr)?)* ':' type ;
statement: VAR '=' expr                     # move
         | 'print' expr                     # print
         | block                            # blockstmt
         | ifelse                           # ifelsestmt
         | whileloop                        # whilestmt
         ;

block: '{' statement '}' ;
ifelse: 'if' '(' expr ')' block ('else' (ifelse | block))? ;
whileloop: 'while' '(' expr ')' block ;

type: 'int64' | 'bool' ;

expr: VAR                                   # variable
    | NUM                                   # number
    | BOOL                                  # boolean
    | op=('~'|'-') expr                     # unop
    | expr op=('*'|'/'|'%') expr            # mul
    | expr op=('+'|'-') expr                # add
    | expr op=('<<'|'>>') expr              # shift
    | expr '&' expr                         # and
    | expr '^' expr                         # xor
    | expr '|' expr                         # or
    | expr '&&' expr                        # booladd
    | expr '||' expr                        # boolor
    | expr op=('=='|'!=') expr              # eq
    | expr op=('<'|'<='|'>'|'>=') expr      # ineq
    | '(' expr ')'                          # parens
    ;

VAR: [A-Za-z_][A-Za-z0-9_]* ;
NUM: [0-9]+ ;
BOOL: 'true' | 'false' ;

COMMENT: '//' ~[\r\n]* '\r'? '\n' -> skip ;
WS: [ \t\r\n]+ -> skip ;
