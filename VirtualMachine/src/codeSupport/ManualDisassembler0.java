package codeSupport;

import java.awt.EventQueue;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

//import disks.DiskMetrics;
//import disks.DiskUtility.DirEntry;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JPanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.border.BevelBorder;
import javax.swing.JLabel;

import java.awt.FlowLayout;

import javax.swing.JScrollPane;

import java.awt.Dimension;

import javax.swing.JTextArea;

import java.awt.Font;
import java.awt.Color;

import javax.swing.border.LineBorder;
import javax.swing.JRadioButton;

import myComponents.Hex64KSpinner;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import codeSupport.Disassembler.OperationStructure;

import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.ListSelectionModel;

public class ManualDisassembler0 implements ActionListener, ListSelectionListener {

	private JFrame frmManualDisassembler;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ManualDisassembler0 window = new ManualDisassembler0();
					window.frmManualDisassembler.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}// try
			}// run
		});
	}

	private void addFragement() {
		int startLocNew = (int) spinnerStartFragment.getValue();
		int endLocNew = (int) spinnerEndFragment.getValue();
		String type = type = bgFragments.getSelection().getActionCommand();
		codeFragmentModel.addItem(new CodeFragment(startLocNew, endLocNew, type));
	}// addFragement

	private void removeFragment() {
		int index = listCodeFragments.getSelectedIndex();
		if (index == -1) {
			return;
		}
		// System.out.printf("<removeFragment> getSelectedIndex() =  %s%n", listCodeFragments.getSelectedIndex());
		codeFragmentModel.removeItem(listCodeFragments.getSelectedIndex());
		if (index > codeFragmentModel.getSize() - 1) {
			listCodeFragments.setSelectedIndex(codeFragmentModel.getSize() - 1);
		}
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

		try1();
	}

	private void try1() {
		try {
			asmDoc.remove(0, asmDoc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int lastLocation = binaryData.capacity();

		beenThere = new HashMap<Integer, Boolean>();
		beenThere.put(5, true);
		beenThere.put(0, true);

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
	}// try1

	private void factorFragments() {
		ArrayList<CodeFragment> tempFragments = new ArrayList<CodeFragment>();
		;

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

		startLocation = entryPoints.pop();
		currentLocation = startLocation;
		boolean keepGoing = true;
		while (keepGoing) {
			int a = currentLocation;

			if (beenThere.put(currentLocation, true) != null) {
				System.out.printf("already visited %04X%n", startLocation);
				codeFragmentModel.addItem(new CodeFragment(startLocation, (currentLocation - 1), CodeFragment.CODE));

				return;
			}//

			currentOpCode = opcodeMap.get(binaryData.get(currentLocation));
			System.out.printf("Location = %04X, opcode = %s%n", currentLocation, currentOpCode.instruction);
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
				if (currentOpCode.instruction.equals("JMP")) { // only for JMP
					entryPoints.push(makeTargetAddress(currentLocation));
				}
				currentLocation += currentOpCode.getSize();
				keepGoing = false;
				break;
			}// switch
		}// - keep going
		codeFragmentModel.addItem(new CodeFragment(startLocation, (currentLocation - 1), CodeFragment.CODE));
		// codeDestiation = currentOpCode.getDestination();

		return;
	}// followCode

	private int makeTargetAddress(int currentLocation) {
		int hi = (binaryData.get(currentLocation + 2) & 0xFF) * 256;
		int lo = binaryData.get(currentLocation + 1);
		int v = hi + lo;
		return ((binaryData.get(currentLocation + 2) & 0xFF) * 256) + (binaryData.get(currentLocation + 1) & 0xFF);
	}

	// ------------------------------------------------------------------------------------------
	private void mnuOpenFile() {
		String fileLocation = "C:\\Users\\admin\\Dropbox\\Resources\\NativeFiles";
		Path sourcePath = Paths.get(fileLocation);
		JFileChooser chooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}// if
		binaryFile = chooser.getSelectedFile();
		binaryFilePath = chooser.getSelectedFile().getAbsolutePath().toString();
		lblBinaryFileName.setText(binaryFile.getName());
		lblBinaryFileName.setToolTipText(binaryFilePath);

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
		binaryData = ByteBuffer.allocate((int) fileSize + OFFSET);
		binaryData.position(OFFSET);
		codeFragmentModel.addItem(new CodeFragment(OFFSET, (int) (fileSize + OFFSET), CodeFragment.UNKNOWN));
		codeFragmentModel.addItem(new CodeFragment(0xFFFF, 0xFFFF, CodeFragment.UNKNOWN));
		// byte[] sectorData = new byte[sectorSize];
		try {
			fcIn.read(binaryData);
			fcIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// try
		haveFile(true);
	}

	private void mnuSave(String cfFileName) {
		// try {
		// ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName + FILE_SUFFIX_PERIOD));
		// oos.writeObject(ccr);
		// oos.writeObject(wrs);
		// oos.writeObject(core);
		// oos.close();
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }// try - write objects

	}

	private void haveFile(boolean state) {
		btnStart.setEnabled(state);
		tabPaneFragments.setEnabled(state);
		spinnerStartFragment.setEnabled(state);
		spinnerEndFragment.setEnabled(state);
		rbCode.setEnabled(state);
		rbConstant.setEnabled(state);
		rbLiteral.setEnabled(state);
		rbReserved.setEnabled(state);
		rbUnknown.setEnabled(state);
		if (state) {

		} else {
			btnProcessFragment.setEnabled(state);
			btnAddFragment.setEnabled(state);
			btnRemoveFragment.setEnabled(state);
		}
	}//

	// ------------------------------------------------------------------------------------------

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String actionCommand = actionEvent.getActionCommand();
		String message = null;
		switch (actionCommand) {
		case AC_MNU_FILE_OPEN:
			mnuOpenFile();
			break;
		case AC_MNU_FILE_SAVE:
			message = AC_MNU_FILE_SAVE;
			break;
		case AC_MNU_FILE_SAVEAS:
			message = AC_MNU_FILE_SAVEAS;
			break;
		case AC_MNU_FILE_CLOSE:
			message = AC_MNU_FILE_CLOSE;
			break;
		case AC_MNU_FILE_EXIT:
			appClose();
			break;

		case AC_MNU_CODE_NEW:
			message = AC_MNU_CODE_NEW;
			break;
		case AC_MNU_CODE_LOAD:
			message = AC_MNU_CODE_LOAD;
			break;
		case AC_MNU_CODE_SAVE:
			message = AC_MNU_CODE_SAVE;
			break;
		case AC_MNU_CODE_SAVE_AS:
			message = AC_MNU_CODE_SAVE_AS;
			break;

		case AC_BTN_START:
			message = AC_BTN_START;
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
			break;

		case AC_BTN_ADD_SYMBOL:
			message = AC_BTN_ADD_SYMBOL;
			break;

		case AC_BTN_REMOVE_SYMBOL:
			message = AC_BTN_REMOVE_SYMBOL;
			break;
		case AC_BTN_UPDATE_SYMBOL:
			message = AC_BTN_UPDATE_SYMBOL;
			break;

		default:
			message = "default";
		}// switch

		if (message != null) {
			System.out.printf("actionCommand = %s%n", actionCommand);
		}

	}// actionPerformed

	@Override
	public void valueChanged(ListSelectionEvent lse) {
		if (lse.getValueIsAdjusting()) {
			return;
		}
		int index = ((JList) lse.getSource()).getSelectedIndex();
		CodeFragment cf = codeFragmentModel.getElementAt(index);
		spinnerStartFragment.setValue(cf.startLoc);
		spinnerEndFragment.setValue(cf.endLoc);
		setFragmentRadioButton(cf.type);
		// System.out.printf("<valueChanged> - SelectedIndex =  %d %n", index);
		// System.out.printf("<valueChanged> - firstIndex =  %d ; lastIndex =  %d%n", lse.getFirstIndex(),
		// lse.getLastIndex());
		// System.out.printf("<valueChanged> - ValueIsAdjusting =  %s%n", lse.getValueIsAdjusting());
	}

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
		}
	}

	private void appClose() {
		System.exit(0);
	}// appClose

	private void loadSomeData() {
		symbolDisModel.add(new SymbolDis("0000", SymbolDis.LABEL, 0, false));
		symbolDisModel.add(new SymbolDis("5678", SymbolDis.VALUE, 5, true));
		symbolDisModel.add(new SymbolDis("1234", SymbolDis.LABEL, 10, true));
	}

	@SuppressWarnings("unchecked")
	private void appInit() {

		makeOpcodeMap();
		codeFragmentModel = new CodeFragmentModel();

		symbolDisModel = new SymbolDisModel();

		loadSomeData();

		// Collections.sort( (List<T>) codeFragmentModel);
		listCodeFragments.setModel(codeFragmentModel);
		Collections.sort(symbolDisModel);
		listSymbols.setModel(symbolDisModel);

		asmDoc = txtASM.getDocument();
		try {
			asmDoc.remove(0, asmDoc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		haveFile(false);
	}// appInit

	/**
	 * Create the application.
	 */
	public ManualDisassembler0() {
		initialize();
		appInit();
	}

	private final static int OFFSET = 0X0100; // adjusted to TPA
	private final static int LINE_WIDTH = 54; // calculated by hand for now

	private ByteBuffer binaryData;
	private int currentLocation;
	private int codeDestiation;
	private OperationStructure currentOpCode;
	int opCodeSize;
	int pcAction;
	byte currentValue0;
	byte currentValue1;
	byte currentValue2;
	private String linePart1, linePart2, linePart3;
	private Stack<Integer> entryPoints;
	private Document asmDoc;
	private HashMap<Integer, Boolean> beenThere;

	private File binaryFile;
	private String binaryFilePath;
	private CodeFragmentModel codeFragmentModel;
	private SymbolDisModel symbolDisModel;
	private HashMap<Byte, OperationStructure> opcodeMap;

	private final static String AC_MNU_FILE_OPEN = "mnuFileOpen";
	private final static String AC_MNU_FILE_SAVE = "mnuFileSave";
	private final static String AC_MNU_FILE_SAVEAS = "mnuFileSaveAs";
	private final static String AC_MNU_FILE_CLOSE = "mnuFileClose";
	private final static String AC_MNU_FILE_EXIT = "mnuFileExit";

	private final static String AC_MNU_CODE_NEW = "mnuCodeFragmentNew";
	private final static String AC_MNU_CODE_LOAD = "mnuCodeFragmentLoad";
	private final static String AC_MNU_CODE_SAVE = "mnuCodeFragmentSave";
	private final static String AC_MNU_CODE_SAVE_AS = "mnuCodeFragmentSaveAs";

	private final static String AC_BTN_ADD_FRAGMENT = "btnAddFragment";
	private final static String AC_BTN_REMOVE_FRAGMENT = "btnRemoveFragment";
	private final static String AC_BTN_PROCESS_FRAGMENT = "btnProcessFragment";

	private final static String AC_BTN_ADD_SYMBOL = "btnAddSymbol";
	private final static String AC_BTN_REMOVE_SYMBOL = "btnRemoveSymbol";
	private final static String AC_BTN_UPDATE_SYMBOL = "btnUpdateSymbol";

	private final static String AC_BTN_START = "btnStart";

	// ++++++++++++++++++++++++++++++++++
	private ButtonGroup bgFragments;
	private JLabel lblBinaryFileName;
	private JList<CodeFragment> listCodeFragments;
	private JList listSymbols;
	private JRadioButton rbCode;
	private JRadioButton rbConstant;
	private JRadioButton rbLiteral;
	private JRadioButton rbReserved;
	private JTextField txtName;
	private JTextField txtID;
	private Hex64KSpinner spinnerDefined;
	private JRadioButton rbValue;
	private JRadioButton rbTypeLocation;
	private JComboBox comboReferences;
	private JTextArea txtASM;
	private JRadioButton rbUnknown;
	private Hex64KSpinner spinnerStartFragment;
	private Hex64KSpinner spinnerEndFragment;
	private JButton btnStart;
	private JTabbedPane tabPaneFragments;
	private JButton btnAddFragment;
	private JButton btnRemoveFragment;
	private JButton btnProcessFragment;

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmManualDisassembler = new JFrame();
		frmManualDisassembler.setTitle("Manual Disassembler");
		frmManualDisassembler.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose(); // use DEFAULT_STATE_FILE
			}
		});

		frmManualDisassembler.setBounds(100, 100, 1015, 824);
		frmManualDisassembler.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmManualDisassembler.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		JMenuItem mnuFileOpen = new JMenuItem("Open Binary File...");
		mnuFileOpen.setActionCommand(AC_MNU_FILE_OPEN);
		mnuFileOpen.addActionListener(this);
		mnuFile.add(mnuFileOpen);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		JMenuItem mnuFileSave = new JMenuItem("Save ASM File...");
		mnuFileSave.setActionCommand(AC_MNU_FILE_SAVE);
		mnuFileSave.addActionListener(this);
		mnuFile.add(mnuFileSave);

		JMenuItem mnuFileSaveAs = new JMenuItem("Save As ASM File...");
		mnuFileSaveAs.setActionCommand(AC_MNU_FILE_SAVEAS);
		mnuFileSaveAs.addActionListener(this);
		mnuFile.add(mnuFileSaveAs);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);

		JMenuItem mnuFileClose = new JMenuItem("Close File");
		mnuFileClose.setActionCommand(AC_MNU_FILE_CLOSE);
		mnuFileClose.addActionListener(this);
		mnuFile.add(mnuFileClose);

		JSeparator separator_2 = new JSeparator();
		mnuFile.add(separator_2);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setActionCommand(AC_MNU_FILE_EXIT);
		mnuFileExit.addActionListener(this);
		mnuFile.add(mnuFileExit);

		JMenu mnuCodeFragment = new JMenu("Code Fragment");
		menuBar.add(mnuCodeFragment);

		JMenuItem mnuCodeFragmentNew = new JMenuItem("New Code Fragment...");
		mnuCodeFragmentNew.setActionCommand(AC_MNU_CODE_NEW);
		mnuCodeFragmentNew.addActionListener(this);
		mnuCodeFragment.add(mnuCodeFragmentNew);

		JMenuItem mnuCodeFragementLoad = new JMenuItem("Load Code Fragement...");
		mnuCodeFragementLoad.setActionCommand(AC_MNU_CODE_LOAD);
		mnuCodeFragementLoad.addActionListener(this);
		mnuCodeFragment.add(mnuCodeFragementLoad);

		JSeparator separator_3 = new JSeparator();
		mnuCodeFragment.add(separator_3);

		JMenuItem mnuCodeFragementSave = new JMenuItem("Save Code Fragements...");
		mnuCodeFragementSave.setActionCommand(AC_MNU_CODE_SAVE);
		mnuCodeFragementSave.addActionListener(this);
		mnuCodeFragment.add(mnuCodeFragementSave);

		JMenuItem mnuCodeFragementsSaveAs = new JMenuItem("Save Code Fragments As...");
		mnuCodeFragementsSaveAs.setActionCommand(AC_MNU_CODE_SAVE_AS);
		mnuCodeFragementsSaveAs.addActionListener(this);
		mnuCodeFragment.add(mnuCodeFragementsSaveAs);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frmManualDisassembler.getContentPane().setLayout(gridBagLayout);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		frmManualDisassembler.getContentPane().add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		btnStart = new JButton("Start");
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.gridx = 0;
		gbc_btnStart.gridy = 1;
		panel_1.add(btnStart, gbc_btnStart);
		btnStart.setActionCommand(AC_BTN_START);
		btnStart.addActionListener(this);

		JPanel panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.insets = new Insets(0, 0, 5, 0);
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 1;
		frmManualDisassembler.getContentPane().add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 278, 0, 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		tabPaneFragments = new JTabbedPane(JTabbedPane.TOP);
		tabPaneFragments.setPreferredSize(new Dimension(0, 0));
		GridBagConstraints gbc_tabPaneFragments = new GridBagConstraints();
		gbc_tabPaneFragments.fill = GridBagConstraints.BOTH;
		gbc_tabPaneFragments.insets = new Insets(0, 0, 0, 5);
		gbc_tabPaneFragments.gridx = 0;
		gbc_tabPaneFragments.gridy = 0;
		panelMain.add(tabPaneFragments, gbc_tabPaneFragments);

		JPanel panelFragments = new JPanel();
		tabPaneFragments.addTab("Fragments", null, panelFragments, null);
		panelFragments.setBorder(null);
		GridBagLayout gbl_panelFragments = new GridBagLayout();
		gbl_panelFragments.columnWidths = new int[] { 0, 200, 0 };
		gbl_panelFragments.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelFragments.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelFragments.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelFragments.setLayout(gbl_panelFragments);

		JPanel panelTypeManipulation = new JPanel();
		GridBagConstraints gbc_panelTypeManipulation = new GridBagConstraints();
		gbc_panelTypeManipulation.insets = new Insets(0, 0, 5, 5);
		gbc_panelTypeManipulation.fill = GridBagConstraints.BOTH;
		gbc_panelTypeManipulation.gridx = 0;
		gbc_panelTypeManipulation.gridy = 0;
		panelFragments.add(panelTypeManipulation, gbc_panelTypeManipulation);

		JPanel panelFragmentBoundary = new JPanel();
		GridBagConstraints gbc_panelFragmentBoundary = new GridBagConstraints();
		gbc_panelFragmentBoundary.gridwidth = 2;
		gbc_panelFragmentBoundary.insets = new Insets(0, 0, 5, 0);
		gbc_panelFragmentBoundary.fill = GridBagConstraints.BOTH;
		gbc_panelFragmentBoundary.gridx = 0;
		gbc_panelFragmentBoundary.gridy = 1;
		panelFragments.add(panelFragmentBoundary, gbc_panelFragmentBoundary);
		GridBagLayout gbl_panelFragmentBoundary = new GridBagLayout();
		gbl_panelFragmentBoundary.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelFragmentBoundary.rowHeights = new int[] { 0, 0 };
		gbl_panelFragmentBoundary.columnWeights = new double[] { 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelFragmentBoundary.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFragmentBoundary.setLayout(gbl_panelFragmentBoundary);

		JLabel lblStart = new JLabel("    Start   ");
		lblStart.setMinimumSize(new Dimension(30, 18));
		lblStart.setPreferredSize(new Dimension(30, 18));
		GridBagConstraints gbc_lblStart = new GridBagConstraints();
		gbc_lblStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblStart.insets = new Insets(0, 0, 0, 5);
		gbc_lblStart.gridx = 0;
		gbc_lblStart.gridy = 0;
		panelFragmentBoundary.add(lblStart, gbc_lblStart);

		spinnerStartFragment = new Hex64KSpinner();
		spinnerStartFragment.setMinimumSize(new Dimension(50, 18));
		spinnerStartFragment.setPreferredSize(new Dimension(50, 18));
		GridBagConstraints gbc_spinnerStartFragment = new GridBagConstraints();
		gbc_spinnerStartFragment.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerStartFragment.gridx = 1;
		gbc_spinnerStartFragment.gridy = 0;
		panelFragmentBoundary.add(spinnerStartFragment, gbc_spinnerStartFragment);

		JLabel lblEnd = new JLabel("     End  ");
		lblEnd.setMinimumSize(new Dimension(30, 18));
		lblEnd.setPreferredSize(new Dimension(30, 18));
		GridBagConstraints gbc_lblEnd = new GridBagConstraints();
		gbc_lblEnd.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblEnd.insets = new Insets(0, 0, 0, 5);
		gbc_lblEnd.gridx = 2;
		gbc_lblEnd.gridy = 0;
		panelFragmentBoundary.add(lblEnd, gbc_lblEnd);

		spinnerEndFragment = new Hex64KSpinner();
		spinnerEndFragment.setPreferredSize(new Dimension(50, 18));
		spinnerEndFragment.setMinimumSize(new Dimension(50, 18));
		GridBagConstraints gbc_spinnerEndFragment = new GridBagConstraints();
		gbc_spinnerEndFragment.anchor = GridBagConstraints.WEST;
		gbc_spinnerEndFragment.gridx = 3;
		gbc_spinnerEndFragment.gridy = 0;
		panelFragmentBoundary.add(spinnerEndFragment, gbc_spinnerEndFragment);

		JPanel panelRadioButtons1 = new JPanel();
		panelRadioButtons1.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"Fragement Type",
				TitledBorder.LEADING, TitledBorder.BELOW_TOP, null, null));
		GridBagConstraints gbc_panelRadioButtons1 = new GridBagConstraints();
		gbc_panelRadioButtons1.gridwidth = 2;
		gbc_panelRadioButtons1.insets = new Insets(0, 0, 5, 0);
		gbc_panelRadioButtons1.fill = GridBagConstraints.BOTH;
		gbc_panelRadioButtons1.gridx = 0;
		gbc_panelRadioButtons1.gridy = 2;
		panelFragments.add(panelRadioButtons1, gbc_panelRadioButtons1);
		GridBagLayout gbl_panelRadioButtons1 = new GridBagLayout();
		gbl_panelRadioButtons1.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelRadioButtons1.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelRadioButtons1.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_panelRadioButtons1.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelRadioButtons1.setLayout(gbl_panelRadioButtons1);

		rbCode = new JRadioButton("Code");
		rbCode.setActionCommand(CodeFragment.CODE);
		GridBagConstraints gbc_rbCode = new GridBagConstraints();
		gbc_rbCode.anchor = GridBagConstraints.WEST;
		gbc_rbCode.insets = new Insets(0, 0, 5, 5);
		gbc_rbCode.gridx = 0;
		gbc_rbCode.gridy = 0;
		panelRadioButtons1.add(rbCode, gbc_rbCode);

		rbConstant = new JRadioButton("Constant");
		rbConstant.setActionCommand(CodeFragment.CONSTANT);
		GridBagConstraints gbc_rbConstant = new GridBagConstraints();
		gbc_rbConstant.anchor = GridBagConstraints.WEST;
		gbc_rbConstant.insets = new Insets(0, 0, 5, 0);
		gbc_rbConstant.gridx = 1;
		gbc_rbConstant.gridy = 0;
		panelRadioButtons1.add(rbConstant, gbc_rbConstant);

		rbLiteral = new JRadioButton("Literal");
		rbLiteral.setActionCommand(CodeFragment.LITERAL);
		GridBagConstraints gbc_rbLiteral = new GridBagConstraints();
		gbc_rbLiteral.anchor = GridBagConstraints.WEST;
		gbc_rbLiteral.insets = new Insets(0, 0, 5, 5);
		gbc_rbLiteral.gridx = 0;
		gbc_rbLiteral.gridy = 1;
		panelRadioButtons1.add(rbLiteral, gbc_rbLiteral);

		rbReserved = new JRadioButton("Reserved");
		rbReserved.setActionCommand(CodeFragment.RESERVED);
		GridBagConstraints gbc_rbReserved = new GridBagConstraints();
		gbc_rbReserved.anchor = GridBagConstraints.WEST;
		gbc_rbReserved.insets = new Insets(0, 0, 5, 0);
		gbc_rbReserved.gridx = 1;
		gbc_rbReserved.gridy = 1;
		panelRadioButtons1.add(rbReserved, gbc_rbReserved);

		rbUnknown = new JRadioButton("Unknown");
		rbUnknown.setActionCommand(CodeFragment.UNKNOWN);
		GridBagConstraints gbc_rbUnknown = new GridBagConstraints();
		gbc_rbUnknown.anchor = GridBagConstraints.WEST;
		gbc_rbUnknown.insets = new Insets(0, 0, 0, 5);
		gbc_rbUnknown.gridx = 0;
		gbc_rbUnknown.gridy = 2;
		panelRadioButtons1.add(rbUnknown, gbc_rbUnknown);

		bgFragments = new ButtonGroup();
		bgFragments.add(rbCode);
		bgFragments.add(rbLiteral);
		bgFragments.add(rbUnknown);
		bgFragments.add(rbConstant);
		bgFragments.add(rbReserved);
		rbUnknown.setSelected(true);

		JPanel panelFragmentButtons = new JPanel();
		GridBagConstraints gbc_panelFragmentButtons = new GridBagConstraints();
		gbc_panelFragmentButtons.gridwidth = 2;
		gbc_panelFragmentButtons.insets = new Insets(0, 0, 5, 5);
		gbc_panelFragmentButtons.fill = GridBagConstraints.BOTH;
		gbc_panelFragmentButtons.gridx = 0;
		gbc_panelFragmentButtons.gridy = 3;
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

		JScrollPane scrollPaneFragments = new JScrollPane();
		GridBagConstraints gbc_scrollPaneFragments = new GridBagConstraints();
		gbc_scrollPaneFragments.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneFragments.gridwidth = 2;
		gbc_scrollPaneFragments.gridx = 0;
		gbc_scrollPaneFragments.gridy = 5;
		panelFragments.add(scrollPaneFragments, gbc_scrollPaneFragments);

		listCodeFragments = new JList<CodeFragment>();
		scrollPaneFragments.setViewportView(listCodeFragments);
		listCodeFragments.setFont(new Font("Courier New", Font.BOLD, 12));
		listCodeFragments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listCodeFragments.addListSelectionListener(this);
		listCodeFragments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// scrollPaneFragements.setViewportView(listCodeFragments);
		listCodeFragments.setVisibleRowCount(28);
		listCodeFragments.setBorder(new LineBorder(Color.BLUE));

		JLabel lblNewLabel = new JLabel("Start   End    Len     Type");
		lblNewLabel.setForeground(Color.BLUE);
		lblNewLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
		scrollPaneFragments.setColumnHeaderView(lblNewLabel);

		JPanel panelSymbols = new JPanel();
		tabPaneFragments.addTab("Symbols", null, panelSymbols, null);
		GridBagLayout gbl_panelSymbols = new GridBagLayout();
		gbl_panelSymbols.columnWidths = new int[] { 0, 0 };
		gbl_panelSymbols.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelSymbols.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelSymbols.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelSymbols.setLayout(gbl_panelSymbols);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		panelSymbols.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 60, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel label1 = new JLabel("ID");
		GridBagConstraints gbc_label1 = new GridBagConstraints();
		gbc_label1.anchor = GridBagConstraints.EAST;
		gbc_label1.insets = new Insets(0, 0, 5, 5);
		gbc_label1.gridx = 1;
		gbc_label1.gridy = 1;
		panel.add(label1, gbc_label1);

		txtID = new JTextField();
		GridBagConstraints gbc_txtID = new GridBagConstraints();
		gbc_txtID.insets = new Insets(0, 0, 5, 5);
		gbc_txtID.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtID.gridx = 2;
		gbc_txtID.gridy = 1;
		panel.add(txtID, gbc_txtID);
		txtID.setColumns(10);

		JLabel lblDefined = new JLabel("Defined");
		GridBagConstraints gbc_lblDefined = new GridBagConstraints();
		gbc_lblDefined.anchor = GridBagConstraints.EAST;
		gbc_lblDefined.insets = new Insets(0, 0, 5, 5);
		gbc_lblDefined.gridx = 1;
		gbc_lblDefined.gridy = 2;
		panel.add(lblDefined, gbc_lblDefined);

		spinnerDefined = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerDefined = new GridBagConstraints();
		gbc_spinnerDefined.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerDefined.gridx = 2;
		gbc_spinnerDefined.gridy = 2;
		panel.add(spinnerDefined, gbc_spinnerDefined);

		rbValue = new JRadioButton("Value");
		GridBagConstraints gbc_rbValue = new GridBagConstraints();
		gbc_rbValue.insets = new Insets(0, 0, 5, 5);
		gbc_rbValue.gridx = 1;
		gbc_rbValue.gridy = 3;
		panel.add(rbValue, gbc_rbValue);

		rbTypeLocation = new JRadioButton("Location");
		GridBagConstraints gbc_rbTypeLocation = new GridBagConstraints();
		gbc_rbTypeLocation.insets = new Insets(0, 0, 5, 5);
		gbc_rbTypeLocation.gridx = 2;
		gbc_rbTypeLocation.gridy = 3;
		panel.add(rbTypeLocation, gbc_rbTypeLocation);

		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 3;
		gbc_txtName.insets = new Insets(0, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 4;
		panel.add(txtName, gbc_txtName);
		txtName.setColumns(10);

		JButton btnAddSymbol = new JButton("Add");
		btnAddSymbol.setActionCommand(AC_BTN_ADD_SYMBOL);
		btnAddSymbol.addActionListener(this);

		JButton btnUpdateSymbol = new JButton("Update");
		btnUpdateSymbol.setActionCommand(AC_BTN_UPDATE_SYMBOL);
		btnUpdateSymbol.addActionListener(this);
		GridBagConstraints gbc_btnUpdateSymbol = new GridBagConstraints();
		gbc_btnUpdateSymbol.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdateSymbol.gridx = 1;
		gbc_btnUpdateSymbol.gridy = 5;
		panel.add(btnUpdateSymbol, gbc_btnUpdateSymbol);

		comboReferences = new JComboBox();
		comboReferences.setEditable(true);
		GridBagConstraints gbc_comboReferences = new GridBagConstraints();
		gbc_comboReferences.insets = new Insets(0, 0, 5, 5);
		gbc_comboReferences.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboReferences.gridx = 2;
		gbc_comboReferences.gridy = 5;
		panel.add(comboReferences, gbc_comboReferences);
		GridBagConstraints gbc_btnAddSymbol = new GridBagConstraints();
		gbc_btnAddSymbol.anchor = GridBagConstraints.ABOVE_BASELINE;
		gbc_btnAddSymbol.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddSymbol.gridx = 1;
		gbc_btnAddSymbol.gridy = 6;
		panel.add(btnAddSymbol, gbc_btnAddSymbol);

		JButton btnRemoveSymbol = new JButton("Remove");
		btnRemoveSymbol.setActionCommand(AC_BTN_REMOVE_SYMBOL);
		btnRemoveSymbol.addActionListener(this);
		GridBagConstraints gbc_btnRemoveSymbol = new GridBagConstraints();
		gbc_btnRemoveSymbol.insets = new Insets(0, 0, 0, 5);
		gbc_btnRemoveSymbol.gridx = 1;
		gbc_btnRemoveSymbol.gridy = 7;
		panel.add(btnRemoveSymbol, gbc_btnRemoveSymbol);

		listSymbols = new JList();
		listSymbols.setFont(new Font("Courier New", Font.BOLD, 12));
		GridBagConstraints gbc_listSymbols = new GridBagConstraints();
		gbc_listSymbols.fill = GridBagConstraints.BOTH;
		gbc_listSymbols.gridx = 0;
		gbc_listSymbols.gridy = 1;
		panelSymbols.add(listSymbols, gbc_listSymbols);

		JTabbedPane tabbedPaneMain = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPaneMain = new GridBagConstraints();
		gbc_tabbedPaneMain.fill = GridBagConstraints.BOTH;
		gbc_tabbedPaneMain.insets = new Insets(0, 0, 0, 5);
		gbc_tabbedPaneMain.gridx = 1;
		gbc_tabbedPaneMain.gridy = 0;
		panelMain.add(tabbedPaneMain, gbc_tabbedPaneMain);

		JPanel panelWIP = new JPanel();
		tabbedPaneMain.addTab("W.I.P", null, panelWIP, null);
		GridBagLayout gbl_panelWIP = new GridBagLayout();
		gbl_panelWIP.columnWidths = new int[] { 0 };
		gbl_panelWIP.rowHeights = new int[] { 0 };
		gbl_panelWIP.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_panelWIP.rowWeights = new double[] { Double.MIN_VALUE };
		panelWIP.setLayout(gbl_panelWIP);

		JScrollPane scrollPaneASM = new JScrollPane();
		tabbedPaneMain.addTab("Assembler Code", null, scrollPaneASM, null);
		scrollPaneASM.setPreferredSize(new Dimension(300, 4));

		txtASM = new JTextArea();
		scrollPaneASM.setViewportView(txtASM);

		JScrollPane scrollPaneBinary = new JScrollPane();
		tabbedPaneMain.addTab("Binary", null, scrollPaneBinary, null);
		scrollPaneBinary.setPreferredSize(new Dimension(680, 400));

		JTextArea textAreaBinary = new JTextArea();
		textAreaBinary.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneBinary.setViewportView(textAreaBinary);

		JLabel label = new JLabel("      00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F");
		label.setForeground(Color.BLUE);
		label.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneBinary.setColumnHeaderView(label);

		JPanel toolBar = new JPanel();
		toolBar.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar.anchor = GridBagConstraints.SOUTH;
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 2;
		frmManualDisassembler.getContentPane().add(toolBar, gbc_toolBar);
		GridBagLayout gbl_toolBar = new GridBagLayout();
		gbl_toolBar.columnWidths = new int[] { 476, 46, 0 };
		gbl_toolBar.rowHeights = new int[] { 14, 0 };
		gbl_toolBar.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_toolBar.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		toolBar.setLayout(gbl_toolBar);

		lblBinaryFileName = new JLabel("New label");
		GridBagConstraints gbc_lblBinaryFileName = new GridBagConstraints();
		gbc_lblBinaryFileName.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblBinaryFileName.gridx = 1;
		gbc_lblBinaryFileName.gridy = 0;
		toolBar.add(lblBinaryFileName, gbc_lblBinaryFileName);

	}

	// initialize()

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
			return endLoc - startLoc;
		}// size

		public String toString() {
			return String.format("%04X : %04X ; (%04X) %s", startLoc, endLoc, size(), this.type);
		}// toString

		@Override
		public int compareTo(CodeFragment ct0) {
			return startLoc - ct0.startLoc;
		}// compareTo
	}// class CodeType

	class CodeFragmentModel extends AbstractListModel<CodeFragment> implements ListModel<CodeFragment> {
		ArrayList<CodeFragment> codeFragements;

		public CodeFragmentModel() {
			codeFragements = new ArrayList<CodeFragment>();
			// codeFragements.add(new CodeFragment(0X0000, 0X000, CodeFragment.RESERVED));
			// codeFragements.add(new CodeFragment(0XFFFF, 0XFFFF, CodeFragment.RESERVED));
		}// Constructor

		// public void addListDataListener(ManualDisassembler manualDisassembler) {
		// // TODO Auto-generated method stub
		//
		// }

		@Override
		public CodeFragment getElementAt(int index) {
			return codeFragements.get(index);
		}// getElementAt

		public boolean addItem(CodeFragment codeFragment) {
			boolean result = true;
			int startLocNew = codeFragment.startLoc;
			int endLocNew = codeFragment.endLoc;
			String typeNew = codeFragment.type;

			int containerIndex = codeFragmentModel.withinFragement(startLocNew);

			if (containerIndex != -1) {// there is a container
				CodeFragment cfContainer = codeFragmentModel.getElementAt(containerIndex);
				CodeFragment cfToAdd;
				int containerStartLoc = cfContainer.startLoc;
				int containerEndLoc = cfContainer.endLoc;
				String containerType = cfContainer.type;
				if (containerIndex < codeFragmentModel.getSize()) {
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

		// // ListSelectionModel
		// private Vector<ListSelectionListener> listSelectionListeners = new Vector<ListSelectionListener>();

	}// class CodeFragmentModel

	public class SymbolDis implements Comparable<SymbolDis> {
		private String ID;
		private String name;
		private int value;
		private String type;
		private int definedLocation;
		private ArrayList<Integer> references;

		public static final String LABEL = "Label";
		public static final String VALUE = "Value";

		public SymbolDis(String ID, String type, int location, boolean isReference) {
			references = new ArrayList<Integer>();
			this.ID = ID;
			this.value = Integer.valueOf(ID, 16);
			this.type = type;
			if (isReference) {
				references.add(location);
			} else {
				this.definedLocation = location;
			}// if
		}// Constructor

		public String toString() {
			return String.format("%4s  %s", this.ID, this.type);
		}// toString

		@Override
		public int compareTo(SymbolDis sd) {
			return value - sd.value;
		}// compareTo

		public void addReference(int location) {
			if (!references.contains(location)) {
				references.add(location);
			}// if
		}// addReference
	}// class SymbolsDis

	class SymbolDisModel extends ArrayList<SymbolDis> implements ListModel {
		@Override
		public void addListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub
		}// addListDataListener

		@Override
		public Object getElementAt(int index) {
			// TODO Auto-generated method stub
			return this.get(index);
		}// getElementAt

		@Override
		public int getSize() {
			return this.size();
		}// getSize

		@Override
		public void removeListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub
		}// removeListDataListener

	}// class SymbolDisModel

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

	class OperationStructure {
		private byte opCode;
		private int size;
		private String instruction;
		private String source;
		private String destination;
		private int pcAction;

		public final static int NORMAL = 0;
		public final static int TOTAL = 1; // JMP POP L
		public final static int CONTINUATION = 2; // CALL and All conditionals
		public final static int TERMINATES = 3; // RET

		OperationStructure(byte opCode, int size,
				String instruction, String destination, String source) {
			this(opCode, size, instruction, destination, source, NORMAL);
		}

		OperationStructure(byte opCode, int size,
				String instruction, String destination, String source, int pcAction) {
			this.opCode = opCode;
			this.size = size;
			this.instruction = instruction;
			this.source = source;
			this.destination = destination;
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

		public int getPcAction() {
			return this.pcAction;
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

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private void makeOpcodeMap() {
		int NORMAL = OperationStructure.NORMAL;
		int TOTAL = OperationStructure.TOTAL; // JMP POP PCHL
		int CONTINUATION = OperationStructure.CONTINUATION; // CALL and All conditionals
		int TERMINATES = OperationStructure.TERMINATES; // RET

		opcodeMap = new HashMap<Byte, OperationStructure>();
		opcodeMap.put((byte) 0X00, new OperationStructure((byte) 0X00, 1, "NOP", "", ""));
		opcodeMap.put((byte) 0X01, new OperationStructure((byte) 0X01, 3, "LXI", "B", "D16"));
		opcodeMap.put((byte) 0X02, new OperationStructure((byte) 0X02, 1, "STAX", "B", ""));
		opcodeMap.put((byte) 0X03, new OperationStructure((byte) 0X03, 1, "INX", "B", ""));
		opcodeMap.put((byte) 0X04, new OperationStructure((byte) 0X04, 1, "INR", "B", ""));
		opcodeMap.put((byte) 0X05, new OperationStructure((byte) 0X05, 1, "DCR", "B", ""));
		opcodeMap.put((byte) 0X06, new OperationStructure((byte) 0X06, 2, "MVI", "B", "D8"));
		opcodeMap.put((byte) 0X07, new OperationStructure((byte) 0X07, 1, "RLC", "", ""));
		opcodeMap.put((byte) 0X08, new OperationStructure((byte) 0X08, 1, "Alt", "", ""));
		opcodeMap.put((byte) 0X09, new OperationStructure((byte) 0X09, 1, "DAD", "B", ""));
		opcodeMap.put((byte) 0X0A, new OperationStructure((byte) 0X0A, 1, "LDAX", "B", ""));
		opcodeMap.put((byte) 0X0B, new OperationStructure((byte) 0X0B, 1, "DCX", "B", ""));
		opcodeMap.put((byte) 0X0C, new OperationStructure((byte) 0X0C, 1, "INR", "C", ""));
		opcodeMap.put((byte) 0X0D, new OperationStructure((byte) 0X0D, 1, "DCR", "C", ""));
		opcodeMap.put((byte) 0X0E, new OperationStructure((byte) 0X0E, 2, "MVI", "C", "D8"));
		opcodeMap.put((byte) 0X0F, new OperationStructure((byte) 0X0F, 1, "RRC", "", ""));

		opcodeMap.put((byte) 0X10, new OperationStructure((byte) 0X10, 1, "Alt", "", ""));
		opcodeMap.put((byte) 0X11, new OperationStructure((byte) 0X11, 3, "LXI", "D", "D16"));
		opcodeMap.put((byte) 0X12, new OperationStructure((byte) 0X12, 1, "STAX", "D", ""));
		opcodeMap.put((byte) 0X13, new OperationStructure((byte) 0X13, 1, "INX", "D", ""));
		opcodeMap.put((byte) 0X14, new OperationStructure((byte) 0X14, 1, "INR", "D", ""));
		opcodeMap.put((byte) 0X15, new OperationStructure((byte) 0X15, 1, "DCR", "D", ""));
		opcodeMap.put((byte) 0X16, new OperationStructure((byte) 0X16, 2, "MVI", "D", "D8"));
		opcodeMap.put((byte) 0X17, new OperationStructure((byte) 0X17, 1, "RAL", "D", ""));
		opcodeMap.put((byte) 0X18, new OperationStructure((byte) 0X18, 1, "Alt", "", ""));
		opcodeMap.put((byte) 0X19, new OperationStructure((byte) 0X19, 1, "DAD", "D", ""));
		opcodeMap.put((byte) 0X1A, new OperationStructure((byte) 0X1A, 1, "LDAX", "D", ""));
		opcodeMap.put((byte) 0X1B, new OperationStructure((byte) 0X1B, 1, "DCX", "D", ""));
		opcodeMap.put((byte) 0X1C, new OperationStructure((byte) 0X1C, 1, "INR", "E", ""));
		opcodeMap.put((byte) 0X1D, new OperationStructure((byte) 0X1D, 1, "DCR", "E", ""));
		opcodeMap.put((byte) 0X1E, new OperationStructure((byte) 0X1E, 2, "MVI", "E", "D8"));
		opcodeMap.put((byte) 0X1F, new OperationStructure((byte) 0X1F, 1, "RAR", "", ""));

		opcodeMap.put((byte) 0X20, new OperationStructure((byte) 0X20, 1, "NOP*", "", ""));// special
		opcodeMap.put((byte) 0X21, new OperationStructure((byte) 0X21, 3, "LXI", "H", "D16"));
		opcodeMap.put((byte) 0X22, new OperationStructure((byte) 0X22, 3, "SHLD", "addr", ""));
		opcodeMap.put((byte) 0X23, new OperationStructure((byte) 0X23, 1, "INX", "H", ""));
		opcodeMap.put((byte) 0X24, new OperationStructure((byte) 0X24, 1, "INR", "H", ""));
		opcodeMap.put((byte) 0X25, new OperationStructure((byte) 0X25, 1, "DCR", "H", ""));
		opcodeMap.put((byte) 0X26, new OperationStructure((byte) 0X26, 2, "MVI", "H", "D8"));
		opcodeMap.put((byte) 0X27, new OperationStructure((byte) 0X27, 1, "DAA", "", "")); // special
		opcodeMap.put((byte) 0X28, new OperationStructure((byte) 0X28, 1, "Alt", "", ""));
		opcodeMap.put((byte) 0X29, new OperationStructure((byte) 0X29, 1, "DAD", "H", ""));
		opcodeMap.put((byte) 0X2A, new OperationStructure((byte) 0X2A, 3, "LHLD", "addr", ""));
		opcodeMap.put((byte) 0X2B, new OperationStructure((byte) 0X2B, 1, "DCX", "H", ""));
		opcodeMap.put((byte) 0X2C, new OperationStructure((byte) 0X2C, 1, "INR", "L", ""));
		opcodeMap.put((byte) 0X2D, new OperationStructure((byte) 0X2D, 1, "DCR", "L", ""));
		opcodeMap.put((byte) 0X2E, new OperationStructure((byte) 0X2E, 2, "MVI", "L", "D8"));
		opcodeMap.put((byte) 0X2F, new OperationStructure((byte) 0X2F, 1, "CMA", "", ""));

		opcodeMap.put((byte) 0X30, new OperationStructure((byte) 0X30, 1, "Alt", "", "")); // special
		opcodeMap.put((byte) 0X31, new OperationStructure((byte) 0X31, 3, "LXI", "SP", "D16"));
		opcodeMap.put((byte) 0X32, new OperationStructure((byte) 0X32, 3, "STA", "addr", ""));
		opcodeMap.put((byte) 0X33, new OperationStructure((byte) 0X33, 1, "INX", "SP", ""));
		opcodeMap.put((byte) 0X34, new OperationStructure((byte) 0X34, 1, "INR", "M", ""));
		opcodeMap.put((byte) 0X35, new OperationStructure((byte) 0X35, 1, "DCR", "M", ""));
		opcodeMap.put((byte) 0X36, new OperationStructure((byte) 0X36, 2, "MVI", "M", "D8"));
		opcodeMap.put((byte) 0X37, new OperationStructure((byte) 0X37, 1, "STC", "", ""));
		opcodeMap.put((byte) 0X38, new OperationStructure((byte) 0X38, 1, "Alt", "", ""));
		opcodeMap.put((byte) 0X39, new OperationStructure((byte) 0X39, 1, "DAD", "SP", ""));
		opcodeMap.put((byte) 0X3A, new OperationStructure((byte) 0X3A, 3, "LDA", "addr", ""));
		opcodeMap.put((byte) 0X3B, new OperationStructure((byte) 0X3B, 1, "DCX", "SP", ""));
		opcodeMap.put((byte) 0X3C, new OperationStructure((byte) 0X3C, 1, "INR", "A", ""));
		opcodeMap.put((byte) 0X3D, new OperationStructure((byte) 0X3D, 1, "DCR", "A", ""));
		opcodeMap.put((byte) 0X3E, new OperationStructure((byte) 0X3E, 2, "MVI", "A", "D8"));
		opcodeMap.put((byte) 0X3F, new OperationStructure((byte) 0X3F, 1, "CMC", "", ""));

		opcodeMap.put((byte) 0X40, new OperationStructure((byte) 0X40, 1, "MOV", "B", ",B"));
		opcodeMap.put((byte) 0X41, new OperationStructure((byte) 0X41, 1, "MOV", "B", ",C"));
		opcodeMap.put((byte) 0X42, new OperationStructure((byte) 0X42, 1, "MOV", "B", ",D"));
		opcodeMap.put((byte) 0X43, new OperationStructure((byte) 0X43, 1, "MOV", "B", ",E"));
		opcodeMap.put((byte) 0X44, new OperationStructure((byte) 0X44, 1, "MOV", "B", ",H"));
		opcodeMap.put((byte) 0X45, new OperationStructure((byte) 0X45, 1, "MOV", "B", ",L"));
		opcodeMap.put((byte) 0X46, new OperationStructure((byte) 0X46, 1, "MOV", "B", ",M"));
		opcodeMap.put((byte) 0X47, new OperationStructure((byte) 0X47, 1, "MOV", "B", ",A"));

		opcodeMap.put((byte) 0X48, new OperationStructure((byte) 0X48, 1, "MOV", "C", ",B"));
		opcodeMap.put((byte) 0X49, new OperationStructure((byte) 0X49, 1, "MOV", "C", ",C"));
		opcodeMap.put((byte) 0X4A, new OperationStructure((byte) 0X4A, 1, "MOV", "C", ",D"));
		opcodeMap.put((byte) 0X4B, new OperationStructure((byte) 0X4B, 1, "MOV", "C", ",E"));
		opcodeMap.put((byte) 0X4C, new OperationStructure((byte) 0X4C, 1, "MOV", "C", ",H"));
		opcodeMap.put((byte) 0X4D, new OperationStructure((byte) 0X4D, 1, "MOV", "C", ",L"));
		opcodeMap.put((byte) 0X4E, new OperationStructure((byte) 0X4E, 1, "MOV", "C", ",M"));
		opcodeMap.put((byte) 0X4F, new OperationStructure((byte) 0X4F, 1, "MOV", "C", ",A"));

		opcodeMap.put((byte) 0X50, new OperationStructure((byte) 0X50, 1, "MOV", "D", ",B"));
		opcodeMap.put((byte) 0X51, new OperationStructure((byte) 0X51, 1, "MOV", "D", ",C"));
		opcodeMap.put((byte) 0X52, new OperationStructure((byte) 0X52, 1, "MOV", "D", ",D"));
		opcodeMap.put((byte) 0X53, new OperationStructure((byte) 0X53, 1, "MOV", "D", ",E"));
		opcodeMap.put((byte) 0X54, new OperationStructure((byte) 0X54, 1, "MOV", "D", ",H"));
		opcodeMap.put((byte) 0X55, new OperationStructure((byte) 0X55, 1, "MOV", "D", ",L"));
		opcodeMap.put((byte) 0X56, new OperationStructure((byte) 0X56, 1, "MOV", "D", ",M"));
		opcodeMap.put((byte) 0X57, new OperationStructure((byte) 0X57, 1, "MOV", "D", ",A"));

		opcodeMap.put((byte) 0X58, new OperationStructure((byte) 0X58, 1, "MOV", "E", ",B"));
		opcodeMap.put((byte) 0X59, new OperationStructure((byte) 0X59, 1, "MOV", "E", ",C"));
		opcodeMap.put((byte) 0X5A, new OperationStructure((byte) 0X5A, 1, "MOV", "E", ",D"));
		opcodeMap.put((byte) 0X5B, new OperationStructure((byte) 0X5B, 1, "MOV", "E", ",E"));
		opcodeMap.put((byte) 0X5C, new OperationStructure((byte) 0X5C, 1, "MOV", "E", ",H"));
		opcodeMap.put((byte) 0X5D, new OperationStructure((byte) 0X5D, 1, "MOV", "E", ",L"));
		opcodeMap.put((byte) 0X5E, new OperationStructure((byte) 0X5E, 1, "MOV", "E", ",M"));
		opcodeMap.put((byte) 0X5F, new OperationStructure((byte) 0X5F, 1, "MOV", "E", ",A"));

		opcodeMap.put((byte) 0X60, new OperationStructure((byte) 0X60, 1, "MOV", "H", ",B"));
		opcodeMap.put((byte) 0X61, new OperationStructure((byte) 0X61, 1, "MOV", "H", ",C"));
		opcodeMap.put((byte) 0X62, new OperationStructure((byte) 0X62, 1, "MOV", "H", ",D"));
		opcodeMap.put((byte) 0X63, new OperationStructure((byte) 0X63, 1, "MOV", "H", ",E"));
		opcodeMap.put((byte) 0X64, new OperationStructure((byte) 0X64, 1, "MOV", "H", ",H"));
		opcodeMap.put((byte) 0X65, new OperationStructure((byte) 0X65, 1, "MOV", "H", ",L"));
		opcodeMap.put((byte) 0X66, new OperationStructure((byte) 0X66, 1, "MOV", "H", ",M"));
		opcodeMap.put((byte) 0X67, new OperationStructure((byte) 0X67, 1, "MOV", "H", ",A"));

		opcodeMap.put((byte) 0X68, new OperationStructure((byte) 0X68, 1, "MOV", "L", ",B"));
		opcodeMap.put((byte) 0X69, new OperationStructure((byte) 0X69, 1, "MOV", "L", ",C"));
		opcodeMap.put((byte) 0X6A, new OperationStructure((byte) 0X6A, 1, "MOV", "L", ",D"));
		opcodeMap.put((byte) 0X6B, new OperationStructure((byte) 0X6B, 1, "MOV", "L", ",E"));
		opcodeMap.put((byte) 0X6C, new OperationStructure((byte) 0X6C, 1, "MOV", "L", ",H"));
		opcodeMap.put((byte) 0X6D, new OperationStructure((byte) 0X6D, 1, "MOV", "L", ",L"));
		opcodeMap.put((byte) 0X6E, new OperationStructure((byte) 0X6E, 1, "MOV", "L", ",M"));
		opcodeMap.put((byte) 0X6F, new OperationStructure((byte) 0X6F, 1, "MOV", "L", ",A"));

		opcodeMap.put((byte) 0X70, new OperationStructure((byte) 0X70, 1, "MOV", "M", ",B"));
		opcodeMap.put((byte) 0X71, new OperationStructure((byte) 0X71, 1, "MOV", "M", ",C"));
		opcodeMap.put((byte) 0X72, new OperationStructure((byte) 0X72, 1, "MOV", "M", ",D"));
		opcodeMap.put((byte) 0X73, new OperationStructure((byte) 0X73, 1, "MOV", "M", ",E"));
		opcodeMap.put((byte) 0X74, new OperationStructure((byte) 0X74, 1, "MOV", "M", ",H"));
		opcodeMap.put((byte) 0X75, new OperationStructure((byte) 0X75, 1, "MOV", "M", ",L"));
		opcodeMap.put((byte) 0X76, new OperationStructure((byte) 0X76, 1, "HLT", "", "")); // Special
		opcodeMap.put((byte) 0X77, new OperationStructure((byte) 0X77, 1, "MOV", "M", ",A"));

		opcodeMap.put((byte) 0X78, new OperationStructure((byte) 0X78, 1, "MOV", "A", ",B"));
		opcodeMap.put((byte) 0X79, new OperationStructure((byte) 0X79, 1, "MOV", "A", ",C"));
		opcodeMap.put((byte) 0X7A, new OperationStructure((byte) 0X7A, 1, "MOV", "A", ",D"));
		opcodeMap.put((byte) 0X7B, new OperationStructure((byte) 0X7B, 1, "MOV", "A", ",E"));
		opcodeMap.put((byte) 0X7C, new OperationStructure((byte) 0X7C, 1, "MOV", "A", ",H"));
		opcodeMap.put((byte) 0X7D, new OperationStructure((byte) 0X7D, 1, "MOV", "A", ",L"));
		opcodeMap.put((byte) 0X7E, new OperationStructure((byte) 0X7E, 1, "MOV", "A", ",M"));
		opcodeMap.put((byte) 0X7F, new OperationStructure((byte) 0X7F, 1, "MOV", "A", ",A"));

		opcodeMap.put((byte) 0X80, new OperationStructure((byte) 0X80, 1, "ADD", "B", ""));
		opcodeMap.put((byte) 0X81, new OperationStructure((byte) 0X81, 1, "ADD", "C", ""));
		opcodeMap.put((byte) 0X82, new OperationStructure((byte) 0X82, 1, "ADD", "D", ""));
		opcodeMap.put((byte) 0X83, new OperationStructure((byte) 0X83, 1, "ADD", "E", ""));
		opcodeMap.put((byte) 0X84, new OperationStructure((byte) 0X84, 1, "ADD", "H", ""));
		opcodeMap.put((byte) 0X85, new OperationStructure((byte) 0X85, 1, "ADD", "L", ""));
		opcodeMap.put((byte) 0X86, new OperationStructure((byte) 0X86, 1, "ADD", "M", ""));
		opcodeMap.put((byte) 0X87, new OperationStructure((byte) 0X87, 1, "ADD", "A", ""));

		opcodeMap.put((byte) 0X88, new OperationStructure((byte) 0X88, 1, "ADC", "B", ""));
		opcodeMap.put((byte) 0X89, new OperationStructure((byte) 0X89, 1, "ADC", "C", ""));
		opcodeMap.put((byte) 0X8A, new OperationStructure((byte) 0X8A, 1, "ADC", "D", ""));
		opcodeMap.put((byte) 0X8B, new OperationStructure((byte) 0X8B, 1, "ADC", "E", ""));
		opcodeMap.put((byte) 0X8C, new OperationStructure((byte) 0X8C, 1, "ADC", "H", ""));
		opcodeMap.put((byte) 0X8D, new OperationStructure((byte) 0X8D, 1, "ADC", "L", ""));
		opcodeMap.put((byte) 0X8E, new OperationStructure((byte) 0X8E, 1, "ADC", "M", ""));
		opcodeMap.put((byte) 0X8F, new OperationStructure((byte) 0X8F, 1, "ADC", "A", ""));

		opcodeMap.put((byte) 0X90, new OperationStructure((byte) 0X90, 1, "SUB", "B", ""));
		opcodeMap.put((byte) 0X91, new OperationStructure((byte) 0X91, 1, "SUB", "C", ""));
		opcodeMap.put((byte) 0X92, new OperationStructure((byte) 0X92, 1, "SUB", "D", ""));
		opcodeMap.put((byte) 0X93, new OperationStructure((byte) 0X93, 1, "SUB", "E", ""));
		opcodeMap.put((byte) 0X94, new OperationStructure((byte) 0X94, 1, "SUB", "H", ""));
		opcodeMap.put((byte) 0X95, new OperationStructure((byte) 0X95, 1, "SUB", "L", ""));
		opcodeMap.put((byte) 0X96, new OperationStructure((byte) 0X96, 1, "SUB", "M", ""));
		opcodeMap.put((byte) 0X97, new OperationStructure((byte) 0X97, 1, "SUB", "A", ""));

		opcodeMap.put((byte) 0X98, new OperationStructure((byte) 0X98, 1, "SBB", "B", ""));
		opcodeMap.put((byte) 0X99, new OperationStructure((byte) 0X99, 1, "SBB", "C", ""));
		opcodeMap.put((byte) 0X9A, new OperationStructure((byte) 0X9A, 1, "SBB", "D", ""));
		opcodeMap.put((byte) 0X9B, new OperationStructure((byte) 0X9B, 1, "SBB", "E", ""));
		opcodeMap.put((byte) 0X9C, new OperationStructure((byte) 0X9C, 1, "SBB", "H", ""));
		opcodeMap.put((byte) 0X9D, new OperationStructure((byte) 0X9D, 1, "SBB", "L", ""));
		opcodeMap.put((byte) 0X9E, new OperationStructure((byte) 0X9E, 1, "SBB", "M", ""));
		opcodeMap.put((byte) 0X9F, new OperationStructure((byte) 0X9F, 1, "SBB", "A", ""));

		opcodeMap.put((byte) 0XA0, new OperationStructure((byte) 0XA0, 1, "ANA", "B", ""));
		opcodeMap.put((byte) 0XA1, new OperationStructure((byte) 0XA1, 1, "ANA", "C", ""));
		opcodeMap.put((byte) 0XA2, new OperationStructure((byte) 0XA2, 1, "ANA", "D", ""));
		opcodeMap.put((byte) 0XA3, new OperationStructure((byte) 0XA3, 1, "ANA", "E", ""));
		opcodeMap.put((byte) 0XA4, new OperationStructure((byte) 0XA4, 1, "ANA", "H", ""));
		opcodeMap.put((byte) 0XA5, new OperationStructure((byte) 0XA5, 1, "ANA", "L", ""));
		opcodeMap.put((byte) 0XA6, new OperationStructure((byte) 0XA6, 1, "ANA", "M", ""));
		opcodeMap.put((byte) 0XA7, new OperationStructure((byte) 0XA7, 1, "ANA", "A", ""));

		opcodeMap.put((byte) 0XA8, new OperationStructure((byte) 0XA8, 1, "XRA", "B", ""));
		opcodeMap.put((byte) 0XA9, new OperationStructure((byte) 0XA9, 1, "XRA", "C", ""));
		opcodeMap.put((byte) 0XAA, new OperationStructure((byte) 0XAA, 1, "XRA", "D", ""));
		opcodeMap.put((byte) 0XAB, new OperationStructure((byte) 0XAB, 1, "XRA", "E", ""));
		opcodeMap.put((byte) 0XAC, new OperationStructure((byte) 0XAC, 1, "XRA", "H", ""));
		opcodeMap.put((byte) 0XAD, new OperationStructure((byte) 0XAD, 1, "XRA", "L", ""));
		opcodeMap.put((byte) 0XAE, new OperationStructure((byte) 0XAE, 1, "XRA", "M", ""));
		opcodeMap.put((byte) 0XAF, new OperationStructure((byte) 0XAF, 1, "XRA", "A", ""));

		opcodeMap.put((byte) 0XB0, new OperationStructure((byte) 0XB0, 1, "ORA", "B", ""));
		opcodeMap.put((byte) 0XB1, new OperationStructure((byte) 0XB1, 1, "ORA", "C", ""));
		opcodeMap.put((byte) 0XB2, new OperationStructure((byte) 0XB2, 1, "ORA", "D", ""));
		opcodeMap.put((byte) 0XB3, new OperationStructure((byte) 0XB3, 1, "ORA", "E", ""));
		opcodeMap.put((byte) 0XB4, new OperationStructure((byte) 0XB4, 1, "ORA", "H", ""));
		opcodeMap.put((byte) 0XB5, new OperationStructure((byte) 0XB5, 1, "ORA", "L", ""));
		opcodeMap.put((byte) 0XB6, new OperationStructure((byte) 0XB6, 1, "ORA", "M", ""));
		opcodeMap.put((byte) 0XB7, new OperationStructure((byte) 0XB7, 1, "ORA", "A", ""));

		opcodeMap.put((byte) 0XB8, new OperationStructure((byte) 0XB8, 1, "CMP", "B", ""));
		opcodeMap.put((byte) 0XB9, new OperationStructure((byte) 0XB9, 1, "CMP", "C", ""));
		opcodeMap.put((byte) 0XBA, new OperationStructure((byte) 0XBA, 1, "CMP", "D", ""));
		opcodeMap.put((byte) 0XBB, new OperationStructure((byte) 0XBB, 1, "CMP", "E", ""));
		opcodeMap.put((byte) 0XBC, new OperationStructure((byte) 0XBC, 1, "CMP", "H", ""));
		opcodeMap.put((byte) 0XBD, new OperationStructure((byte) 0XBD, 1, "CMP", "L", ""));
		opcodeMap.put((byte) 0XBE, new OperationStructure((byte) 0XBE, 1, "CMP", "M", ""));
		opcodeMap.put((byte) 0XBF, new OperationStructure((byte) 0XBF, 1, "CMP", "A", ""));

		opcodeMap.put((byte) 0XC0, new OperationStructure((byte) 0XC0, 1, "RNZ", "", ""));
		opcodeMap.put((byte) 0XC1, new OperationStructure((byte) 0XC1, 1, "POP", "B", ""));
		opcodeMap.put((byte) 0XC2, new OperationStructure((byte) 0XC2, 3, "JNZ", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XC3, new OperationStructure((byte) 0XC3, 3, "JMP", "addr", "", TOTAL));
		opcodeMap.put((byte) 0XC4, new OperationStructure((byte) 0XC4, 3, "CNZ", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XC5, new OperationStructure((byte) 0XC5, 1, "PUSH", "B", ""));
		opcodeMap.put((byte) 0XC6, new OperationStructure((byte) 0XC6, 2, "ADI", "D8", ""));
		opcodeMap.put((byte) 0XC7, new OperationStructure((byte) 0XC7, 1, "RST", "0", "", CONTINUATION));
		opcodeMap.put((byte) 0XC8, new OperationStructure((byte) 0XC8, 1, "RZ", "", ""));
		opcodeMap.put((byte) 0XC9, new OperationStructure((byte) 0XC9, 1, "RET", "", "", TERMINATES));
		opcodeMap.put((byte) 0XCA, new OperationStructure((byte) 0XCA, 3, "JZ", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XCB, new OperationStructure((byte) 0XCB, 3, "Alt", "addr", "", TOTAL));
		opcodeMap.put((byte) 0XCC, new OperationStructure((byte) 0XCC, 3, "CZ", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XCD, new OperationStructure((byte) 0XCD, 3, "CALL", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XCE, new OperationStructure((byte) 0XCE, 2, "ACI", "D8", ""));
		opcodeMap.put((byte) 0XCF, new OperationStructure((byte) 0XCF, 1, "RST", "1", "", CONTINUATION));

		opcodeMap.put((byte) 0XD0, new OperationStructure((byte) 0XD0, 1, "RNC", "", ""));
		opcodeMap.put((byte) 0XD1, new OperationStructure((byte) 0XD1, 1, "POP", "D", ""));
		opcodeMap.put((byte) 0XD2, new OperationStructure((byte) 0XD2, 3, "JNC", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XD3, new OperationStructure((byte) 0XD3, 2, "OUT", "D8", "")); // Special
		opcodeMap.put((byte) 0XD4, new OperationStructure((byte) 0XD4, 3, "CNC", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XD5, new OperationStructure((byte) 0XD5, 1, "PUSH", "D", ""));
		opcodeMap.put((byte) 0XD6, new OperationStructure((byte) 0XD6, 2, "SUI", "D8", ""));
		opcodeMap.put((byte) 0XD7, new OperationStructure((byte) 0XD7, 1, "RST", "2", "", CONTINUATION));
		opcodeMap.put((byte) 0XD8, new OperationStructure((byte) 0XD8, 1, "RC", "", ""));
		opcodeMap.put((byte) 0XD9, new OperationStructure((byte) 0XD9, 1, "RET*", "", "", TOTAL));
		opcodeMap.put((byte) 0XDA, new OperationStructure((byte) 0XDA, 3, "JC", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XDB, new OperationStructure((byte) 0XDB, 2, "IN", "D8", "")); // Special
		opcodeMap.put((byte) 0XDC, new OperationStructure((byte) 0XDC, 3, "CC", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XDD, new OperationStructure((byte) 0XDD, 3, "Alt", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XDE, new OperationStructure((byte) 0XDE, 2, "SBI", "D8", ""));
		opcodeMap.put((byte) 0XDF, new OperationStructure((byte) 0XDF, 1, "RST", "3", "", CONTINUATION));

		opcodeMap.put((byte) 0XE0, new OperationStructure((byte) 0XE0, 1, "RPO", "", ""));
		opcodeMap.put((byte) 0XE1, new OperationStructure((byte) 0XE1, 1, "POP", "H", ""));
		opcodeMap.put((byte) 0XE2, new OperationStructure((byte) 0XE2, 3, "JPO", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XE3, new OperationStructure((byte) 0XE3, 1, "XTHL", "", ""));
		opcodeMap.put((byte) 0XE4, new OperationStructure((byte) 0XE4, 3, "CPO", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XE5, new OperationStructure((byte) 0XE5, 1, "PUSH", "H", ""));
		opcodeMap.put((byte) 0XE6, new OperationStructure((byte) 0XE6, 2, "ANI", "D8", ""));
		opcodeMap.put((byte) 0XE7, new OperationStructure((byte) 0XE7, 1, "RST", "4", "", CONTINUATION));
		opcodeMap.put((byte) 0XE8, new OperationStructure((byte) 0XE8, 1, "RPE", "", ""));
		opcodeMap.put((byte) 0XE9, new OperationStructure((byte) 0XE9, 1, "PCHL", "", "", TOTAL));
		opcodeMap.put((byte) 0XEA, new OperationStructure((byte) 0XEA, 3, "JPE", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XEB, new OperationStructure((byte) 0XEB, 1, "XCHG", "", "")); // Special
		opcodeMap.put((byte) 0XEC, new OperationStructure((byte) 0XEC, 3, "CPE", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XED, new OperationStructure((byte) 0XED, 3, "Alt", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XEE, new OperationStructure((byte) 0XEE, 2, "XRI", "D8", ""));
		opcodeMap.put((byte) 0XEF, new OperationStructure((byte) 0XEF, 1, "RST", "5", "", CONTINUATION));

		opcodeMap.put((byte) 0XF0, new OperationStructure((byte) 0XF0, 1, "RP", "", ""));
		opcodeMap.put((byte) 0XF1, new OperationStructure((byte) 0XF1, 1, "POP", "PSW", ""));
		opcodeMap.put((byte) 0XF2, new OperationStructure((byte) 0XF2, 3, "JP", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XF3, new OperationStructure((byte) 0XF3, 1, "DI", "", "")); // Special
		opcodeMap.put((byte) 0XF4, new OperationStructure((byte) 0XF4, 3, "CP", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XF5, new OperationStructure((byte) 0XF5, 1, "PUSH", "PSW", ""));
		opcodeMap.put((byte) 0XF6, new OperationStructure((byte) 0XF6, 2, "ORI", "D8", ""));
		opcodeMap.put((byte) 0XF7, new OperationStructure((byte) 0XF7, 1, "RST", "6", "", CONTINUATION));
		opcodeMap.put((byte) 0XF8, new OperationStructure((byte) 0XF8, 1, "RM", "", ""));
		opcodeMap.put((byte) 0XF9, new OperationStructure((byte) 0XF9, 1, "SPHL", "", ""));
		opcodeMap.put((byte) 0XFA, new OperationStructure((byte) 0XFA, 3, "JM", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XFB, new OperationStructure((byte) 0XFB, 1, "EI", "", "")); // Special
		opcodeMap.put((byte) 0XFC, new OperationStructure((byte) 0XFC, 3, "CM", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XFD, new OperationStructure((byte) 0XFD, 3, "Alt", "addr", "", CONTINUATION));
		opcodeMap.put((byte) 0XFE, new OperationStructure((byte) 0XFE, 2, "CPI", "D8", ""));
		opcodeMap.put((byte) 0XFF, new OperationStructure((byte) 0XFF, 1, "RST", "7", "", CONTINUATION));

	}

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

}
