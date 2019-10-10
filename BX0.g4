grammar BX0 ;

program: (vardecl)* (statement)* ;

vardecl: 'var' varinit (',' varinit)* ':' type ';' ;
varinit:  VAR ('=' expr)? ;
type: 'int64' | 'bool' ;

statement: VAR '=' expr ';'                 # move
         | 'print' expr ';'                 # print
         | block                            # blockstmt
         | ifelse                           # ifelsestmt
         | whileloop                        # whilestmt
         ;

block: '{' (statement)* '}' ;
ifelse: 'if' '(' expr ')' block ('else' (ifelse | block))? ;
whileloop: 'while' '(' expr ')' block ;

expr: VAR                                   # variable
    | NUM                                   # number
    | BOOL                                  # boolean
    | op=('~'|'-'|'!') expr                 # unop
    | expr op=('*'|'/'|'%') expr            # mul
    | expr op=('+'|'-') expr                # add
    | expr op=('<<'|'>>') expr              # shift
    | expr '&' expr                         # and
    | expr '^' expr                         # xor
    | expr '|' expr                         # or
    | expr op=('&&'|'||') expr              # boolop
    | expr op=('=='|'!=') expr              # eq
    | expr op=('<'|'<=') expr               # less
    | expr op=('>'|'>=') expr               # greater
    | '(' expr ')'                          # parens
    ;

VAR: [A-Za-z_][A-Za-z0-9_]* ;
NUM: [0-9]+ ;
BOOL: 'true' | 'false' ;

COMMENT: '//' ~[\r\n]* '\r'? '\n' -> skip ;
WS: [ \t\r\n]+ -> skip ;
