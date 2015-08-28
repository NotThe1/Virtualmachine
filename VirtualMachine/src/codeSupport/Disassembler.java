package codeSupport;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import memory.Core;

//import hardware.CentralProcessingUnit;

public class Disassembler implements Runnable {

	private Core core;
	private Document doc;
	// private CentralProcessingUnit cpu;

	private int workingProgramCounter;
	private int nextProgramCounter;

	private int linesToDisplay;
	private int linesToTrail;
	private int currentLine;

	HashMap<Byte, OperationStructure> opcodeMap;

	SimpleAttributeSet[] attrs;
	SimpleAttributeSet[] attrsForCategory;

	@Override
	public void run() {
	}

	public void upDateDisplay(int programCounter) {

		this.workingProgramCounter = programCounter;
		if (currentLine == -1) { // fresh listing
			try {
				// displayLine = 0;
				currentLine = 0;
				doc.remove(0, doc.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}// try
			attrsForCategory = makeAttrsForCategory(1); // current line
			for (int i = 0; i < linesToDisplay; i++) {
				workingProgramCounter += showCode(i);
				nextProgramCounter = (i == 0) ? workingProgramCounter : nextProgramCounter;
				attrsForCategory = makeAttrsForCategory(2); // future
			}// for
		} else { // next instruction
			String oldLine;
			int startLine = LINE_WIDTH * currentLine++; //
			try {
				// Gray out history
				oldLine = doc.getText(startLine, LINE_WIDTH);
				doc.remove(startLine, doc.getLength() - startLine);
				doc.insertString(startLine, oldLine, attrs[ATTR_GRAY]);
				attrsForCategory = makeAttrsForCategory(1); // current line
				for (int i = currentLine; i < linesToDisplay; i++) {
					workingProgramCounter += showCode(i);
					nextProgramCounter = (i == 0) ? workingProgramCounter : nextProgramCounter;
					attrsForCategory = makeAttrsForCategory(2); // future
				}// for

				// now see if we need to drop the first line and add a line at the end
				if (currentLine >= linesToTrail) {
					doc.remove(0, LINE_WIDTH);
					currentLine--;
				}// if

			} catch (BadLocationException e) {
				e.printStackTrace();
			}// try

		}// if

	}// run

	private int showCode(int thisLineNumber) {
		int workingPosition = thisLineNumber * LINE_WIDTH;
		// attrsForCategory = makeAttrsForCategory(thisLineNumber);
		OperationStructure currentOpCode = opcodeMap.get(core.read(workingProgramCounter));
		int opCodeSize = currentOpCode.getSize();
		byte currentValue0 = core.read(workingProgramCounter);
		byte currentValue1 = core.read(workingProgramCounter + 1);
		byte currentValue2 = core.read(workingProgramCounter + 2);
		String linePart1, linePart2, linePart3;
		try {
			linePart1 = String.format("%04X%4s", workingProgramCounter, "");
			doc.insertString(workingPosition, linePart1, attrsForCategory[LC_ATTR_ADDRESS]);
			workingPosition += linePart1.length();
			switch (opCodeSize) {
			case 1:
				linePart2 = String.format("%02X%8s", currentValue0, "");
				doc.insertString(workingPosition, linePart2, attrsForCategory[LC_ATTR_OPCODE]);
				workingPosition += linePart2.length();
				linePart3 = currentOpCode.getAssemblerCode();
				doc.insertString(doc.getLength(), linePart3, attrsForCategory[LC_ATTR_INSTRUCTION]);
				workingPosition += linePart3.length();
				break;
			case 2:
				linePart2 = String.format("%02X%02X%6s", currentValue0, currentValue1, "");
				doc.insertString(workingPosition, linePart2, attrsForCategory[LC_ATTR_OPCODE]);
				workingPosition += linePart2.length();
				linePart3 = currentOpCode.getAssemblerCode(currentValue1);
				doc.insertString(workingPosition, linePart3, attrsForCategory[LC_ATTR_INSTRUCTION]);
				workingPosition += linePart3.length();
				break;
			case 3:
				linePart2 = String.format("%02X%02X%02X%4s", currentValue0, currentValue1, currentValue2, "");
				doc.insertString(workingPosition, linePart2, attrsForCategory[LC_ATTR_OPCODE]);
				workingPosition += linePart2.length();
				linePart3 = currentOpCode.getAssemblerCode(core.read(workingProgramCounter + 1),
						core.read(workingProgramCounter + 2));
				doc.insertString(workingPosition, linePart3, attrsForCategory[LC_ATTR_INSTRUCTION]);
				workingPosition += linePart3.length();
				break;
			default:
			}// switch

			int columnPosition = 32 - (workingPosition % LINE_WIDTH);
			String formatString = "%" + columnPosition + "s";
			String padding = String.format(formatString, "");
			doc.insertString(workingPosition, padding, null);
			workingPosition += padding.length();
			doc.insertString(workingPosition, String.format("%-20s%n", currentOpCode.getFunction()), attrs[ATTR_GRAY]);

		} catch (BadLocationException e) {
			e.printStackTrace();
		}// try
		return opCodeSize;
	}// showCode

	private SimpleAttributeSet[] makeAttrsForCategory(int set) {
		SimpleAttributeSet[] afc = new SimpleAttributeSet[4];

		switch (set) {
		case 0: // History
			afc[LC_ATTR_ADDRESS] = attrs[ATTR_GRAY];
			afc[LC_ATTR_OPCODE] = attrs[ATTR_GRAY];
			afc[LC_ATTR_INSTRUCTION] = attrs[ATTR_GRAY];
			afc[LC_ATTR_DESCRIPTION] = attrs[ATTR_GRAY];
			break;
		case 1: // Present
			afc[LC_ATTR_ADDRESS] = attrs[ATTR_GRAY_BOLD];
			afc[LC_ATTR_OPCODE] = attrs[ATTR_RED_BOLD];
			afc[LC_ATTR_INSTRUCTION] = attrs[ATTR_BLACK_BOLD];
			afc[LC_ATTR_DESCRIPTION] = attrs[ATTR_BLACK_BOLD];
			break;
		case 2: // Future
			afc[LC_ATTR_ADDRESS] = attrs[ATTR_GRAY];
			afc[LC_ATTR_OPCODE] = attrs[ATTR_RED];
			afc[LC_ATTR_INSTRUCTION] = attrs[ATTR_BLACK];
			afc[LC_ATTR_DESCRIPTION] = attrs[ATTR_GRAY];
			break;
		default:
			afc[LC_ATTR_ADDRESS] = attrs[ATTR_GRAY];
			afc[LC_ATTR_OPCODE] = attrs[ATTR_GRAY];
			afc[LC_ATTR_INSTRUCTION] = attrs[ATTR_GRAY];
			afc[LC_ATTR_DESCRIPTION] = attrs[ATTR_GRAY];
		}// switch

		return afc;
	}// makeAttrsForCategory

	private SimpleAttributeSet[] makeAttributes() {
		int baseFontSize = 16;
		SimpleAttributeSet baseAttribute = new SimpleAttributeSet();
		StyleConstants.setFontFamily(baseAttribute, "Courier New");
		StyleConstants.setFontSize(baseAttribute, baseFontSize);

		SimpleAttributeSet[] a = new SimpleAttributeSet[8]; // hand calculated value - fix it
		a[ATTR_BLACK] = new SimpleAttributeSet();
		a[ATTR_BLUE] = new SimpleAttributeSet();
		a[ATTR_GRAY] = new SimpleAttributeSet();
		a[ATTR_RED] = new SimpleAttributeSet();
		StyleConstants.setForeground(a[ATTR_BLACK], Color.BLACK);
		StyleConstants.setForeground(a[ATTR_BLUE], Color.BLUE);
		StyleConstants.setForeground(a[ATTR_GRAY], Color.GRAY);
		StyleConstants.setForeground(a[ATTR_RED], Color.RED);

		SimpleAttributeSet boldAttribute = new SimpleAttributeSet(baseAttribute);
		StyleConstants.setFontSize(boldAttribute, baseFontSize + 1);
		StyleConstants.setBold(boldAttribute, true);

		a[ATTR_BLACK_BOLD] = new SimpleAttributeSet(boldAttribute);
		a[ATTR_BLUE_BOLD] = new SimpleAttributeSet(boldAttribute);
		a[ATTR_GRAY_BOLD] = new SimpleAttributeSet(boldAttribute);
		a[ATTR_RED_BOLD] = new SimpleAttributeSet(boldAttribute);
		StyleConstants.setForeground(a[ATTR_BLACK_BOLD], Color.BLACK);
		StyleConstants.setForeground(a[ATTR_BLUE_BOLD], Color.BLUE);
		StyleConstants.setForeground(a[ATTR_GRAY_BOLD], Color.GRAY);
		StyleConstants.setForeground(a[ATTR_RED_BOLD], Color.RED);

		return a;
	}// SimpleAttributeSet

	public void resetDisplay() {
		currentLine = -1;
	}// resetDisplay

	public Disassembler(Core core, Document doc) {
		this.core = core;
		this.doc = doc;
		// this.programCounter = cpu.getProgramCounter();
		linesToDisplay = LTD;
		linesToTrail = LTT;
		currentLine = -1;
		nextProgramCounter = -1;

		makeOpcodeMap();
		attrs = makeAttributes();
		attrsForCategory = new SimpleAttributeSet[4]; // hand calculated - fix
	}//

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private void makeOpcodeMap() {
		opcodeMap = new HashMap<Byte, OperationStructure>();
		opcodeMap.put((byte) 0X00, new OperationStructure((byte) 0X00, 1, "NOP", "", "", ""));
		opcodeMap.put((byte) 0X01, new OperationStructure((byte) 0X01, 3, "LXI", "B", "D16", "B<- byte3,C<- byte2"));
		opcodeMap.put((byte) 0X02, new OperationStructure((byte) 0X02, 1, "STAX", "B", "", "(BC)<-A"));
		opcodeMap.put((byte) 0X03, new OperationStructure((byte) 0X03, 1, "INX", "B", "", "BC<-BC+1"));
		opcodeMap.put((byte) 0X04, new OperationStructure((byte) 0X04, 1, "INR", "B", "", "B<-B+1"));
		opcodeMap.put((byte) 0X05, new OperationStructure((byte) 0X05, 1, "DCR", "B", "", "B<-B-1"));
		opcodeMap.put((byte) 0X06, new OperationStructure((byte) 0X06, 2, "MVI", "B", "D8", "B<-byte2"));
		opcodeMap.put((byte) 0X07, new OperationStructure((byte) 0X07, 1, "RLC", "", "", "A=A << not thru carry"));
		opcodeMap.put((byte) 0X08, new OperationStructure((byte) 0X08, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X09, new OperationStructure((byte) 0X09, 1, "DAD", "B", "", "HL = HL + BC"));
		opcodeMap.put((byte) 0X0A, new OperationStructure((byte) 0X0A, 1, "LDAX", "B", "", "A<-(BC)"));
		opcodeMap.put((byte) 0X0B, new OperationStructure((byte) 0X0B, 1, "DCX", "B", "", "BC = BC-1"));
		opcodeMap.put((byte) 0X0C, new OperationStructure((byte) 0X0C, 1, "INR", "C", "", "C <-C+1"));
		opcodeMap.put((byte) 0X0D, new OperationStructure((byte) 0X0D, 1, "DCR", "C", "", "C <-C-1"));
		opcodeMap.put((byte) 0X0E, new OperationStructure((byte) 0X0E, 2, "MVI", "C", "D8", "C,-byte2"));
		opcodeMap.put((byte) 0X0F, new OperationStructure((byte) 0X0F, 1, "RRC", "", "", "A = A>> not thru carry"));

		opcodeMap.put((byte) 0X10, new OperationStructure((byte) 0X10, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X11, new OperationStructure((byte) 0X11, 3, "LXI", "D", "D16", "D<-byte3,E<-byte2"));
		opcodeMap.put((byte) 0X12, new OperationStructure((byte) 0X12, 1, "STAX", "D", "", "(DE)<-A"));
		opcodeMap.put((byte) 0X13, new OperationStructure((byte) 0X13, 1, "INX", "D", "", "DE<-DE + 1"));
		opcodeMap.put((byte) 0X14, new OperationStructure((byte) 0X14, 1, "INR", "D", "", "D<-D+1"));
		opcodeMap.put((byte) 0X15, new OperationStructure((byte) 0X15, 1, "DCR", "D", "", "D<-D-1"));
		opcodeMap.put((byte) 0X16, new OperationStructure((byte) 0X16, 2, "MVI", "D", "D8", "D<-byte2"));
		opcodeMap.put((byte) 0X17, new OperationStructure((byte) 0X17, 1, "RAL", "D", "", "A=A << thru carry"));
		opcodeMap.put((byte) 0X18, new OperationStructure((byte) 0X18, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X19, new OperationStructure((byte) 0X19, 1, "DAD", "D", "", "HL = HL + DE"));
		opcodeMap.put((byte) 0X1A, new OperationStructure((byte) 0X1A, 1, "LDAX", "D", "", "A<-(DE)"));
		opcodeMap.put((byte) 0X1B, new OperationStructure((byte) 0X1B, 1, "DCX", "D", "", "DE = DE-1"));
		opcodeMap.put((byte) 0X1C, new OperationStructure((byte) 0X1C, 1, "INR", "E", "", "E <-E+1"));
		opcodeMap.put((byte) 0X1D, new OperationStructure((byte) 0X1D, 1, "DCR", "E", "", "E <-E-1"));
		opcodeMap.put((byte) 0X1E, new OperationStructure((byte) 0X1E, 2, "MVI", "E", "D8", "E,-byte2"));
		opcodeMap.put((byte) 0X1F, new OperationStructure((byte) 0X1F, 1, "RAR", "", "", "A = A>>  thru carry"));

		opcodeMap.put((byte) 0X20, new OperationStructure((byte) 0X20, 1, "NOP*", "", "", "Alt NOP (RIM)"));// special
		opcodeMap.put((byte) 0X21, new OperationStructure((byte) 0X21, 3, "LXI", "H", "D16", "H<-byte3,L<-byte2"));
		opcodeMap.put((byte) 0X22,
				new OperationStructure((byte) 0X22, 3, "SHLD", "addr", "", "(addr)<-L;(addr+1)<-H"));
		opcodeMap.put((byte) 0X23, new OperationStructure((byte) 0X23, 1, "INX", "H", "", "HL<-HL + 1"));
		opcodeMap.put((byte) 0X24, new OperationStructure((byte) 0X24, 1, "INR", "H", "", "H<-H+1"));
		opcodeMap.put((byte) 0X25, new OperationStructure((byte) 0X25, 1, "DCR", "H", "", "H<-H-1"));
		opcodeMap.put((byte) 0X26, new OperationStructure((byte) 0X26, 2, "MVI", "H", "D8", "H<-byte2"));
		opcodeMap.put((byte) 0X27, new OperationStructure((byte) 0X27, 1, "DAA", "", "", "")); // special
		opcodeMap.put((byte) 0X28, new OperationStructure((byte) 0X28, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X29, new OperationStructure((byte) 0X29, 1, "DAD", "H", "", "HL = HL + HL"));
		opcodeMap.put((byte) 0X2A,
				new OperationStructure((byte) 0X2A, 3, "LHLD", "addr", "", "L<-(addr);H<-(addr+1)"));
		opcodeMap.put((byte) 0X2B, new OperationStructure((byte) 0X2B, 1, "DCX", "H", "", "HL = HL-1"));
		opcodeMap.put((byte) 0X2C, new OperationStructure((byte) 0X2C, 1, "INR", "L", "", "L <-L+1"));
		opcodeMap.put((byte) 0X2D, new OperationStructure((byte) 0X2D, 1, "DCR", "L", "", "L <-L-1"));
		opcodeMap.put((byte) 0X2E, new OperationStructure((byte) 0X2E, 2, "MVI", "L", "D8", "L,-byte2"));
		opcodeMap.put((byte) 0X2F, new OperationStructure((byte) 0X2F, 1, "CMA", "", "", "A = !A"));

		opcodeMap.put((byte) 0X30, new OperationStructure((byte) 0X30, 1, "Alt", "", "", "Alt NOP(SIM)")); // special
		opcodeMap.put((byte) 0X31, new OperationStructure((byte) 0X31, 3, "LXI", "SP", "D16", "SP-data"));
		opcodeMap.put((byte) 0X32, new OperationStructure((byte) 0X32, 3, "STA", "addr", "", "(addr)<-A"));
		opcodeMap.put((byte) 0X33, new OperationStructure((byte) 0X33, 1, "INX", "SP", "", "SP<-SP + 1"));
		opcodeMap.put((byte) 0X34, new OperationStructure((byte) 0X34, 1, "INR", "M", "", "(HL)<-(HL)+1"));
		opcodeMap.put((byte) 0X35, new OperationStructure((byte) 0X35, 1, "DCR", "M", "", "(HL)<-(HL)-1"));
		opcodeMap.put((byte) 0X36, new OperationStructure((byte) 0X36, 2, "MVI", "M", "D8", "(HL)<-byte2"));
		opcodeMap.put((byte) 0X37, new OperationStructure((byte) 0X37, 1, "STC", "", "", "CY=1"));
		opcodeMap.put((byte) 0X38, new OperationStructure((byte) 0X38, 1, "Alt", "", "", "Alt NOP"));
		opcodeMap.put((byte) 0X39, new OperationStructure((byte) 0X39, 1, "DAD", "SP", "", "HL = HL + SP"));
		opcodeMap.put((byte) 0X3A, new OperationStructure((byte) 0X3A, 3, "LDA", "addr", "", "A<-(addr)"));
		opcodeMap.put((byte) 0X3B, new OperationStructure((byte) 0X3B, 1, "DCX", "SP", "", "SP = SP-1"));
		opcodeMap.put((byte) 0X3C, new OperationStructure((byte) 0X3C, 1, "INR", "A", "", "A <-A+1"));
		opcodeMap.put((byte) 0X3D, new OperationStructure((byte) 0X3D, 1, "DCR", "A", "", "A <-A-1"));
		opcodeMap.put((byte) 0X3E, new OperationStructure((byte) 0X3E, 2, "MVI", "A", "D8", "A,-byte2"));
		opcodeMap.put((byte) 0X3F, new OperationStructure((byte) 0X3F, 1, "CMC", "", "", "CY=!CY"));

		opcodeMap.put((byte) 0X40, new OperationStructure((byte) 0X40, 1, "MOV", "B", ",B", "B <- B"));
		opcodeMap.put((byte) 0X41, new OperationStructure((byte) 0X41, 1, "MOV", "B", ",C", "B <- C"));
		opcodeMap.put((byte) 0X42, new OperationStructure((byte) 0X42, 1, "MOV", "B", ",D", "B <- D"));
		opcodeMap.put((byte) 0X43, new OperationStructure((byte) 0X43, 1, "MOV", "B", ",E", "B <- E"));
		opcodeMap.put((byte) 0X44, new OperationStructure((byte) 0X44, 1, "MOV", "B", ",H", "B <- H"));
		opcodeMap.put((byte) 0X45, new OperationStructure((byte) 0X45, 1, "MOV", "B", ",L", "B <- L"));
		opcodeMap.put((byte) 0X46, new OperationStructure((byte) 0X46, 1, "MOV", "B", ",M", "B <- (HL)"));
		opcodeMap.put((byte) 0X47, new OperationStructure((byte) 0X47, 1, "MOV", "B", ",A", "B <- A"));

		opcodeMap.put((byte) 0X48, new OperationStructure((byte) 0X48, 1, "MOV", "C", ",B", "C <- B"));
		opcodeMap.put((byte) 0X49, new OperationStructure((byte) 0X49, 1, "MOV", "C", ",C", "C <- C"));
		opcodeMap.put((byte) 0X4A, new OperationStructure((byte) 0X4A, 1, "MOV", "C", ",D", "C <- D"));
		opcodeMap.put((byte) 0X4B, new OperationStructure((byte) 0X4B, 1, "MOV", "C", ",E", "C <- E"));
		opcodeMap.put((byte) 0X4C, new OperationStructure((byte) 0X4C, 1, "MOV", "C", ",H", "C <- H"));
		opcodeMap.put((byte) 0X4D, new OperationStructure((byte) 0X4D, 1, "MOV", "C", ",L", "C <- L"));
		opcodeMap.put((byte) 0X4E, new OperationStructure((byte) 0X4E, 1, "MOV", "C", ",M", "C <- (HL)"));
		opcodeMap.put((byte) 0X4F, new OperationStructure((byte) 0X4F, 1, "MOV", "C", ",A", "C <- A"));

		opcodeMap.put((byte) 0X50, new OperationStructure((byte) 0X50, 1, "MOV", "D", ",B", "D <- B"));
		opcodeMap.put((byte) 0X51, new OperationStructure((byte) 0X51, 1, "MOV", "D", ",C", "D <- C"));
		opcodeMap.put((byte) 0X52, new OperationStructure((byte) 0X52, 1, "MOV", "D", ",D", "D <- D"));
		opcodeMap.put((byte) 0X53, new OperationStructure((byte) 0X53, 1, "MOV", "D", ",E", "D <- E"));
		opcodeMap.put((byte) 0X54, new OperationStructure((byte) 0X54, 1, "MOV", "D", ",H", "D <- H"));
		opcodeMap.put((byte) 0X55, new OperationStructure((byte) 0X55, 1, "MOV", "D", ",L", "D <- L"));
		opcodeMap.put((byte) 0X56, new OperationStructure((byte) 0X56, 1, "MOV", "D", ",M", "D <- (HL)"));
		opcodeMap.put((byte) 0X57, new OperationStructure((byte) 0X57, 1, "MOV", "D", ",A", "D <- A"));

		opcodeMap.put((byte) 0X58, new OperationStructure((byte) 0X58, 1, "MOV", "E", ",B", "E <- B"));
		opcodeMap.put((byte) 0X59, new OperationStructure((byte) 0X59, 1, "MOV", "E", ",C", "E <- C"));
		opcodeMap.put((byte) 0X5A, new OperationStructure((byte) 0X5A, 1, "MOV", "E", ",D", "E <- D"));
		opcodeMap.put((byte) 0X5B, new OperationStructure((byte) 0X5B, 1, "MOV", "E", ",E", "E <- E"));
		opcodeMap.put((byte) 0X5C, new OperationStructure((byte) 0X5C, 1, "MOV", "E", ",H", "E <- H"));
		opcodeMap.put((byte) 0X5D, new OperationStructure((byte) 0X5D, 1, "MOV", "E", ",L", "E <- L"));
		opcodeMap.put((byte) 0X5E, new OperationStructure((byte) 0X5E, 1, "MOV", "E", ",M", "E <- (HL)"));
		opcodeMap.put((byte) 0X5F, new OperationStructure((byte) 0X5F, 1, "MOV", "E", ",A", "E <- A"));

		opcodeMap.put((byte) 0X60, new OperationStructure((byte) 0X60, 1, "MOV", "H", ",B", "H <- B"));
		opcodeMap.put((byte) 0X61, new OperationStructure((byte) 0X61, 1, "MOV", "H", ",C", "H <- C"));
		opcodeMap.put((byte) 0X62, new OperationStructure((byte) 0X62, 1, "MOV", "H", ",D", "H <- D"));
		opcodeMap.put((byte) 0X63, new OperationStructure((byte) 0X63, 1, "MOV", "H", ",E", "H <- E"));
		opcodeMap.put((byte) 0X64, new OperationStructure((byte) 0X64, 1, "MOV", "H", ",H", "H <- H"));
		opcodeMap.put((byte) 0X65, new OperationStructure((byte) 0X65, 1, "MOV", "H", ",L", "H <- L"));
		opcodeMap.put((byte) 0X66, new OperationStructure((byte) 0X66, 1, "MOV", "H", ",M", "H <- (HL)"));
		opcodeMap.put((byte) 0X67, new OperationStructure((byte) 0X67, 1, "MOV", "H", ",A", "H <- A"));

		opcodeMap.put((byte) 0X68, new OperationStructure((byte) 0X68, 1, "MOV", "L", ",B", "L <- B"));
		opcodeMap.put((byte) 0X69, new OperationStructure((byte) 0X69, 1, "MOV", "L", ",C", "L <- C"));
		opcodeMap.put((byte) 0X6A, new OperationStructure((byte) 0X6A, 1, "MOV", "L", ",D", "L <- D"));
		opcodeMap.put((byte) 0X6B, new OperationStructure((byte) 0X6B, 1, "MOV", "L", ",E", "L <- E"));
		opcodeMap.put((byte) 0X6C, new OperationStructure((byte) 0X6C, 1, "MOV", "L", ",H", "L <- H"));
		opcodeMap.put((byte) 0X6D, new OperationStructure((byte) 0X6D, 1, "MOV", "L", ",L", "L <- L"));
		opcodeMap.put((byte) 0X6E, new OperationStructure((byte) 0X6E, 1, "MOV", "L", ",M", "L <- (HL)"));
		opcodeMap.put((byte) 0X6F, new OperationStructure((byte) 0X6F, 1, "MOV", "L", ",A", "L <- A"));

		opcodeMap.put((byte) 0X70, new OperationStructure((byte) 0X70, 1, "MOV", "M", ",B", "(HL) <- B"));
		opcodeMap.put((byte) 0X71, new OperationStructure((byte) 0X71, 1, "MOV", "M", ",C", "(HL) <- C"));
		opcodeMap.put((byte) 0X72, new OperationStructure((byte) 0X72, 1, "MOV", "M", ",D", "(HL) <- D"));
		opcodeMap.put((byte) 0X73, new OperationStructure((byte) 0X73, 1, "MOV", "M", ",E", "(HL) <- E"));
		opcodeMap.put((byte) 0X74, new OperationStructure((byte) 0X74, 1, "MOV", "M", ",H", "(HL) <- H"));
		opcodeMap.put((byte) 0X75, new OperationStructure((byte) 0X75, 1, "MOV", "M", ",L", "(HL) <- L"));
		opcodeMap.put((byte) 0X76, new OperationStructure((byte) 0X76, 1, "HLT", "", "", "Halt")); // Special
		opcodeMap.put((byte) 0X77, new OperationStructure((byte) 0X77, 1, "MOV", "M", ",A", "(HL) <- A"));

		opcodeMap.put((byte) 0X78, new OperationStructure((byte) 0X78, 1, "MOV", "A", ",B", "A <- B"));
		opcodeMap.put((byte) 0X79, new OperationStructure((byte) 0X79, 1, "MOV", "A", ",C", "A <- C"));
		opcodeMap.put((byte) 0X7A, new OperationStructure((byte) 0X7A, 1, "MOV", "A", ",D", "A <- D"));
		opcodeMap.put((byte) 0X7B, new OperationStructure((byte) 0X7B, 1, "MOV", "A", ",E", "A <- E"));
		opcodeMap.put((byte) 0X7C, new OperationStructure((byte) 0X7C, 1, "MOV", "A", ",H", "A <- H"));
		opcodeMap.put((byte) 0X7D, new OperationStructure((byte) 0X7D, 1, "MOV", "A", ",L", "A <- L"));
		opcodeMap.put((byte) 0X7E, new OperationStructure((byte) 0X7E, 1, "MOV", "A", ",M", "A <- (HL)"));
		opcodeMap.put((byte) 0X7F, new OperationStructure((byte) 0X7F, 1, "MOV", "A", ",A", "A <- A"));

		opcodeMap.put((byte) 0X80, new OperationStructure((byte) 0X80, 1, "ADD", "B", "", "A <- A+B"));
		opcodeMap.put((byte) 0X81, new OperationStructure((byte) 0X81, 1, "ADD", "C", "", "A <- A+C"));
		opcodeMap.put((byte) 0X82, new OperationStructure((byte) 0X82, 1, "ADD", "D", "", "A <- A+D"));
		opcodeMap.put((byte) 0X83, new OperationStructure((byte) 0X83, 1, "ADD", "E", "", "A <- A+E"));
		opcodeMap.put((byte) 0X84, new OperationStructure((byte) 0X84, 1, "ADD", "H", "", "A <- A+H"));
		opcodeMap.put((byte) 0X85, new OperationStructure((byte) 0X85, 1, "ADD", "L", "", "A <- A+L"));
		opcodeMap.put((byte) 0X86, new OperationStructure((byte) 0X86, 1, "ADD", "M", "", "A <- A+(HL)"));
		opcodeMap.put((byte) 0X87, new OperationStructure((byte) 0X87, 1, "ADD", "A", "", "A <- A+A"));

		opcodeMap.put((byte) 0X88, new OperationStructure((byte) 0X88, 1, "ADC", "B", "", "A <- A+B + CY"));
		opcodeMap.put((byte) 0X89, new OperationStructure((byte) 0X89, 1, "ADC", "C", "", "A <- A+C + CY"));
		opcodeMap.put((byte) 0X8A, new OperationStructure((byte) 0X8A, 1, "ADC", "D", "", "A <- A+D + CY"));
		opcodeMap.put((byte) 0X8B, new OperationStructure((byte) 0X8B, 1, "ADC", "E", "", "A <- A+E + CY"));
		opcodeMap.put((byte) 0X8C, new OperationStructure((byte) 0X8C, 1, "ADC", "H", "", "A <- A+H + CY"));
		opcodeMap.put((byte) 0X8D, new OperationStructure((byte) 0X8D, 1, "ADC", "L", "", "A <- A+L + CY"));
		opcodeMap.put((byte) 0X8E, new OperationStructure((byte) 0X8E, 1, "ADC", "M", "", "A <- A+(HL) + CY"));
		opcodeMap.put((byte) 0X8F, new OperationStructure((byte) 0X8F, 1, "ADC", "A", "", "A <- A+A + CY"));

		opcodeMap.put((byte) 0X90, new OperationStructure((byte) 0X90, 1, "SUB", "B", "", "A <- A-B"));
		opcodeMap.put((byte) 0X91, new OperationStructure((byte) 0X91, 1, "SUB", "C", "", "A <- A-C"));
		opcodeMap.put((byte) 0X92, new OperationStructure((byte) 0X92, 1, "SUB", "D", "", "A <- A-D"));
		opcodeMap.put((byte) 0X93, new OperationStructure((byte) 0X93, 1, "SUB", "E", "", "A <- A-E"));
		opcodeMap.put((byte) 0X94, new OperationStructure((byte) 0X94, 1, "SUB", "H", "", "A <- A-H"));
		opcodeMap.put((byte) 0X95, new OperationStructure((byte) 0X95, 1, "SUB", "L", "", "A <- A-L"));
		opcodeMap.put((byte) 0X96, new OperationStructure((byte) 0X96, 1, "SUB", "M", "", "A <- A-(HL)"));
		opcodeMap.put((byte) 0X97, new OperationStructure((byte) 0X97, 1, "SUB", "A", "", "A <- A-A"));

		opcodeMap.put((byte) 0X98, new OperationStructure((byte) 0X98, 1, "SBB", "B", "", "A <- A-B - CY"));
		opcodeMap.put((byte) 0X99, new OperationStructure((byte) 0X99, 1, "SBB", "C", "", "A <- A-C - CY"));
		opcodeMap.put((byte) 0X9A, new OperationStructure((byte) 0X9A, 1, "SBB", "D", "", "A <- A-D - CY"));
		opcodeMap.put((byte) 0X9B, new OperationStructure((byte) 0X9B, 1, "SBB", "E", "", "A <- A-E - CY"));
		opcodeMap.put((byte) 0X9C, new OperationStructure((byte) 0X9C, 1, "SBB", "H", "", "A <- A-H - CY"));
		opcodeMap.put((byte) 0X9D, new OperationStructure((byte) 0X9D, 1, "SBB", "L", "", "A <- A-L - CY"));
		opcodeMap.put((byte) 0X9E, new OperationStructure((byte) 0X9E, 1, "SBB", "M", "", "A <- A-(HL) - CY"));
		opcodeMap.put((byte) 0X9F, new OperationStructure((byte) 0X9F, 1, "SBB", "A", "", "A <- A-A - CY"));

		opcodeMap.put((byte) 0XA0, new OperationStructure((byte) 0XA0, 1, "ANA", "B", "", "A <- A&B"));
		opcodeMap.put((byte) 0XA1, new OperationStructure((byte) 0XA1, 1, "ANA", "C", "", "A <- A&C"));
		opcodeMap.put((byte) 0XA2, new OperationStructure((byte) 0XA2, 1, "ANA", "D", "", "A <- A&D"));
		opcodeMap.put((byte) 0XA3, new OperationStructure((byte) 0XA3, 1, "ANA", "E", "", "A <- A&E"));
		opcodeMap.put((byte) 0XA4, new OperationStructure((byte) 0XA4, 1, "ANA", "H", "", "A <- A&H"));
		opcodeMap.put((byte) 0XA5, new OperationStructure((byte) 0XA5, 1, "ANA", "L", "", "A <- A&L"));
		opcodeMap.put((byte) 0XA6, new OperationStructure((byte) 0XA6, 1, "ANA", "M", "", "A <- A&(HL)"));
		opcodeMap.put((byte) 0XA7, new OperationStructure((byte) 0XA7, 1, "ANA", "A", "", "A <- A&A"));

		opcodeMap.put((byte) 0XA8, new OperationStructure((byte) 0XA8, 1, "XRA", "B", "", "A <- A^B"));
		opcodeMap.put((byte) 0XA9, new OperationStructure((byte) 0XA9, 1, "XRA", "C", "", "A <- A^C"));
		opcodeMap.put((byte) 0XAA, new OperationStructure((byte) 0XAA, 1, "XRA", "D", "", "A <- A^D"));
		opcodeMap.put((byte) 0XAB, new OperationStructure((byte) 0XAB, 1, "XRA", "E", "", "A <- A^E"));
		opcodeMap.put((byte) 0XAC, new OperationStructure((byte) 0XAC, 1, "XRA", "H", "", "A <- A^H"));
		opcodeMap.put((byte) 0XAD, new OperationStructure((byte) 0XAD, 1, "XRA", "L", "", "A <- A^L"));
		opcodeMap.put((byte) 0XAE, new OperationStructure((byte) 0XAE, 1, "XRA", "M", "", "A <- A^(HL)"));
		opcodeMap.put((byte) 0XAF, new OperationStructure((byte) 0XAF, 1, "XRA", "A", "", "A <- A^A"));

		opcodeMap.put((byte) 0XB0, new OperationStructure((byte) 0XB0, 1, "ORA", "B", "", "A <- A|B"));
		opcodeMap.put((byte) 0XB1, new OperationStructure((byte) 0XB1, 1, "ORA", "C", "", "A <- A|C"));
		opcodeMap.put((byte) 0XB2, new OperationStructure((byte) 0XB2, 1, "ORA", "D", "", "A <- A|D"));
		opcodeMap.put((byte) 0XB3, new OperationStructure((byte) 0XB3, 1, "ORA", "E", "", "A <- A|E"));
		opcodeMap.put((byte) 0XB4, new OperationStructure((byte) 0XB4, 1, "ORA", "H", "", "A <- A|H"));
		opcodeMap.put((byte) 0XB5, new OperationStructure((byte) 0XB5, 1, "ORA", "L", "", "A <- A|L"));
		opcodeMap.put((byte) 0XB6, new OperationStructure((byte) 0XB6, 1, "ORA", "M", "", "A <- A|(HL)"));
		opcodeMap.put((byte) 0XB7, new OperationStructure((byte) 0XB7, 1, "ORA", "A", "", "A <- A|A"));

		opcodeMap.put((byte) 0XB8, new OperationStructure((byte) 0XB8, 1, "CMP", "B", "", "A - B"));
		opcodeMap.put((byte) 0XB9, new OperationStructure((byte) 0XB9, 1, "CMP", "C", "", "A - C"));
		opcodeMap.put((byte) 0XBA, new OperationStructure((byte) 0XBA, 1, "CMP", "D", "", "A - D"));
		opcodeMap.put((byte) 0XBB, new OperationStructure((byte) 0XBB, 1, "CMP", "E", "", "A - E"));
		opcodeMap.put((byte) 0XBC, new OperationStructure((byte) 0XBC, 1, "CMP", "H", "", "A - H"));
		opcodeMap.put((byte) 0XBD, new OperationStructure((byte) 0XBD, 1, "CMP", "L", "", "A - L"));
		opcodeMap.put((byte) 0XBE, new OperationStructure((byte) 0XBE, 1, "CMP", "M", "", "A - (HL)"));
		opcodeMap.put((byte) 0XBF, new OperationStructure((byte) 0XBF, 1, "CMP", "A", "", "A - A"));

		opcodeMap.put((byte) 0XC0, new OperationStructure((byte) 0XC0, 1, "RNZ", "", "", "if NZ, ret"));
		opcodeMap.put((byte) 0XC1, new OperationStructure((byte) 0XC1, 1, "POP", "B", "", ""));
		opcodeMap.put((byte) 0XC2, new OperationStructure((byte) 0XC2, 3, "JNZ", "addr", "", "if NZ,PC<-addr"));
		opcodeMap.put((byte) 0XC3, new OperationStructure((byte) 0XC3, 3, "JMP", "addr", "", "PC<-addr"));
		opcodeMap.put((byte) 0XC4, new OperationStructure((byte) 0XC4, 3, "CNZ", "addr", "", "if NZ,CALL addr"));
		opcodeMap.put((byte) 0XC5, new OperationStructure((byte) 0XC5, 1, "PUSH", "B", "", ""));
		opcodeMap.put((byte) 0XC6, new OperationStructure((byte) 0XC6, 2, "ADI", "D8", "", "A<-A + byte2"));
		opcodeMap.put((byte) 0XC7, new OperationStructure((byte) 0XC7, 1, "RST", "0", "", "CALL $0"));
		opcodeMap.put((byte) 0XC8, new OperationStructure((byte) 0XC8, 1, "RZ", "", "", "if Z, ret"));
		opcodeMap.put((byte) 0XC9, new OperationStructure((byte) 0XC9, 1, "RET", "", "", "PC<-(SP); SP<-SP+2"));
		opcodeMap.put((byte) 0XCA, new OperationStructure((byte) 0XCA, 3, "JZ", "addr", "", "if Z,PC<-addr"));
		opcodeMap.put((byte) 0XCB, new OperationStructure((byte) 0XCB, 3, "Alt", "addr", "", "Alt RET PC<-addr"));
		opcodeMap.put((byte) 0XCC, new OperationStructure((byte) 0XCC, 3, "CZ", "addr", "", "if Z,CALL addr"));
		opcodeMap.put((byte) 0XCD, new OperationStructure((byte) 0XCD, 3, "CALL", "addr", "", "CALL addr"));
		opcodeMap.put((byte) 0XCE, new OperationStructure((byte) 0XCE, 2, "ACI", "D8", "", "A<- A + data + cy"));
		opcodeMap.put((byte) 0XCF, new OperationStructure((byte) 0XCF, 1, "RST", "1", "", "CALL $8"));

		opcodeMap.put((byte) 0XD0, new OperationStructure((byte) 0XD0, 1, "RNC", "", "", "if NCY, ret"));
		opcodeMap.put((byte) 0XD1, new OperationStructure((byte) 0XD1, 1, "POP", "D", "", ""));
		opcodeMap.put((byte) 0XD2, new OperationStructure((byte) 0XD2, 3, "JNC", "addr", "", "if NCY,PC<-addr"));
		opcodeMap.put((byte) 0XD3, new OperationStructure((byte) 0XD3, 2, "OUT", "D8", "", "i/O")); // Special
		opcodeMap.put((byte) 0XD4, new OperationStructure((byte) 0XD4, 3, "CNC", "addr", "", "if NC,CALL addr"));
		opcodeMap.put((byte) 0XD5, new OperationStructure((byte) 0XD5, 1, "PUSH", "D", "", ""));
		opcodeMap.put((byte) 0XD6, new OperationStructure((byte) 0XD6, 2, "SUI", "D8", "", "A<-A - byte2"));
		opcodeMap.put((byte) 0XD7, new OperationStructure((byte) 0XD7, 1, "RST", "2", "", "CALL $10"));
		opcodeMap.put((byte) 0XD8, new OperationStructure((byte) 0XD8, 1, "RC", "", "", "if CY, ret"));
		opcodeMap.put((byte) 0XD9, new OperationStructure((byte) 0XD9, 1, "RET*", "", "", "Alt RET"));
		opcodeMap.put((byte) 0XDA, new OperationStructure((byte) 0XDA, 3, "JC", "addr", "", "if CY,PC<-addr"));
		opcodeMap.put((byte) 0XDB, new OperationStructure((byte) 0XDB, 2, "IN", "D8", "", "i/O")); // Special
		opcodeMap.put((byte) 0XDC, new OperationStructure((byte) 0XDC, 3, "CC", "addr", "", "if CY,CALL addr"));
		opcodeMap.put((byte) 0XDD, new OperationStructure((byte) 0XDD, 3, "Alt", "addr", "", "Alt CALL addr"));
		opcodeMap.put((byte) 0XDE, new OperationStructure((byte) 0XDE, 2, "SBI", "D8", "", "A<- A - data - cy"));
		opcodeMap.put((byte) 0XDF, new OperationStructure((byte) 0XDF, 1, "RST", "3", "", "CALL $18"));

		opcodeMap.put((byte) 0XE0, new OperationStructure((byte) 0XE0, 1, "RPO", "", "", "if PO, ret"));
		opcodeMap.put((byte) 0XE1, new OperationStructure((byte) 0XE1, 1, "POP", "H", "", ""));
		opcodeMap.put((byte) 0XE2, new OperationStructure((byte) 0XE2, 3, "JPO", "addr", "", "if PO,PC<-addr"));
		opcodeMap.put((byte) 0XE3, new OperationStructure((byte) 0XE3, 1, "XTHL", "", "", "L<->(SP);H<->(SP+1)"));
		opcodeMap.put((byte) 0XE4, new OperationStructure((byte) 0XE4, 3, "CPO", "addr", "", "if PO,CALL addr"));
		opcodeMap.put((byte) 0XE5, new OperationStructure((byte) 0XE5, 1, "PUSH", "H", "", ""));
		opcodeMap.put((byte) 0XE6, new OperationStructure((byte) 0XE6, 2, "ANI", "D8", "", "A<-A & byte2"));
		opcodeMap.put((byte) 0XE7, new OperationStructure((byte) 0XE7, 1, "RST", "4", "", "CALL $20"));
		opcodeMap.put((byte) 0XE8, new OperationStructure((byte) 0XE8, 1, "RPE", "", "", "if PE, ret"));
		opcodeMap.put((byte) 0XE9, new OperationStructure((byte) 0XE9, 1, "PCHL", "", "", "PC.hi<-H;PC.lo<-L"));
		opcodeMap.put((byte) 0XEA, new OperationStructure((byte) 0XEA, 3, "JPE", "addr", "", "if PE,PC<-addr"));
		opcodeMap.put((byte) 0XEB, new OperationStructure((byte) 0XEB, 1, "XCHG", "", "", "H<->D;L<->E")); // Special
		opcodeMap.put((byte) 0XEC, new OperationStructure((byte) 0XEC, 3, "CPE", "addr", "", "if PE,CALL addr"));
		opcodeMap.put((byte) 0XED, new OperationStructure((byte) 0XED, 3, "Alt", "addr", "", "Alt CALL addr"));
		opcodeMap.put((byte) 0XEE, new OperationStructure((byte) 0XEE, 2, "XRI", "D8", "", "A<- A ^ data"));
		opcodeMap.put((byte) 0XEF, new OperationStructure((byte) 0XEF, 1, "RST", "5", "", "CALL $28"));

		opcodeMap.put((byte) 0XF0, new OperationStructure((byte) 0XF0, 1, "RP", "", "", "if P, ret"));
		opcodeMap.put((byte) 0XF1, new OperationStructure((byte) 0XF1, 1, "POP", "PSW", "",
				"flags<-(SP);A<-(SP+1); SP<-SP+2"));
		opcodeMap.put((byte) 0XF2, new OperationStructure((byte) 0XF2, 3, "JP", "addr", "", "if P,PC<-addr"));
		opcodeMap.put((byte) 0XF3, new OperationStructure((byte) 0XF3, 1, "DI", "", "", "")); // Special
		opcodeMap.put((byte) 0XF4, new OperationStructure((byte) 0XF4, 3, "CP", "addr", "", "if P,CALL addr"));
		opcodeMap.put((byte) 0XF5, new OperationStructure((byte) 0XF5, 1, "PUSH", "PSW", "", ""));
		opcodeMap.put((byte) 0XF6, new OperationStructure((byte) 0XF6, 2, "ORI", "D8", "", "A<-A | byte2"));
		opcodeMap.put((byte) 0XF7, new OperationStructure((byte) 0XF7, 1, "RST", "6", "", "CALL $30"));
		opcodeMap.put((byte) 0XF8, new OperationStructure((byte) 0XF8, 1, "RM", "", "", "if M, ret"));
		opcodeMap.put((byte) 0XF9, new OperationStructure((byte) 0XF9, 1, "SPHL", "", "", "SP=HL"));
		opcodeMap.put((byte) 0XFA, new OperationStructure((byte) 0XFA, 3, "JM", "addr", "", "if M,PC<-addr"));
		opcodeMap.put((byte) 0XFB, new OperationStructure((byte) 0XFB, 1, "EI", "", "", "")); // Special
		opcodeMap.put((byte) 0XFC, new OperationStructure((byte) 0XFC, 3, "CM", "addr", "", "if M,CALL addr"));
		opcodeMap.put((byte) 0XFD, new OperationStructure((byte) 0XFD, 3, "Alt", "addr", "", "Alt CALL addr"));
		opcodeMap.put((byte) 0XFE, new OperationStructure((byte) 0XFE, 2, "CPI", "D8", "", "A - data"));
		opcodeMap.put((byte) 0XFF, new OperationStructure((byte) 0XFF, 1, "RST", "7", "", "CALL $38"));

	}

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

	class OperationStructure {
		private byte opCode;
		private int size;
		private String instruction;
		private String source;
		private String destination;
		private String function;

		OperationStructure(byte opCode, int size,
				String instruction, String destination, String source, String function) {
			this.opCode = opCode;
			this.size = size;
			this.instruction = instruction;
			this.source = source;
			this.destination = destination;
			this.function = function;
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

		// public String getFunctionFormatted() {
		// return String.format("%8s%s%n", "", this.function);
		// }// getFunction

		public String getAssemblerCode() {
			return String.format("%-4s %s%s", getInstruction(), getDestination(), getSource());
		}

		public String getAssemblerCode(byte plusOne) {
			String ans;
			if (getDestination().equals("D8")) {
				ans = String.format("%-4s %02X",
						getInstruction(), plusOne);
			} else {
				ans = String.format("%-4s %s,%02X",
						getInstruction(), getDestination(), plusOne);
			}
			return ans;
		}

		public String getAssemblerCode(byte plusOne, byte plusTwo) {
			String ans;
			if (getDestination().equals("addr")) {
				ans = String.format("%-4s %02X%02X",
						getInstruction(), plusTwo, plusOne);
			} else {
				ans = String.format("%-4s %s,%02X%02X", getInstruction(), getDestination(), plusTwo, plusOne);
			}
			return ans;
		}

	}// class operationStructure

	private final static int LTD = 20; // LTD-> Lines To Display
	private final static int LTT = 4; // LTT-> Lines to Trail

	private final static int LINE_WIDTH = 54; // calculated by hand for now

	private static final int ATTR_BLACK = 0;
	private static final int ATTR_BLUE = 1;
	private static final int ATTR_GRAY = 2;
	private static final int ATTR_RED = 3;
	private static final int ATTR_BLACK_BOLD = 4;
	private static final int ATTR_BLUE_BOLD = 5;
	private static final int ATTR_GRAY_BOLD = 6;
	private static final int ATTR_RED_BOLD = 7;

	private static final int LC_ATTR_ADDRESS = 0; // LC -> Line category
	private static final int LC_ATTR_OPCODE = 1;
	private static final int LC_ATTR_INSTRUCTION = 2;
	private static final int LC_ATTR_DESCRIPTION = 3;

}// class Disassembler
