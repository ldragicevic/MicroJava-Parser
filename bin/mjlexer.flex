package rs.ac.bg.etf.pp1;
import java_cup.runtime.Symbol; 

%% //-------------------------------------------------------------------------------------------------------

%{ 
	private Symbol next_token(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	private Symbol next_token(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}
%}

%cup 				
%line 				 
%column 			

%xstate COMMENT
 
%eofval{
	return next_token(sym.EOF);
%eofval}

%% //-------------------------------------------------------------------------------------------------------

" "			{ }
"\b"		{ }
"\t"		{ }
"\r\n"		{ }
"\f"		{ }

"program" 	{ return next_token(sym.PROGRAM, yytext()); }
"class" 	{ return next_token(sym.CLASS, yytext()); }
"extends" 	{ return next_token(sym.EXTENDS, yytext()); }
"const" 	{ return next_token(sym.CONST, yytext()); }
"static"	{ return next_token(sym.STATIC, yytext()); }
"void" 		{ return next_token(sym.VOID, yytext()); }
"return" 	{ return next_token(sym.RETURN, yytext()); }
"if" 		{ return next_token(sym.IF, yytext()); }
"else" 		{ return next_token(sym.ELSE, yytext()); }
"for" 		{ return next_token(sym.FOR, yytext()); }
"continue" 	{ return next_token(sym.CONTINUE, yytext()); }
"break" 	{ return next_token(sym.BREAK, yytext()); }
"print" 	{ return next_token(sym.PRINT, yytext()); }
"read" 		{ return next_token(sym.READ, yytext()); }
"new" 		{ return next_token(sym.NEW, yytext()); }

";" 		{ return next_token(sym.SEMI_COMMA, yytext()); }
"," 		{ return next_token(sym.COMMA, yytext()); }
"." 		{ return next_token(sym.DOT, yytext()); }
"+" 		{ return next_token(sym.PLUS, yytext()); }
"-" 		{ return next_token(sym.MINUS, yytext()); }
"*" 		{ return next_token(sym.MULTIPLE, yytext()); }
"/" 		{ return next_token(sym.DIVIDE, yytext()); }
"%" 		{ return next_token(sym.MOD, yytext()); }
"+=" 		{ return next_token(sym.ASSIGNPLUS, yytext()); }
"-="		{ return next_token(sym.ASSIGNMINUS, yytext()); }
"*=" 		{ return next_token(sym.ASSIGNMULTIPLE, yytext()); }
"/=" 		{ return next_token(sym.ASSIGNDIVIDE, yytext()); }
"%=" 		{ return next_token(sym.ASSIGNMOD, yytext()); }
"=" 		{ return next_token(sym.ASSIGN, yytext()); }
"++" 		{ return next_token(sym.INCREMENT, yytext()); }
"--" 		{ return next_token(sym.DECREMENT, yytext()); }
"==" 		{ return next_token(sym.EQUAL, yytext()); }
"!=" 		{ return next_token(sym.NOTEQUAL, yytext()); }
">" 		{ return next_token(sym.GREATER, yytext()); }
">=" 		{ return next_token(sym.GREATEREQUAL, yytext()); }
"<" 		{ return next_token(sym.LESS, yytext()); }
"<=" 		{ return next_token(sym.LESSEQUAL, yytext()); }
"&&" 		{ return next_token(sym.LOGICALAND, yytext()); }
"||" 		{ return next_token(sym.LOGICALOR, yytext()); }
"(" 		{ return next_token(sym.LEFTPAREN, yytext()); }
")" 		{ return next_token(sym.RIGHTPAREN, yytext()); }
"[" 		{ return next_token(sym.LEFTSQBRACKET, yytext()); }
"]" 		{ return next_token(sym.RIGHTSQBRACKET, yytext()); }
"{" 		{ return next_token(sym.LEFTBRACE, yytext()); }
"}" 		{ return next_token(sym.RIGHTBRACE, yytext()); }
"^^"		{ return next_token(sym.KAPICA, yytext()); }

"//"								{ yybegin(COMMENT); }
<COMMENT> 	. 						{ yybegin(COMMENT); }
<COMMENT> 	"\r\n" 					{ yybegin(YYINITIAL); }	

(true|false)						{ return next_token(sym.BOOLCONST, new Boolean(yytext())); }
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* 		{ return next_token(sym.IDENT, yytext()); }
[0-9]+								{ return next_token(sym.NUMBER, new Integer(yytext())); }
"'"([a-z]|[A-Z]|[0-9])"'"			{ return next_token(sym.CHARCONST, new Character(yytext().charAt(1))); }

//-------------------------------------------------------------------------------------------------------

.  { System.err.println("Leksicka greska (" + yytext() + ") u liniji " + (yyline+1)); }

//-------------------------------------------------------------------------------------------------------