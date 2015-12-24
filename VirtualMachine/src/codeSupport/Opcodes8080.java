package codeSupport;

import java.util.HashMap;


public class Opcodes8080 {
	HashMap<Byte, OpcodeStructure8080> opcodeMap;
	
	private Opcodes8080() {
		makeOpcodeMap();
	}//CONSTRUCTOR
	
	public static Opcodes8080 makeCodeMap(){
		return new Opcodes8080();
	}//makeCodeMap
	
	public OpcodeStructure8080 get(byte index){
		return opcodeMap.get(index);
	}
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	private static final int  NORMAL = OpcodeStructure8080.NORMAL;
	private static final int TOTAL = OpcodeStructure8080.TOTAL; // JMP POP PCHL
	private static final int CONTINUATION = OpcodeStructure8080.CONTINUATION; // CALL and All conditionals
	private static final int TERMINATES = OpcodeStructure8080.TERMINATES; // RET

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	private void makeOpcodeMap() {
		opcodeMap = new HashMap<Byte, OpcodeStructure8080>();
		opcodeMap.put((byte) 0X00, new OpcodeStructure8080((byte) 0X00, 1, "NOP", "", "", ""));
		opcodeMap.put((byte) 0X01, new OpcodeStructure8080((byte) 0X01, 3, "LXI", "B", "D16", "B<- byte3,C<- byte2"));
		opcodeMap.put((byte) 0X02, new OpcodeStructure8080((byte) 0X02, 1, "STAX", "B", "", "(BC)<-A"));
		opcodeMap.put((byte) 0X03, new OpcodeStructure8080((byte) 0X03, 1, "INX", "B", "", "BC<-BC+1"));
		opcodeMap.put((byte) 0X04, new OpcodeStructure8080((byte) 0X04, 1, "INR", "B", "", "B<-B+1"));
		opcodeMap.put((byte) 0X05, new OpcodeStructure8080((byte) 0X05, 1, "DCR", "B", "", "B<-B-1"));
		opcodeMap.put((byte) 0X06, new OpcodeStructure8080((byte) 0X06, 2, "MVI", "B", "D8", "B<-byte2"));
		opcodeMap.put((byte) 0X07, new OpcodeStructure8080((byte) 0X07, 1, "RLC", "", "", "A=A << not thru carry"));
		opcodeMap.put((byte) 0X08, new OpcodeStructure8080((byte) 0X08, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X09, new OpcodeStructure8080((byte) 0X09, 1, "DAD", "B", "", "HL = HL + BC"));
		opcodeMap.put((byte) 0X0A, new OpcodeStructure8080((byte) 0X0A, 1, "LDAX", "B", "", "A<-(BC)"));
		opcodeMap.put((byte) 0X0B, new OpcodeStructure8080((byte) 0X0B, 1, "DCX", "B", "", "BC = BC-1"));
		opcodeMap.put((byte) 0X0C, new OpcodeStructure8080((byte) 0X0C, 1, "INR", "C", "", "C <-C+1"));
		opcodeMap.put((byte) 0X0D, new OpcodeStructure8080((byte) 0X0D, 1, "DCR", "C", "", "C <-C-1"));
		opcodeMap.put((byte) 0X0E, new OpcodeStructure8080((byte) 0X0E, 2, "MVI", "C", "D8", "C,-byte2"));
		opcodeMap.put((byte) 0X0F, new OpcodeStructure8080((byte) 0X0F, 1, "RRC", "", "", "A = A>> not thru carry"));

		opcodeMap.put((byte) 0X10, new OpcodeStructure8080((byte) 0X10, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X11, new OpcodeStructure8080((byte) 0X11, 3, "LXI", "D", "D16", "D<-byte3,E<-byte2"));
		opcodeMap.put((byte) 0X12, new OpcodeStructure8080((byte) 0X12, 1, "STAX", "D", "", "(DE)<-A"));
		opcodeMap.put((byte) 0X13, new OpcodeStructure8080((byte) 0X13, 1, "INX", "D", "", "DE<-DE + 1"));
		opcodeMap.put((byte) 0X14, new OpcodeStructure8080((byte) 0X14, 1, "INR", "D", "", "D<-D+1"));
		opcodeMap.put((byte) 0X15, new OpcodeStructure8080((byte) 0X15, 1, "DCR", "D", "", "D<-D-1"));
		opcodeMap.put((byte) 0X16, new OpcodeStructure8080((byte) 0X16, 2, "MVI", "D", "D8", "D<-byte2"));
		opcodeMap.put((byte) 0X17, new OpcodeStructure8080((byte) 0X17, 1, "RAL", "D", "", "A=A << thru carry"));
		opcodeMap.put((byte) 0X18, new OpcodeStructure8080((byte) 0X18, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X19, new OpcodeStructure8080((byte) 0X19, 1, "DAD", "D", "", "HL = HL + DE"));
		opcodeMap.put((byte) 0X1A, new OpcodeStructure8080((byte) 0X1A, 1, "LDAX", "D", "", "A<-(DE)"));
		opcodeMap.put((byte) 0X1B, new OpcodeStructure8080((byte) 0X1B, 1, "DCX", "D", "", "DE = DE-1"));
		opcodeMap.put((byte) 0X1C, new OpcodeStructure8080((byte) 0X1C, 1, "INR", "E", "", "E <-E+1"));
		opcodeMap.put((byte) 0X1D, new OpcodeStructure8080((byte) 0X1D, 1, "DCR", "E", "", "E <-E-1"));
		opcodeMap.put((byte) 0X1E, new OpcodeStructure8080((byte) 0X1E, 2, "MVI", "E", "D8", "E,-byte2"));
		opcodeMap.put((byte) 0X1F, new OpcodeStructure8080((byte) 0X1F, 1, "RAR", "", "", "A = A>>  thru carry"));

		opcodeMap.put((byte) 0X20, new OpcodeStructure8080((byte) 0X20, 1, "NOP*", "", "", "Alt NOP (RIM)"));// special
		opcodeMap.put((byte) 0X21, new OpcodeStructure8080((byte) 0X21, 3, "LXI", "H", "D16", "H<-byte3,L<-byte2"));
		opcodeMap.put((byte) 0X22,
				new OpcodeStructure8080((byte) 0X22, 3, "SHLD", "addr", "", "(addr)<-L;(addr+1)<-H"));
		opcodeMap.put((byte) 0X23, new OpcodeStructure8080((byte) 0X23, 1, "INX", "H", "", "HL<-HL + 1"));
		opcodeMap.put((byte) 0X24, new OpcodeStructure8080((byte) 0X24, 1, "INR", "H", "", "H<-H+1"));
		opcodeMap.put((byte) 0X25, new OpcodeStructure8080((byte) 0X25, 1, "DCR", "H", "", "H<-H-1"));
		opcodeMap.put((byte) 0X26, new OpcodeStructure8080((byte) 0X26, 2, "MVI", "H", "D8", "H<-byte2"));
		opcodeMap.put((byte) 0X27, new OpcodeStructure8080((byte) 0X27, 1, "DAA", "", "", "")); // special
		opcodeMap.put((byte) 0X28, new OpcodeStructure8080((byte) 0X28, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X29, new OpcodeStructure8080((byte) 0X29, 1, "DAD", "H", "", "HL = HL + HL"));
		opcodeMap.put((byte) 0X2A,
				new OpcodeStructure8080((byte) 0X2A, 3, "LHLD", "addr", "", "L<-(addr);H<-(addr+1)"));
		opcodeMap.put((byte) 0X2B, new OpcodeStructure8080((byte) 0X2B, 1, "DCX", "H", "", "HL = HL-1"));
		opcodeMap.put((byte) 0X2C, new OpcodeStructure8080((byte) 0X2C, 1, "INR", "L", "", "L <-L+1"));
		opcodeMap.put((byte) 0X2D, new OpcodeStructure8080((byte) 0X2D, 1, "DCR", "L", "", "L <-L-1"));
		opcodeMap.put((byte) 0X2E, new OpcodeStructure8080((byte) 0X2E, 2, "MVI", "L", "D8", "L,-byte2"));
		opcodeMap.put((byte) 0X2F, new OpcodeStructure8080((byte) 0X2F, 1, "CMA", "", "", "A = !A"));

		opcodeMap.put((byte) 0X30, new OpcodeStructure8080((byte) 0X30, 1, "Alt", "", "", "Alt NOP(SIM)")); // special
		opcodeMap.put((byte) 0X31, new OpcodeStructure8080((byte) 0X31, 3, "LXI", "SP", "D16", "SP-data"));
		opcodeMap.put((byte) 0X32, new OpcodeStructure8080((byte) 0X32, 3, "STA", "addr", "", "(addr)<-A"));
		opcodeMap.put((byte) 0X33, new OpcodeStructure8080((byte) 0X33, 1, "INX", "SP", "", "SP<-SP + 1"));
		opcodeMap.put((byte) 0X34, new OpcodeStructure8080((byte) 0X34, 1, "INR", "M", "", "(HL)<-(HL)+1"));
		opcodeMap.put((byte) 0X35, new OpcodeStructure8080((byte) 0X35, 1, "DCR", "M", "", "(HL)<-(HL)-1"));
		opcodeMap.put((byte) 0X36, new OpcodeStructure8080((byte) 0X36, 2, "MVI", "M", "D8", "(HL)<-byte2"));
		opcodeMap.put((byte) 0X37, new OpcodeStructure8080((byte) 0X37, 1, "STC", "", "", "CY=1"));
		opcodeMap.put((byte) 0X38, new OpcodeStructure8080((byte) 0X38, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X39, new OpcodeStructure8080((byte) 0X39, 1, "DAD", "SP", "", "HL = HL + SP"));
		opcodeMap.put((byte) 0X3A, new OpcodeStructure8080((byte) 0X3A, 3, "LDA", "addr", "", "A<-(addr)"));
		opcodeMap.put((byte) 0X3B, new OpcodeStructure8080((byte) 0X3B, 1, "DCX", "SP", "", "SP = SP-1"));
		opcodeMap.put((byte) 0X3C, new OpcodeStructure8080((byte) 0X3C, 1, "INR", "A", "", "A <-A+1"));
		opcodeMap.put((byte) 0X3D, new OpcodeStructure8080((byte) 0X3D, 1, "DCR", "A", "", "A <-A-1"));
		opcodeMap.put((byte) 0X3E, new OpcodeStructure8080((byte) 0X3E, 2, "MVI", "A", "D8", "A,-byte2"));
		opcodeMap.put((byte) 0X3F, new OpcodeStructure8080((byte) 0X3F, 1, "CMC", "", "", "CY=!CY"));

		opcodeMap.put((byte) 0X40, new OpcodeStructure8080((byte) 0X40, 1, "MOV", "B", ",B", "B <- B"));
		opcodeMap.put((byte) 0X41, new OpcodeStructure8080((byte) 0X41, 1, "MOV", "B", ",C", "B <- C"));
		opcodeMap.put((byte) 0X42, new OpcodeStructure8080((byte) 0X42, 1, "MOV", "B", ",D", "B <- D"));
		opcodeMap.put((byte) 0X43, new OpcodeStructure8080((byte) 0X43, 1, "MOV", "B", ",E", "B <- E"));
		opcodeMap.put((byte) 0X44, new OpcodeStructure8080((byte) 0X44, 1, "MOV", "B", ",H", "B <- H"));
		opcodeMap.put((byte) 0X45, new OpcodeStructure8080((byte) 0X45, 1, "MOV", "B", ",L", "B <- L"));
		opcodeMap.put((byte) 0X46, new OpcodeStructure8080((byte) 0X46, 1, "MOV", "B", ",M", "B <- (HL)"));
		opcodeMap.put((byte) 0X47, new OpcodeStructure8080((byte) 0X47, 1, "MOV", "B", ",A", "B <- A"));

		opcodeMap.put((byte) 0X48, new OpcodeStructure8080((byte) 0X48, 1, "MOV", "C", ",B", "C <- B"));
		opcodeMap.put((byte) 0X49, new OpcodeStructure8080((byte) 0X49, 1, "MOV", "C", ",C", "C <- C"));
		opcodeMap.put((byte) 0X4A, new OpcodeStructure8080((byte) 0X4A, 1, "MOV", "C", ",D", "C <- D"));
		opcodeMap.put((byte) 0X4B, new OpcodeStructure8080((byte) 0X4B, 1, "MOV", "C", ",E", "C <- E"));
		opcodeMap.put((byte) 0X4C, new OpcodeStructure8080((byte) 0X4C, 1, "MOV", "C", ",H", "C <- H"));
		opcodeMap.put((byte) 0X4D, new OpcodeStructure8080((byte) 0X4D, 1, "MOV", "C", ",L", "C <- L"));
		opcodeMap.put((byte) 0X4E, new OpcodeStructure8080((byte) 0X4E, 1, "MOV", "C", ",M", "C <- (HL)"));
		opcodeMap.put((byte) 0X4F, new OpcodeStructure8080((byte) 0X4F, 1, "MOV", "C", ",A", "C <- A"));

		opcodeMap.put((byte) 0X50, new OpcodeStructure8080((byte) 0X50, 1, "MOV", "D", ",B", "D <- B"));
		opcodeMap.put((byte) 0X51, new OpcodeStructure8080((byte) 0X51, 1, "MOV", "D", ",C", "D <- C"));
		opcodeMap.put((byte) 0X52, new OpcodeStructure8080((byte) 0X52, 1, "MOV", "D", ",D", "D <- D"));
		opcodeMap.put((byte) 0X53, new OpcodeStructure8080((byte) 0X53, 1, "MOV", "D", ",E", "D <- E"));
		opcodeMap.put((byte) 0X54, new OpcodeStructure8080((byte) 0X54, 1, "MOV", "D", ",H", "D <- H"));
		opcodeMap.put((byte) 0X55, new OpcodeStructure8080((byte) 0X55, 1, "MOV", "D", ",L", "D <- L"));
		opcodeMap.put((byte) 0X56, new OpcodeStructure8080((byte) 0X56, 1, "MOV", "D", ",M", "D <- (HL)"));
		opcodeMap.put((byte) 0X57, new OpcodeStructure8080((byte) 0X57, 1, "MOV", "D", ",A", "D <- A"));

		opcodeMap.put((byte) 0X58, new OpcodeStructure8080((byte) 0X58, 1, "MOV", "E", ",B", "E <- B"));
		opcodeMap.put((byte) 0X59, new OpcodeStructure8080((byte) 0X59, 1, "MOV", "E", ",C", "E <- C"));
		opcodeMap.put((byte) 0X5A, new OpcodeStructure8080((byte) 0X5A, 1, "MOV", "E", ",D", "E <- D"));
		opcodeMap.put((byte) 0X5B, new OpcodeStructure8080((byte) 0X5B, 1, "MOV", "E", ",E", "E <- E"));
		opcodeMap.put((byte) 0X5C, new OpcodeStructure8080((byte) 0X5C, 1, "MOV", "E", ",H", "E <- H"));
		opcodeMap.put((byte) 0X5D, new OpcodeStructure8080((byte) 0X5D, 1, "MOV", "E", ",L", "E <- L"));
		opcodeMap.put((byte) 0X5E, new OpcodeStructure8080((byte) 0X5E, 1, "MOV", "E", ",M", "E <- (HL)"));
		opcodeMap.put((byte) 0X5F, new OpcodeStructure8080((byte) 0X5F, 1, "MOV", "E", ",A", "E <- A"));

		opcodeMap.put((byte) 0X60, new OpcodeStructure8080((byte) 0X60, 1, "MOV", "H", ",B", "H <- B"));
		opcodeMap.put((byte) 0X61, new OpcodeStructure8080((byte) 0X61, 1, "MOV", "H", ",C", "H <- C"));
		opcodeMap.put((byte) 0X62, new OpcodeStructure8080((byte) 0X62, 1, "MOV", "H", ",D", "H <- D"));
		opcodeMap.put((byte) 0X63, new OpcodeStructure8080((byte) 0X63, 1, "MOV", "H", ",E", "H <- E"));
		opcodeMap.put((byte) 0X64, new OpcodeStructure8080((byte) 0X64, 1, "MOV", "H", ",H", "H <- H"));
		opcodeMap.put((byte) 0X65, new OpcodeStructure8080((byte) 0X65, 1, "MOV", "H", ",L", "H <- L"));
		opcodeMap.put((byte) 0X66, new OpcodeStructure8080((byte) 0X66, 1, "MOV", "H", ",M", "H <- (HL)"));
		opcodeMap.put((byte) 0X67, new OpcodeStructure8080((byte) 0X67, 1, "MOV", "H", ",A", "H <- A"));

		opcodeMap.put((byte) 0X68, new OpcodeStructure8080((byte) 0X68, 1, "MOV", "L", ",B", "L <- B"));
		opcodeMap.put((byte) 0X69, new OpcodeStructure8080((byte) 0X69, 1, "MOV", "L", ",C", "L <- C"));
		opcodeMap.put((byte) 0X6A, new OpcodeStructure8080((byte) 0X6A, 1, "MOV", "L", ",D", "L <- D"));
		opcodeMap.put((byte) 0X6B, new OpcodeStructure8080((byte) 0X6B, 1, "MOV", "L", ",E", "L <- E"));
		opcodeMap.put((byte) 0X6C, new OpcodeStructure8080((byte) 0X6C, 1, "MOV", "L", ",H", "L <- H"));
		opcodeMap.put((byte) 0X6D, new OpcodeStructure8080((byte) 0X6D, 1, "MOV", "L", ",L", "L <- L"));
		opcodeMap.put((byte) 0X6E, new OpcodeStructure8080((byte) 0X6E, 1, "MOV", "L", ",M", "L <- (HL)"));
		opcodeMap.put((byte) 0X6F, new OpcodeStructure8080((byte) 0X6F, 1, "MOV", "L", ",A", "L <- A"));

		opcodeMap.put((byte) 0X70, new OpcodeStructure8080((byte) 0X70, 1, "MOV", "M", ",B", "(HL) <- B"));
		opcodeMap.put((byte) 0X71, new OpcodeStructure8080((byte) 0X71, 1, "MOV", "M", ",C", "(HL) <- C"));
		opcodeMap.put((byte) 0X72, new OpcodeStructure8080((byte) 0X72, 1, "MOV", "M", ",D", "(HL) <- D"));
		opcodeMap.put((byte) 0X73, new OpcodeStructure8080((byte) 0X73, 1, "MOV", "M", ",E", "(HL) <- E"));
		opcodeMap.put((byte) 0X74, new OpcodeStructure8080((byte) 0X74, 1, "MOV", "M", ",H", "(HL) <- H"));
		opcodeMap.put((byte) 0X75, new OpcodeStructure8080((byte) 0X75, 1, "MOV", "M", ",L", "(HL) <- L"));
		opcodeMap.put((byte) 0X76, new OpcodeStructure8080((byte) 0X76, 1, "HLT", "", "", "Halt")); // Special
		opcodeMap.put((byte) 0X77, new OpcodeStructure8080((byte) 0X77, 1, "MOV", "M", ",A", "(HL) <- A"));

		opcodeMap.put((byte) 0X78, new OpcodeStructure8080((byte) 0X78, 1, "MOV", "A", ",B", "A <- B"));
		opcodeMap.put((byte) 0X79, new OpcodeStructure8080((byte) 0X79, 1, "MOV", "A", ",C", "A <- C"));
		opcodeMap.put((byte) 0X7A, new OpcodeStructure8080((byte) 0X7A, 1, "MOV", "A", ",D", "A <- D"));
		opcodeMap.put((byte) 0X7B, new OpcodeStructure8080((byte) 0X7B, 1, "MOV", "A", ",E", "A <- E"));
		opcodeMap.put((byte) 0X7C, new OpcodeStructure8080((byte) 0X7C, 1, "MOV", "A", ",H", "A <- H"));
		opcodeMap.put((byte) 0X7D, new OpcodeStructure8080((byte) 0X7D, 1, "MOV", "A", ",L", "A <- L"));
		opcodeMap.put((byte) 0X7E, new OpcodeStructure8080((byte) 0X7E, 1, "MOV", "A", ",M", "A <- (HL)"));
		opcodeMap.put((byte) 0X7F, new OpcodeStructure8080((byte) 0X7F, 1, "MOV", "A", ",A", "A <- A"));

		opcodeMap.put((byte) 0X80, new OpcodeStructure8080((byte) 0X80, 1, "ADD", "B", "", "A <- A+B"));
		opcodeMap.put((byte) 0X81, new OpcodeStructure8080((byte) 0X81, 1, "ADD", "C", "", "A <- A+C"));
		opcodeMap.put((byte) 0X82, new OpcodeStructure8080((byte) 0X82, 1, "ADD", "D", "", "A <- A+D"));
		opcodeMap.put((byte) 0X83, new OpcodeStructure8080((byte) 0X83, 1, "ADD", "E", "", "A <- A+E"));
		opcodeMap.put((byte) 0X84, new OpcodeStructure8080((byte) 0X84, 1, "ADD", "H", "", "A <- A+H"));
		opcodeMap.put((byte) 0X85, new OpcodeStructure8080((byte) 0X85, 1, "ADD", "L", "", "A <- A+L"));
		opcodeMap.put((byte) 0X86, new OpcodeStructure8080((byte) 0X86, 1, "ADD", "M", "", "A <- A+(HL)"));
		opcodeMap.put((byte) 0X87, new OpcodeStructure8080((byte) 0X87, 1, "ADD", "A", "", "A <- A+A"));

		opcodeMap.put((byte) 0X88, new OpcodeStructure8080((byte) 0X88, 1, "ADC", "B", "", "A <- A+B + CY"));
		opcodeMap.put((byte) 0X89, new OpcodeStructure8080((byte) 0X89, 1, "ADC", "C", "", "A <- A+C + CY"));
		opcodeMap.put((byte) 0X8A, new OpcodeStructure8080((byte) 0X8A, 1, "ADC", "D", "", "A <- A+D + CY"));
		opcodeMap.put((byte) 0X8B, new OpcodeStructure8080((byte) 0X8B, 1, "ADC", "E", "", "A <- A+E + CY"));
		opcodeMap.put((byte) 0X8C, new OpcodeStructure8080((byte) 0X8C, 1, "ADC", "H", "", "A <- A+H + CY"));
		opcodeMap.put((byte) 0X8D, new OpcodeStructure8080((byte) 0X8D, 1, "ADC", "L", "", "A <- A+L + CY"));
		opcodeMap.put((byte) 0X8E, new OpcodeStructure8080((byte) 0X8E, 1, "ADC", "M", "", "A <- A+(HL) + CY"));
		opcodeMap.put((byte) 0X8F, new OpcodeStructure8080((byte) 0X8F, 1, "ADC", "A", "", "A <- A+A + CY"));

		opcodeMap.put((byte) 0X90, new OpcodeStructure8080((byte) 0X90, 1, "SUB", "B", "", "A <- A-B"));
		opcodeMap.put((byte) 0X91, new OpcodeStructure8080((byte) 0X91, 1, "SUB", "C", "", "A <- A-C"));
		opcodeMap.put((byte) 0X92, new OpcodeStructure8080((byte) 0X92, 1, "SUB", "D", "", "A <- A-D"));
		opcodeMap.put((byte) 0X93, new OpcodeStructure8080((byte) 0X93, 1, "SUB", "E", "", "A <- A-E"));
		opcodeMap.put((byte) 0X94, new OpcodeStructure8080((byte) 0X94, 1, "SUB", "H", "", "A <- A-H"));
		opcodeMap.put((byte) 0X95, new OpcodeStructure8080((byte) 0X95, 1, "SUB", "L", "", "A <- A-L"));
		opcodeMap.put((byte) 0X96, new OpcodeStructure8080((byte) 0X96, 1, "SUB", "M", "", "A <- A-(HL)"));
		opcodeMap.put((byte) 0X97, new OpcodeStructure8080((byte) 0X97, 1, "SUB", "A", "", "A <- A-A"));

		opcodeMap.put((byte) 0X98, new OpcodeStructure8080((byte) 0X98, 1, "SBB", "B", "", "A <- A-B - CY"));
		opcodeMap.put((byte) 0X99, new OpcodeStructure8080((byte) 0X99, 1, "SBB", "C", "", "A <- A-C - CY"));
		opcodeMap.put((byte) 0X9A, new OpcodeStructure8080((byte) 0X9A, 1, "SBB", "D", "", "A <- A-D - CY"));
		opcodeMap.put((byte) 0X9B, new OpcodeStructure8080((byte) 0X9B, 1, "SBB", "E", "", "A <- A-E - CY"));
		opcodeMap.put((byte) 0X9C, new OpcodeStructure8080((byte) 0X9C, 1, "SBB", "H", "", "A <- A-H - CY"));
		opcodeMap.put((byte) 0X9D, new OpcodeStructure8080((byte) 0X9D, 1, "SBB", "L", "", "A <- A-L - CY"));
		opcodeMap.put((byte) 0X9E, new OpcodeStructure8080((byte) 0X9E, 1, "SBB", "M", "", "A <- A-(HL) - CY"));
		opcodeMap.put((byte) 0X9F, new OpcodeStructure8080((byte) 0X9F, 1, "SBB", "A", "", "A <- A-A - CY"));

		opcodeMap.put((byte) 0XA0, new OpcodeStructure8080((byte) 0XA0, 1, "ANA", "B", "", "A <- A&B"));
		opcodeMap.put((byte) 0XA1, new OpcodeStructure8080((byte) 0XA1, 1, "ANA", "C", "", "A <- A&C"));
		opcodeMap.put((byte) 0XA2, new OpcodeStructure8080((byte) 0XA2, 1, "ANA", "D", "", "A <- A&D"));
		opcodeMap.put((byte) 0XA3, new OpcodeStructure8080((byte) 0XA3, 1, "ANA", "E", "", "A <- A&E"));
		opcodeMap.put((byte) 0XA4, new OpcodeStructure8080((byte) 0XA4, 1, "ANA", "H", "", "A <- A&H"));
		opcodeMap.put((byte) 0XA5, new OpcodeStructure8080((byte) 0XA5, 1, "ANA", "L", "", "A <- A&L"));
		opcodeMap.put((byte) 0XA6, new OpcodeStructure8080((byte) 0XA6, 1, "ANA", "M", "", "A <- A&(HL)"));
		opcodeMap.put((byte) 0XA7, new OpcodeStructure8080((byte) 0XA7, 1, "ANA", "A", "", "A <- A&A"));

		opcodeMap.put((byte) 0XA8, new OpcodeStructure8080((byte) 0XA8, 1, "XRA", "B", "", "A <- A^B"));
		opcodeMap.put((byte) 0XA9, new OpcodeStructure8080((byte) 0XA9, 1, "XRA", "C", "", "A <- A^C"));
		opcodeMap.put((byte) 0XAA, new OpcodeStructure8080((byte) 0XAA, 1, "XRA", "D", "", "A <- A^D"));
		opcodeMap.put((byte) 0XAB, new OpcodeStructure8080((byte) 0XAB, 1, "XRA", "E", "", "A <- A^E"));
		opcodeMap.put((byte) 0XAC, new OpcodeStructure8080((byte) 0XAC, 1, "XRA", "H", "", "A <- A^H"));
		opcodeMap.put((byte) 0XAD, new OpcodeStructure8080((byte) 0XAD, 1, "XRA", "L", "", "A <- A^L"));
		opcodeMap.put((byte) 0XAE, new OpcodeStructure8080((byte) 0XAE, 1, "XRA", "M", "", "A <- A^(HL)"));
		opcodeMap.put((byte) 0XAF, new OpcodeStructure8080((byte) 0XAF, 1, "XRA", "A", "", "A <- A^A"));

		opcodeMap.put((byte) 0XB0, new OpcodeStructure8080((byte) 0XB0, 1, "ORA", "B", "", "A <- A|B"));
		opcodeMap.put((byte) 0XB1, new OpcodeStructure8080((byte) 0XB1, 1, "ORA", "C", "", "A <- A|C"));
		opcodeMap.put((byte) 0XB2, new OpcodeStructure8080((byte) 0XB2, 1, "ORA", "D", "", "A <- A|D"));
		opcodeMap.put((byte) 0XB3, new OpcodeStructure8080((byte) 0XB3, 1, "ORA", "E", "", "A <- A|E"));
		opcodeMap.put((byte) 0XB4, new OpcodeStructure8080((byte) 0XB4, 1, "ORA", "H", "", "A <- A|H"));
		opcodeMap.put((byte) 0XB5, new OpcodeStructure8080((byte) 0XB5, 1, "ORA", "L", "", "A <- A|L"));
		opcodeMap.put((byte) 0XB6, new OpcodeStructure8080((byte) 0XB6, 1, "ORA", "M", "", "A <- A|(HL)"));
		opcodeMap.put((byte) 0XB7, new OpcodeStructure8080((byte) 0XB7, 1, "ORA", "A", "", "A <- A|A"));

		opcodeMap.put((byte) 0XB8, new OpcodeStructure8080((byte) 0XB8, 1, "CMP", "B", "", "A - B"));
		opcodeMap.put((byte) 0XB9, new OpcodeStructure8080((byte) 0XB9, 1, "CMP", "C", "", "A - C"));
		opcodeMap.put((byte) 0XBA, new OpcodeStructure8080((byte) 0XBA, 1, "CMP", "D", "", "A - D"));
		opcodeMap.put((byte) 0XBB, new OpcodeStructure8080((byte) 0XBB, 1, "CMP", "E", "", "A - E"));
		opcodeMap.put((byte) 0XBC, new OpcodeStructure8080((byte) 0XBC, 1, "CMP", "H", "", "A - H"));
		opcodeMap.put((byte) 0XBD, new OpcodeStructure8080((byte) 0XBD, 1, "CMP", "L", "", "A - L"));
		opcodeMap.put((byte) 0XBE, new OpcodeStructure8080((byte) 0XBE, 1, "CMP", "M", "", "A - (HL)"));
		opcodeMap.put((byte) 0XBF, new OpcodeStructure8080((byte) 0XBF, 1, "CMP", "A", "", "A - A"));

		opcodeMap.put((byte) 0XC0, new OpcodeStructure8080((byte) 0XC0, 1, "RNZ", "", "", "if NZ, ret"));
		opcodeMap.put((byte) 0XC1, new OpcodeStructure8080((byte) 0XC1, 1, "POP", "B", "", ""));
		opcodeMap.put((byte) 0XC2, new OpcodeStructure8080((byte) 0XC2, 3, "JNZ", "addr", "", "if NZ,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XC3, new OpcodeStructure8080((byte) 0XC3, 3, "JMP", "addr", "", "PC<-addr", TOTAL));
		opcodeMap.put((byte) 0XC4, new OpcodeStructure8080((byte) 0XC4, 3, "CNZ", "addr", "", "if NZ,CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XC5, new OpcodeStructure8080((byte) 0XC5, 1, "PUSH", "B", "", ""));
		opcodeMap.put((byte) 0XC6, new OpcodeStructure8080((byte) 0XC6, 2, "ADI", "D8", "", "A<-A + byte2"));
		opcodeMap.put((byte) 0XC7, new OpcodeStructure8080((byte) 0XC7, 1, "RST", "0", "", "CALL $0", CONTINUATION));
		opcodeMap.put((byte) 0XC8, new OpcodeStructure8080((byte) 0XC8, 1, "RZ", "", "", "if Z, ret"));
		opcodeMap.put((byte) 0XC9, new OpcodeStructure8080((byte) 0XC9, 1, "RET", "", "", "PC<-(SP); SP<-SP+2", TERMINATES));
		opcodeMap.put((byte) 0XCA, new OpcodeStructure8080((byte) 0XCA, 3, "JZ", "addr", "", "if Z,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XCB, new OpcodeStructure8080((byte) 0XCB, 3, "Alt", "addr", "", "Alt RET PC<-addr", TOTAL));
		opcodeMap.put((byte) 0XCC, new OpcodeStructure8080((byte) 0XCC, 3, "CZ", "addr", "", "if Z,CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XCD, new OpcodeStructure8080((byte) 0XCD, 3, "CALL", "addr", "", "CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XCE, new OpcodeStructure8080((byte) 0XCE, 2, "ACI", "D8", "", "A<- A + data + cy"));
		opcodeMap.put((byte) 0XCF, new OpcodeStructure8080((byte) 0XCF, 1, "RST", "1", "", "CALL $8", CONTINUATION));

		opcodeMap.put((byte) 0XD0, new OpcodeStructure8080((byte) 0XD0, 1, "RNC", "", "", "if NCY, ret"));
		opcodeMap.put((byte) 0XD1, new OpcodeStructure8080((byte) 0XD1, 1, "POP", "D", "", ""));
		opcodeMap.put((byte) 0XD2, new OpcodeStructure8080((byte) 0XD2, 3, "JNC", "addr", "", "if NCY,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XD3, new OpcodeStructure8080((byte) 0XD3, 2, "OUT", "D8", "", "i/O")); // Special
		opcodeMap.put((byte) 0XD4, new OpcodeStructure8080((byte) 0XD4, 3, "CNC", "addr", "", "if NC,CALL addr, CONTINUATION"));
		opcodeMap.put((byte) 0XD5, new OpcodeStructure8080((byte) 0XD5, 1, "PUSH", "D", "", ""));
		opcodeMap.put((byte) 0XD6, new OpcodeStructure8080((byte) 0XD6, 2, "SUI", "D8", "", "A<-A - byte2"));
		opcodeMap.put((byte) 0XD7, new OpcodeStructure8080((byte) 0XD7, 1, "RST", "2", "", "CALL $10", CONTINUATION));
		opcodeMap.put((byte) 0XD8, new OpcodeStructure8080((byte) 0XD8, 1, "RC", "", "", "if CY, ret"));
		opcodeMap.put((byte) 0XD9, new OpcodeStructure8080((byte) 0XD9, 1, "RET*", "", "", "Alt RET", TOTAL));
		opcodeMap.put((byte) 0XDA, new OpcodeStructure8080((byte) 0XDA, 3, "JC", "addr", "", "if CY,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XDB, new OpcodeStructure8080((byte) 0XDB, 2, "IN", "D8", "", "i/O")); // Special
		opcodeMap.put((byte) 0XDC, new OpcodeStructure8080((byte) 0XDC, 3, "CC", "addr", "", "if CY,CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XDD, new OpcodeStructure8080((byte) 0XDD, 3, "Alt", "addr", "", "Alt CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XDE, new OpcodeStructure8080((byte) 0XDE, 2, "SBI", "D8", "", "A<- A - data - cy"));
		opcodeMap.put((byte) 0XDF, new OpcodeStructure8080((byte) 0XDF, 1, "RST", "3", "", "CALL $18", CONTINUATION));

		opcodeMap.put((byte) 0XE0, new OpcodeStructure8080((byte) 0XE0, 1, "RPO", "", "", "if PO, ret"));
		opcodeMap.put((byte) 0XE1, new OpcodeStructure8080((byte) 0XE1, 1, "POP", "H", "", ""));
		opcodeMap.put((byte) 0XE2, new OpcodeStructure8080((byte) 0XE2, 3, "JPO", "addr", "", "if PO,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XE3, new OpcodeStructure8080((byte) 0XE3, 1, "XTHL", "", "", "L<->(SP);H<->(SP+1)"));
		opcodeMap.put((byte) 0XE4, new OpcodeStructure8080((byte) 0XE4, 3, "CPO", "addr", "", "if PO,CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XE5, new OpcodeStructure8080((byte) 0XE5, 1, "PUSH", "H", "", ""));
		opcodeMap.put((byte) 0XE6, new OpcodeStructure8080((byte) 0XE6, 2, "ANI", "D8", "", "A<-A & byte2"));
		opcodeMap.put((byte) 0XE7, new OpcodeStructure8080((byte) 0XE7, 1, "RST", "4", "", "CALL $20", CONTINUATION));
		opcodeMap.put((byte) 0XE8, new OpcodeStructure8080((byte) 0XE8, 1, "RPE", "", "", "if PE, ret"));
		opcodeMap.put((byte) 0XE9, new OpcodeStructure8080((byte) 0XE9, 1, "PCHL", "", "", "PC.hi<-H;PC.lo<-L", TOTAL));
		opcodeMap.put((byte) 0XEA, new OpcodeStructure8080((byte) 0XEA, 3, "JPE", "addr", "", "if PE,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XEB, new OpcodeStructure8080((byte) 0XEB, 1, "XCHG", "", "", "H<->D;L<->E")); // Special
		opcodeMap.put((byte) 0XEC, new OpcodeStructure8080((byte) 0XEC, 3, "CPE", "addr", "", "if PE,CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XED, new OpcodeStructure8080((byte) 0XED, 3, "Alt", "addr", "", "Alt CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XEE, new OpcodeStructure8080((byte) 0XEE, 2, "XRI", "D8", "", "A<- A ^ data"));
		opcodeMap.put((byte) 0XEF, new OpcodeStructure8080((byte) 0XEF, 1, "RST", "5", "", "CALL $28", CONTINUATION));

		opcodeMap.put((byte) 0XF0, new OpcodeStructure8080((byte) 0XF0, 1, "RP", "", "", "if P, ret"));
		opcodeMap.put((byte) 0XF1, new OpcodeStructure8080((byte) 0XF1, 1, "POP", "PSW", "",
				"flags<-(SP);A<-(SP+1); SP<-SP+2"));
		opcodeMap.put((byte) 0XF2, new OpcodeStructure8080((byte) 0XF2, 3, "JP", "addr", "", "if P,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XF3, new OpcodeStructure8080((byte) 0XF3, 1, "DI", "", "", "")); // Special
		opcodeMap.put((byte) 0XF4, new OpcodeStructure8080((byte) 0XF4, 3, "CP", "addr", "", "if P,CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XF5, new OpcodeStructure8080((byte) 0XF5, 1, "PUSH", "PSW", "", ""));
		opcodeMap.put((byte) 0XF6, new OpcodeStructure8080((byte) 0XF6, 2, "ORI", "D8", "", "A<-A | byte2"));
		opcodeMap.put((byte) 0XF7, new OpcodeStructure8080((byte) 0XF7, 1, "RST", "6", "", "CALL $30", CONTINUATION));
		opcodeMap.put((byte) 0XF8, new OpcodeStructure8080((byte) 0XF8, 1, "RM", "", "", "if M, ret"));
		opcodeMap.put((byte) 0XF9, new OpcodeStructure8080((byte) 0XF9, 1, "SPHL", "", "", "SP=HL"));
		opcodeMap.put((byte) 0XFA, new OpcodeStructure8080((byte) 0XFA, 3, "JM", "addr", "", "if M,PC<-addr", CONTINUATION));
		opcodeMap.put((byte) 0XFB, new OpcodeStructure8080((byte) 0XFB, 1, "EI", "", "", "")); // Special
		opcodeMap.put((byte) 0XFC, new OpcodeStructure8080((byte) 0XFC, 3, "CM", "addr", "", "if M,CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XFD, new OpcodeStructure8080((byte) 0XFD, 3, "Alt", "addr", "", "Alt CALL addr", CONTINUATION));
		opcodeMap.put((byte) 0XFE, new OpcodeStructure8080((byte) 0XFE, 2, "CPI", "D8", "", "A - data"));
		opcodeMap.put((byte) 0XFF, new OpcodeStructure8080((byte) 0XFF, 1, "RST", "7", "", "CALL $38", CONTINUATION));

	}


}
