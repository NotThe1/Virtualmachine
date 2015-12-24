package codeSupport;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.AbstractListModel;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.JTextArea;
import javax.swing.ButtonGroup;

import codeSupport.ManualDisassembler0.CodeFragment;
import codeSupport.ManualDisassembler0.OperationStructure;

import javax.swing.JSplitPane;

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
	}// removeFragment

	private void processFragment() {
		int index = listCodeFragments.getSelectedIndex();
		CodeFragment cf = codeFragmentModel.getElementAt(index);
		if (!cf.type.equals(CodeFragment.UNKNOWN)) {
			return;
		}// if
		System.out.printf("<processFragment> - DoIt!%n");
		entryPoints.push(cf.startLoc);
		buildFragments();
	}//

	private void actionStart() {
		btnProcessFragment.setEnabled(true);
		btnAddFragment.setEnabled(true);
		btnRemoveFragment.setEnabled(true);

		int lastLocation = binaryData.capacity();

		beenThere = new HashMap<Integer, Integer>();
		beenThere.put(5, 1);
		beenThere.put(0, 1);

		entryPoints = new Stack<Integer>();
		entryPoints.push(OFFSET);
		int counter = 0;
		while (!entryPoints.isEmpty()) {
			if (!beenThere.containsKey(entryPoints.peek())) {
				buildFragments();
			} else {
				entryPoints.pop(); // toss the entry point , been there!
			}
		}// while
		factorFragments();
		tabPaneDisplays.setSelectedIndex(TAB_WIP);
	}// actionStart

	private void try1() {
		// try {
		// asmDoc.remove(0, asmDoc.getLength());
		// } catch (BadLocationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}// try1

	private void factorFragments() {
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

		int a = 0;
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
		while (keepGoing) {
			int a = currentLocation;

			if (beenThere.put(currentLocation, 0) != null) {
				// System.out.printf("already visited %04X%n", startLocation);
				codeFragmentModel.addItem(new CodeFragment(startLocation, (currentLocation - 1), CodeFragment.CODE));

				return;
			}//

			currentOpCode = opcodeMap.get(binaryData.get(currentLocation));
			// System.out.printf("Location = %04X, opcode = %s%n", currentLocation, currentOpCode.getInstruction());
			pcAction = currentOpCode.getPcAction();

			// opCodeSize = currentOpCode.getSize();
			// currentValue0 = binaryData.get(currentLocation);
			// currentValue1 = binaryData.get(currentLocation + 1);
			// currentValue2 = binaryData.get(currentLocation + 2);
			switch (pcAction) {
			case OperationStructure.NORMAL: // regular opcodes and conditional RETURNS
				currentLocation += currentOpCode.getSize();
				keepGoing = true;
				break;
			case OperationStructure.CONTINUATION: // All CALLs and all Conditional RETURNS
				entryPoints.push(makeTargetAddress(currentLocation));
				currentLocation += currentOpCode.getSize();
				keepGoing = true;
				break;
			case OperationStructure.TERMINATES: // RET
				currentLocation += currentOpCode.getSize();
				keepGoing = false;
				break;
			case OperationStructure.TOTAL: // JUMP PCHL
				if (currentOpCode.getInstruction().equals("JMP")) { // only for JMP
					entryPoints.push(makeTargetAddress(currentLocation));
				}
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
	}

	private void openBinaryFile() {
		String fileLocation = "C:\\Users\\admin\\Dropbox\\Resources\\NativeFiles";
		Path sourcePath = Paths.get(fileLocation);
		JFileChooser chooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}// if
		binaryFile = chooser.getSelectedFile();
		binaryFileName = binaryFile.getName();
		binaryFilePath = chooser.getSelectedFile().getAbsolutePath().toString();
		// lblBinaryFileName.setText(binaryFile.getName());
		// lblBinaryFileName.setToolTipText(binaryFilePath);

		FileChannel fcIn = null;
		FileInputStream fout = null;
		try {
			fout = new FileInputStream(binaryFile);
			fcIn = fout.getChannel();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long fileSize = binaryFile.length();
		int roundedFileSize = ((((int) fileSize + OFFSET) / CHARACTERS_PER_LINE)) * CHARACTERS_PER_LINE;
		binaryData = ByteBuffer.allocate(roundedFileSize);
		binaryData.position(OFFSET);
		// byte[] sectorData = new byte[sectorSize];
		try {
			fcIn.read(binaryData);
			fcIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// try
			// binaryData.flip();
		displayBinaryFile(binaryData, (int) roundedFileSize);
		codeFragmentModel.removeItem(0);
		codeFragmentModel.addItem(new CodeFragment(OFFSET, (int) fileSize + OFFSET, CodeFragment.UNKNOWN));
		haveBinanryFile(true);
		frame.setTitle(APP_NAME + " - " + binaryFileName);
		tabPaneDisplays.setSelectedIndex(TAB_BINARY_FILE);
	}// openBinaryFile

	private void displayBinaryFile(ByteBuffer binaryData, int roundedFileSize) {
		clearDocument(docBinary);
		docBinary = txtBinaryFile.getDocument();
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

		if (state) {

		} else {
			btnAddFragment.setEnabled(state);
			btnRemoveFragment.setEnabled(state);
			btnProcessFragment.setEnabled(state);
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
			showFragmentCode(txtArea, codeFragment);
			break;
		case CodeFragment.CONSTANT:
			break;
		case CodeFragment.LITERAL:
			break;
		case CodeFragment.RESERVED:
			break;
		case CodeFragment.UNKNOWN:
			break;
		default:
		}// switch
	}// displayFragmentSource

	private void showFragmentCode(JTextArea txtArea, CodeFragment codeFragment) {
		int startLocation = codeFragment.startLoc;
		int endLocation = codeFragment.endLoc;
		int codeSize = codeFragment.size();
		Document doc = txtArea.getDocument();
		clearDocument(doc);
		OpcodeStructure8080 currentOpCode = null;

		int currentLocation = startLocation;
		int opCodeSize;
		byte currentValue0, currentValue1, currentValue2;
		String part1, part2, part3;
		while (currentLocation < endLocation) {
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
					part3 = String.format("%s%n", currentOpCode.getAssemblerCode());
					doc.insertString(doc.getLength(), part3, null);
					break;
				case 2:
					part2 = String.format("%02X%02X%6s", currentValue0,currentValue1, "");
					doc.insertString(doc.getLength(), part2, null);
					part3 = String.format("%s%n",currentOpCode.getAssemblerCode(currentValue1));
					doc.insertString(doc.getLength(), part3, null);
					break;
				case 3:
					part2 = String.format("%02X%02X%02X%4s", currentValue0,currentValue1,currentValue2, "");
					doc.insertString(doc.getLength(), part2, null);
					part3 = String.format("%s%n",currentOpCode.getAssemblerCode(currentValue1,currentValue2));
					doc.insertString(doc.getLength(), part3, null);
					break;
				default:
				}// switch
			} catch (BadLocationException badLocationException) {
				badLocationException.printStackTrace();
			}
			currentLocation += opCodeSize;

		}// while opcodeMap

	}// showFragmentCode

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String actionCommand = actionEvent.getActionCommand();
		String message = null;
		switch (actionCommand) {
		case AC_MNU_FILE_OPEN:
			openBinaryFile();
			break;
		case AC_MNU_FILE_SAVE:
			message = "mnuFileSave";
			break;
		case AC_MNU_FILE_SAVEAS:
			message = "mnuFileSaveAs";
			break;
		case AC_MNU_FILE_CLOSE:
			message = "mnuFileClose";
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

		case AC_BTN_ADD_FRAGMENT:
			addFragement();
			break;
		case AC_BTN_REMOVE_FRAGMENT:
			removeFragment();
			break;
		case AC_BTN_PROCESS_FRAGMENT:
			processFragment();
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
		opcodeMap = Opcodes8080.makeCodeMap();
		docWIPbinary = txtWIPsource.getDocument();
		codeFragmentModel = new CodeFragmentModel();
		listCodeFragments.setModel(codeFragmentModel);

		haveBinanryFile(false);
		frame.setTitle(APP_NAME + binaryFileName);
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
	private Document docWIPbinary;
	private Document docWIPsource;

	private Stack<Integer> entryPoints;
	// private Document asmDoc;
	private HashMap<Integer, Integer> beenThere;

	private final int OFFSET = 0X0100; // Transient Program Area starts at 256 (0x0100)
	private final int CHARACTERS_PER_LINE = 0x0010; //

	private final int TAB_WIP = 0;
	private final int TAB_BINARY_FILE = 1;

	private final static String AC_MNU_FILE_NEW = "mnuFileNew";
	private final static String AC_MNU_FILE_OPEN = "mnuFileOpen";
	private final static String AC_MNU_FILE_SAVE = "mnuFileSave";
	private final static String AC_MNU_FILE_SAVEAS = "mnuFileSaveAs";
	private final static String AC_MNU_FILE_CLOSE = "mnuFileClose";
	private final static String AC_MNU_FILE_EXIT = "mnuFileExit";

	private final static String AC_MNU_CODE_NEW = "mnuCodeFragmentNew";
	private final static String AC_MNU_CODE_LOAD = "mnuCodeFragmentLoad";
	private final static String AC_MNU_CODE_SAVE = "mnuCodeFragmentSave";
	private final static String AC_MNU_CODE_SAVE_AS = "mnuCodeFragmentSaveAs";

	private final static String AC_BTN_START = "btnStart";
	private final static String AC_BTN_ADD_FRAGMENT = "btnAddFragment";
	private final static String AC_BTN_REMOVE_FRAGMENT = "btnRemoveFragment";
	private final static String AC_BTN_PROCESS_FRAGMENT = "btnProcessFragment";

	private final static String APP_NAME = "Manual Disassembler ";
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
		gbl_paneTop.columnWidths = new int[] { 0, 0 };
		gbl_paneTop.rowHeights = new int[] { 0, 0, 0 };
		gbl_paneTop.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_paneTop.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		paneTop.setLayout(gbl_paneTop);

		btnStart = new JButton("Start");
		btnStart.setActionCommand(AC_BTN_START);
		btnStart.addActionListener(this);
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.anchor = GridBagConstraints.WEST;
		gbc_btnStart.insets = new Insets(0, 0, 5, 0);
		gbc_btnStart.gridx = 0;
		gbc_btnStart.gridy = 0;
		paneTop.add(btnStart, gbc_btnStart);

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
		gbc_btnProcessFragment.gridwidth = 2;
		gbc_btnProcessFragment.insets = new Insets(0, 0, 0, 5);
		gbc_btnProcessFragment.gridx = 0;
		gbc_btnProcessFragment.gridy = 1;
		panelFragmentButtons.add(btnProcessFragment, gbc_btnProcessFragment);

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

		// paneTop.add(panelMain, gbc_panelMain);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		JMenuItem mnuFileOpenBinaryFile = new JMenuItem("Open Binary File...");
		mnuFileOpenBinaryFile.setActionCommand(AC_MNU_FILE_OPEN);
		mnuFileOpenBinaryFile.addActionListener(this);
		mnuFile.add(mnuFileOpenBinaryFile);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setActionCommand(AC_MNU_FILE_EXIT);
		mnuFileExit.addActionListener(this);
		mnuFile.add(mnuFileExit);
	}// initalize

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

	public class CodeFragment implements Comparable<CodeFragment> {
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

	// ............................................................................................

	class CodeFragmentModel extends AbstractListModel<CodeFragment> implements ListModel<CodeFragment> {
		ArrayList<CodeFragment> codeFragements;

		public CodeFragmentModel() {
			codeFragements = new ArrayList<CodeFragment>();
			codeFragements.add(new CodeFragment(0X0000, 0X000, CodeFragment.RESERVED));
			codeFragements.add(new CodeFragment(0XFFFF, 0XFFFF, CodeFragment.RESERVED));
		}// Constructor

		@Override
		public CodeFragment getElementAt(int index) {
			return codeFragements.get(index);
		}// getElementAt

		public boolean addItem(CodeFragment codeFragment) {
			boolean result = true;
			int startLocNew = codeFragment.startLoc;
			int endLocNew = codeFragment.endLoc;
			String typeNew = codeFragment.type;

			int containerIndex = this.withinFragement(startLocNew);

			if (containerIndex != -1) {// there is a container
				CodeFragment cfContainer = this.getElementAt(containerIndex);
				CodeFragment cfToAdd;
				int containerStartLoc = cfContainer.startLoc;
				int containerEndLoc = cfContainer.endLoc;
				String containerType = cfContainer.type;
				if (containerIndex < this.getSize()) {
					this.removeItem(containerIndex);
				}// if last element

				if ((startLocNew != containerStartLoc) & (endLocNew != containerEndLoc)) {
					cfToAdd = new CodeFragment(containerStartLoc, startLocNew - 1, containerType);
					codeFragements.add(insertAt(containerStartLoc), cfToAdd);
					cfToAdd = new CodeFragment(startLocNew, endLocNew, typeNew);
					codeFragements.add(insertAt(startLocNew), cfToAdd);
					cfToAdd = new CodeFragment(endLocNew + 1, containerEndLoc, containerType);
					codeFragements.add(insertAt(endLocNew + 1), cfToAdd);
				} else if (startLocNew == containerStartLoc) {
					cfToAdd = new CodeFragment(startLocNew, endLocNew, typeNew);
					codeFragements.add(insertAt(startLocNew), cfToAdd);
					if (endLocNew != containerEndLoc) {
						cfToAdd = new CodeFragment(endLocNew + 1, containerEndLoc, containerType);
						codeFragements.add(insertAt(endLocNew + 1), cfToAdd);
					}// inner if
				} else if (endLocNew == containerEndLoc) {
					cfToAdd = new CodeFragment(containerStartLoc, startLocNew - 1, containerType);
					codeFragements.add(insertAt(containerStartLoc), cfToAdd);
					cfToAdd = new CodeFragment(startLocNew, containerEndLoc, typeNew);
					codeFragements.add(insertAt(startLocNew), cfToAdd);
				} else {
					result = false;
				}// if else

			} else { // NO container
				codeFragements.add(insertAt(startLocNew), codeFragment);
			}// if - container ?
			listCodeFragments.updateUI();
			return result;
		}// addItem

		public void removeItem(int index) {
			codeFragements.remove(index);
			listCodeFragments.updateUI();
			return;
		}// removeItem

		private int insertAt(int location) {
			int loc = codeFragements.size();
			for (int i = 0; i < codeFragements.size(); i++) {
				if (location < codeFragements.get(i).startLoc) {
					loc = i;
					break;
				}// if
			}// for
			return loc;
		}// insertAt

		private void clear() {
			codeFragements.clear();
		}// clear

		/**
		 * 
		 * @param location
		 * @return fragment that contains location , or -1 for no container
		 */
		private int withinFragement(int location) {
			int lowerIndex = -1;

			for (int i = 0; i < codeFragements.size(); i++) {
				if (location <= codeFragements.get(i).startLoc) {
					lowerIndex = (i == 0) ? 0 : i - 1;
					break;
				}// if
			}// for - find lower boundary

			if (lowerIndex == -1) {
				return -1; // not within any fragment
			}//
			int loLoc;
			int hiLoc = codeFragements.get(lowerIndex).startLoc;

			for (int i = lowerIndex + 1; i < codeFragements.size(); i++) {// lowerIndex + 1
				loLoc = hiLoc;
				hiLoc = codeFragements.get(i).startLoc;
				if ((location >= loLoc) & (location <= hiLoc)) {
					if (location <= codeFragements.get(i - 1).endLoc) {
						return i - 1;
					}// if
				} else if (location > hiLoc) {
					break; // too far
				}// if between lo & hi
			}// for
			return -1;
		}// withinFragement

		@Override
		public int getSize() {
			return codeFragements.size();
		} // getSize

	}// class CodeFragmentModel
		// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

}// class ManualDissambler
