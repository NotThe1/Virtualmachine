package codeSupport;

import java.io.Serializable;

//import codeSupport.ManualDisassembler.CodeFragment;

public class CodeFragment implements Comparable<CodeFragment>, Serializable {
	static final long serialVersionUID = 1L;
	public int startLoc;
	public int endLoc;
	public String type;

	public static final String CODE = "code";
	public static final String CONSTANT = "constant";
	public static final String LITERAL = "literal";
	public static final String RESERVED = "reserved";
	public static final String UNKNOWN = "unknown";
	

	public CodeFragment(int startLoc, int endLoc, String type) {
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.type = (type == null) ? UNKNOWN : type;
		if (startLoc > endLoc) {
			this.endLoc = startLoc;
			System.out.printf("<Constructor - CodeFragment startLoc = %04X, endLoc = %04X%n", startLoc, endLoc);
			this.type = UNKNOWN;
		}//
	}// Constructor

	public int size() {
		return endLoc - startLoc + 1;
	}// size

	public String toString() {
		return String.format("%04X : %04X ; (%04X) %s", startLoc, endLoc, size(), this.type);
	}// toString

	@Override
	public int compareTo(CodeFragment ct0) {
		return startLoc - ct0.startLoc;
	}// compareTo
}// class CodeType