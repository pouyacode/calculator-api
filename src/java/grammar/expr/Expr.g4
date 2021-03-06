grammar Expr;

/** Parser */
prog:	expr+ ;

expr:	expr ('*'|'/') expr	# MulDiv
    |	expr ('+'|'-') expr	# AddSub
    |	NUM  	       		# int
    |	'-' NUM			# neg
    |	'(' expr ')'		# parens
    ;

/* Lexer */
NUM :	[0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)? ;
WS  :	[ \t]+ -> skip ; // toss out whitespace
