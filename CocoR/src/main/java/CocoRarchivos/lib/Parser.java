
package CocoRarchivos.lib;

public class Parser {
	public static final int _EOF = 0;
	public static final int _colon = 1;
	public static final int _comma = 2;
	public static final int _lbrace = 3;
	public static final int _rbrace = 4;
	public static final int _lbracket = 5;
	public static final int _rbracket = 6;
	public static final int _ident = 7;
	public static final int _string_ = 8;
	public static final int _badString = 9;
	public static final int _integer_ = 10;
	public static final int _double_ = 11;
	public static final int maxT = 15;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	

	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void JSON() {
		if (la.kind == 3) {
			jsonobject();
		} else if (la.kind == 5) {
			jsonarray();
		} else SynErr(16);
	}

	void jsonobject() {
		object();
	}

	void jsonarray() {
		array();
	}

	void object() {
		Expect(3);
		if (la.kind == 8) {
			objectelement();
			while (la.kind == 2) {
				Get();
				objectelement();
			}
		}
		Expect(4);
	}

	void array() {
		Expect(5);
		if (StartOf(1)) {
			value();
			while (la.kind == 2) {
				Get();
				value();
			}
		}
		Expect(6);
	}

	void objectelement() {
		Expect(8);
		Expect(1);
		value();
	}

	void value() {
		switch (la.kind) {
		case 8: {
			Get();
			break;
		}
		case 10: {
			Get();
			break;
		}
		case 11: {
			Get();
			break;
		}
		case 3: {
			object();
			break;
		}
		case 5: {
			array();
			break;
		}
		case 12: {
			Get();
			break;
		}
		case 13: {
			Get();
			break;
		}
		case 14: {
			Get();
			break;
		}
		default: SynErr(17); break;
		}
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		JSON();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_x,_x,_T, _x,_T,_x,_x, _T,_x,_T,_T, _T,_T,_T,_x, _x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "colon expected"; break;
			case 2: s = "comma expected"; break;
			case 3: s = "lbrace expected"; break;
			case 4: s = "rbrace expected"; break;
			case 5: s = "lbracket expected"; break;
			case 6: s = "rbracket expected"; break;
			case 7: s = "ident expected"; break;
			case 8: s = "string_ expected"; break;
			case 9: s = "badString expected"; break;
			case 10: s = "integer_ expected"; break;
			case 11: s = "double_ expected"; break;
			case 12: s = "\"true\" expected"; break;
			case 13: s = "\"false\" expected"; break;
			case 14: s = "\"null\" expected"; break;
			case 15: s = "??? expected"; break;
			case 16: s = "invalid JSON"; break;
			case 17: s = "invalid value"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
