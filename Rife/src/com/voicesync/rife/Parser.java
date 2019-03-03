package com.voicesync.rife;

import java.io.BufferedReader;
import java.io.IOException;

public class Parser { // generic parser
	public char ch;
	char[]in;
	int cin, lin, chInt;
	public int sym;
	public String id;
	String snum, sstring;
	float fnum;
	double dnum;
	public final static int sNULL=0,  sNUMBER=1, sIDENT=2, sSTRING=3;
	boolean bufferMode;
	BufferedReader buffReader;
	
	public Parser() {bufferMode=false; cin=lin=0; ch=0;}
	public Parser(char[]in) {this.in=in; cin=0; ch=' '; if (in!=null) lin=in.length; else lin=0; bufferMode=false;}
	public Parser(BufferedReader buffReader) {	this.buffReader=buffReader; 				bufferMode=true; ch=' ';}
	public boolean islower(char c) {return (c >= 'a' && c <= 'z');	}
	public boolean isupper(char c) {return (c >= 'A' && c <= 'Z');	}
	public boolean isalpha(char c) {return (islower(c) || isupper(c));}
	public boolean isdigit(char c) {return (c >= '0' && c <= '9');	}
	public boolean isalnum(char c) {return (isalpha(c) || isdigit(c));	}
	public boolean isident(char c) {return (isalpha(c) || isdigit(c) || c=='_' );}
	public boolean isfloat(char c) {return (isdigit(c) || c=='.' | c=='-' | c=='+');}
	public boolean isfloatBody(char c) {return isfloat(c) | ch=='e' | ch=='E'| ch=='+' | ch=='-';}

	public char getch() {
		if (bufferMode) {try {chInt=buffReader.read(); ch=(chInt==-1)?0:(char)chInt; cin++;} catch (IOException e) {ch=0;}} 
		else 			{if (cin<lin) ch=in[cin++]; else ch=0;}
		return ch;
	}
	public boolean notEOF() { return ch!=0; }
	public int getSym() { 
		id=snum=""; fnum=0; //init
		sym=sNULL;
		do {// skip junk & comments
			while (ch<=' ' & notEOF()) getch(); 
			while (ch=='#') {while (getch()!='\n' & notEOF()); getch(); }
		} while (ch<=' ' & notEOF());
		if (notEOF()) {
			if (isalpha(ch)) {
				sym=sIDENT;	for (id=""+ch; isident(getch()); id+=ch); 
			} else
				if (isfloat(ch)) { // not valid for expressions, --3
					sym=sNUMBER;
					for (snum=""+ch; isfloatBody(getch()) ; snum+=ch);	
					try {fnum=Float.valueOf(snum);} catch (NumberFormatException e) { fnum=0;}
					try {dnum=Double.valueOf(snum);} catch (NumberFormatException e) { dnum=0;}
				} else 
					if (ch=='"') {	sym=sSTRING; for (sstring=""; getch()!='"'; sstring+=ch); getch();	}
					else {
						sym=ch; getch();
					}
		}
		return sym;
	}
	public boolean isToken(String tok) {	return sym==sIDENT & tok.compareTo(id)==0;	}
	public float getfNum() { return fnum; }
	public double getsymdNum() { getSym(); return dnum; }
	public float getsymfNum() { getSym(); return fnum; }
}