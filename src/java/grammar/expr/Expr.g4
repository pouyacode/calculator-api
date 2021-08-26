grammar Expr;

/** The start rule; begin parsing here. */
prog:   stat+ ;

stat:   expr
    ;

expr:   expr ('*'|'/') expr	# MulDiv
    |   expr ('+'|'-') expr	# AddSub
    |   INT  	       		# int
    |   NEG     		# negative
    |   '(' expr ')'		# parens
    ;

MUL :   '*' ; // assigns token name to '*' used above in grammar
DIV :   '/' ;
ADD :   '+' ;
SUB :   '-' ;
INT :   [0-9]+ ;         // match integers
NEG :   ~[)(0-9 ]'-'[0-9]+ ;	 // match negative
NEWLINE:'\r'? '\n' ;     // return newlines to parser (is end-statement signal)
WS  :   [ \t]+ -> skip ; // toss out whitespace
