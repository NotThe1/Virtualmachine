package codeSupport;

import java.awt.EventQueue;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileNameExtensionFilter;

//import disks.DiskMetrics;
//import disks.DiskUtility.DirEntry;


import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

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

public class manualDisassembler implements ActionListener {

	private JFrame frmManualDisassembler;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					manualDisassembler window = new manualDisassembler();
					window.frmManualDisassembler.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// ------------------------------------------------------------------------------------------
	private void mnuOpenFile() {
		String fileLocation = ".";
		Path sourcePath = Paths.get(fileLocation, "Disks");
		JFileChooser chooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}// if
		binaryFile = chooser.getSelectedFile();
		binaryFilePath = chooser.getSelectedFile().getAbsolutePath().toString();
		lblBinaryFileName.setText(binaryFile.getName());
		lblBinaryFileName.setToolTipText(binaryFilePath);

	}
	private void mnuSave(String cfFileName){
//		try {
//			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName + FILE_SUFFIX_PERIOD));
//			oos.writeObject(ccr);
//			oos.writeObject(wrs);
//			oos.writeObject(core);
//			oos.close();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}// try - write objects

	}

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

		case AC_BTN_ADD:
			message = AC_BTN_ADD;
			break;
		case AC_BTN_REMOVE:
			message = AC_BTN_REMOVE;
			break;
		default:
			message = "default";
		}// switch

		if (message != null) {
			System.out.printf("actionCommand = %s%n", actionCommand);
		}

	}// actionPerformed

	private void appClose() {
		System.exit(0);
	}// appClose

	private void appInit() {
		codeFragmentModel = new CodeFragmentModel();
		codeFragmentModel.add(new CodeFragment(0X100, 0X400, CodeFragment.CODE));
		codeFragmentModel.add(new CodeFragment(0X0, 0X0, CodeFragment.CODE));
		codeFragmentModel.add(new CodeFragment(0X25, 0X50, CodeFragment.CONSTANT));
//		codeTypeModel.add(new CodeType(0X75, 0X50, CodeType.RESERVED));
		 Collections.sort(codeFragmentModel);
		listCodeTypes.setModel(codeFragmentModel);
	}// appInit

	/**
	 * Create the application.
	 */
	public manualDisassembler() {
		initialize();
		appInit();
	}

	private File binaryFile;
	private String binaryFilePath;
	private CodeFragmentModel codeFragmentModel;
	private final static String AC_MNU_FILE_OPEN = "mnuFileOpen";
	private final static String AC_MNU_FILE_SAVE = "mnuFileSave";
	private final static String AC_MNU_FILE_SAVEAS = "mnuFileSaveAs";
	private final static String AC_MNU_FILE_CLOSE = "mnuFileClose";
	private final static String AC_MNU_FILE_EXIT = "mnuFileExit";
	
	private final static String AC_MNU_CODE_NEW = "mnuCodeFragmentNew";
	private final static String AC_MNU_CODE_LOAD = "mnuCodeFragmentLoad";
	private final static String AC_MNU_CODE_SAVE = "mnuCodeFragmentSave";
	private final static String AC_MNU_CODE_SAVE_AS = "mnuCodeFragmentSaveAs";

	
	
	private final static String AC_BTN_ADD = "btnAdd";
	private final static String AC_BTN_REMOVE = "btnRemove";

	// ++++++++++++++++++++++++++++++++++
	private JLabel lblBinaryFileName;
	private JList listCodeTypes;

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
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		frmManualDisassembler.getContentPane().setLayout(gridBagLayout);

		JPanel panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.insets = new Insets(0, 0, 5, 0);
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 0;
		frmManualDisassembler.getContentPane().add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.insets = new Insets(0, 0, 0, 5);
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		panelMain.add(tabbedPane, gbc_tabbedPane);

		JPanel panelFragments = new JPanel();
		tabbedPane.addTab("Fragments", null, panelFragments, null);
		panelFragments.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagLayout gbl_panelFragments = new GridBagLayout();
		gbl_panelFragments.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelFragments.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelFragments.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelFragments.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelFragments.setLayout(gbl_panelFragments);

		JPanel panelTypeManipulation = new JPanel();
		GridBagConstraints gbc_panelTypeManipulation = new GridBagConstraints();
		gbc_panelTypeManipulation.insets = new Insets(0, 0, 5, 5);
		gbc_panelTypeManipulation.fill = GridBagConstraints.VERTICAL;
		gbc_panelTypeManipulation.gridx = 0;
		gbc_panelTypeManipulation.gridy = 0;
		panelFragments.add(panelTypeManipulation, gbc_panelTypeManipulation);

		JLabel lblStart = new JLabel("Start");
		panelTypeManipulation.add(lblStart);

		Hex64KSpinner hex64KSpinner = new Hex64KSpinner();
		GridBagConstraints gbc_hex64KSpinner = new GridBagConstraints();
		gbc_hex64KSpinner.anchor = GridBagConstraints.EAST;
		gbc_hex64KSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_hex64KSpinner.gridx = 1;
		gbc_hex64KSpinner.gridy = 0;
		panelFragments.add(hex64KSpinner, gbc_hex64KSpinner);

		JLabel lblEnd = new JLabel("End");
		GridBagConstraints gbc_lblEnd = new GridBagConstraints();
		gbc_lblEnd.insets = new Insets(0, 0, 5, 5);
		gbc_lblEnd.gridx = 0;
		gbc_lblEnd.gridy = 1;
		panelFragments.add(lblEnd, gbc_lblEnd);

		Hex64KSpinner hex64KSpinner_1 = new Hex64KSpinner();
		GridBagConstraints gbc_hex64KSpinner_1 = new GridBagConstraints();
		gbc_hex64KSpinner_1.anchor = GridBagConstraints.EAST;
		gbc_hex64KSpinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_hex64KSpinner_1.gridx = 1;
		gbc_hex64KSpinner_1.gridy = 1;
		panelFragments.add(hex64KSpinner_1, gbc_hex64KSpinner_1);

		JRadioButton rdbtnCode = new JRadioButton("Code");
		GridBagConstraints gbc_rdbtnCode = new GridBagConstraints();
		gbc_rdbtnCode.anchor = GridBagConstraints.WEST;
		gbc_rdbtnCode.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnCode.gridx = 0;
		gbc_rdbtnCode.gridy = 2;
		panelFragments.add(rdbtnCode, gbc_rdbtnCode);

		JRadioButton rdbtnCnstant = new JRadioButton("Constant");
		GridBagConstraints gbc_rdbtnCnstant = new GridBagConstraints();
		gbc_rdbtnCnstant.anchor = GridBagConstraints.WEST;
		gbc_rdbtnCnstant.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnCnstant.gridx = 0;
		gbc_rdbtnCnstant.gridy = 3;
		panelFragments.add(rdbtnCnstant, gbc_rdbtnCnstant);

		JRadioButton rdbtnReserved = new JRadioButton("Reserved");
		GridBagConstraints gbc_rdbtnReserved = new GridBagConstraints();
		gbc_rdbtnReserved.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnReserved.anchor = GridBagConstraints.WEST;
		gbc_rdbtnReserved.gridx = 0;
		gbc_rdbtnReserved.gridy = 4;
		panelFragments.add(rdbtnReserved, gbc_rdbtnReserved);

		JButton btnAdd = new JButton("Add");
		btnAdd.setActionCommand(AC_BTN_ADD);
		btnAdd.addActionListener(this);
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.NORTH;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 5;
		panelFragments.add(btnAdd, gbc_btnAdd);

		JButton btnRemove = new JButton("Remove");
		btnRemove.setActionCommand(AC_BTN_REMOVE);
		btnRemove.addActionListener(this);
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.gridx = 1;
		gbc_btnRemove.gridy = 5;
		panelFragments.add(btnRemove, gbc_btnRemove);

		listCodeTypes = new JList();
		listCodeTypes.setBorder(new LineBorder(Color.BLUE));
		GridBagConstraints gbc_listCodeTypes = new GridBagConstraints();
		gbc_listCodeTypes.gridwidth = 2;
		gbc_listCodeTypes.insets = new Insets(0, 0, 0, 5);
		gbc_listCodeTypes.fill = GridBagConstraints.BOTH;
		gbc_listCodeTypes.gridx = 0;
		gbc_listCodeTypes.gridy = 7;
		panelFragments.add(listCodeTypes, gbc_listCodeTypes);
		
		JPanel panelSymbols = new JPanel();
		tabbedPane.addTab("Symbols", null, panelSymbols, null);
		GridBagLayout gbl_panelSymbols = new GridBagLayout();
		gbl_panelSymbols.columnWidths = new int[]{0};
		gbl_panelSymbols.rowHeights = new int[]{0};
		gbl_panelSymbols.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panelSymbols.rowWeights = new double[]{Double.MIN_VALUE};
		panelSymbols.setLayout(gbl_panelSymbols);

		JScrollPane scrollPaneBinary = new JScrollPane();
		scrollPaneBinary.setPreferredSize(new Dimension(680, 400));
		GridBagConstraints gbc_scrollPaneBinary = new GridBagConstraints();
		gbc_scrollPaneBinary.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneBinary.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneBinary.gridx = 1;
		gbc_scrollPaneBinary.gridy = 0;
		panelMain.add(scrollPaneBinary, gbc_scrollPaneBinary);

		JTextArea textAreaBinary = new JTextArea();
		textAreaBinary.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneBinary.setViewportView(textAreaBinary);

		JLabel label = new JLabel("      00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F");
		label.setForeground(Color.BLUE);
		label.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneBinary.setColumnHeaderView(label);

		JPanel panelASM = new JPanel();
		panelASM.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelASM = new GridBagConstraints();
		gbc_panelASM.fill = GridBagConstraints.BOTH;
		gbc_panelASM.gridx = 2;
		gbc_panelASM.gridy = 0;
		panelMain.add(panelASM, gbc_panelASM);
		GridBagLayout gbl_panelASM = new GridBagLayout();
		gbl_panelASM.columnWidths = new int[] { 111, 2, 0 };
		gbl_panelASM.rowHeights = new int[] { 2, 0 };
		gbl_panelASM.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelASM.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelASM.setLayout(gbl_panelASM);

		JScrollPane scrollPaneASM = new JScrollPane();
		GridBagConstraints gbc_scrollPaneASM = new GridBagConstraints();
		gbc_scrollPaneASM.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPaneASM.gridx = 1;
		gbc_scrollPaneASM.gridy = 0;
		panelASM.add(scrollPaneASM, gbc_scrollPaneASM);

		JPanel toolBar = new JPanel();
		toolBar.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar.anchor = GridBagConstraints.SOUTH;
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 1;
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

	public class CodeFragment implements Comparable<CodeFragment> {
		public int startLoc;
		public int endLoc;
		public String type;

		public static final String CODE = "code";
		public static final String CONSTANT = "constant";
		public static final String RESERVED = "reserved";

		public CodeFragment(int startLoc, int endLoc, String type) {
			this.startLoc = startLoc;
			this.endLoc = startLoc > endLoc ? -1 : endLoc;
			this.type = type == null ? CODE : type;
		}// Constructor

		public int size() {
			return endLoc - startLoc;
		}// size

		public String toString() {
			return String.format("%04X : %04X ; (%04X) %s", startLoc, endLoc, size(), type);
		}// toString

		@Override
		public int compareTo(CodeFragment ct0) {
			return startLoc - ct0.startLoc;
		}

	}// class CodeType

	class CodeFragmentModel extends ArrayList<CodeFragment> implements ListModel {

		@Override
		public void addListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub

		}
		
		@Override
		public Object getElementAt(int index) {
			// TODO Auto-generated method stub
			return this.get(index);
		}
		
		

		@Override
		public int getSize() {
			return this.size();
		}

		@Override
		public void removeListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub

		}

	}
	// class CodeTypeModel extends AbstractListModel implements ComboBoxModel {
	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	// List<CodeType> modelItemList;
	// DirEntry selection = null;
	//
	// public FileCpmModel() {
	// modelItemList = new ArrayList<CodeType>();
	// }// Constructor
	//
	// public void add(DirEntry item) {
	// modelItemList.add(item);
	// }// add
	//
	// @Override
	// public DirEntry getElementAt(int index) {
	// return modelItemList.get(index);
	// }// getElementAt
	//
	// @Override
	// public int getSize() {
	// return modelItemList.size();
	// }// getSize
	//
	// @Override
	// public CodeType getSelectedItem() {
	// return selection;
	// }// getSelectedItem
	//
	// @Override
	// public void setSelectedItem(Object arg0) {
	// if (arg0 instanceof CodeType) {
	// selection = (CodeType) arg0;
	// } else {
	// selection = new CodeType((String) arg0, -1);
	// }// if
	// }// setSelectedItem
	// }// class FileCpmModel

}
