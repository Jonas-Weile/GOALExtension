parser grammar PLAN2GParser;

options{ tokenVocab=GOALLexer; }

plan
	: useclause* focusoption? PLANNER ID PARLIST? '{'  mainTask method* operator* '}' EOF
	;


useclause
	: USE ref (',' ref)* (AS usecase)? '.'
	;

	
ref
	: string | (ID ('.' ID)*)
	;
	

string
	: (StringLiteral ('+' StringLiteral)*)
	| (SingleQuotedStringLiteral ('+' SingleQuotedStringLiteral)*)
	;


usecase
	: KNOWLEDGE | ACTIONSPEC
	;
	
	
focusoption
	: FOCUS '=' (value = NONE | value = SELECT | value = FILTER) '.'
	;
		
	
mainTask
	: TASK task
	;
	
task
	: ID PARLIST?
	; 

	
	
mentalatom  
	: mentalop PARLIST
	;
	
	
mentalop 
	: AGOAL_OP
	;
	

method
	: METHOD ID PARLIST? WITH decompositions+
	;
	
	
decompositions
	:	mentalatom? pre subtasks
	;

		
subtasks
	: SUBTASKS '{' ( task (',' task)* )? '}'
	;
				
operator 
	: OPERATOR ID PARLIST? WITH pre (post | (postdel postadd))
	;
	

pre
	: PRE KR_BLOCK
	;


post
	: POST KR_BLOCK
	;

	
postadd
	: POSTADD KR_BLOCK
	;

	
postdel
	: POSTDEL KR_BLOCK
	;