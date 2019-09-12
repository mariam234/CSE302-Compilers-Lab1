grammar BX0 ;

program: statement (';' statement)* ';'? ;

statement: VAR '=' expr              # move
         | 'print' expr              # print
         ;

expr: VAR                            # variable
    | NUM                            # number
    | op=('~'|'-') expr              # unop
    | expr op=('*'|'/'|'%') expr     # mul
    | expr op=('+'|'-') expr         # add
    | expr op=('<<'|'>>') expr       # shift
    | expr '&' expr                  # and
    | expr '^' expr                  # xor
    | expr '|' expr                  # or
    | '(' expr ')'                   # parens
    ;

VAR: [A-Za-z_][A-Za-z0-9_]* ;
NUM: [0-9]+ ;

WS: [ \t\r\n]+ -> skip ;