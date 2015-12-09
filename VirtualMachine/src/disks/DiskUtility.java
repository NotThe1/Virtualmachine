package disks;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import java.awt.GridBagLayout;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JTabbedPane;
import javax.swing.JButton;

import myComponents.Hex64KSpinner;

import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Dimension;

import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.jgoodies.forms.factories.DefaultComponentFactory;

import disks.NativeDiskTool.DirEntry;

import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.Component;

import javax.swing.Box;

public class DiskUtility implements ActionListener, ChangeListener {

	private JFrame frmDiskUtility;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DiskUtility window = new DiskUtility();
					window.frmDiskUtility.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	private void displayPhysicalSector() {
		spinnerHeadHex.setValue(diskDrive.getCurrentHead());
		spinnerTrackHex.setValue(diskDrive.getCurrentTrack());
		spinnerSectorHex.setValue(diskDrive.getCurrentSector());
		currentAbsoluteSector = diskDrive.getCurrentAbsoluteSector();
		lblAbsoluteSector.setText(String.format(moduloFormat, currentAbsoluteSector));

		aSector = diskDrive.read();

		clearSectorDisplay();
		docPhysical = txtSector.getDocument();

		try {
			for (int i = 0; i < linesToDisplay; i++) {
				docPhysical.insertString(docPhysical.getLength(), formatLine(i), null);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		txtSector.setCaretPosition(0);

	}
	
	private void clearSectorDisplay() {
		if (docPhysical == null) {
			return;
		}
		try {
			docPhysical.remove(0, docPhysical.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// doc = null;
	}
	private String formatLine(int lineNumber) {
		byte target;
		StringBuilder sbHex = new StringBuilder();
		StringBuilder sbDot = new StringBuilder();
		sbHex.append(String.format("%04X: ", lineNumber * CHARACTERS_PER_LINE));
		int indexIntoSector = lineNumber % linesToDisplay;
		for (int i = 0; i < CHARACTERS_PER_LINE; i++) {
			// target = aSector[(lineNumber * CHARACTERS_PER_LINE) + i];
			target = aSector[(indexIntoSector * CHARACTERS_PER_LINE) + i];
			sbHex.append(String.format("%02X ", target));

			sbDot.append((target >= 0x20 && target <= 0x7F) ? (char) target : ".");
			if (i == 7) {
				sbHex.append(" ");
				sbDot.append(" ");
			}
			// sbDot.append((char)target);
		}
		sbHex.append(" ");
		sbDot.append(String.format("%n"));
		return sbHex.toString() + sbDot.toString();
	}


	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
	private void setFileNameInformation(String selectedAbsolutePath) {
		int index = selectedAbsolutePath.lastIndexOf(File.separator);
		fileName = (selectedAbsolutePath.substring(index + 1, selectedAbsolutePath.length()));
		fileName = fileName.trim().toUpperCase();
//		lblFileName.setText(fileName);
		String[] nameParts = fileName.split("\\.");
		diskType = nameParts[1];
//		lblFileName.setToolTipText(selectedAbsolutePath.substring(0, index));
		fileNamePath = selectedAbsolutePath.substring(0, index);
	}

	private void loadDiskDrive(String selectedAbsolutePath) {
		diskDrive = new RawDiskDrive(selectedAbsolutePath);
		setFileNameInformation(selectedAbsolutePath);
		haveDisk(true);
	}

	private String getDisk() {
		String fileLocation = ".";
		Path sourcePath = Paths.get(fileLocation, "Disks");
		JFileChooser chooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		chooser.setMultiSelectionEnabled(false);

		String[] fileTypes = DiskMetrics.getDiskTypes();
		String[] fileDesc = DiskMetrics.getDiskDescriptionss();
		for (int i = 0; i < fileTypes.length; i++) {
			chooser.addChoosableFileFilter(new FileNameExtensionFilter(fileDesc[i], fileTypes[i]));
		}
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showDialog(null, "Select the disk") != JFileChooser.APPROVE_OPTION) {
			return null;
		}// if
		File selectedFile = chooser.getSelectedFile();
		javax.swing.filechooser.FileFilter chooserFilter = chooser.getFileFilter();

		if ((!chooserFilter.accept(selectedFile)) | (!selectedFile.exists())) {
			JOptionPane.showMessageDialog(null, "Not valid file, try again", "Adding disk",
					JOptionPane.WARNING_MESSAGE);
			return null;
		}// if

		return chooser.getSelectedFile().getAbsolutePath().toString();
	}// getDisk()

	private void setSpinnerLimits() {

		((SpinnerNumberModel) spinnerHeadDecimal.getModel()).setMaximum(heads - 1);
		((SpinnerNumberModel) spinnerHeadHex.getModel()).setMaximum(heads - 1);

		((SpinnerNumberModel) spinnerTrackDecimal.getModel()).setMaximum(tracksPerHead - 1);
		((SpinnerNumberModel) spinnerTrackHex.getModel()).setMaximum(tracksPerHead - 1);

		((SpinnerNumberModel) spinnerSectorDecimal.getModel()).setMaximum(sectorsPerTrack);
		((SpinnerNumberModel) spinnerSectorHex.getModel()).setMaximum(sectorsPerTrack);
	}

	private void setNumberBase() {
		moduloFormat = mnuToolsNumberBase.isSelected() ? "%X" : "%,d";
		refreshDisplay();
	}

	// private void refreshDiskMetrics(DiskMetrics diskMetrics){
	//
	// }
	private void refreshDisplay() {
		boolean displayHex = mnuToolsNumberBase.isSelected();
		spinnerHeadHex.setVisible(displayHex);
		spinnerHeadDecimal.setVisible(!displayHex);
		spinnerTrackHex.setVisible(displayHex);
		spinnerTrackDecimal.setVisible(!displayHex);
		spinnerSectorHex.setVisible(displayHex);
		spinnerSectorDecimal.setVisible(!displayHex);

		lblAbsoluteSector.setText(String.format(moduloFormat, currentAbsoluteSector));
		lblHeads.setText(String.format(moduloFormat, heads));
		lblTracksPerHead.setText(String.format(moduloFormat, tracksPerHead));
		lblSectorsPerTrack.setText(String.format(moduloFormat, sectorsPerTrack));
		lblBytesPerSector.setText(String.format(moduloFormat, bytesPerSector));
		lblTotalTracks.setText(String.format(moduloFormat, totalTracks));
		lblTotalSectors.setText(String.format(moduloFormat, totalSectors));
		lblTracksBeforeDirectory.setText(String.format(moduloFormat, tracksBeforeDirectory));
		lblLogicalBlockSizeInSectors.setText(String.format(moduloFormat, blockSizeInSectors));
		lblMaxDirectoryEntry.setText(String.format(moduloFormat, maxDirectoryEntry));
		lblMaxBlockNumber.setText(String.format(moduloFormat, maxBlockNumber));
	}
	
	private void refreshMetrics(boolean state){
		// state = true we have a valid disk
			if (diskMetrics != null) {
			diskMetrics = null;
		}
		if (state) {
			diskMetrics = DiskMetrics.diskMetric(diskType);
		}

		currentHead = 0;
		currentTrack = 0;
		;
		currentSector = 1;
		currentAbsoluteSector = 0;

		heads = state ? diskMetrics.heads : 0;
		tracksPerHead = state ? diskMetrics.tracksPerHead : 0;
		sectorsPerTrack = state ? diskMetrics.sectorsPerTrack : 0;
		bytesPerSector = state ? diskMetrics.bytesPerSector : 0;
		totalTracks = state ? heads * tracksPerHead : 0;
		totalSectors = state ? diskMetrics.getTotalSectorsOnDisk() : 0;
		tracksBeforeDirectory = state ? diskMetrics.getOFS() : 0;
		blockSizeInSectors = state ? diskMetrics.directoryBlockCount : 0;
		setSpinnerLimits();
		;
		maxDirectoryEntry = state ? diskMetrics.getDRM() : 0;
		maxBlockNumber = state ? diskMetrics.getDSM() : 0;
		lblFileName.setText(state?fileName:"<No File Active>");
		lblFileName.setToolTipText(state?fileNamePath:"<No File Active>");
		linesToDisplay = state?bytesPerSector /CHARACTERS_PER_LINE:0;

		
}

	private void haveDisk(boolean state) {
		// handle the menus
		mnuFileNew.setEnabled(!state);
		mnuFileLoad.setEnabled(!state);
		mnuFileClose.setEnabled(state);
		mnuFileSave.setEnabled(state);
		mnuFileSaveAs.setEnabled(state);
		mnuCbBootable.setEnabled(state);
		
		refreshMetrics(state);
		refreshDisplay();
		if (state){
		diskDrive.setCurrentAbsoluteSector(0);
		displayPhysicalSector();
		}

	}

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		String menuChoice = "menuChoice";
		selectedAbsolutePath = null;
		;
		switch (ae.getActionCommand()) {
		case AC_MNU_FILE_NEW:
			menuChoice = AC_MNU_FILE_NEW;
			break;
		case AC_MNU_FILE_LOAD:
			menuChoice = AC_MNU_FILE_LOAD;
			selectedAbsolutePath = getDisk();
			if (selectedAbsolutePath == null) {
				return;
			}// if
			loadDiskDrive(selectedAbsolutePath);
			break;
		case AC_MNU_FILE_CLOSE:
			menuChoice = AC_MNU_FILE_CLOSE;
			haveDisk(false);
			break;
		case AC_MNU_FILE_SAVE:
			menuChoice = AC_MNU_FILE_SAVE;
			break;
		case AC_MNU_FILE_SAVE_AS:
			menuChoice = AC_MNU_FILE_SAVE_AS;
			break;
		case AC_MNU_FILE_EXIT:
			appClose();
			break;
		case AC_MNU_TOOLS_NUMBER_BASE:
			// menuChoice = AC_MNU_TOOLS_NUMBER_BASE;
			setNumberBase();
		case AC_MNU_CP_BOOTABLE:
		menuChoice = AC_MNU_CP_BOOTABLE;

			// Physical View
		case AC_BTN_DISPLAY_PHYSICAL:
			menuChoice = AC_BTN_DISPLAY_PHYSICAL;
			break;
		case AC_BTN_FIRST:
			menuChoice = AC_BTN_FIRST;
			break;
		case AC_BTN_PREVIOUS:
			menuChoice = AC_BTN_PREVIOUS;
			break;
		case AC_BTN_NEXT:
			menuChoice = AC_BTN_NEXT;
			break;
		case AC_BTN_LAST:
			menuChoice = AC_BTN_LAST;
			break;

		// File View
		case AC_CB_FILE_NAMES:
			menuChoice = AC_CB_FILE_NAMES;
			break;

		// Copy
		case AC_CB_CPM_TARGET:
			menuChoice = AC_CB_CPM_TARGET;
			break;
		case AC_BTN_NATIVE_SOURCE:
			menuChoice = AC_BTN_NATIVE_SOURCE;
			break;
		case AC_BTN_COPY_TO_CPM:
			menuChoice = AC_BTN_COPY_TO_CPM;
			break;

		default:
			menuChoice = " You hit the default in actionPerformed()";

		}
		System.out.println(menuChoice);

	} // -----------------------------------------------------------------------------------------------------

	private void appInit() {
		String headerString = "      00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F";
		JLabel lblHeaderString = new JLabel(headerString);
		lblHeaderString.setForeground(Color.blue);
		lblHeaderString.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPanePhysical.setColumnHeaderView(lblHeaderString);

		JLabel lblHeaderString1 = new JLabel(headerString);
		lblHeaderString1.setForeground(Color.BLUE);
		lblHeaderString1.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneFile.setColumnHeaderView(lblHeaderString1);
		mnuToolsNumberBase.setSelected(true);

		spinnerHeadDecimal.setModel(spinnerHeadHex.getModel());
		spinnerTrackDecimal.setModel(spinnerTrackHex.getModel());
		spinnerSectorDecimal.setModel(spinnerSectorHex.getModel());
		setNumberBase();
		haveDisk(false);
	}

	private void appClose() {
		System.exit(-1);
	}

	// -----------------------------------------------------------------------------------------------------

	/**
	 * Create the application.
	 */
	public DiskUtility() {
		initialize();
		appInit();
	}

	// +++++++++++++++++ Constants ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Main
	private final static String AC_MNU_FILE_NEW = "MenuFileNew";
	private final static String AC_MNU_FILE_LOAD = "MenuFileLoad";
	private final static String AC_MNU_FILE_CLOSE = "MenuFileClose";
	private final static String AC_MNU_FILE_SAVE = "MenuFileSave";
	private final static String AC_MNU_FILE_SAVE_AS = "MenuFileSaveAs";
	private final static String AC_MNU_FILE_EXIT = "MenuFileExit";
	private final static String AC_MNU_TOOLS_NUMBER_BASE = "MenuToolsNumberBase";

	private final static String AC_MNU_CP_BOOTABLE = "MenuBootable";

	// Physical View
	private final static String AC_BTN_DISPLAY_PHYSICAL = "btnDisplayPhysical";
	private final static String AC_BTN_FIRST = "btnFirst";
	private final static String AC_BTN_PREVIOUS = "btnPrevious";
	private final static String AC_BTN_NEXT = "btnNext";
	private final static String AC_BTN_LAST = "btnLast";
	// File View
	private final static String AC_CB_FILE_NAMES = "cbFileNames";
	private JScrollPane scrollPaneFile;
	private JScrollPane scrollPanePhysical;
	// Copy
	private final static String AC_CB_CPM_TARGET = "cbCpmTarget";
	private final static String AC_BTN_NATIVE_SOURCE = "btnNativeSource";
	private final static String AC_BTN_COPY_TO_CPM = "btnCopyToCPM";
	
	//misc
	private final static int CHARACTERS_PER_LINE = 16;


	// +++++++++++++++++ Constants ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private String diskType;
	private DiskMetrics diskMetrics;
	private RawDiskDrive diskDrive;
	private String fileName ;
	private String fileNamePath;
	private String selectedAbsolutePath;

	private int currentHead;
	private int currentTrack;
	private int currentSector;
	private int currentAbsoluteSector;

	private int heads;
	private int tracksPerHead;
	private int sectorsPerTrack;
	private int bytesPerSector;
	private int tracksBeforeDirectory;
	private int blockSizeInSectors;
	private int totalTracks;
	private int totalSectors;
	private int maxDirectoryEntry;
	private int maxBlockNumber;
	
	private byte[] aSector;
	private Document docPhysical;
	private int linesToDisplay;


	// private int recordCount = 0;
	//
	//
	// private int directoryEntriesPerSector;
	// private int numberOfDirectorySectors;
	// private int blockZeroBias;

	private String moduloFormat;
	private JCheckBoxMenuItem mnuToolsNumberBase;
	private JCheckBoxMenuItem mnuCbBootable;
	private Hex64KSpinner spinnerHeadHex;
	private JSpinner spinnerHeadDecimal;
	private Hex64KSpinner spinnerTrackHex;
	private JSpinner spinnerTrackDecimal;
	private Hex64KSpinner spinnerSectorHex;
	private JSpinner spinnerSectorDecimal;
	private JMenuItem mnuFileNew;
	private JMenuItem mnuFileLoad;
	private JMenuItem mnuFileClose;
	private JMenuItem mnuFileSave;
	private JMenuItem mnuFileSaveAs;
	private JLabel lblAbsoluteSector;
	private JLabel lblFileName;
	private JLabel lblMaxBlockNumber;
	private JLabel lblMaxDirectoryEntry;
	private JLabel lblLogicalBlockSizeInSectors;
	private JLabel lblTracksBeforeDirectory;
	private JLabel lblTotalSectors;
	private JLabel lblTotalTracks;
	private JLabel lblBytesPerSector;
	private JLabel lblSectorsPerTrack;
	private JLabel lblTracksPerHead;
	private JLabel lblHeads;
	private JTextArea txtSector;

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDiskUtility = new JFrame();
		frmDiskUtility.setTitle("DIsk Utility");
		frmDiskUtility.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				appClose();
			}
		});
		frmDiskUtility.setBounds(100, 100, 772, 846);
		frmDiskUtility.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmDiskUtility.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		mnuFileNew = new JMenuItem("New Disk");
		mnuFileNew.setActionCommand(AC_MNU_FILE_NEW);
		mnuFileNew.addActionListener(this);
		mnuFile.add(mnuFileNew);

		mnuFileLoad = new JMenuItem("Load Disk ...");
		mnuFileLoad.setActionCommand(AC_MNU_FILE_LOAD);
		mnuFileLoad.addActionListener(this);
		mnuFile.add(mnuFileLoad);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		mnuFileClose = new JMenuItem("Close");
		mnuFileClose.setActionCommand(AC_MNU_FILE_CLOSE);
		mnuFileClose.addActionListener(this);
		mnuFile.add(mnuFileClose);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);

		mnuFileSave = new JMenuItem("Save");
		mnuFileSave.setActionCommand(AC_MNU_FILE_SAVE);
		mnuFile.add(mnuFileSave);
		mnuFileSave.addActionListener(this);

		mnuFileSaveAs = new JMenuItem("Save As ...");
		mnuFileSaveAs.setActionCommand(AC_MNU_FILE_SAVE_AS);
		mnuFileSaveAs.addActionListener(this);
		mnuFile.add(mnuFileSaveAs);

		JSeparator separator_2 = new JSeparator();
		mnuFile.add(separator_2);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setActionCommand(AC_MNU_FILE_EXIT);
		mnuFileExit.addActionListener(this);
		mnuFile.add(mnuFileExit);

		JMenu mnuTools = new JMenu("Tools");
		menuBar.add(mnuTools);

		mnuToolsNumberBase = new JCheckBoxMenuItem("Hex Display");
		mnuToolsNumberBase.setActionCommand(AC_MNU_TOOLS_NUMBER_BASE);
		mnuToolsNumberBase.addActionListener(this);
		mnuToolsNumberBase.setSelected(true);
		mnuTools.add(mnuToolsNumberBase);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		menuBar.add(horizontalStrut);

		mnuCbBootable = new JCheckBoxMenuItem("Bootable");
		mnuCbBootable.setActionCommand(AC_MNU_CP_BOOTABLE);
		mnuCbBootable.addActionListener(this);
		mnuCbBootable.setSelected(true);
		mnuCbBootable.setFont(new Font("Arial", Font.PLAIN, 12));
		menuBar.add(mnuCbBootable);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frmDiskUtility.getContentPane().setLayout(gridBagLayout);

		JPanel panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.insets = new Insets(0, 0, 5, 0);
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 0;
		frmDiskUtility.getContentPane().add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		lblFileName = new JLabel("<None>");
		lblFileName.setFont(new Font("Arial", Font.BOLD, 18));
		GridBagConstraints gbc_lblFileName = new GridBagConstraints();
		gbc_lblFileName.insets = new Insets(0, 0, 5, 0);
		gbc_lblFileName.gridx = 0;
		gbc_lblFileName.gridy = 0;
		panelMain.add(lblFileName, gbc_lblFileName);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		frmDiskUtility.getContentPane().add(tabbedPane, gbc_tabbedPane);

		JPanel tabPhysical = new JPanel();
		tabbedPane.addTab("Physical View", null, tabPhysical, null);
		GridBagLayout gbl_tabPhysical = new GridBagLayout();
		gbl_tabPhysical.columnWidths = new int[] { 0, 0, 0 };
		gbl_tabPhysical.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_tabPhysical.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_tabPhysical.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		tabPhysical.setLayout(gbl_tabPhysical);

		JPanel panelHTS = new JPanel();
		GridBagConstraints gbc_panelHTS = new GridBagConstraints();
		gbc_panelHTS.insets = new Insets(0, 0, 5, 5);
		gbc_panelHTS.fill = GridBagConstraints.BOTH;
		gbc_panelHTS.gridx = 0;
		gbc_panelHTS.gridy = 0;
		tabPhysical.add(panelHTS, gbc_panelHTS);
		GridBagLayout gbl_panelHTS = new GridBagLayout();
		gbl_panelHTS.columnWidths = new int[] { 5, 0, 0, 0, 20, 0, 0, 0, 20, 0, 0, 0, 10, 0, 0 };
		gbl_panelHTS.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelHTS.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				1.0, Double.MIN_VALUE };
		gbl_panelHTS.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelHTS.setLayout(gbl_panelHTS);

		JLabel lblHead = new JLabel("Head");
		GridBagConstraints gbc_lblHead = new GridBagConstraints();
		gbc_lblHead.insets = new Insets(0, 0, 5, 5);
		gbc_lblHead.gridx = 1;
		gbc_lblHead.gridy = 0;
		panelHTS.add(lblHead, gbc_lblHead);

		spinnerHeadHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerHeadHex = new GridBagConstraints();
		gbc_spinnerHeadHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerHeadHex.gridx = 2;
		gbc_spinnerHeadHex.gridy = 0;
		panelHTS.add(spinnerHeadHex, gbc_spinnerHeadHex);

		spinnerHeadDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerHeadDecimal = new GridBagConstraints();
		gbc_spinnerHeadDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerHeadDecimal.gridx = 3;
		gbc_spinnerHeadDecimal.gridy = 0;
		panelHTS.add(spinnerHeadDecimal, gbc_spinnerHeadDecimal);

		JLabel lblTrack = new JLabel("Track");
		GridBagConstraints gbc_lblTrack = new GridBagConstraints();
		gbc_lblTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblTrack.gridx = 5;
		gbc_lblTrack.gridy = 0;
		panelHTS.add(lblTrack, gbc_lblTrack);

		spinnerTrackHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerTrackHex = new GridBagConstraints();
		gbc_spinnerTrackHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerTrackHex.gridx = 6;
		gbc_spinnerTrackHex.gridy = 0;
		panelHTS.add(spinnerTrackHex, gbc_spinnerTrackHex);

		spinnerTrackDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerTrackDecimal = new GridBagConstraints();
		gbc_spinnerTrackDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerTrackDecimal.gridx = 7;
		gbc_spinnerTrackDecimal.gridy = 0;
		panelHTS.add(spinnerTrackDecimal, gbc_spinnerTrackDecimal);

		JLabel lblSector = new JLabel("Sector");
		GridBagConstraints gbc_lblSector = new GridBagConstraints();
		gbc_lblSector.insets = new Insets(0, 0, 5, 5);
		gbc_lblSector.gridx = 9;
		gbc_lblSector.gridy = 0;
		panelHTS.add(lblSector, gbc_lblSector);

		spinnerSectorHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerSectorHex = new GridBagConstraints();
		gbc_spinnerSectorHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerSectorHex.gridx = 10;
		gbc_spinnerSectorHex.gridy = 0;
		panelHTS.add(spinnerSectorHex, gbc_spinnerSectorHex);

		spinnerSectorDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerSectorDecimal = new GridBagConstraints();
		gbc_spinnerSectorDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerSectorDecimal.gridx = 11;
		gbc_spinnerSectorDecimal.gridy = 0;
		panelHTS.add(spinnerSectorDecimal, gbc_spinnerSectorDecimal);

		JButton btnDisplayPhysical = new JButton("Display Sector");
		btnDisplayPhysical.setActionCommand(AC_BTN_DISPLAY_PHYSICAL);
		btnDisplayPhysical.addActionListener(this);
		GridBagConstraints gbc_btnDisplayPhysical = new GridBagConstraints();
		gbc_btnDisplayPhysical.insets = new Insets(0, 0, 5, 0);
		gbc_btnDisplayPhysical.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnDisplayPhysical.gridx = 13;
		gbc_btnDisplayPhysical.gridy = 0;
		panelHTS.add(btnDisplayPhysical, gbc_btnDisplayPhysical);

		scrollPanePhysical = new JScrollPane();
		scrollPanePhysical.setPreferredSize(new Dimension(680, 400));
		GridBagConstraints gbc_scrollPanePhysical = new GridBagConstraints();
		gbc_scrollPanePhysical.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPanePhysical.fill = GridBagConstraints.BOTH;
		gbc_scrollPanePhysical.gridx = 0;
		gbc_scrollPanePhysical.gridy = 1;
		tabPhysical.add(scrollPanePhysical, gbc_scrollPanePhysical);

		txtSector = new JTextArea();
		txtSector.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPanePhysical.setViewportView(txtSector);

		JPanel panelLogicalSector = new JPanel();
		GridBagConstraints gbc_panelLogicalSector = new GridBagConstraints();
		gbc_panelLogicalSector.insets = new Insets(0, 0, 0, 5);
		gbc_panelLogicalSector.gridx = 0;
		gbc_panelLogicalSector.gridy = 2;
		tabPhysical.add(panelLogicalSector, gbc_panelLogicalSector);
		GridBagLayout gbl_panelLogicalSector = new GridBagLayout();
		gbl_panelLogicalSector.columnWidths = new int[] { 0, 0 };
		gbl_panelLogicalSector.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelLogicalSector.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelLogicalSector.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelLogicalSector.setLayout(gbl_panelLogicalSector);

		lblAbsoluteSector = new JLabel("000000000");
		lblAbsoluteSector.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblAbsoluteSector = new GridBagConstraints();
		gbc_lblAbsoluteSector.insets = new Insets(0, 0, 5, 0);
		gbc_lblAbsoluteSector.anchor = GridBagConstraints.BASELINE;
		gbc_lblAbsoluteSector.gridx = 0;
		gbc_lblAbsoluteSector.gridy = 0;
		panelLogicalSector.add(lblAbsoluteSector, gbc_lblAbsoluteSector);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		panelLogicalSector.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton btnFirst = new JButton("<<");
		btnFirst.setActionCommand(AC_BTN_FIRST);
		btnFirst.addActionListener(this);
		btnFirst.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_btnFirst = new GridBagConstraints();
		gbc_btnFirst.insets = new Insets(0, 0, 0, 5);
		gbc_btnFirst.gridx = 0;
		gbc_btnFirst.gridy = 0;
		panel.add(btnFirst, gbc_btnFirst);

		JButton btnPrevious = new JButton("<");
		btnPrevious.setActionCommand(AC_BTN_PREVIOUS);
		btnPrevious.addActionListener(this);
		btnPrevious.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_btnPrevious = new GridBagConstraints();
		gbc_btnPrevious.insets = new Insets(0, 0, 0, 5);
		gbc_btnPrevious.gridx = 1;
		gbc_btnPrevious.gridy = 0;
		panel.add(btnPrevious, gbc_btnPrevious);

		JButton btnNext = new JButton(">");
		btnNext.setActionCommand(AC_BTN_NEXT);
		btnNext.addActionListener(this);
		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.insets = new Insets(0, 0, 0, 5);
		gbc_btnNext.gridx = 2;
		gbc_btnNext.gridy = 0;
		panel.add(btnNext, gbc_btnNext);

		JButton btnLast = new JButton(">>");
		btnLast.setActionCommand(AC_BTN_LAST);
		btnLast.addActionListener(this);
		btnLast.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_btnLast = new GridBagConstraints();
		gbc_btnLast.gridx = 3;
		gbc_btnLast.gridy = 0;
		panel.add(btnLast, gbc_btnLast);

		JPanel tabFile = new JPanel();
		tabbedPane.addTab("File View", null, tabFile, null);
		GridBagLayout gbl_tabFile = new GridBagLayout();
		gbl_tabFile.columnWidths = new int[] { 0, 0, 0 };
		gbl_tabFile.rowHeights = new int[] { 0, 0 };
		gbl_tabFile.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_tabFile.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		tabFile.setLayout(gbl_tabFile);

		JPanel panelFileDetails = new JPanel();
		GridBagConstraints gbc_panelFileDetails = new GridBagConstraints();
		gbc_panelFileDetails.insets = new Insets(0, 0, 0, 5);
		gbc_panelFileDetails.fill = GridBagConstraints.BOTH;
		gbc_panelFileDetails.gridx = 0;
		gbc_panelFileDetails.gridy = 0;
		tabFile.add(panelFileDetails, gbc_panelFileDetails);
		GridBagLayout gbl_panelFileDetails = new GridBagLayout();
		gbl_panelFileDetails.columnWidths = new int[] { 0, 0 };
		gbl_panelFileDetails.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelFileDetails.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelFileDetails.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelFileDetails.setLayout(gbl_panelFileDetails);

		JPanel panelFileSelection = new JPanel();
		GridBagConstraints gbc_panelFileSelection = new GridBagConstraints();
		gbc_panelFileSelection.insets = new Insets(0, 0, 5, 0);
		gbc_panelFileSelection.fill = GridBagConstraints.BOTH;
		gbc_panelFileSelection.gridx = 0;
		gbc_panelFileSelection.gridy = 0;
		panelFileDetails.add(panelFileSelection, gbc_panelFileSelection);
		GridBagLayout gbl_panelFileSelection = new GridBagLayout();
		gbl_panelFileSelection.columnWidths = new int[] { 0, 0, 100, 0, 0, 0, 50, 0, 25, 0, 0 };
		gbl_panelFileSelection.rowHeights = new int[] { 0, 0 };
		gbl_panelFileSelection.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panelFileSelection.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFileSelection.setLayout(gbl_panelFileSelection);

		JComboBox cbFileNames = new JComboBox();
		GridBagConstraints gbc_cbFileNames = new GridBagConstraints();
		gbc_cbFileNames.insets = new Insets(0, 0, 0, 5);
		gbc_cbFileNames.anchor = GridBagConstraints.NORTHWEST;
		gbc_cbFileNames.gridx = 0;
		gbc_cbFileNames.gridy = 0;
		panelFileSelection.add(cbFileNames, gbc_cbFileNames);
		cbFileNames.setActionCommand(AC_CB_FILE_NAMES);
		cbFileNames.addActionListener(this);
		cbFileNames.setEnabled(true);
		cbFileNames.setEditable(false);
		cbFileNames.setMinimumSize(new Dimension(0, 0));

		JLabel lblRecordCount = new JLabel("0");
		GridBagConstraints gbc_lblRecordCount = new GridBagConstraints();
		gbc_lblRecordCount.insets = new Insets(0, 0, 0, 5);
		gbc_lblRecordCount.anchor = GridBagConstraints.EAST;
		gbc_lblRecordCount.gridx = 3;
		gbc_lblRecordCount.gridy = 0;
		panelFileSelection.add(lblRecordCount, gbc_lblRecordCount);
		lblRecordCount.setFont(new Font("Arial", Font.BOLD, 15));

		JLabel lblNewLabel_2 = new JLabel("Record Count");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 4;
		gbc_lblNewLabel_2.gridy = 0;
		panelFileSelection.add(lblNewLabel_2, gbc_lblNewLabel_2);
		lblNewLabel_2.setFont(new Font("Arial", Font.PLAIN, 13));

		JLabel lblReadOnly = new JLabel("Read Only");
		GridBagConstraints gbc_lblReadOnly = new GridBagConstraints();
		gbc_lblReadOnly.insets = new Insets(0, 0, 0, 5);
		gbc_lblReadOnly.gridx = 7;
		gbc_lblReadOnly.gridy = 0;
		panelFileSelection.add(lblReadOnly, gbc_lblReadOnly);
		lblReadOnly.setForeground(Color.RED);
		lblReadOnly.setFont(new Font("Arial", Font.BOLD, 15));

		JLabel lblSystemFile = new JLabel("System File");
		GridBagConstraints gbc_lblSystemFile = new GridBagConstraints();
		gbc_lblSystemFile.gridx = 9;
		gbc_lblSystemFile.gridy = 0;
		panelFileSelection.add(lblSystemFile, gbc_lblSystemFile);
		lblSystemFile.setForeground(Color.RED);
		lblSystemFile.setFont(new Font("Arial", Font.BOLD, 15));

		scrollPaneFile = new JScrollPane();
		scrollPaneFile.setPreferredSize(new Dimension(680, 400));
		GridBagConstraints gbc_scrollPaneFile = new GridBagConstraints();
		gbc_scrollPaneFile.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneFile.gridx = 0;
		gbc_scrollPaneFile.gridy = 2;
		panelFileDetails.add(scrollPaneFile, gbc_scrollPaneFile);

		JTextArea txtFile = new JTextArea();
		txtFile.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneFile.setViewportView(txtFile);

		JPanel tabDirectory = new JPanel();
		tabbedPane.addTab("Directory View", null, tabDirectory, null);
		GridBagLayout gbl_tabDirectory = new GridBagLayout();
		gbl_tabDirectory.columnWidths = new int[] { 0, 0, 0 };
		gbl_tabDirectory.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_tabDirectory.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_tabDirectory.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		tabDirectory.setLayout(gbl_tabDirectory);

		JPanel panelDirRaw = new JPanel();
		GridBagConstraints gbc_panelDirRaw = new GridBagConstraints();
		gbc_panelDirRaw.insets = new Insets(0, 0, 5, 5);
		gbc_panelDirRaw.fill = GridBagConstraints.VERTICAL;
		gbc_panelDirRaw.gridx = 0;
		gbc_panelDirRaw.gridy = 0;
		tabDirectory.add(panelDirRaw, gbc_panelDirRaw);
		GridBagLayout gbl_panelDirRaw = new GridBagLayout();
		gbl_panelDirRaw.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelDirRaw.rowHeights = new int[] { 0, 0 };
		gbl_panelDirRaw.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelDirRaw.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelDirRaw.setLayout(gbl_panelDirRaw);

		JPanel panelRawUser = new JPanel();
		panelRawUser.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelRawUser = new GridBagConstraints();
		gbc_panelRawUser.insets = new Insets(0, 0, 0, 5);
		gbc_panelRawUser.anchor = GridBagConstraints.WEST;
		gbc_panelRawUser.fill = GridBagConstraints.VERTICAL;
		gbc_panelRawUser.gridx = 0;
		gbc_panelRawUser.gridy = 0;
		panelDirRaw.add(panelRawUser, gbc_panelRawUser);
		GridBagLayout gbl_panelRawUser = new GridBagLayout();
		gbl_panelRawUser.columnWidths = new int[] { 0, 0 };
		gbl_panelRawUser.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelRawUser.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelRawUser.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelRawUser.setLayout(gbl_panelRawUser);

		JLabel lblUser_1 = new JLabel("User [0]");
		lblUser_1.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblUser_1 = new GridBagConstraints();
		gbc_lblUser_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblUser_1.gridx = 0;
		gbc_lblUser_1.gridy = 0;
		panelRawUser.add(lblUser_1, gbc_lblUser_1);

		JLabel lblRawUser = new JLabel("00");
		lblRawUser.setForeground(new Color(0, 0, 255));
		lblRawUser.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawUser = new GridBagConstraints();
		gbc_lblRawUser.gridx = 0;
		gbc_lblRawUser.gridy = 1;
		panelRawUser.add(lblRawUser, gbc_lblRawUser);

		JPanel panelRawName = new JPanel();
		panelRawName.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelRawName = new GridBagConstraints();
		gbc_panelRawName.insets = new Insets(0, 0, 0, 5);
		gbc_panelRawName.anchor = GridBagConstraints.WEST;
		gbc_panelRawName.fill = GridBagConstraints.VERTICAL;
		gbc_panelRawName.gridx = 1;
		gbc_panelRawName.gridy = 0;
		panelDirRaw.add(panelRawName, gbc_panelRawName);
		GridBagLayout gbl_panelRawName = new GridBagLayout();
		gbl_panelRawName.columnWidths = new int[] { 27, 0 };
		gbl_panelRawName.rowHeights = new int[] { 15, 18, 0 };
		gbl_panelRawName.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelRawName.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelRawName.setLayout(gbl_panelRawName);

		JLabel lblName_1 = new JLabel("Name [1-8]");
		lblName_1.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblName_1 = new GridBagConstraints();
		gbc_lblName_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblName_1.gridx = 0;
		gbc_lblName_1.gridy = 0;
		panelRawName.add(lblName_1, gbc_lblName_1);

		JLabel lblRawName = new JLabel("00 00 00 00 00 00 00 00");
		lblRawName.setForeground(Color.BLUE);
		lblRawName.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawName = new GridBagConstraints();
		gbc_lblRawName.gridx = 0;
		gbc_lblRawName.gridy = 1;
		panelRawName.add(lblRawName, gbc_lblRawName);

		JPanel panelRawType = new JPanel();
		panelRawType.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelRawType = new GridBagConstraints();
		gbc_panelRawType.insets = new Insets(0, 0, 0, 5);
		gbc_panelRawType.anchor = GridBagConstraints.WEST;
		gbc_panelRawType.fill = GridBagConstraints.VERTICAL;
		gbc_panelRawType.gridx = 2;
		gbc_panelRawType.gridy = 0;
		panelDirRaw.add(panelRawType, gbc_panelRawType);
		GridBagLayout gbl_panelRawType = new GridBagLayout();
		gbl_panelRawType.columnWidths = new int[] { 0, 0 };
		gbl_panelRawType.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelRawType.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelRawType.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelRawType.setLayout(gbl_panelRawType);

		JLabel lblType_1 = new JLabel("Type [9-11]");
		lblType_1.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblType_1 = new GridBagConstraints();
		gbc_lblType_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblType_1.gridx = 0;
		gbc_lblType_1.gridy = 0;
		panelRawType.add(lblType_1, gbc_lblType_1);

		JLabel lblRawType = new JLabel("00 00 00");
		lblRawType.setForeground(Color.BLUE);
		lblRawType.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawType = new GridBagConstraints();
		gbc_lblRawType.gridx = 0;
		gbc_lblRawType.gridy = 1;
		panelRawType.add(lblRawType, gbc_lblRawType);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.insets = new Insets(0, 0, 0, 5);
		gbc_panel_4.anchor = GridBagConstraints.WEST;
		gbc_panel_4.fill = GridBagConstraints.VERTICAL;
		gbc_panel_4.gridx = 3;
		gbc_panel_4.gridy = 0;
		panelDirRaw.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 0, 0 };
		gbl_panel_4.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_4.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		JLabel lblEx_1 = new JLabel("EX [12]");
		lblEx_1.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblEx_1 = new GridBagConstraints();
		gbc_lblEx_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblEx_1.gridx = 0;
		gbc_lblEx_1.gridy = 0;
		panel_4.add(lblEx_1, gbc_lblEx_1);

		JLabel lblRawEX = new JLabel("00");
		lblRawEX.setForeground(Color.BLUE);
		lblRawEX.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawEX = new GridBagConstraints();
		gbc_lblRawEX.gridx = 0;
		gbc_lblRawEX.gridy = 1;
		panel_4.add(lblRawEX, gbc_lblRawEX);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.insets = new Insets(0, 0, 0, 5);
		gbc_panel_5.anchor = GridBagConstraints.WEST;
		gbc_panel_5.fill = GridBagConstraints.VERTICAL;
		gbc_panel_5.gridx = 4;
		gbc_panel_5.gridy = 0;
		panelDirRaw.add(panel_5, gbc_panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] { 0, 0 };
		gbl_panel_5.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_5.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_5.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_5.setLayout(gbl_panel_5);

		JLabel lblS_2 = new JLabel("S1 [13]");
		lblS_2.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_2 = new GridBagConstraints();
		gbc_lblS_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblS_2.gridx = 0;
		gbc_lblS_2.gridy = 0;
		panel_5.add(lblS_2, gbc_lblS_2);

		JLabel lblRawS1 = new JLabel("00");
		lblRawS1.setForeground(Color.BLUE);
		lblRawS1.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawS1 = new GridBagConstraints();
		gbc_lblRawS1.gridx = 0;
		gbc_lblRawS1.gridy = 1;
		panel_5.add(lblRawS1, gbc_lblRawS1);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.insets = new Insets(0, 0, 0, 5);
		gbc_panel_6.anchor = GridBagConstraints.WEST;
		gbc_panel_6.fill = GridBagConstraints.VERTICAL;
		gbc_panel_6.gridx = 5;
		gbc_panel_6.gridy = 0;
		panelDirRaw.add(panel_6, gbc_panel_6);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[] { 0, 0 };
		gbl_panel_6.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_6.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_6.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_6.setLayout(gbl_panel_6);

		JLabel lblS_3 = new JLabel("S2 [14]");
		lblS_3.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_3 = new GridBagConstraints();
		gbc_lblS_3.insets = new Insets(0, 0, 5, 0);
		gbc_lblS_3.gridx = 0;
		gbc_lblS_3.gridy = 0;
		panel_6.add(lblS_3, gbc_lblS_3);

		JLabel lblRawS2 = new JLabel("00");
		lblRawS2.setForeground(Color.BLUE);
		lblRawS2.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawS2 = new GridBagConstraints();
		gbc_lblRawS2.gridx = 0;
		gbc_lblRawS2.gridy = 1;
		panel_6.add(lblRawS2, gbc_lblRawS2);

		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.anchor = GridBagConstraints.WEST;
		gbc_panel_7.fill = GridBagConstraints.VERTICAL;
		gbc_panel_7.gridx = 6;
		gbc_panel_7.gridy = 0;
		panelDirRaw.add(panel_7, gbc_panel_7);
		GridBagLayout gbl_panel_7 = new GridBagLayout();
		gbl_panel_7.columnWidths = new int[] { 0, 0 };
		gbl_panel_7.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_7.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_7.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_7.setLayout(gbl_panel_7);

		JLabel RC = new JLabel("User [15]");
		RC.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_RC = new GridBagConstraints();
		gbc_RC.insets = new Insets(0, 0, 5, 0);
		gbc_RC.gridx = 0;
		gbc_RC.gridy = 0;
		panel_7.add(RC, gbc_RC);

		JLabel lblRawRC = new JLabel("00");
		lblRawRC.setForeground(Color.BLUE);
		lblRawRC.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawRC = new GridBagConstraints();
		gbc_lblRawRC.gridx = 0;
		gbc_lblRawRC.gridy = 1;
		panel_7.add(lblRawRC, gbc_lblRawRC);

		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_8 = new GridBagConstraints();
		gbc_panel_8.insets = new Insets(0, 0, 5, 5);
		gbc_panel_8.fill = GridBagConstraints.VERTICAL;
		gbc_panel_8.gridx = 0;
		gbc_panel_8.gridy = 1;
		tabDirectory.add(panel_8, gbc_panel_8);
		GridBagLayout gbl_panel_8 = new GridBagLayout();
		gbl_panel_8.columnWidths = new int[] { 0, 0 };
		gbl_panel_8.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_8.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_8.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_8.setLayout(gbl_panel_8);

		JLabel lblAllocationVector = new JLabel("Allocation Vector [16-31]");
		lblAllocationVector.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblAllocationVector = new GridBagConstraints();
		gbc_lblAllocationVector.insets = new Insets(0, 0, 5, 0);
		gbc_lblAllocationVector.gridx = 0;
		gbc_lblAllocationVector.gridy = 0;
		panel_8.add(lblAllocationVector, gbc_lblAllocationVector);

		JLabel lblRawAllocation = new JLabel("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		lblRawAllocation.setForeground(Color.BLUE);
		lblRawAllocation.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawAllocation = new GridBagConstraints();
		gbc_lblRawAllocation.gridx = 0;
		gbc_lblRawAllocation.gridy = 1;
		panel_8.add(lblRawAllocation, gbc_lblRawAllocation);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setPreferredSize(new Dimension(531, 0));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 2;
		tabDirectory.add(scrollPane_1, gbc_scrollPane_1);

		JPanel tabCopy = new JPanel();
		tabbedPane.addTab("Copy Files", null, tabCopy, null);
		GridBagLayout gbl_tabCopy = new GridBagLayout();
		gbl_tabCopy.columnWidths = new int[] { 0, 0 };
		gbl_tabCopy.rowHeights = new int[] { 0, 0 };
		gbl_tabCopy.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_tabCopy.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		tabCopy.setLayout(gbl_tabCopy);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Copy Fom Native file to CP/M file", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		tabCopy.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JButton btnNativeSource = new JButton("Native Source");
		btnNativeSource.setActionCommand(AC_BTN_NATIVE_SOURCE);
		btnNativeSource.addActionListener(this);
		GridBagConstraints gbc_btnNativeSource = new GridBagConstraints();
		gbc_btnNativeSource.insets = new Insets(0, 0, 5, 5);
		gbc_btnNativeSource.gridx = 0;
		gbc_btnNativeSource.gridy = 1;
		panel_2.add(btnNativeSource, gbc_btnNativeSource);

		JLabel lbllNativeSource = new JLabel("<none>");
		lbllNativeSource.setForeground(new Color(0, 0, 255));
		lbllNativeSource.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lbllNativeSource = new GridBagConstraints();
		gbc_lbllNativeSource.insets = new Insets(0, 0, 5, 0);
		gbc_lbllNativeSource.gridx = 2;
		gbc_lbllNativeSource.gridy = 1;
		panel_2.add(lbllNativeSource, gbc_lbllNativeSource);

		JLabel lblCpmTarget = new JLabel("CP/M Target");
		GridBagConstraints gbc_lblCpmTarget = new GridBagConstraints();
		gbc_lblCpmTarget.insets = new Insets(0, 0, 5, 5);
		gbc_lblCpmTarget.gridx = 0;
		gbc_lblCpmTarget.gridy = 3;
		panel_2.add(lblCpmTarget, gbc_lblCpmTarget);

		JComboBox cbCpmTarget = new JComboBox();
		cbCpmTarget.setActionCommand(AC_CB_CPM_TARGET);
		cbCpmTarget.addActionListener(this);
		GridBagConstraints gbc_cbCpmTarget = new GridBagConstraints();
		gbc_cbCpmTarget.insets = new Insets(0, 0, 5, 0);
		gbc_cbCpmTarget.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbCpmTarget.gridx = 2;
		gbc_cbCpmTarget.gridy = 3;
		panel_2.add(cbCpmTarget, gbc_cbCpmTarget);

		JButton btnCopyToCPM = new JButton("Copy");
		btnCopyToCPM.setActionCommand(AC_BTN_COPY_TO_CPM);
		btnCopyToCPM.addActionListener(this);
		GridBagConstraints gbc_btnCopyToCPM = new GridBagConstraints();
		gbc_btnCopyToCPM.insets = new Insets(0, 0, 0, 5);
		gbc_btnCopyToCPM.gridx = 0;
		gbc_btnCopyToCPM.gridy = 5;
		panel_2.add(btnCopyToCPM, gbc_btnCopyToCPM);

		JPanel tabMetrics = new JPanel();
		tabMetrics.setPreferredSize(new Dimension(0, 0));
		tabbedPane.addTab("Metrics", null, tabMetrics, null);
		GridBagLayout gbl_tabMetrics = new GridBagLayout();
		gbl_tabMetrics.columnWidths = new int[] { 0, 0 };
		gbl_tabMetrics.rowHeights = new int[] { 0, 0 };
		gbl_tabMetrics.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabMetrics.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		tabMetrics.setLayout(gbl_tabMetrics);

		JPanel panelMetrics = new JPanel();
		panelMetrics.setMinimumSize(new Dimension(380, 200));
		panelMetrics.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true),
				"Disk & File System Metrics", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		// tabPhysical.add(panelMetrics, gbc_panelMetrics);
		GridBagConstraints gbc_panelMetrics = new GridBagConstraints();
		gbc_panelMetrics.fill = GridBagConstraints.VERTICAL;
		gbc_panelMetrics.gridx = 0;
		gbc_panelMetrics.gridy = 0;
		tabMetrics.add(panelMetrics, gbc_panelMetrics);
		GridBagLayout gbl_panelMetrics = new GridBagLayout();
		gbl_panelMetrics.columnWidths = new int[] { 0, 0 };
		gbl_panelMetrics.rowHeights = new int[] { 20, 0, 20, 0, 0 };
		gbl_panelMetrics.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMetrics.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelMetrics.setLayout(gbl_panelMetrics);

		JPanel panelDiskGeometry = new JPanel();
		panelDiskGeometry.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Disk Geometry",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelDiskGeometry = new GridBagConstraints();
		gbc_panelDiskGeometry.insets = new Insets(0, 0, 5, 0);
		gbc_panelDiskGeometry.fill = GridBagConstraints.BOTH;
		gbc_panelDiskGeometry.gridx = 0;
		gbc_panelDiskGeometry.gridy = 1;
		panelMetrics.add(panelDiskGeometry, gbc_panelDiskGeometry);
		GridBagLayout gbl_panelDiskGeometry = new GridBagLayout();
		gbl_panelDiskGeometry.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelDiskGeometry.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelDiskGeometry.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelDiskGeometry.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelDiskGeometry.setLayout(gbl_panelDiskGeometry);

		lblHeads = new JLabel("0");
		lblHeads.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHeads.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblHeads = new GridBagConstraints();
		gbc_lblHeads.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeads.anchor = GridBagConstraints.EAST;
		gbc_lblHeads.gridx = 0;
		gbc_lblHeads.gridy = 1;
		panelDiskGeometry.add(lblHeads, gbc_lblHeads);

		JLabel lblNewLabel_1 = new JLabel("Heads");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 1;
		panelDiskGeometry.add(lblNewLabel_1, gbc_lblNewLabel_1);

		lblTracksPerHead = new JLabel("0");
		lblTracksPerHead.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTracksPerHead.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblTracksPerHead = new GridBagConstraints();
		gbc_lblTracksPerHead.insets = new Insets(0, 0, 5, 5);
		gbc_lblTracksPerHead.gridx = 0;
		gbc_lblTracksPerHead.gridy = 2;
		panelDiskGeometry.add(lblTracksPerHead, gbc_lblTracksPerHead);

		JLabel lblTracksPerHead_1 = new JLabel("Tracks per Head");
		lblTracksPerHead_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblTracksPerHead_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblTracksPerHead_1 = new GridBagConstraints();
		gbc_lblTracksPerHead_1.anchor = GridBagConstraints.WEST;
		gbc_lblTracksPerHead_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblTracksPerHead_1.gridx = 2;
		gbc_lblTracksPerHead_1.gridy = 2;
		panelDiskGeometry.add(lblTracksPerHead_1, gbc_lblTracksPerHead_1);

		lblSectorsPerTrack = new JLabel("0");
		lblSectorsPerTrack.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSectorsPerTrack.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblSectorsPerTrack = new GridBagConstraints();
		gbc_lblSectorsPerTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblSectorsPerTrack.gridx = 0;
		gbc_lblSectorsPerTrack.gridy = 3;
		panelDiskGeometry.add(lblSectorsPerTrack, gbc_lblSectorsPerTrack);

		JLabel lblSectorsPerTrack_1 = new JLabel("Sectors per Track");
		lblSectorsPerTrack_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblSectorsPerTrack_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblSectorsPerTrack_1 = new GridBagConstraints();
		gbc_lblSectorsPerTrack_1.anchor = GridBagConstraints.WEST;
		gbc_lblSectorsPerTrack_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblSectorsPerTrack_1.gridx = 2;
		gbc_lblSectorsPerTrack_1.gridy = 3;
		panelDiskGeometry.add(lblSectorsPerTrack_1, gbc_lblSectorsPerTrack_1);

		lblBytesPerSector = new JLabel("0");
		lblBytesPerSector.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBytesPerSector.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblBytesPerSector = new GridBagConstraints();
		gbc_lblBytesPerSector.insets = new Insets(0, 0, 5, 5);
		gbc_lblBytesPerSector.gridx = 0;
		gbc_lblBytesPerSector.gridy = 4;
		panelDiskGeometry.add(lblBytesPerSector, gbc_lblBytesPerSector);

		JLabel lblHeads_1 = new JLabel("Bytes per Sector");
		lblHeads_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblHeads_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblHeads_1 = new GridBagConstraints();
		gbc_lblHeads_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblHeads_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblHeads_1.gridx = 2;
		gbc_lblHeads_1.gridy = 4;
		panelDiskGeometry.add(lblHeads_1, gbc_lblHeads_1);

		lblTotalTracks = new JLabel("0");
		lblTotalTracks.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTotalTracks.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblTotalTracks = new GridBagConstraints();
		gbc_lblTotalTracks.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalTracks.gridx = 0;
		gbc_lblTotalTracks.gridy = 6;
		panelDiskGeometry.add(lblTotalTracks, gbc_lblTotalTracks);

		JLabel lblTotalTracks_1 = new JLabel("Total Tracks");
		lblTotalTracks_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblTotalTracks_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblTotalTracks_1 = new GridBagConstraints();
		gbc_lblTotalTracks_1.anchor = GridBagConstraints.WEST;
		gbc_lblTotalTracks_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblTotalTracks_1.gridx = 2;
		gbc_lblTotalTracks_1.gridy = 6;
		panelDiskGeometry.add(lblTotalTracks_1, gbc_lblTotalTracks_1);

		lblTotalSectors = new JLabel("0");
		lblTotalSectors.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTotalSectors.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblTotalSectors = new GridBagConstraints();
		gbc_lblTotalSectors.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotalSectors.anchor = GridBagConstraints.WEST;
		gbc_lblTotalSectors.gridx = 0;
		gbc_lblTotalSectors.gridy = 7;
		panelDiskGeometry.add(lblTotalSectors, gbc_lblTotalSectors);

		JLabel lblTotalSectors_1 = new JLabel("Total Sectors");
		lblTotalSectors_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblTotalSectors_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblTotalSectors_1 = new GridBagConstraints();
		gbc_lblTotalSectors_1.anchor = GridBagConstraints.WEST;
		gbc_lblTotalSectors_1.gridx = 2;
		gbc_lblTotalSectors_1.gridy = 7;
		panelDiskGeometry.add(lblTotalSectors_1, gbc_lblTotalSectors_1);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "File System Parameters",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		panelMetrics.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		lblTracksBeforeDirectory = new JLabel("0");
		lblTracksBeforeDirectory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTracksBeforeDirectory.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblTracksBeforeDirectory = new GridBagConstraints();
		gbc_lblTracksBeforeDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblTracksBeforeDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblTracksBeforeDirectory.gridx = 0;
		gbc_lblTracksBeforeDirectory.gridy = 1;
		panel_1.add(lblTracksBeforeDirectory, gbc_lblTracksBeforeDirectory);

		JLabel lblHeads_2 = new JLabel("Tracks Before Directory");
		lblHeads_2.setHorizontalAlignment(SwingConstants.LEFT);
		lblHeads_2.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblHeads_2 = new GridBagConstraints();
		gbc_lblHeads_2.anchor = GridBagConstraints.WEST;
		gbc_lblHeads_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblHeads_2.gridx = 2;
		gbc_lblHeads_2.gridy = 1;
		panel_1.add(lblHeads_2, gbc_lblHeads_2);

		lblLogicalBlockSizeInSectors = new JLabel("0");
		lblLogicalBlockSizeInSectors.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLogicalBlockSizeInSectors.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblLogicalBlockSizeInSectors = new GridBagConstraints();
		gbc_lblLogicalBlockSizeInSectors.insets = new Insets(0, 0, 5, 5);
		gbc_lblLogicalBlockSizeInSectors.anchor = GridBagConstraints.EAST;
		gbc_lblLogicalBlockSizeInSectors.gridx = 0;
		gbc_lblLogicalBlockSizeInSectors.gridy = 2;
		panel_1.add(lblLogicalBlockSizeInSectors, gbc_lblLogicalBlockSizeInSectors);

		JLabel lblLogicalBlockSize = new JLabel("Logical Block Size in Sectors");
		lblLogicalBlockSize.setHorizontalAlignment(SwingConstants.LEFT);
		lblLogicalBlockSize.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblLogicalBlockSize = new GridBagConstraints();
		gbc_lblLogicalBlockSize.anchor = GridBagConstraints.WEST;
		gbc_lblLogicalBlockSize.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogicalBlockSize.gridx = 2;
		gbc_lblLogicalBlockSize.gridy = 2;
		panel_1.add(lblLogicalBlockSize, gbc_lblLogicalBlockSize);

		lblMaxDirectoryEntry = new JLabel("0");
		lblMaxDirectoryEntry.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxDirectoryEntry.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblMaxDirectoryEntry = new GridBagConstraints();
		gbc_lblMaxDirectoryEntry.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxDirectoryEntry.anchor = GridBagConstraints.EAST;
		gbc_lblMaxDirectoryEntry.gridx = 0;
		gbc_lblMaxDirectoryEntry.gridy = 3;
		panel_1.add(lblMaxDirectoryEntry, gbc_lblMaxDirectoryEntry);

		JLabel lblMaxDirectoryEntry_1 = new JLabel("Max Directory Entry");
		lblMaxDirectoryEntry_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblMaxDirectoryEntry_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblMaxDirectoryEntry_1 = new GridBagConstraints();
		gbc_lblMaxDirectoryEntry_1.anchor = GridBagConstraints.WEST;
		gbc_lblMaxDirectoryEntry_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblMaxDirectoryEntry_1.gridx = 2;
		gbc_lblMaxDirectoryEntry_1.gridy = 3;
		panel_1.add(lblMaxDirectoryEntry_1, gbc_lblMaxDirectoryEntry_1);

		lblMaxBlockNumber = new JLabel("0");
		lblMaxBlockNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxBlockNumber.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblMaxBlockNumber = new GridBagConstraints();
		gbc_lblMaxBlockNumber.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaxBlockNumber.anchor = GridBagConstraints.EAST;
		gbc_lblMaxBlockNumber.gridx = 0;
		gbc_lblMaxBlockNumber.gridy = 4;
		panel_1.add(lblMaxBlockNumber, gbc_lblMaxBlockNumber);

		JLabel lblMaxBlockNumber_1 = new JLabel("Max Block Number");
		lblMaxBlockNumber_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblMaxBlockNumber_1.setFont(new Font("Arial", Font.PLAIN, 15));
		GridBagConstraints gbc_lblMaxBlockNumber_1 = new GridBagConstraints();
		gbc_lblMaxBlockNumber_1.anchor = GridBagConstraints.WEST;
		gbc_lblMaxBlockNumber_1.gridx = 2;
		gbc_lblMaxBlockNumber_1.gridy = 4;
		panel_1.add(lblMaxBlockNumber_1, gbc_lblMaxBlockNumber_1);

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	class DirEntry {
		public String fileName;
		public int directoryIndex;

		public DirEntry(String fileName, int directoryIndex) {
			this.fileName = fileName;
			this.directoryIndex = directoryIndex;
		}// constructor

		public String toString() {
			return this.fileName;
		}// toString
	}// class DirEntry

	class FileCpmModel extends AbstractListModel implements ComboBoxModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		List<DirEntry> modelItemList;
		DirEntry selection = null;

		public FileCpmModel() {
			modelItemList = new ArrayList<DirEntry>();
		}// Constructor

		public void add(DirEntry item) {
			modelItemList.add(item);
		}// add

		@Override
		public DirEntry getElementAt(int index) {
			return modelItemList.get(index);
		}// getElementAt

		@Override
		public int getSize() {
			return modelItemList.size();
		}// getSize

		@Override
		public DirEntry getSelectedItem() {
			return selection;
		}// getSelectedItem

		@Override
		public void setSelectedItem(Object arg0) {
			if (arg0 instanceof DirEntry) {
				selection = (DirEntry) arg0;
			} else {
				selection = new DirEntry((String) arg0, -1);
			}// if
		}// setSelectedItem
	}// class FileCpmModel

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}