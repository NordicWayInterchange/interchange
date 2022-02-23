grammar Selector;

expr
    : LEFT_PARA? BOOLEAN_LITERAL RIGHT_PARA?
    ;

BOOLEAN_LITERAL :  TRUE | FALSE ;
DECIMAL_LITERAL : DIGIT+;

AND : A N D;
TRUE : T R U E;
FALSE : F A L S E;

LEFT_PARA : '(';
RIGHT_PARA : ')';



fragment DIGIT : [0-9];
fragment A : [aA];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment L : [lL];
fragment N : [nN];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];


WS : [\t\r\n ]+ -> skip;
