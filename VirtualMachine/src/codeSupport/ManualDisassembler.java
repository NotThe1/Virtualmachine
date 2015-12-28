package codeSupport;

import hardware.ConditionCodeRegister;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.AbstractListModel;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.ListModel;

import java.awt.GridBagConstraints;

import javax.swing.JButton;

import java.awt.Insets;

import javax.swing.JTabbedPane;

import myComponents.Hex64KSpinner;

import java.awt.Dimension;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.swing.JTextArea;
import javax.swing.ButtonGroup;

//import codeSupport.ManualDisassembler0.CodeFragment;
//import codeSupport.ManualDisassembler0.OperationStructure;

import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

public class ManualDisassembler implements ActionListener, ListSelectionListener {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ManualDisassembler window = new ManualDisassembler();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void addFragement() {
		int startLocNew = (int) spinnerBeginFragment.getValue();
		int endLocNew = (int) spinnerEndFragment.getValue();
		String type = groupFragment.getSelection().getActionCommand();
		codeFragmentModel.addItem(new CodeFragment(startLocNew, endLocNew, type));
		listCodeFragments.updateUI();
	}// addFragement

	private void removeFragment() {
		int index = listCodeFragments.getSelectedIndex();
		if (index == -1) {
			return;
		}// if
			// System.out.printf("<removeFragment> getSelectedIndex() =  %s%n", listCodeFragments.getSelectedIndex());
		codeFragmentModel.removeItem(listCodeFragments.getSelectedIndex());
		if (index > codeFragmentModel.getSize() - 1) {
			listCodeFragments.setSelectedIndex(codeFragmentModel.getSize() - 1);
		}// if
		listCodeFragments.updateUI();
	}// removeFragment

	private void processFragment() {
		int index = listCodeFragments.getSelectedIndex();
		if (index == -1) {
			return;
		}// if
		CodeFragment cf = codeFragmentModel.getElementAt(index);
		if (!cf.type.equals(CodeFragment.UNKNOWN)) {
			return;
		}// if
		System.out.printf("<processFragment> - DoIt!%n");
		entryPoints.push(cf.startLoc);
		buildFragments();
		listCodeFragments.updateUI();
	}//

	private void actionStart() {
		btnProcessFragment.setEnabled(true);
		btnAddFragment.setEnabled(true);
		btnRemoveFragment.setEnabled(true);
		btnCombineFragments.setEnabled(true);
		btnBuildASM.setEnabled(true);
		listCodeFragments.setEnabled(true);

		// int lastLocation = binaryData.capacity();

		labels = new HashSet<Integer>();

		beenThere = new HashSet<Integer>();
		beenThere.add(5);
		beenThere.add(0);

		entryPoints.push(OFFSET);
		// int counter = 0;
		while (!entryPoints.isEmpty()) {
			if (!beenThere.contains(entryPoints.peek())) {
				buildFragments();
			} else {
				entryPoints.pop(); // toss the entry point , been there!
			}
		}// while
			// combineFragments();
		listCodeFragments.updateUI();
		tabPaneDisplays.setSelectedIndex(TAB_WIP);
	}// actionStart

	private void try1() {

	}// try1

	private void combineFragments() {
		ArrayList<CodeFragment> tempFragments = new ArrayList<CodeFragment>();

		if (codeFragmentModel.getSize() < 2) {
			return;
		}
		CodeFragment cfOriginal = null;
		CodeFragment cfNew = codeFragmentModel.getElementAt(0);

		for (int i = 1; i < codeFragmentModel.getSize(); i++) {
			cfOriginal = codeFragmentModel.getElementAt(i);
			if (!cfOriginal.type.equals(cfNew.type)) {
				tempFragments.add(cfNew);
				cfNew = codeFragmentModel.getElementAt(i);
			} else if (cfOriginal.startLoc == cfNew.endLoc + 1) {
				cfNew.endLoc = cfOriginal.endLoc;
			} else {
				tempFragments.add(cfNew);
				cfNew = codeFragmentModel.getElementAt(i);
			}// if
		}
		tempFragments.add(cfNew);
		codeFragmentModel.clear();
		for (int i = 0; i < tempFragments.size(); i++) {
			codeFragmentModel.addItem(tempFragments.get(i));
		}// for - rebuild codeFragments
		listCodeFragments.updateUI();
	}

	private void buildFragments() {
		int startLocation = 0;
		int currentLocation = 0;
		OpcodeStructure8080 currentOpCode = null;
		;
		int pcAction = 0;
		;

		startLocation = entryPoints.pop();
		currentLocation = startLocation;
		boolean keepGoing = true;
		int targetAddress;
		while (keepGoing) {
			int a = currentLocation;

			if (beenThere.add(currentLocation) == false) {
				// System.out.printf("already visited %04X%n", startLocation);
				codeFragmentModel.addItem(new CodeFragment(startLocation, (currentLocation - 1), CodeFragment.CODE));

				return;
			}//

			currentOpCode = opcodeMap.get(binaryData.get(currentLocation));
			// System.out.printf("Location = %04X, opcode = %s%n", currentLocation, currentOpCode.getInstruction());
			pcAction = currentOpCode.getPcAction();

			switch (pcAction) {
			case OpcodeStructure8080.NORMAL: // regular opcodes and conditional RETURNS
				currentLocation += currentOpCode.getSize();
				keepGoing = true;
				break;
			case OpcodeStructure8080.CONTINUATION: // All CALLs and all Conditional RETURNS
				targetAddress = makeTargetAddress(currentLocation);
				entryPoints.push(targetAddress);
				labels.add(targetAddress);
				currentLocation += currentOpCode.getSize();
				keepGoing = true;
				break;
			case OpcodeStructure8080.TERMINATES: // RET
				currentLocation += currentOpCode.getSize();
				keepGoing = false;
				break;
			case OpcodeStructure8080.TOTAL: // JUMP PCHL
				if (currentOpCode.getInstruction().equals("JMP")) { // only for JMP
					targetAddress = makeTargetAddress(currentLocation);
					entryPoints.push(targetAddress);
					labels.add(targetAddress);
				}// if
				currentLocation += currentOpCode.getSize();
				keepGoing = false;
				break;
			}// switch
		}// - keep going
		codeFragmentModel.addItem(new CodeFragment(startLocation, (currentLocation - 1), CodeFragment.CODE));
		return;
	}// buildFragments

	private int makeTargetAddress(int currentLocation) {
		int hi = (binaryData.get(currentLocation + 2) & 0xFF) * 256;
		int lo = binaryData.get(currentLocation + 1);
		int v = hi + lo;
		// System.out.printf("<makeTargetAddress> target = %04X from currentLocation %04X%n", v, currentLocation);
		return ((binaryData.get(currentLocation + 2) & 0xFF) * 256) + (binaryData.get(currentLocation + 1) & 0xFF);
	}// makeTargetAddress

	private void openBinaryFile() {
		String fileLocation = "C:\\Users\\admin\\Dropbox\\Resources\\NativeFiles";
		Path sourcePath = Paths.get(fileLocation);
		JFileChooser chooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}// if

		binaryFile = chooser.getSelectedFile();
		if (!processBianryFile(binaryFile)) {
			JOptionPane.showMessageDialog(null, "Problem with binary Source file" + binaryFile.getAbsolutePath()
					, "openBinaryFile()",
					JOptionPane.ERROR_MESSAGE);
			return;
		}//if 
		codeFragmentModel.removeItem(0);
		codeFragmentModel.addItem(new CodeFragment(OFFSET, (int) (binaryFile.length() + OFFSET) - 1, CodeFragment.UNKNOWN));

	}// openBinaryFile

	private boolean processBianryFile(File binaryFile) {
		boolean result = true; // assume all goes well
		binaryFileName = binaryFile.getName();
		binaryFilePath = binaryFile.getAbsolutePath();

		FileChannel fcIn = null;
		FileInputStream fout = null;
		try {
			fout = new FileInputStream(binaryFile);
			fcIn = fout.getChannel();
		} catch (FileNotFoundException fileNotFoundException) {
			JOptionPane.showMessageDialog(null, binaryFile.getAbsolutePath()
					+ "not found", "processBianryFile",
					JOptionPane.ERROR_MESSAGE);
			return false;
		} // exit gracefully

		long fileSize = binaryFile.length();
		int roundedFileSize = ((((int) fileSize + OFFSET) / CHARACTERS_PER_LINE)) * CHARACTERS_PER_LINE;
		binaryData = ByteBuffer.allocate(roundedFileSize);
		binaryData.position(OFFSET);
		// byte[] sectorData = new byte[sectorSize];
		try {
			fcIn.read(binaryData);
			fcIn.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, binaryFile.getAbsolutePath()
					+ " - IOException ", "processBianryFile",
					JOptionPane.ERROR_MESSAGE);
			return false; // exit gracefully
		}// try
			// binaryData.flip();
		displayBinaryFile(binaryData, (int) roundedFileSize);
		haveBinanryFile(true);
		frame.setTitle(APP_NAME + " - " + binaryFileName);
		tabPaneDisplays.setSelectedIndex(TAB_BINARY_FILE);
		return result;
	}

	private void displayBinaryFile(ByteBuffer binaryData, int roundedFileSize) {
		clearDocument(docBinary);
		// docBinary = txtBinaryFile.getDocument();
		txtWIPbinary.setDocument(docBinary);
		byte[] displayLine = new byte[CHARACTERS_PER_LINE];
		binaryData.position(OFFSET);
		try {
			int lineNumber = OFFSET / CHARACTERS_PER_LINE;
			while (binaryData.position() + CHARACTERS_PER_LINE <= binaryData.capacity()) {
				// System.out.printf("<displayBinaryFile> lineNumber = %d,  position = %d%n", lineNumber,
				// binaryData.position());
				binaryData.get(displayLine, 0, CHARACTERS_PER_LINE);
				docBinary.insertString(docBinary.getLength(), formatLine(displayLine, lineNumber++), null);
			}// while
		} catch (BadLocationException badLocationException) {
			badLocationException.printStackTrace();

		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			indexOutOfBoundsException.printStackTrace();
		}
		binaryData.rewind();
		txtBinaryFile.setCaretPosition(0);
		txtWIPbinary.setCaretPosition(0);
	}// displayBinaryFile

	private String formatLine(byte[] lineToDisplay, int lineNumber) {
		StringBuilder sbHex = new StringBuilder();
		StringBuilder sbDot = new StringBuilder();

		// byte[] lineOfBytes = new byte[CHARACTERS_PER_LINE];
		byte subject;

		sbHex.append(String.format("%04X: ", lineNumber * CHARACTERS_PER_LINE));
		// int bufferIndex;
		for (int i = 0; i < lineToDisplay.length; i++) {
			// bufferIndex = (lineNumber * CHARACTERS_PER_LINE) + i;
			subject = lineToDisplay[i];
			sbHex.append(String.format("%02X ", subject));
			sbDot.append((subject >= 0x20 && subject <= 0x7F) ? (char) subject : ".");
			if (i == 7) {
				sbHex.append(" ");
				sbDot.append(" ");
			}// if
		}// for
		sbHex.append(" ");
		sbDot.append(String.format("%n"));
		return sbHex.toString() + sbDot.toString();
	}// formatLine

	private void clearDocument(Document doc) {
		if (doc == null) {
			return;
		}// if
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			// ignore
			e.printStackTrace();
		}// try
	}// clearDocument

	private void haveBinanryFile(boolean state) {
		btnStart.setEnabled(state);
		spinnerBeginFragment.setEnabled(state);
		spinnerEndFragment.setEnabled(state);
		panelFragmentTypes.setEnabled(state);
		mnuFileSaveWIP.setEnabled(state);
		mnuFileSaveWIPas.setEnabled(state);
		mnuFileReset.setEnabled(state);

		mnuFileLoadWIP.setEnabled(!state);
		mnuFileOpenBinaryFile.setEnabled(!state);

		if (state) {

		} else {
			btnAddFragment.setEnabled(state);
			btnRemoveFragment.setEnabled(state);
			btnProcessFragment.setEnabled(state);
			btnCombineFragments.setEnabled(state);
			btnBuildASM.setEnabled(state);
			listCodeFragments.setEnabled(state);
		}// if
	}// haveBinanryFile

	private void setFragmentRadioButton(String type) {
		switch (type) {
		case CodeFragment.CODE:
			rbCode.setSelected(true);
			break;
		case CodeFragment.CONSTANT:
			rbConstant.setSelected(true);
			break;
		case CodeFragment.LITERAL:
			rbLiteral.setSelected(true);
			break;
		case CodeFragment.RESERVED:
			rbReserved.setSelected(true);
			break;
		case CodeFragment.UNKNOWN:
			rbUnknown.setSelected(true);
			break;
		}// switch
	}// //

	private void displayFragmenBinary(JTextArea txtArea, int startLocation, int endLocation) {
		Highlighter.HighlightPainter yellowPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

		try {
			int lineStart = txtArea.getLineStartOffset((startLocation / CHARACTERS_PER_LINE));
			int lineEnd = txtArea.getLineEndOffset((endLocation / CHARACTERS_PER_LINE));

			txtArea.getHighlighter().removeAllHighlights();
			txtArea.getHighlighter().addHighlight(lineStart, lineEnd, yellowPainter);
			txtArea.setCaretPosition(lineStart);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//
	}// displayFragmentSource

	private void displayFragmentSource(JTextArea txtArea, CodeFragment codeFragment) {
		String type = codeFragment.type;
		switch (type) {
		case CodeFragment.CODE:
			showFragmentCode(txtArea.getDocument(), codeFragment);
			break;
		case CodeFragment.CONSTANT:
			break;
		case CodeFragment.LITERAL:
			break;
		case CodeFragment.RESERVED:
			break;
		case CodeFragment.UNKNOWN:
			showFragmentCode(txtArea.getDocument(), codeFragment);
			break;
		default:
		}// switch
		txtArea.setCaretPosition(0);
	}// displayFragmentSource

	private void showFragmentCode(Document doc, CodeFragment codeFragment) {
		int startLocation = codeFragment.startLoc;
		int endLocation = codeFragment.endLoc;
		int codeSize = codeFragment.size();
		clearDocument(doc);
		OpcodeStructure8080 currentOpCode = null;

		int currentLocation = startLocation;
		int opCodeSize;
		byte currentValue0, currentValue1, currentValue2;
		String part1, part2, part3, part4;
		while (currentLocation <= endLocation) {
			currentOpCode = opcodeMap.get(binaryData.get(currentLocation));
			currentValue0 = binaryData.get(currentLocation);
			opCodeSize = currentOpCode.getSize();
			currentValue1 = opCodeSize > 1 ? binaryData.get(currentLocation + 1) : 0;
			currentValue2 = opCodeSize > 2 ? binaryData.get(currentLocation + 2) : 0;
			try {

				part1 = String.format("%04X%4s", currentLocation, "");
				doc.insertString(doc.getLength(), part1, null);
				switch (opCodeSize) {
				case 1:
					part2 = String.format("%02X%8s", currentValue0, "");
					doc.insertString(doc.getLength(), part2, null);
					part3 = String.format("%-15s", currentOpCode.getAssemblerCode());
					doc.insertString(doc.getLength(), part3, null);
					break;
				case 2:
					part2 = String.format("%02X%02X%6s", currentValue0, currentValue1, "");
					doc.insertString(doc.getLength(), part2, null);
					part3 = String.format("%-15s", currentOpCode.getAssemblerCode(currentValue1));
					doc.insertString(doc.getLength(), part3, null);
					break;
				case 3:
					part2 = String.format("%02X%02X%02X%4s", currentValue0, currentValue1, currentValue2, "");
					doc.insertString(doc.getLength(), part2, null);
					part3 = String.format("%-15s", currentOpCode.getAssemblerCode(currentValue1, currentValue2));
					doc.insertString(doc.getLength(), part3, null);
					break;
				default:
				}// switch
				part4 = String.format("%s%n", currentOpCode.getFunction());
				doc.insertString(doc.getLength(), part4, null);
			} catch (BadLocationException badLocationException) {
				badLocationException.printStackTrace();
			}
			currentLocation += opCodeSize;

		}// while opcodeMap
	}// showFragmentCode

	private void buildSourceHeader(Document doc) {
		lblSourceHeader.setText(binaryFilePath);
		clearDocument(docASM);
		String header = String.format(";Source File name - %s%n", binaryFileName);
		appendToDocASM(header);
		header = String.format(";Generated by - ManualDisassembler V A.0 on %s%n%n", new Date().toString());
		appendToDocASM(header);
		header = String.format("%-10s %-7s %-10s %-25s%n", "LF", "EQU", "0AH", "; Line Feed");
		appendToDocASM(header);
		header = String.format("%-10s %-7s %-10s %-25s%n", "CR", "EQU", "0DH", "; Carriage Return");
		appendToDocASM(header);
		header = String.format("%-10s %-7s %-10s %-25s%n", "DOLLARSIGN", "EQU", "24H", "; Dollar Sign");
		appendToDocASM(header);
		header = String.format("%-10s %-7s %-10s %-25s%n", "QMARK", "EQU", "3FH", "; Question Mark");
		appendToDocASM(header);

		header = String.format("%n%n%15s  %05XH%n%n", "ORG", OFFSET);
		appendToDocASM(header);

	}// buildSourceHeader

	private void buildFragmentHeader(Document doc, CodeFragment codeFragment) {
		String displayText = String.format(
				"%n;     <New %s fragment-----from %04X to %04X (%3$4X : %3$4d)>%n",
				codeFragment.type, codeFragment.startLoc, codeFragment.endLoc, codeFragment.size());
		appendToDocASM(displayText);
		displayText = String.format(";%17s  %05XH%n", "ORG", codeFragment.startLoc);
		appendToDocASM(displayText);
	}

	private void appendToDocASM(String textToAppend) {
		appendToDocASM(textToAppend, null);
	}// appendToDoc

	private void appendToDocASM(String textToAppend, AttributeSet attributeSet) {
		try {
			docASM.insertString(docASM.getLength(), textToAppend, attributeSet);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// try
	}// appendToDoc

	private void buildASM() {
		if (codeFragmentModel.getSize() < 2) {
			return;
		}// if
		buildSourceHeader(docASM);
		CodeFragment codeFragment;

		for (int i = 0; i < codeFragmentModel.getSize(); i++) {
			codeFragment = codeFragmentModel.getElementAt(i);
			switch (codeFragment.type) {
			case CodeFragment.CODE:
				buildCodeFragement(txtASM.getDocument(), codeFragment);
				break;
			case CodeFragment.CONSTANT:
				buildConstantFragment(txtASM.getDocument(), codeFragment);
				break;
			case CodeFragment.LITERAL:
				buildLiteralFragment(txtASM.getDocument(), codeFragment);
				break;
			case CodeFragment.RESERVED:
				buildUnknownFragment(txtASM.getDocument(), codeFragment);
				break;
			case CodeFragment.UNKNOWN:
				buildUnknownFragment(txtASM.getDocument(), codeFragment);
				break;
			default:
			}// switch

		}// for
		txtASM.setCaretPosition(0);
	}// buildASM

	private void buildLiteralFragment(Document doc, CodeFragment codeFragment) {

		buildFragmentHeader(doc, codeFragment);
		byte[] literalValues = new byte[codeFragment.size()];
		binaryData.position(codeFragment.startLoc);
		binaryData.get(literalValues, 0, codeFragment.size());
		int crCount = 0;
		int lfCount = 0;

		if (codeFragment.size() > 3) { // look for line terminators

		}

		String literalData = new String(literalValues);
		System.out.println(literalData);
		String displayText = String.format("%17s  '%s'%n", "DB", literalData);
		appendToDocASM(displayText);

	}// buildUnknownFragent

	private void buildConstantFragment(Document doc, CodeFragment codeFragment) {
		int dbPerLine = 5;
		int locBase = codeFragment.startLoc;
		buildFragmentHeader(doc, codeFragment);
		String displayText = null;
		for (int i = codeFragment.size(); i > 0;) {
			if (i == 0) {
				break;
			}// if
			switch (i % dbPerLine) {
			case 0:
				displayText = String.format("%17s  %03XH,%03XH,%03XH,%03XH,%03XH%n", "DB",
						binaryData.get(locBase), binaryData.get(locBase + 1), binaryData.get(locBase + 2),
						binaryData.get(locBase + 3), binaryData.get(locBase + 4));
				i -= dbPerLine;
				locBase += dbPerLine;
				break;
			case 4:
				displayText = String.format("%17s  %03XH,%03XH,%03XH,%03XH%n", "DB",
						binaryData.get(locBase), binaryData.get(locBase + 1), binaryData.get(locBase + 2),
						binaryData.get(locBase + 3));
				i -= dbPerLine - 1;
				locBase += dbPerLine - 1;
				break;
			case 3:
				displayText = String.format("%17s  %03XH,%03XH,%03XH%n", "DB",
						binaryData.get(locBase), binaryData.get(locBase + 1), binaryData.get(locBase + 2));
				i -= dbPerLine - 2;
				locBase += dbPerLine - 2;
				break;
			case 2:
				displayText = String.format("%17s  %03XH,%03XH%n", "DB",
						binaryData.get(locBase), binaryData.get(locBase + 1));
				i -= dbPerLine - 3;
				locBase += dbPerLine - 3;
				break;
			case 1:
				displayText = String.format("%17s  %03XH%n", "DB",
						binaryData.get(locBase));
				i -= dbPerLine - 4;
				locBase += dbPerLine - 4;
				break;
			}// switch
				// displayText = String.format("%17s  %05XH%n", "DS", codeFragment.size());
			appendToDocASM(displayText);

		}

	}// buildUnknownFragent

	private void buildUnknownFragment(Document doc, CodeFragment codeFragment) {

		buildFragmentHeader(doc, codeFragment);
		String displayText = String.format("%17s  %05XH%n", "DS", codeFragment.size());
		appendToDocASM(displayText);

	}// buildUnknownFragent

	private void buildCodeFragement(Document doc, CodeFragment codeFragment) {
		buildFragmentHeader(doc, codeFragment);
		OpcodeStructure8080 currentOpCode = null;

		int currentLocation = codeFragment.startLoc;
		int endLocation = codeFragment.endLoc;
		int opCodeSize;

		byte currentValue0, currentValue1, currentValue2;
		String part1, part2, part3, part4, thisLabel;
		while (currentLocation <= endLocation) {
			currentOpCode = opcodeMap.get(binaryData.get(currentLocation));
			currentValue0 = binaryData.get(currentLocation);
			opCodeSize = currentOpCode.getSize();
			currentValue1 = opCodeSize > 1 ? binaryData.get(currentLocation + 1) : 0;
			currentValue2 = opCodeSize > 2 ? binaryData.get(currentLocation + 2) : 0;
			try {
				if (labels.contains(currentLocation)) {
					appendToDocASM(String.format("L%04X:%n", currentLocation));
				}// if - its a label

				switch (opCodeSize) {
				case 1:
					part3 = String.format("%10s%-15s%n", "", currentOpCode.getAssemblerCode());
					doc.insertString(doc.getLength(), part3, null);
					break;
				case 2:
					part3 = String.format("%10s%-15s%n", "", currentOpCode.getAssemblerCode(currentValue1));
					doc.insertString(doc.getLength(), part3, null);
					break;
				case 3:
					part3 = String.format("%10s%-15s%n", "",
							currentOpCode.getAssemblerCode(currentValue1, currentValue2));
					doc.insertString(doc.getLength(), part3, null);
					break;
				default:
				}// switch
			} catch (BadLocationException badLocationException) {
				badLocationException.printStackTrace();
			}
			currentLocation += opCodeSize;

		}// while opcodeMap
			// txtArea.setCaretPosition(0);
	}// buildCodeFragment

	// ---------------------------------
	private JFileChooser getFileChooser(String directory, String filterDescription, String... filterExtensions) {
		Path sourcePath = Paths.get(directory);
		if (!Files.exists(sourcePath)) {
			try {
				Files.createDirectory(sourcePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// try
		}// if - make directory if not there

		JFileChooser chooser = new JFileChooser(directory);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDescription, filterExtensions));
		chooser.setAcceptAllFileFilterUsed(false);
		return chooser;
	}// getFileChooser
		// private void saveWIPas(){

	//
	// }

	private void saveWIP() {
		String fileNameParts[] = binaryFileName.split("\\.");
		// String fileName = fileNameParts[0] + FILE_SUFFIX_PERIOD;
		saveWIP(fileNameParts[0] + FILE_SUFFIX_PERIOD);

	}// saveWIP

	private void saveWIP(String fileName) {
		try {
			String relativeFileName = FILE_LOCATION + "/" + fileName;
			System.out.printf("relativeFileName = %s%n", relativeFileName);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(relativeFileName));
			oos.writeObject(binaryFile);
			// oos.writeObject(binaryData);
			oos.writeObject(beenThere);
			oos.writeObject(labels);
			oos.writeObject(codeFragmentModel);
			oos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}// try - write objects
	}// saveWIP

	private void loadWIP(String fileName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
			binaryFile = (File) ois.readObject();
			// binaryData = (ByteBuffer) ois.readObject();
			beenThere = (Set<Integer>) ois.readObject();
			labels = (Set<Integer>) ois.readObject();
			codeFragmentModel = (CodeFragmentModel) ois.readObject();
			ois.close();
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(null, binaryFile.getAbsolutePath()
					+ "not found", "loadWIP()",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		} catch (IOException ie) {
			JOptionPane.showMessageDialog(null, binaryFile.getAbsolutePath()
					+ ie.getMessage(), "IO error",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "ClassNotFoundException", "loadWIP()",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		}// try

		if (!processBianryFile(binaryFile)) {
			JOptionPane.showMessageDialog(null, "Problem with binary Source file" + binaryFile.getAbsolutePath()
					, "openBinaryFile()",
					JOptionPane.ERROR_MESSAGE);
			return;
		}//if
		

		btnProcessFragment.setEnabled(true);
		btnAddFragment.setEnabled(true);
		btnRemoveFragment.setEnabled(true);
		btnCombineFragments.setEnabled(true);
		btnBuildASM.setEnabled(true);
		listCodeFragments.setModel(codeFragmentModel);
		listCodeFragments.setEnabled(true);

		listCodeFragments.updateUI();
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String actionCommand = actionEvent.getActionCommand();
		String message = null;
		switch (actionCommand) {
		case AC_MNU_FILE_OPEN:
			openBinaryFile();
			break;
		case AC_MNU_FILE_LOAD_WIP:
			JFileChooser chooserLoad = getFileChooser(FILE_LOCATION, WIP, FILE_SUFFIX);
			if (chooserLoad.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
				System.out.printf("You cancelled the Save as...%n", "");
			} else {
				String fileNameParts[] = chooserLoad.getSelectedFile().getName().split("\\.");
				loadWIP(fileNameParts[0] + FILE_SUFFIX_PERIOD);
			}// if - returnValue

			break;
		case AC_MNU_FILE_SAVE_WIP:
			saveWIP();
			break;
		case AC_MNU_FILE_SAVE_WIP_AS:
			JFileChooser chooserSaveAs = getFileChooser(FILE_LOCATION, WIP, FILE_SUFFIX);
			if (chooserSaveAs.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
				System.out.printf("You cancelled the Save as...%n", "");
			} else {
				String fileNameParts[] = chooserSaveAs.getSelectedFile().getName().split("\\.");
				saveWIP(fileNameParts[0] + FILE_SUFFIX_PERIOD);
			}// if - returnValue
			break;
		case AC_MNU_FILE_RESET:
			appInit();
			break;
		case AC_MNU_FILE_EXIT:
			appClose();

			break;
		case AC_MNU_CODE_NEW:
			message = "mnuCodeFragmentNew";
			break;
		case AC_MNU_CODE_LOAD:
			message = "mnuCodeFragmentLoad";
			break;
		case AC_MNU_CODE_SAVE:
			message = "mnuCodeFragmentSave";
			break;
		case AC_MNU_CODE_SAVE_AS:
			message = "mnuCodeFragmentSaveAs";
			break;

		case AC_BTN_START:
			actionStart();
			break;
		case AC_BTN_BUILD_ASM:
			buildASM();
			break;

		case AC_BTN_ADD_FRAGMENT:
			addFragement();
			break;
		case AC_BTN_REMOVE_FRAGMENT:
			removeFragment();
			break;
		case AC_BTN_PROCESS_FRAGMENT:
			processFragment();
			break;
		case AC_BTN_COMBINE_FRAGMENTS:
			combineFragments();
			break;
		default:
			message = actionCommand;
		}// switch
		if (message != null) {
			System.out.printf("<actionPerformed> actionCommand = %s%n", actionCommand);
		}// if
	}// actionPerformed

	@Override
	public void valueChanged(ListSelectionEvent lse) {
		if (lse.getValueIsAdjusting()) {
			return;
		}
		int index = ((JList) lse.getSource()).getSelectedIndex();
		if (index == -1) {
			return;
		}
		CodeFragment codeFragment = codeFragmentModel.getElementAt(index);
		spinnerBeginFragment.setValue(codeFragment.startLoc);
		spinnerEndFragment.setValue(codeFragment.endLoc);
		setFragmentRadioButton(codeFragment.type);
		displayFragmenBinary(txtWIPbinary, codeFragment.startLoc - OFFSET, codeFragment.endLoc - OFFSET);
		displayFragmentSource(txtWIPsource, codeFragment);
	}// valueChanged

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private void appClose() {
		System.exit(0);
	}// appClose

	private void appInit() {
		// System.out.printf("System.getProperty(\"java.class.path\") = %s%n",System.getProperty("java.class.path"));
		// System.out.printf("%n--------Enviornment Variables --------------%n");
		// Map<String,String> envVars = System.getenv();
		// Set<String> envVarKeys = envVars.keySet();
		// System.out.printf("$d vars in list%n%n",envVars.size());
		// for(String enVarKey:envVarKeys){
		// System.out.printf("%25s  :  %-25s%n",enVarKey,System.getenv(enVarKey));
		// }
		// System.out.printf("%n--------System Properties-------------%n");
		// Properties properties = System.getProperties();
		// Set<String> propertyNames = properties.stringPropertyNames();
		// for(String propertyName : propertyNames ){
		// System.out.printf("%25s  :  %-25s%n",propertyName,System.getProperty(propertyName));
		// }
		// System.out.printf("FILE_LOCATION = %s%n",FILE_LOCATION);

		if (opcodeMap == null) {
			opcodeMap = Opcodes8080.makeCodeMap();
		}// if
		if (codeFragmentModel != null) {
			codeFragmentModel = null;
		}// if
		if (entryPoints != null) {
			entryPoints = null;
		}// if

		entryPoints = new Stack<Integer>();
		codeFragmentModel = new CodeFragmentModel();

		docBinary = txtBinaryFile.getDocument();
		docASM = txtASM.getDocument();
		clearDocument(txtWIPsource.getDocument());
		clearDocument(docBinary);
		clearDocument(docASM);

		listCodeFragments.setModel(codeFragmentModel);

		haveBinanryFile(false);
		binaryFileName = "<No biary file selected>";
		frame.setTitle(APP_NAME + "   " + binaryFileName);
	}// appInit

	/**
	 * Create the application.
	 */
	public ManualDisassembler() {
		initialize();
		appInit();
	}

	// ------------------------------------------------------------------------------
	private File binaryFile;
	private String binaryFilePath;
	private String binaryFileName = "";
	private ByteBuffer binaryData;
	private CodeFragmentModel codeFragmentModel;
	private Opcodes8080 opcodeMap;
	private Document docBinary;
	// private Document docWIPbinary;
	// private Document docWIPsource;
	private Document docASM;

	private Stack<Integer> entryPoints;
	private Set<Integer> beenThere;
	private Set<Integer> labels;

	private final int OFFSET = 0X0100; // Transient Program Area starts at 256 (0x0100)
	private final int CHARACTERS_PER_LINE = 0x0010; //

	private final int TAB_WIP = 0;
	private final int TAB_BINARY_FILE = 1;
	private final int TAB_ASM = 2;

	// private final static String AC_MNU_FILE_NEW = "mnuFileNew";
	private final static String AC_MNU_FILE_OPEN = "mnuFileOpen";
	private final static String AC_MNU_FILE_LOAD_WIP = "mnuFileLoadWIP";
	private final static String AC_MNU_FILE_SAVE_WIP = "mnuFileSaveWIP";
	private final static String AC_MNU_FILE_SAVE_WIP_AS = "mnuFileSaveWIPAs";
	private final static String AC_MNU_FILE_RESET = "mnuFileReset";
	private final static String AC_MNU_FILE_EXIT = "mnuFileExit";

	private final static String AC_MNU_CODE_NEW = "mnuCodeFragmentNew";
	private final static String AC_MNU_CODE_LOAD = "mnuCodeFragmentLoad";
	private final static String AC_MNU_CODE_SAVE = "mnuCodeFragmentSave";
	private final static String AC_MNU_CODE_SAVE_AS = "mnuCodeFragmentSaveAs";

	private final static String AC_BTN_START = "btnStart";
	private final static String AC_BTN_BUILD_ASM = "btnBuildASM";

	private final static String AC_BTN_ADD_FRAGMENT = "btnAddFragment";
	private final static String AC_BTN_REMOVE_FRAGMENT = "btnRemoveFragment";
	private final static String AC_BTN_PROCESS_FRAGMENT = "btnProcessFragment";
	private final static String AC_BTN_COMBINE_FRAGMENTS = "btnCombineFragment";

	private final static String APP_NAME = "Manual Disassembler ";
	private final static String SEMI = ";"; // SemiColon
	private final static String WIP = "Work In Process";
	private final static String FILE_SUFFIX = "WIP";
	private final static String FILE_SUFFIX_PERIOD = "." + FILE_SUFFIX;
	public final static String FILE_SEPARATOR = System.getProperty("file.separator");
	public final static String FILE_LOCATION = System.getenv("APPDATA") + FILE_SEPARATOR + "Disassembler";
	// public final static String FILE_LOCATION = "Disassembler";file.separator

	private Hex64KSpinner spinnerEndFragment;
	private Hex64KSpinner spinnerBeginFragment;
	private JRadioButton rbUnknown;
	private JRadioButton rbReserved;
	private JRadioButton rbLiteral;
	private JRadioButton rbConstant;
	private JRadioButton rbCode;
	private JList listCodeFragments;
	private JButton btnStart;
	private JButton btnAddFragment;
	private JButton btnRemoveFragment;
	private JButton btnProcessFragment;
	private final ButtonGroup groupFragment = new ButtonGroup();
	private JPanel panelFragmentTypes;
	private JTextArea txtBinaryFile;
	private JTabbedPane tabPaneDisplays;
	private JTextArea txtWIPbinary;
	private JScrollPane scrollPaneWIPbinary;
	private JTextArea txtWIPsource;
	private JButton btnCombineFragments;
	private JButton btnBuildASM;
	private JTextArea txtASM;
	private JLabel lblSourceHeader;
	private JMenuItem mnuFileSaveWIP;
	private JMenuItem mnuFileLoadWIP;
	private JMenuItem mnuFileSaveWIPas;
	private JMenuItem mnuFileOpenBinaryFile;
	private JSeparator separator_2;
	private JMenuItem mnuFileReset;

	// ------------------------------------------------------------------------------

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 986, 836);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		JPanel paneTop = new JPanel();
		GridBagConstraints gbc_paneTop = new GridBagConstraints();
		gbc_paneTop.fill = GridBagConstraints.BOTH;
		gbc_paneTop.gridx = 0;
		gbc_paneTop.gridy = 0;
		frame.getContentPane().add(paneTop, gbc_paneTop);
		GridBagLayout gbl_paneTop = new GridBagLayout();
		gbl_paneTop.columnWidths = new int[] { 0, 0, 0 };
		gbl_paneTop.rowHeights = new int[] { 0, 0, 0 };
		gbl_paneTop.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_paneTop.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		paneTop.setLayout(gbl_paneTop);

		btnStart = new JButton("Start");
		btnStart.setActionCommand(AC_BTN_START);
		btnStart.addActionListener(this);
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.anchor = GridBagConstraints.WEST;
		gbc_btnStart.insets = new Insets(0, 0, 5, 5);
		gbc_btnStart.gridx = 0;
		gbc_btnStart.gridy = 0;
		paneTop.add(btnStart, gbc_btnStart);

		btnBuildASM = new JButton("Build ASM");
		btnBuildASM.setActionCommand(AC_BTN_BUILD_ASM);
		btnBuildASM.addActionListener(this);
		GridBagConstraints gbc_btnBuildASM = new GridBagConstraints();
		gbc_btnBuildASM.insets = new Insets(0, 0, 5, 0);
		gbc_btnBuildASM.gridx = 1;
		gbc_btnBuildASM.gridy = 0;
		paneTop.add(btnBuildASM, gbc_btnBuildASM);

		JPanel panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 1;
		frame.getContentPane().add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 270, 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		JTabbedPane tabPaneFragments = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabPaneFragments = new GridBagConstraints();
		gbc_tabPaneFragments.insets = new Insets(0, 0, 0, 5);
		gbc_tabPaneFragments.fill = GridBagConstraints.BOTH;
		gbc_tabPaneFragments.gridx = 0;
		gbc_tabPaneFragments.gridy = 0;
		panelMain.add(tabPaneFragments, gbc_tabPaneFragments);

		JPanel panelFragments = new JPanel();
		panelFragments.setMinimumSize(new Dimension(0, 0));
		panelFragments.setPreferredSize(new Dimension(0, 0));
		tabPaneFragments.addTab("Code Fragments", null, panelFragments, null);
		GridBagLayout gbl_panelFragments = new GridBagLayout();
		gbl_panelFragments.columnWidths = new int[] { 60, 0 };
		gbl_panelFragments.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelFragments.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelFragments.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelFragments.setLayout(gbl_panelFragments);

		JPanel panelBeginEnd = new JPanel();
		GridBagConstraints gbc_panelBeginEnd = new GridBagConstraints();
		gbc_panelBeginEnd.insets = new Insets(0, 0, 5, 0);
		gbc_panelBeginEnd.fill = GridBagConstraints.VERTICAL;
		gbc_panelBeginEnd.gridx = 0;
		gbc_panelBeginEnd.gridy = 0;
		panelFragments.add(panelBeginEnd, gbc_panelBeginEnd);
		GridBagLayout gbl_panelBeginEnd = new GridBagLayout();
		gbl_panelBeginEnd.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelBeginEnd.rowHeights = new int[] { 0, 0 };
		gbl_panelBeginEnd.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelBeginEnd.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelBeginEnd.setLayout(gbl_panelBeginEnd);

		JLabel lblBegin = new JLabel("  Begin   ");
		GridBagConstraints gbc_lblBegin = new GridBagConstraints();
		gbc_lblBegin.insets = new Insets(0, 0, 0, 5);
		gbc_lblBegin.gridx = 0;
		gbc_lblBegin.gridy = 0;
		panelBeginEnd.add(lblBegin, gbc_lblBegin);

		spinnerBeginFragment = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerBeginFragment = new GridBagConstraints();
		gbc_spinnerBeginFragment.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerBeginFragment.gridx = 1;
		gbc_spinnerBeginFragment.gridy = 0;
		panelBeginEnd.add(spinnerBeginFragment, gbc_spinnerBeginFragment);
		spinnerBeginFragment.setMinimumSize(new Dimension(50, 20));
		spinnerBeginFragment.setPreferredSize(new Dimension(50, 20));

		JLabel lblEnd = new JLabel(" End  ");
		GridBagConstraints gbc_lblEnd = new GridBagConstraints();
		gbc_lblEnd.insets = new Insets(0, 0, 0, 5);
		gbc_lblEnd.gridx = 2;
		gbc_lblEnd.gridy = 0;
		panelBeginEnd.add(lblEnd, gbc_lblEnd);

		spinnerEndFragment = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerEndFragment = new GridBagConstraints();
		gbc_spinnerEndFragment.gridx = 3;
		gbc_spinnerEndFragment.gridy = 0;
		panelBeginEnd.add(spinnerEndFragment, gbc_spinnerEndFragment);
		spinnerEndFragment.setPreferredSize(new Dimension(50, 20));
		spinnerEndFragment.setMinimumSize(new Dimension(50, 20));

		panelFragmentTypes = new JPanel();
		panelFragmentTypes.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
				"Fragement Type", TitledBorder.CENTER, TitledBorder.BELOW_TOP, null, null));
		GridBagConstraints gbc_panelFragmentTypes = new GridBagConstraints();
		gbc_panelFragmentTypes.insets = new Insets(0, 0, 5, 0);
		gbc_panelFragmentTypes.fill = GridBagConstraints.VERTICAL;
		gbc_panelFragmentTypes.gridx = 0;
		gbc_panelFragmentTypes.gridy = 1;
		panelFragments.add(panelFragmentTypes, gbc_panelFragmentTypes);
		GridBagLayout gbl_panelFragmentTypes = new GridBagLayout();
		gbl_panelFragmentTypes.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelFragmentTypes.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelFragmentTypes.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelFragmentTypes.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelFragmentTypes.setLayout(gbl_panelFragmentTypes);

		rbCode = new JRadioButton("Code");
		rbCode.setActionCommand(CodeFragment.CODE);
		groupFragment.add(rbCode);
		GridBagConstraints gbc_rbCode = new GridBagConstraints();
		gbc_rbCode.anchor = GridBagConstraints.WEST;
		gbc_rbCode.insets = new Insets(0, 0, 5, 5);
		gbc_rbCode.gridx = 0;
		gbc_rbCode.gridy = 0;
		panelFragmentTypes.add(rbCode, gbc_rbCode);

		rbConstant = new JRadioButton("Constant");
		rbConstant.setActionCommand(CodeFragment.CONSTANT);
		groupFragment.add(rbConstant);
		GridBagConstraints gbc_rbConstant = new GridBagConstraints();
		gbc_rbConstant.anchor = GridBagConstraints.WEST;
		gbc_rbConstant.insets = new Insets(0, 0, 5, 5);
		gbc_rbConstant.gridx = 1;
		gbc_rbConstant.gridy = 0;
		panelFragmentTypes.add(rbConstant, gbc_rbConstant);

		rbLiteral = new JRadioButton("Literal");
		rbLiteral.setActionCommand(CodeFragment.LITERAL);
		groupFragment.add(rbLiteral);
		GridBagConstraints gbc_rbLiteral = new GridBagConstraints();
		gbc_rbLiteral.anchor = GridBagConstraints.WEST;
		gbc_rbLiteral.insets = new Insets(0, 0, 5, 0);
		gbc_rbLiteral.gridx = 2;
		gbc_rbLiteral.gridy = 0;
		panelFragmentTypes.add(rbLiteral, gbc_rbLiteral);

		rbReserved = new JRadioButton("Reserved");
		rbReserved.setActionCommand(CodeFragment.RESERVED);
		groupFragment.add(rbReserved);
		GridBagConstraints gbc_rbReserved = new GridBagConstraints();
		gbc_rbReserved.anchor = GridBagConstraints.WEST;
		gbc_rbReserved.insets = new Insets(0, 0, 0, 5);
		gbc_rbReserved.gridx = 0;
		gbc_rbReserved.gridy = 1;
		panelFragmentTypes.add(rbReserved, gbc_rbReserved);

		rbUnknown = new JRadioButton("Unknown");
		rbUnknown.setActionCommand(CodeFragment.UNKNOWN);
		groupFragment.add(rbUnknown);
		GridBagConstraints gbc_rbUnknown = new GridBagConstraints();
		gbc_rbUnknown.anchor = GridBagConstraints.WEST;
		gbc_rbUnknown.gridx = 2;
		gbc_rbUnknown.gridy = 1;
		panelFragmentTypes.add(rbUnknown, gbc_rbUnknown);

		JPanel panelFragmentButtons = new JPanel();
		GridBagConstraints gbc_panelFragmentButtons = new GridBagConstraints();
		gbc_panelFragmentButtons.insets = new Insets(0, 0, 5, 0);
		gbc_panelFragmentButtons.fill = GridBagConstraints.VERTICAL;
		gbc_panelFragmentButtons.gridx = 0;
		gbc_panelFragmentButtons.gridy = 2;
		panelFragments.add(panelFragmentButtons, gbc_panelFragmentButtons);
		GridBagLayout gbl_panelFragmentButtons = new GridBagLayout();
		gbl_panelFragmentButtons.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelFragmentButtons.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelFragmentButtons.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelFragmentButtons.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelFragmentButtons.setLayout(gbl_panelFragmentButtons);

		btnAddFragment = new JButton("Add/Update");
		btnAddFragment.setActionCommand(AC_BTN_ADD_FRAGMENT);
		btnAddFragment.addActionListener(this);
		GridBagConstraints gbc_btnAddFragment = new GridBagConstraints();
		gbc_btnAddFragment.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddFragment.gridx = 0;
		gbc_btnAddFragment.gridy = 0;
		panelFragmentButtons.add(btnAddFragment, gbc_btnAddFragment);

		btnRemoveFragment = new JButton("Remove");
		btnRemoveFragment.setActionCommand(AC_BTN_REMOVE_FRAGMENT);
		btnRemoveFragment.addActionListener(this);
		GridBagConstraints gbc_btnRemoveFragment = new GridBagConstraints();
		gbc_btnRemoveFragment.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveFragment.gridx = 1;
		gbc_btnRemoveFragment.gridy = 0;
		panelFragmentButtons.add(btnRemoveFragment, gbc_btnRemoveFragment);

		btnProcessFragment = new JButton("Process");
		btnProcessFragment.setActionCommand(AC_BTN_PROCESS_FRAGMENT);
		btnProcessFragment.addActionListener(this);
		GridBagConstraints gbc_btnProcessFragment = new GridBagConstraints();
		gbc_btnProcessFragment.insets = new Insets(0, 0, 0, 5);
		gbc_btnProcessFragment.gridx = 0;
		gbc_btnProcessFragment.gridy = 1;
		panelFragmentButtons.add(btnProcessFragment, gbc_btnProcessFragment);

		btnCombineFragments = new JButton("Combine");
		btnCombineFragments.setActionCommand(AC_BTN_COMBINE_FRAGMENTS);
		btnCombineFragments.addActionListener(this);
		GridBagConstraints gbc_btnCombineFragments = new GridBagConstraints();
		gbc_btnCombineFragments.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnCombineFragments.gridx = 1;
		gbc_btnCombineFragments.gridy = 1;
		panelFragmentButtons.add(btnCombineFragments, gbc_btnCombineFragments);

		JScrollPane scrollPaneFragments = new JScrollPane();
		GridBagConstraints gbc_scrollPaneFragments = new GridBagConstraints();
		gbc_scrollPaneFragments.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneFragments.gridx = 0;
		gbc_scrollPaneFragments.gridy = 3;
		panelFragments.add(scrollPaneFragments, gbc_scrollPaneFragments);

		listCodeFragments = new JList();
		listCodeFragments.addListSelectionListener(this);
		listCodeFragments.setFont(new Font("Courier New", Font.PLAIN, 12));
		scrollPaneFragments.setViewportView(listCodeFragments);

		JLabel lblNewLabel = new JLabel("Start   End    Len     Type");
		lblNewLabel.setPreferredSize(new Dimension(110, 14));
		lblNewLabel.setMinimumSize(new Dimension(110, 14));
		lblNewLabel.setForeground(Color.BLUE);
		lblNewLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
		scrollPaneFragments.setColumnHeaderView(lblNewLabel);

		tabPaneDisplays = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabPaneDisplays = new GridBagConstraints();
		gbc_tabPaneDisplays.fill = GridBagConstraints.BOTH;
		gbc_tabPaneDisplays.gridx = 1;
		gbc_tabPaneDisplays.gridy = 0;
		panelMain.add(tabPaneDisplays, gbc_tabPaneDisplays);

		JPanel panelWIP = new JPanel();
		tabPaneDisplays.addTab("WIP", null, panelWIP, null);
		GridBagLayout gbl_panelWIP = new GridBagLayout();
		gbl_panelWIP.columnWidths = new int[] { 0, 0 };
		gbl_panelWIP.rowHeights = new int[] { 0, 0 };
		gbl_panelWIP.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelWIP.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelWIP.setLayout(gbl_panelWIP);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		panelWIP.add(splitPane, gbc_splitPane);

		scrollPaneWIPbinary = new JScrollPane();
		splitPane.setLeftComponent(scrollPaneWIPbinary);

		txtWIPbinary = new JTextArea();
		txtWIPbinary.setEditable(false);
		txtWIPbinary.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneWIPbinary.setViewportView(txtWIPbinary);

		JLabel label_1 = new JLabel("      00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F");
		label_1.setForeground(Color.BLUE);
		label_1.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneWIPbinary.setColumnHeaderView(label_1);

		JScrollPane scrollPaneWIPsource = new JScrollPane();
		splitPane.setRightComponent(scrollPaneWIPsource);

		txtWIPsource = new JTextArea();
		txtWIPsource.setFont(new Font("Courier New", Font.PLAIN, 16));
		scrollPaneWIPsource.setViewportView(txtWIPsource);
		splitPane.setDividerLocation(300);

		JPanel panelBinanryFile = new JPanel();
		tabPaneDisplays.addTab("Binary File", null, panelBinanryFile, null);
		GridBagLayout gbl_panelBinanryFile = new GridBagLayout();
		gbl_panelBinanryFile.columnWidths = new int[] { 680, 0 };
		gbl_panelBinanryFile.rowHeights = new int[] { 400, 0 };
		gbl_panelBinanryFile.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelBinanryFile.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelBinanryFile.setLayout(gbl_panelBinanryFile);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(680, 400));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelBinanryFile.add(scrollPane, gbc_scrollPane);

		txtBinaryFile = new JTextArea();
		txtBinaryFile.setEditable(false);
		txtBinaryFile.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPane.setViewportView(txtBinaryFile);

		JLabel label = new JLabel("      00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F");
		label.setForeground(Color.BLUE);
		label.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPane.setColumnHeaderView(label);

		JScrollPane panelScrollASM = new JScrollPane();
		tabPaneDisplays.addTab("Source Code", null, panelScrollASM, null);

		txtASM = new JTextArea();
		txtASM.setFont(new Font("Courier New", Font.PLAIN, 15));
		panelScrollASM.setViewportView(txtASM);

		lblSourceHeader = new JLabel("Source code");
		lblSourceHeader.setForeground(Color.BLUE);
		lblSourceHeader.setFont(new Font("Courier New", Font.ITALIC, 15));
		lblSourceHeader.setHorizontalAlignment(SwingConstants.CENTER);
		panelScrollASM.setColumnHeaderView(lblSourceHeader);

		// paneTop.add(panelMain, gbc_panelMain);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		mnuFileOpenBinaryFile = new JMenuItem("Open Binary File...");
		mnuFileOpenBinaryFile.setActionCommand(AC_MNU_FILE_OPEN);
		mnuFileOpenBinaryFile.addActionListener(this);
		mnuFile.add(mnuFileOpenBinaryFile);

		mnuFileLoadWIP = new JMenuItem("Load WIP...");
		mnuFileLoadWIP.setActionCommand(AC_MNU_FILE_LOAD_WIP);
		mnuFileLoadWIP.addActionListener(this);
		mnuFile.add(mnuFileLoadWIP);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setActionCommand(AC_MNU_FILE_EXIT);
		mnuFileExit.addActionListener(this);

		mnuFileSaveWIP = new JMenuItem("Save WIP");
		mnuFileSaveWIP.setActionCommand(AC_MNU_FILE_SAVE_WIP);
		mnuFileSaveWIP.addActionListener(this);
		mnuFile.add(mnuFileSaveWIP);

		mnuFileSaveWIPas = new JMenuItem("Save WIP as...");
		mnuFileSaveWIPas.setActionCommand(AC_MNU_FILE_SAVE_WIP_AS);
		mnuFileSaveWIPas.addActionListener(this);
		mnuFile.add(mnuFileSaveWIPas);

		separator_2 = new JSeparator();
		mnuFile.add(separator_2);

		mnuFileReset = new JMenuItem("Reset");
		mnuFileReset.setActionCommand(AC_MNU_FILE_RESET);
		mnuFileReset.addActionListener(this);
		mnuFile.add(mnuFileReset);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);
		mnuFile.add(mnuFileExit);
	}// initalize

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

	// ....................................................................................................

	// ............................................................................................

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

}// class ManualDissambler
