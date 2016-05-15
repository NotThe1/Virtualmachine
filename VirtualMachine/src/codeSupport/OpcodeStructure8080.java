package codeSupport;

public class OpcodeStructure8080 {

	// public OpcodeStructure8080() {
	// // TODO Auto-generated constructor stub
	// }

	private byte opCode;
	private int size;
	private String instruction;
	private String source;
	private String destination;
	private String function;
	private int pcAction; // program counter action

	OpcodeStructure8080(byte opCode, int size, String instruction, String destination, String source, String function) {
		this(opCode, size, instruction, destination, source, function, NORMAL);
	}// CONSTRUCTOR

	OpcodeStructure8080(byte opCode, int size, String instruction, String destination, String source, String function,
			int pcAction) {
		this.opCode = opCode;
		this.size = size;
		this.instruction = instruction;
		this.source = source;
		this.destination = destination;
		this.function = function;
		this.pcAction = pcAction;
	}// CONSTRUCTOR

	public byte getOpCode() {
		return this.opCode;
	}// getOpCode

	public int getSize() {
		return this.size;
	}// getSize

	public String getInstruction() {
		return this.instruction;
	}// getInstruction

	public String getSource() {
		return this.source;
	}// getSource

	public String getDestination() {
		return this.destination;
	}// getDestination

	public String getFunction() {
		return this.function;
	}// getFunction
	
	public int getPcAction() {
		return this.pcAction;
	}// getFunction

	public String getFunctionFormatted() {
		return String.format("%8s%s%n", "", this.function);
	}// getFunctionFormatted

	public String getAssemblerCode() {
		return String.format("%-4s %s%s", getInstruction(), getDestination(), getSource());
	}// getAssemblerCode

	public String getAssemblerCode(byte plusOne) {
		String ans;
		if (getDestination().equals("D8")) {
			ans = String.format("%-4s 0%02XH",
					getInstruction(), plusOne);
		} else {
			ans = String.format("%-4s %s,0%02XH",
					getInstruction(), getDestination(), plusOne);
		}
		return ans;
	}// getAssemblerCode

	public String getAssemblerCode(byte plusOne, byte plusTwo) {
		String ans;
		if (getDestination().equals("addr")) {
			ans = String.format("%-4s 0%02X%02XH",
					getInstruction(), plusTwo, plusOne);	//0%02X%02XH
		} else {
			ans = String.format("%-4s %s,0%02X%02XH", getInstruction(), getDestination(), plusTwo, plusOne);
		}
		return ans;
	}// getAssemblerCode

	public final static int NORMAL = 0;
	public final static int TOTAL = 1; // JMP POP L
	public final static int CONTINUATION = 2; // CALL and All conditionals
	public final static int TERMINATES = 3; // RET

}// class OpcodeStructure8080
