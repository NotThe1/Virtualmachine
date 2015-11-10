package disks;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.ScrollPane;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import java.awt.GridBagConstraints;

import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import java.awt.Insets;

import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.GridLayout;

import myComponents.Hex64KSpinner;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import myComponents.Hex64KSpinner16;

import javax.swing.JTextField;

public class NativeDiskTool implements ActionListener, ChangeListener {

	private JFrame frmNativeDiskTool;
	RawDiskDrive diskDrive;
	Document doc;

	byte[] aSector;
	byte[] dotSector;
	String geometryString;

	int sectorSize;
	int linesToDisplay;

	ArrayList<Byte> rawDirectory;
	String strDirectory;
	JTable dirTable;
	DefaultTableModel modelDir;

	int maxDirectoryEntries;
	int directoryEntriesPerSector;
	int numberOfDirectorySectors;
	int absoluteSector;

	// private final static String AC_BTN;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NativeDiskTool window = new NativeDiskTool();

					window.frmNativeDiskTool.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// ----------------------------Physical View ----------------------------------------
	private void loadDiskDrive(String selectedAbsolutePath) {
		diskDrive = new RawDiskDrive(selectedAbsolutePath);
		setFileNameLabel(selectedAbsolutePath);
		showGeometry(diskDrive);
		haveFile(true);
		diskDrive.setCurrentAbsoluteSector(0);
		displaySector();
		// setSpinnerLimits();

	}

	private String getDisk() {
		String fileLocation = ".";
		Path sourcePath = Paths.get(fileLocation, "Disks");
		JFileChooser chooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		chooser.setMultiSelectionEnabled(false);
		//
		for (DiskLayout diskLayout : DiskLayout.values()) {
			if (diskLayout.fileExtension.startsWith("F")) {
				chooser.addChoosableFileFilter(
						new FileNameExtensionFilter(diskLayout.descriptor, diskLayout.fileExtension));
			}// if - correct type
		}// for

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
	}// getDisk

	private void showGeometry(RawDiskDrive diskDrive) {
		lblHeads.setText(String.format(geometryString, diskDrive.getHeads()));
		lblTracksPerHead.setText(String.format(geometryString, diskDrive.getTracksPerHead()));
		lblSectorsPerTrack.setText(String.format(geometryString, diskDrive.getSectorsPerTrack()));
		lblSectorSize.setText(String.format(geometryString, diskDrive.getBytesPerSector()));
		lblTotalTracks.setText(String.format(geometryString, diskDrive.getTotalTracks()));
		lblTotalSectors.setText(String.format(geometryString, diskDrive.getTotalSectorsOnDisk()));
		lblTotalBytes.setText(String.format(geometryString, diskDrive.getTotalBytesOnDisk()));

		setSpinnerLimits();

		sectorSize = diskDrive.getBytesPerSector();
		linesToDisplay = sectorSize / CHARACTERS_PER_LINE;
	}

	private void resetGeometry() {
		lblHeads.setText("<none>");
		lblTracksPerHead.setText("<none>");
		lblSectorsPerTrack.setText("<none>");
		lblSectorSize.setText("<none>");
		lblTotalTracks.setText("<none>");
		lblTotalSectors.setText("<none>");
		lblTotalBytes.setText("<none>");

		sectorSize = 0;
		linesToDisplay = 1;

		lblFileName.setText("<none>");
	}

	private void setSpinnerLimits() {
		((SpinnerNumberModel) spinnerHeadDecimal.getModel()).setMaximum(diskDrive.getHeads() - 1);
		((SpinnerNumberModel) spinnerHeadHex.getModel()).setMaximum(diskDrive.getHeads() - 1);

		((SpinnerNumberModel) spinnerTrackDecimal.getModel()).setMaximum(diskDrive.getTracksPerHead() - 1);
		((SpinnerNumberModel) spinnerTrackHex.getModel()).setMaximum(diskDrive.getTracksPerHead() - 1);

		((SpinnerNumberModel) spinnerSectorDecimal.getModel()).setMaximum(diskDrive.getSectorsPerTrack());
		((SpinnerNumberModel) spinnerSectorHex.getModel()).setMaximum(diskDrive.getSectorsPerTrack());
	}

	private void btnDisplayPhysical() {
		diskDrive.setCurrentAbsoluteSector(
				(int) spinnerHeadHex.getValue(),
				(int) spinnerTrackHex.getValue(),
				(int) spinnerSectorHex.getValue());
		displaySector();
	}

	private void displaySector() {
		spinnerHeadHex.setValue(diskDrive.getCurrentHead());
		spinnerTrackHex.setValue(diskDrive.getCurrentTrack());
		spinnerSectorHex.setValue(diskDrive.getCurrentSector());
		lblLogicalSector.setText(String.format(geometryString, diskDrive.getCurrentAbsoluteSector()));

		aSector = diskDrive.read();
		clearSectorDisplay();
		doc = txtDisplay.getDocument();

		try {
			for (int i = 0; i < linesToDisplay; i++) {
				doc.insertString(doc.getLength(), formatLine(i), null);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		txtDisplay.setCaretPosition(0);

	}

	private String formatLine(int lineNumber) {
		byte target;
		StringBuilder sbHex = new StringBuilder();
		StringBuilder sbDot = new StringBuilder();
		sbHex.append(String.format("%04X: ", lineNumber * CHARACTERS_PER_LINE));
		for (int i = 0; i < CHARACTERS_PER_LINE; i++) {
			target = aSector[(lineNumber * CHARACTERS_PER_LINE) + i];
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

	private void setNumberBase() {
		boolean isHex = true;
		if (btnNumberBase.getText().equals(BTN_LABEL_HEX)) {
			geometryString = "%,d";
			isHex = false;
		} else {
			geometryString = "%X";
		}
		showHexSpinners(isHex);
	}

	private void showHexSpinners(boolean state) {
		spinnerHeadHex.setVisible(state);
		spinnerTrackHex.setVisible(state);
		spinnerSectorHex.setVisible(state);

		spinnerHeadDecimal.setVisible(!state);
		spinnerTrackDecimal.setVisible(!state);
		spinnerSectorDecimal.setVisible(!state);

		spinnerTrackBeforeDirectoryHex.setVisible(state);
		spinnerLogicalBlockSizeHex.setVisible(state);
		spinnerMaxDirectoryEntriesHex.setVisible(state);

		spinnerTrackBeforeDirectoryDecimal.setVisible(!state);
		spinnerLogicalBlockSizeDecimal.setVisible(!state);
		spinnerMaxDirectoryEntriesDecimal.setVisible(!state);

	}

	private void haveFile(boolean state) {
		tabbedPane.setVisible(state);

		mnuFileNewDisk.setEnabled(!state);
		mnuFileOpenDisk.setEnabled(!state);
		mnuFileCloseDisk.setEnabled(state);
		mnuFileSaveDisk.setEnabled(state);
		mnuFileSaveAsDisk.setEnabled(state);

		if (state == false) {
			clearSectorDisplay();
			clearDirectoryDisplay();
			tabbedPane.setSelectedIndex(TAB_PHYSICAL);
		}
	}

	private void setFileNameLabel(String selectedAbsolutePath) {
		int index = selectedAbsolutePath.lastIndexOf(File.separator);
		lblFileName.setText(selectedAbsolutePath.substring(index + 1, selectedAbsolutePath.length()));
		lblFileName.setToolTipText(selectedAbsolutePath.substring(0, index));
	}

	private void clearDirectoryDisplay() {
		modelDir = null;
		dirTable = null;
	}

	private void clearSectorDisplay() {
		if (doc == null) {
			return;
		}
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// doc = null;
	}

	// ----------------------------Directory View ----------------------------------------
	private void tabDirectory() {

		int rowCount = (int) spinnerMaxDirectoryEntriesHex.getValue();
		Object[] columnNames = { "row", "Name", "type", "User", "R/O", "Sys", "Seq", "Count", "Blocks" };
		dirTable = new JTable(new DefaultTableModel(columnNames, 0));
		dirTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dirTable.setFont(new Font("Tahoma", Font.PLAIN, 14));
		dirTable.setEnabled(false);
		adjustTableLook(dirTable);

		scrollDirectoryTable.setViewportView(dirTable);
		fillDirectoryTable(dirTable);
		showDirectoryDetail(0); // dfsfsdfsdfsddddddddddddddddddddddddddddddddddddddddddddddd
	}

	private void fillDirectoryTable(JTable table) {
		rawDirectory = new ArrayList<Byte>();
		strDirectory = "";
		String name, type, aSectorAsString;
		int user, seqNumber, count, blocks;
		boolean readOnly, systemFile;

		boolean bigDisk = false; // need to figure out from total disk size
		maxDirectoryEntries = (int) spinnerMaxDirectoryEntriesHex.getValue();
		directoryEntriesPerSector = (diskDrive.getBytesPerSector() / DIRECTORY_ENTRY_SIZE);
		numberOfDirectorySectors = maxDirectoryEntries / directoryEntriesPerSector;
		absoluteSector = diskDrive.getSectorsPerTrack() * (int) spinnerTrackBeforeDirectoryHex.getValue();
		modelDir = (DefaultTableModel) table.getModel();
		int rowCount = 0;
		for (int i = 0; i < numberOfDirectorySectors; i++) {
			diskDrive.setCurrentAbsoluteSector(absoluteSector + i);
			aSector = diskDrive.read();
			constructRawDirectory(aSector);
		}// for i

		for (int i = 0; i < (int) spinnerMaxDirectoryEntriesHex.getValue(); i++) {
			int bias = i * DIRECTORY_ENTRY_SIZE;
			user = rawDirectory.get(bias + DIR_USER);// EMPTY_ENTRY
			int s = bias + DIR_NAME;
			int e = bias + DIR_NAME + DIR_NAME_SIZE;
			name = strDirectory.substring(bias + DIR_NAME, bias + DIR_NAME + DIR_NAME_SIZE);
			type = strDirectory.substring(bias + DIR_TYPE, bias + DIR_TYPE + DIR_TYPE_SIZE);
			readOnly = (rawDirectory.get(bias + DIR_T1) & 0x80) == 0x80;
			systemFile = (rawDirectory.get(bias + DIR_T2) & 0x80) == 0x80;
			seqNumber = (rawDirectory.get(bias + DIR_S2) * 0x0100) + aSector[DIR_EX];
			count = rawDirectory.get(bias + DIR_RC);
			blocks = 0;
			for (int k = DIR_BLOCKS; k < DIR_BLOCKS + DIR_BLOCKS_SIZE; k++) {
				blocks = rawDirectory.get(bias + k) != 0 ? blocks + 1 : blocks;
			}// for k
			Object[] aRow = makeRow(rowCount, name, type, user, readOnly, systemFile, seqNumber, count, blocks);
			modelDir.insertRow(rowCount++, aRow);
		}
	}

	private Object[] makeRow(int row, String name, String type, int user, boolean readOnly,
			boolean systemFile, int seqNumber, int count, int blocks) {
		Object[] aRow = { row, name, type, user, readOnly, systemFile, seqNumber, count, blocks };
		return aRow;
	}

	private void adjustTableLook(JTable table) {

		Font realColumnFont = table.getFont();
		FontMetrics fontMetrics = table.getFontMetrics(realColumnFont);

		int charWidth = fontMetrics.stringWidth("W");

		TableColumnModel tableColumn = table.getColumnModel();
		tableColumn.getColumn(0).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(1).setPreferredWidth(charWidth * 9);
		tableColumn.getColumn(2).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(3).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(4).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(5).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(6).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(7).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(8).setPreferredWidth(charWidth * 3);

		DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
		rightAlign.setHorizontalAlignment(JLabel.RIGHT);
		tableColumn.getColumn(0).setCellRenderer(rightAlign);
		tableColumn.getColumn(3).setCellRenderer(rightAlign);
		tableColumn.getColumn(6).setCellRenderer(rightAlign);
		tableColumn.getColumn(7).setCellRenderer(rightAlign);
		tableColumn.getColumn(8).setCellRenderer(rightAlign);

		DefaultTableCellRenderer centerAlign = new DefaultTableCellRenderer();
		centerAlign.setHorizontalAlignment(JLabel.CENTER);
		tableColumn.getColumn(4).setCellRenderer(centerAlign);
		tableColumn.getColumn(5).setCellRenderer(centerAlign);
	}

	private void constructRawDirectory(byte[] aSector) {
		strDirectory += new String(aSector);

		for (int i = 0; i < directoryEntriesPerSector * DIRECTORY_ENTRY_SIZE; i++) {
			rawDirectory.add(aSector[i]);
		}
	}

	private void showDirectoryDetail(int entryNumber) {
		int bias = entryNumber * DIRECTORY_ENTRY_SIZE;
		lblDirUser.setText(String.format("%02x", rawDirectory.get(DIR_USER)));

		lblDirName.setText(String.format("%02X %02X %02X %02X %02X %02X %02X %02X ",
				rawDirectory.get(DIR_NAME),
				rawDirectory.get(DIR_NAME + 1),
				rawDirectory.get(DIR_NAME + 2),
				rawDirectory.get(DIR_NAME + 3),
				rawDirectory.get(DIR_NAME + 4),
				rawDirectory.get(DIR_NAME + 5),
				rawDirectory.get(DIR_NAME + 6),
				rawDirectory.get(DIR_NAME + 7)
				));
		lblDirType.setText(String.format("%02X %02X %02X",
				rawDirectory.get(DIR_TYPE),
				rawDirectory.get(DIR_TYPE + 1),
				rawDirectory.get(DIR_TYPE + 2)
				));
		lblDirEX.setText(String.format("%02x", rawDirectory.get(DIR_EX)));
		lblDirS1.setText(String.format("%02x", rawDirectory.get(DIR_S1)));
		lblDirS2.setText(String.format("%02x", rawDirectory.get(DIR_S2)));

		lblDirRC.setText(String.format("%02x", rawDirectory.get(DIR_RC)));
		
		lblDirAllocation1.setText(String.format("%02X %02X %02X %02X %02X %02X %02X %02X ",
				rawDirectory.get(DIR_BLOCKS),
				rawDirectory.get(DIR_BLOCKS + 1),
				rawDirectory.get(DIR_BLOCKS + 2),
				rawDirectory.get(DIR_BLOCKS + 3),
				rawDirectory.get(DIR_BLOCKS + 4),
				rawDirectory.get(DIR_BLOCKS + 5),
				rawDirectory.get(DIR_BLOCKS + 6),
				rawDirectory.get(DIR_BLOCKS + 7)
				));
		lblDirAllocation1.setText(String.format("%02X %02X %02X %02X %02X %02X %02X %02X ",
				rawDirectory.get(DIR_BLOCKS + 8),
				rawDirectory.get(DIR_BLOCKS + 9),
				rawDirectory.get(DIR_BLOCKS + 10),
				rawDirectory.get(DIR_BLOCKS + 11),
				rawDirectory.get(DIR_BLOCKS + 12),
				rawDirectory.get(DIR_BLOCKS + 13),
				rawDirectory.get(DIR_BLOCKS + 14),
				rawDirectory.get(DIR_BLOCKS + 15)
				));



	}

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
	private final static String AC_MNU_FILE_NEW = "MenuFileNew";
	private final static String AC_MNU_FILE_OPEN = "MenuFileOpen";
	private final static String AC_MNU_FILE_CLOSE = "MenuFileClose";
	private final static String AC_MNU_FILE_SAVE = "MenuFileSave";
	private final static String AC_MNU_FILE_SAVE_AS = "MenuFileSaveAs";
	private final static String AC_MNU_FILE_EXIT = "MenuFileExit";

	private final static String AC_BTN_NUMBER_BASE = "btnNumberBase";
	private final static String AC_BTN_DISPLAY_PHYSICAL = "btnDisplayPhysical";

	private final static String AC_BTN_FIRST = "btnFirst";
	private final static String AC_BTN_PREVIOUS = "btnPrevious";
	private final static String AC_BTN_NEXT = "btnNext";
	private final static String AC_BTN_LAST = "btnLast";

	private final static String BTN_LABEL_HEX = "Hex";
	private final static String BTN_LABEL_DECIMAL = "Decimal";

	private final static int TAB_PHYSICAL = 0;
	private final static int TAB_DIRECTORY = 2;
	private final static int TAB_FILE = 1;

	private final static int CHARACTERS_PER_LINE = 16;
	private final static int DIRECTORY_ENTRY_SIZE = 32;
	private final static byte EMPTY_ENTRY = (byte) 0xE5;

	private final static int DIR_USER = 0;
	private final static int DIR_NAME = 1;
	private final static int DIR_NAME_SIZE = 8;
	private final static int DIR_TYPE = 9;
	private final static int DIR_TYPE_SIZE = 3;
	private final static int DIR_T1 = 9;
	private final static int DIR_T2 = 10;
	private final static int DIR_EX = 12; // LOW BYTE
	private final static int DIR_S2 = 14;
	private final static int DIR_S1 = 13;
	private final static int DIR_RC = 15;
	private final static int DIR_BLOCKS = 16;
	private final static int DIR_BLOCKS_SIZE = 16;

	private Hex64KSpinner spinnerHeadHex;
	private JSpinner spinnerHeadDecimal;
	private Hex64KSpinner spinnerTrackHex;
	private JSpinner spinnerTrackDecimal;
	private Hex64KSpinner spinnerSectorHex;
	private JSpinner spinnerSectorDecimal;
	private JLabel lblLogicalSector;
	private JLabel lblFileName;
	private JLabel lblHeads;
	private JLabel lblTracksPerHead;
	private JLabel lblSectorsPerTrack;
	private JLabel lblSectorSize;
	private JLabel lblTotalTracks;
	private JLabel lblTotalSectors;
	private JLabel lblTotalBytes;
	private JTextArea txtDisplay;
	private JScrollPane scrollPane;
	private JButton btnNumberBase;
	private JTabbedPane tabbedPane;
	private JPanel panelHTS;
	private JPanel panelPhysical;
	private JMenuItem mnuFileNewDisk;
	private JMenuItem mnuFileOpenDisk;
	private JMenuItem mnuFileCloseDisk;
	private JMenuItem mnuFileSaveDisk;
	private JMenuItem mnuFileSaveAsDisk;
	private Hex64KSpinner spinnerTrackBeforeDirectoryHex;
	private JSpinner spinnerTrackBeforeDirectoryDecimal;
	private Hex64KSpinner spinnerLogicalBlockSizeHex;
	private JSpinner spinnerLogicalBlockSizeDecimal;
	private JLabel lblMaxDirectoryEntries;
	private JSpinner spinnerMaxDirectoryEntriesDecimal;
	private Hex64KSpinner spinnerMaxDirectoryEntriesHex;
	private JScrollPane scrollDirectoryTable;
	private JPanel panelDirRaw;
	private JLabel lblDirName;
	private JLabel lblDirType;
	private JLabel lblDirEX;
	private JLabel lblDirS1;
	private JLabel lblDirS2;
	private JLabel lblDirRC;
	private JLabel lblDirAllocation1;
	private JLabel lblDirAllocation2;
	private JLabel lblDirUser;

	@Override
	public void actionPerformed(ActionEvent ae) {
		String selectedAbsolutePath;
		int sectorNumber;
		switch (ae.getActionCommand()) {
		// Menus
		case AC_MNU_FILE_NEW:
			selectedAbsolutePath = MakeNewDisk.makeNewDisk();
			if (selectedAbsolutePath == null) {
				return;
			}// if
			loadDiskDrive(selectedAbsolutePath);
			break;
		case AC_MNU_FILE_OPEN:
			selectedAbsolutePath = getDisk();
			if (selectedAbsolutePath == null) {
				return;
			}// if
			loadDiskDrive(selectedAbsolutePath);
			break;
		case AC_MNU_FILE_CLOSE:
			if (diskDrive != null) {
				diskDrive.dismount();
				diskDrive = null;
			}
			haveFile(false);
			resetGeometry();
			break;
		case AC_MNU_FILE_SAVE:
			break;
		case AC_MNU_FILE_SAVE_AS:
			break;
		case AC_MNU_FILE_EXIT:
			appClose();
			break;
		// Buttons
		case AC_BTN_DISPLAY_PHYSICAL:
			btnDisplayPhysical();
			break;

		case AC_BTN_NUMBER_BASE:
			if (btnNumberBase.getText().equals(BTN_LABEL_HEX)) {
				btnNumberBase.setText(BTN_LABEL_DECIMAL);
			} else {
				btnNumberBase.setText(BTN_LABEL_HEX);
			}
			setNumberBase();
			if (diskDrive != null) {
				showGeometry(diskDrive);
			}
			break;

		case AC_BTN_FIRST:
			diskDrive.setCurrentAbsoluteSector(0);
			displaySector();
			break;
		case AC_BTN_PREVIOUS:
			sectorNumber = diskDrive.getCurrentAbsoluteSector() - 1;
			if (sectorNumber <= -1) {
				return; // no more to read
			}
			diskDrive.setCurrentAbsoluteSector(sectorNumber);
			displaySector();

			break;
		case AC_BTN_NEXT:
			sectorNumber = diskDrive.getCurrentAbsoluteSector() + 1;
			if (sectorNumber >= diskDrive.getTotalSectorsOnDisk()) {
				return; // no more to read
			}
			diskDrive.setCurrentAbsoluteSector(sectorNumber);
			displaySector();
			break;
		case AC_BTN_LAST:
			diskDrive.setCurrentAbsoluteSector(diskDrive.getTotalSectorsOnDisk() - 1);
			displaySector();
			break;
		default:
		}// switch
			// TODO Auto-generated method stub

	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		if (ce.getSource() instanceof JTabbedPane) {
			int pane = ((JTabbedPane) ce.getSource()).getSelectedIndex();
			switch (pane) {
			case TAB_PHYSICAL:
				break;
			case TAB_DIRECTORY:
				tabDirectory();
				break;
			case TAB_FILE:
				break;
			default:
			}

		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private void appInit() {
		btnNumberBase.setText(BTN_LABEL_DECIMAL);
		setNumberBase();
		resetGeometry();
		tabbedPane.setSelectedIndex(TAB_PHYSICAL);
		haveFile(false);

		spinnerHeadDecimal.setModel(spinnerHeadHex.getModel());
		spinnerTrackDecimal.setModel(spinnerTrackHex.getModel());
		spinnerSectorDecimal.setModel(spinnerSectorHex.getModel());

		spinnerTrackBeforeDirectoryDecimal.setModel(spinnerTrackBeforeDirectoryHex.getModel());
		spinnerTrackBeforeDirectoryHex.setValue((int) 0x01);
		spinnerLogicalBlockSizeDecimal.setModel(spinnerLogicalBlockSizeHex.getModel());
		spinnerLogicalBlockSizeHex.setValue((int) 0x04);
		spinnerMaxDirectoryEntriesDecimal.setModel(spinnerMaxDirectoryEntriesHex.getModel());
		spinnerMaxDirectoryEntriesHex.setValue((int) 0x80);

		JLabel lblHeaderString = new JLabel("      00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F");
		lblHeaderString.setForeground(Color.blue);
		lblHeaderString.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPane.setColumnHeaderView(lblHeaderString);
	}

	private void appClose() {
		System.exit(-1);
	}

	/**
	 * Create the application.
	 */
	public NativeDiskTool() {
		initialize();
		appInit();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmNativeDiskTool = new JFrame();
		frmNativeDiskTool.setTitle("Native Disk Tool");
		frmNativeDiskTool.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		frmNativeDiskTool.setBounds(100, 100, 950, 752);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.addWindowListener(new WindowListener());
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 766, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 79, 637, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		frmNativeDiskTool.getContentPane().setLayout(gridBagLayout);

		lblFileName = new JLabel("File Name");
		lblFileName.setFont(new Font("Tahoma", Font.PLAIN, 18));
		GridBagConstraints gbc_lblFileName = new GridBagConstraints();
		gbc_lblFileName.insets = new Insets(0, 0, 5, 0);
		gbc_lblFileName.gridx = 0;
		gbc_lblFileName.gridy = 0;
		frmNativeDiskTool.getContentPane().add(lblFileName, gbc_lblFileName);

		JPanel panelGeomertry = new JPanel();
		panelGeomertry.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelGeomertry = new GridBagConstraints();
		gbc_panelGeomertry.insets = new Insets(0, 0, 5, 0);
		gbc_panelGeomertry.fill = GridBagConstraints.BOTH;
		gbc_panelGeomertry.gridx = 0;
		gbc_panelGeomertry.gridy = 1;
		frmNativeDiskTool.getContentPane().add(panelGeomertry, gbc_panelGeomertry);
		GridBagLayout gbl_panelGeomertry = new GridBagLayout();
		gbl_panelGeomertry.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelGeomertry.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelGeomertry.columnWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelGeomertry.rowWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
		panelGeomertry.setLayout(gbl_panelGeomertry);

		JPanel panelGeometry1 = new JPanel();
		panelGeometry1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Disk Geomertry",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelGeometry1 = new GridBagConstraints();
		gbc_panelGeometry1.insets = new Insets(0, 0, 5, 5);
		gbc_panelGeometry1.anchor = GridBagConstraints.NORTH;
		gbc_panelGeometry1.gridx = 0;
		gbc_panelGeometry1.gridy = 0;
		panelGeomertry.add(panelGeometry1, gbc_panelGeometry1);
		GridBagLayout gbl_panelGeometry1 = new GridBagLayout();
		gbl_panelGeometry1.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelGeometry1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelGeometry1.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelGeometry1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelGeometry1.setLayout(gbl_panelGeometry1);

		JLabel lbH1 = new JLabel("Heads");
		GridBagConstraints gbc_lbH1 = new GridBagConstraints();
		gbc_lbH1.insets = new Insets(0, 0, 5, 5);
		gbc_lbH1.gridx = 1;
		gbc_lbH1.gridy = 0;
		panelGeometry1.add(lbH1, gbc_lbH1);

		JLabel lblTPH1 = new JLabel("Tracks/Head");
		GridBagConstraints gbc_lblTPH1 = new GridBagConstraints();
		gbc_lblTPH1.insets = new Insets(0, 0, 5, 5);
		gbc_lblTPH1.gridx = 3;
		gbc_lblTPH1.gridy = 0;
		panelGeometry1.add(lblTPH1, gbc_lblTPH1);

		JLabel lblSPT1 = new JLabel("Sectors/Track");
		GridBagConstraints gbc_lblSPT1 = new GridBagConstraints();
		gbc_lblSPT1.insets = new Insets(0, 0, 5, 5);
		gbc_lblSPT1.gridx = 5;
		gbc_lblSPT1.gridy = 0;
		panelGeometry1.add(lblSPT1, gbc_lblSPT1);

		JLabel lblSS1 = new JLabel("Sector Size");
		GridBagConstraints gbc_lblSS1 = new GridBagConstraints();
		gbc_lblSS1.insets = new Insets(0, 0, 5, 0);
		gbc_lblSS1.gridx = 7;
		gbc_lblSS1.gridy = 0;
		panelGeometry1.add(lblSS1, gbc_lblSS1);

		lblHeads = new JLabel("0");
		lblHeads.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblHeads = new GridBagConstraints();
		gbc_lblHeads.insets = new Insets(0, 0, 0, 5);
		gbc_lblHeads.gridx = 1;
		gbc_lblHeads.gridy = 1;
		panelGeometry1.add(lblHeads, gbc_lblHeads);

		lblTracksPerHead = new JLabel("0");
		lblTracksPerHead.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblTracksPerHead = new GridBagConstraints();
		gbc_lblTracksPerHead.insets = new Insets(0, 0, 0, 5);
		gbc_lblTracksPerHead.gridx = 3;
		gbc_lblTracksPerHead.gridy = 1;
		panelGeometry1.add(lblTracksPerHead, gbc_lblTracksPerHead);

		lblSectorsPerTrack = new JLabel("00");
		lblSectorsPerTrack.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblSectorsPerTrack = new GridBagConstraints();
		gbc_lblSectorsPerTrack.insets = new Insets(0, 0, 0, 5);
		gbc_lblSectorsPerTrack.gridx = 5;
		gbc_lblSectorsPerTrack.gridy = 1;
		panelGeometry1.add(lblSectorsPerTrack, gbc_lblSectorsPerTrack);

		lblSectorSize = new JLabel("000");
		lblSectorSize.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblSectorSize = new GridBagConstraints();
		gbc_lblSectorSize.gridx = 7;
		gbc_lblSectorSize.gridy = 1;
		panelGeometry1.add(lblSectorSize, gbc_lblSectorSize);

		btnNumberBase = new JButton("Hex");
		btnNumberBase.addActionListener(this);
		btnNumberBase.setActionCommand(AC_BTN_NUMBER_BASE);
		GridBagConstraints gbc_btnNumberBase = new GridBagConstraints();
		gbc_btnNumberBase.insets = new Insets(0, 0, 5, 5);
		gbc_btnNumberBase.gridx = 1;
		gbc_btnNumberBase.gridy = 0;
		panelGeomertry.add(btnNumberBase, gbc_btnNumberBase);

		JPanel panelTotals = new JPanel();
		panelTotals.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Disk Capacities",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelTotals = new GridBagConstraints();
		gbc_panelTotals.insets = new Insets(0, 0, 5, 0);
		gbc_panelTotals.anchor = GridBagConstraints.NORTH;
		gbc_panelTotals.gridx = 2;
		gbc_panelTotals.gridy = 0;
		panelGeomertry.add(panelTotals, gbc_panelTotals);
		GridBagLayout gbl_panelTotals = new GridBagLayout();
		gbl_panelTotals.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelTotals.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelTotals.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelTotals.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelTotals.setLayout(gbl_panelTotals);

		JLabel lblTT2 = new JLabel("Total Tracks");
		GridBagConstraints gbc_lblTT2 = new GridBagConstraints();
		gbc_lblTT2.insets = new Insets(0, 0, 5, 5);
		gbc_lblTT2.gridx = 1;
		gbc_lblTT2.gridy = 0;
		panelTotals.add(lblTT2, gbc_lblTT2);

		JLabel lblTS2 = new JLabel("Total Sectors");
		GridBagConstraints gbc_lblTS2 = new GridBagConstraints();
		gbc_lblTS2.insets = new Insets(0, 0, 5, 5);
		gbc_lblTS2.gridx = 3;
		gbc_lblTS2.gridy = 0;
		panelTotals.add(lblTS2, gbc_lblTS2);

		JLabel lblTB2 = new JLabel("Total Bytes");
		GridBagConstraints gbc_lblTB2 = new GridBagConstraints();
		gbc_lblTB2.insets = new Insets(0, 0, 5, 0);
		gbc_lblTB2.gridx = 5;
		gbc_lblTB2.gridy = 0;
		panelTotals.add(lblTB2, gbc_lblTB2);

		lblTotalTracks = new JLabel("000");
		lblTotalTracks.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblTotalTracks = new GridBagConstraints();
		gbc_lblTotalTracks.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotalTracks.gridx = 1;
		gbc_lblTotalTracks.gridy = 1;
		panelTotals.add(lblTotalTracks, gbc_lblTotalTracks);

		lblTotalSectors = new JLabel("0000");
		lblTotalSectors.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblTotalSectors = new GridBagConstraints();
		gbc_lblTotalSectors.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotalSectors.gridx = 3;
		gbc_lblTotalSectors.gridy = 1;
		panelTotals.add(lblTotalSectors, gbc_lblTotalSectors);

		lblTotalBytes = new JLabel("00000000");
		lblTotalBytes.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblTotalBytes = new GridBagConstraints();
		gbc_lblTotalBytes.gridx = 5;
		gbc_lblTotalBytes.gridy = 1;
		panelTotals.add(lblTotalBytes, gbc_lblTotalBytes);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(this);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 2;
		frmNativeDiskTool.getContentPane().add(tabbedPane, gbc_tabbedPane);

		panelPhysical = new JPanel();
		tabbedPane.addTab("Physical View", null, panelPhysical, null);
		GridBagLayout gbl_panelPhysical = new GridBagLayout();
		gbl_panelPhysical.columnWidths = new int[] { 0, 0, 5, 0, 0, 80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelPhysical.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelPhysical.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelPhysical.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panelPhysical.setLayout(gbl_panelPhysical);

		JPanel panelFile = new JPanel();
		tabbedPane.addTab("File View", null, panelFile, null);

		panelHTS = new JPanel();
		GridBagConstraints gbc_panelHTS = new GridBagConstraints();
		gbc_panelHTS.gridwidth = 14;
		gbc_panelHTS.insets = new Insets(0, 0, 5, 5);
		gbc_panelHTS.gridx = 1;
		gbc_panelHTS.gridy = 1;
		panelPhysical.add(panelHTS, gbc_panelHTS);
		GridBagLayout gbl_panelHTS = new GridBagLayout();
		gbl_panelHTS.columnWidths = new int[] { 0, 0, 0, 0, 20, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0 };
		gbl_panelHTS.rowHeights = new int[] { 0, 0 };
		gbl_panelHTS.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, Double.MIN_VALUE };
		gbl_panelHTS.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelHTS.setLayout(gbl_panelHTS);

		JLabel lblH3 = new JLabel("Head");
		lblH3.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblH3 = new GridBagConstraints();
		gbc_lblH3.insets = new Insets(0, 0, 0, 5);
		gbc_lblH3.gridx = 0;
		gbc_lblH3.gridy = 0;
		panelHTS.add(lblH3, gbc_lblH3);

		spinnerHeadHex = new Hex64KSpinner();
		spinnerHeadHex.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_spinnerHeadHex = new GridBagConstraints();
		gbc_spinnerHeadHex.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerHeadHex.gridx = 2;
		gbc_spinnerHeadHex.gridy = 0;
		panelHTS.add(spinnerHeadHex, gbc_spinnerHeadHex);

		spinnerHeadDecimal = new JSpinner();
		spinnerHeadDecimal.setModel(new SpinnerNumberModel(0, 0, 65535, 1));
		spinnerHeadDecimal.setPreferredSize(new Dimension(60, 20));
		spinnerHeadDecimal.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinnerHeadDecimal = new GridBagConstraints();
		gbc_spinnerHeadDecimal.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerHeadDecimal.gridx = 3;
		gbc_spinnerHeadDecimal.gridy = 0;
		panelHTS.add(spinnerHeadDecimal, gbc_spinnerHeadDecimal);

		JLabel lblT3 = new JLabel("Track");
		GridBagConstraints gbc_lblT3 = new GridBagConstraints();
		gbc_lblT3.insets = new Insets(0, 0, 0, 5);
		gbc_lblT3.gridx = 5;
		gbc_lblT3.gridy = 0;
		panelHTS.add(lblT3, gbc_lblT3);

		spinnerTrackHex = new Hex64KSpinner();
		spinnerTrackHex.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_spinnerTrackHex = new GridBagConstraints();
		gbc_spinnerTrackHex.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerTrackHex.gridx = 7;
		gbc_spinnerTrackHex.gridy = 0;
		panelHTS.add(spinnerTrackHex, gbc_spinnerTrackHex);

		spinnerTrackDecimal = new JSpinner();
		spinnerTrackDecimal.setModel(new SpinnerNumberModel(0, 0, 65535, 1));
		spinnerTrackDecimal.setPreferredSize(new Dimension(60, 20));
		spinnerTrackDecimal.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinnerTrackDecimal = new GridBagConstraints();
		gbc_spinnerTrackDecimal.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerTrackDecimal.gridx = 8;
		gbc_spinnerTrackDecimal.gridy = 0;
		panelHTS.add(spinnerTrackDecimal, gbc_spinnerTrackDecimal);

		JLabel lblS3 = new JLabel("Sector");
		GridBagConstraints gbc_lblS3 = new GridBagConstraints();
		gbc_lblS3.insets = new Insets(0, 0, 0, 5);
		gbc_lblS3.gridx = 10;
		gbc_lblS3.gridy = 0;
		panelHTS.add(lblS3, gbc_lblS3);

		spinnerSectorHex = new Hex64KSpinner();
		spinnerSectorHex.setModel(new SpinnerNumberModel(1, 1, 65535, 1));
		spinnerSectorHex.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_spinnerSectorHex = new GridBagConstraints();
		gbc_spinnerSectorHex.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerSectorHex.gridx = 11;
		gbc_spinnerSectorHex.gridy = 0;
		panelHTS.add(spinnerSectorHex, gbc_spinnerSectorHex);

		spinnerSectorDecimal = new JSpinner();
		spinnerSectorDecimal.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		spinnerSectorDecimal.setPreferredSize(new Dimension(60, 20));
		spinnerSectorDecimal.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinnerSectorDecimal = new GridBagConstraints();
		gbc_spinnerSectorDecimal.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerSectorDecimal.gridx = 12;
		gbc_spinnerSectorDecimal.gridy = 0;
		panelHTS.add(spinnerSectorDecimal, gbc_spinnerSectorDecimal);

		JButton btnDisplayPhysical = new JButton("Display");
		btnDisplayPhysical.addActionListener(this);
		btnDisplayPhysical.setActionCommand(AC_BTN_DISPLAY_PHYSICAL);
		GridBagConstraints gbc_btnDisplayPhysical = new GridBagConstraints();
		gbc_btnDisplayPhysical.anchor = GridBagConstraints.NORTH;
		gbc_btnDisplayPhysical.gridx = 14;
		gbc_btnDisplayPhysical.gridy = 0;
		panelHTS.add(btnDisplayPhysical, gbc_btnDisplayPhysical);

		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(680, 400));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 18;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 2;
		panelPhysical.add(scrollPane, gbc_scrollPane);

		txtDisplay = new JTextArea();
		txtDisplay.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPane.setViewportView(txtDisplay);

		JPanel panelSkipButtons = new JPanel();
		GridBagConstraints gbc_panelSkipButtons = new GridBagConstraints();
		gbc_panelSkipButtons.anchor = GridBagConstraints.SOUTH;
		gbc_panelSkipButtons.gridwidth = 13;
		gbc_panelSkipButtons.insets = new Insets(0, 0, 0, 5);
		gbc_panelSkipButtons.gridx = 1;
		gbc_panelSkipButtons.gridy = 3;
		panelPhysical.add(panelSkipButtons, gbc_panelSkipButtons);
		GridBagLayout gbl_panelSkipButtons = new GridBagLayout();
		gbl_panelSkipButtons.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelSkipButtons.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelSkipButtons.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelSkipButtons.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelSkipButtons.setLayout(gbl_panelSkipButtons);

		lblLogicalSector = new JLabel();
		lblLogicalSector.setText("0");
		lblLogicalSector.setPreferredSize(new Dimension(200, 20));
		lblLogicalSector.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_ftfLogicalSector = new GridBagConstraints();
		gbc_ftfLogicalSector.gridwidth = 7;
		gbc_ftfLogicalSector.insets = new Insets(0, 0, 5, 5);
		gbc_ftfLogicalSector.fill = GridBagConstraints.HORIZONTAL;
		gbc_ftfLogicalSector.gridx = 1;
		gbc_ftfLogicalSector.gridy = 1;
		panelSkipButtons.add(lblLogicalSector, gbc_ftfLogicalSector);

		JButton btnFirst = new JButton("<<");
		btnFirst.addActionListener(this);
		btnFirst.setActionCommand(AC_BTN_FIRST);
		GridBagConstraints gbc_btnFirst = new GridBagConstraints();
		gbc_btnFirst.insets = new Insets(0, 0, 0, 5);
		gbc_btnFirst.gridx = 1;
		gbc_btnFirst.gridy = 2;
		panelSkipButtons.add(btnFirst, gbc_btnFirst);

		JButton btnPrevious = new JButton("<");
		btnPrevious.addActionListener(this);
		btnPrevious.setActionCommand(AC_BTN_PREVIOUS);
		GridBagConstraints gbc_btnPrevious = new GridBagConstraints();
		gbc_btnPrevious.insets = new Insets(0, 0, 0, 5);
		gbc_btnPrevious.gridx = 3;
		gbc_btnPrevious.gridy = 2;
		panelSkipButtons.add(btnPrevious, gbc_btnPrevious);

		JButton btnNext = new JButton(">");
		btnNext.addActionListener(this);
		btnNext.setActionCommand(AC_BTN_NEXT);
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.insets = new Insets(0, 0, 0, 5);
		gbc_btnNext.gridx = 5;
		gbc_btnNext.gridy = 2;
		panelSkipButtons.add(btnNext, gbc_btnNext);

		JButton btnLast = new JButton(">>");
		btnLast.addActionListener(this);
		btnLast.setActionCommand(AC_BTN_LAST);
		GridBagConstraints gbc_btnLast = new GridBagConstraints();
		gbc_btnLast.gridx = 7;
		gbc_btnLast.gridy = 2;
		panelSkipButtons.add(btnLast, gbc_btnLast);

		JPanel panelDirectory = new JPanel();
		tabbedPane.addTab("Directory View", null, panelDirectory, null);
		GridBagLayout gbl_panelDirectory = new GridBagLayout();
		gbl_panelDirectory.columnWidths = new int[] { 0, 531, 0, 0, 90, 0 };
		gbl_panelDirectory.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelDirectory.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelDirectory.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelDirectory.setLayout(gbl_panelDirectory);

		JPanel panelFileMetrics = new JPanel();
		GridBagConstraints gbc_panelFileMetrics = new GridBagConstraints();
		gbc_panelFileMetrics.gridwidth = 4;
		gbc_panelFileMetrics.insets = new Insets(0, 0, 5, 0);
		gbc_panelFileMetrics.fill = GridBagConstraints.VERTICAL;
		gbc_panelFileMetrics.gridx = 1;
		gbc_panelFileMetrics.gridy = 0;
		panelDirectory.add(panelFileMetrics, gbc_panelFileMetrics);
		GridBagLayout gbl_panelFileMetrics = new GridBagLayout();
		gbl_panelFileMetrics.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelFileMetrics.rowHeights = new int[] { 0, 0 };
		gbl_panelFileMetrics.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panelFileMetrics.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFileMetrics.setLayout(gbl_panelFileMetrics);

		JLabel lblTracksBeforeDirectory = new JLabel("Tracks Before Directory");
		GridBagConstraints gbc_lblTracksBeforeDirectory = new GridBagConstraints();
		gbc_lblTracksBeforeDirectory.insets = new Insets(0, 0, 0, 5);
		gbc_lblTracksBeforeDirectory.gridx = 1;
		gbc_lblTracksBeforeDirectory.gridy = 0;
		panelFileMetrics.add(lblTracksBeforeDirectory, gbc_lblTracksBeforeDirectory);

		spinnerTrackBeforeDirectoryHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerTrackBeforeDirectoryHex = new GridBagConstraints();
		gbc_spinnerTrackBeforeDirectoryHex.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerTrackBeforeDirectoryHex.gridx = 3;
		gbc_spinnerTrackBeforeDirectoryHex.gridy = 0;
		panelFileMetrics.add(spinnerTrackBeforeDirectoryHex, gbc_spinnerTrackBeforeDirectoryHex);

		spinnerTrackBeforeDirectoryDecimal = new JSpinner();
		spinnerTrackBeforeDirectoryDecimal.setModel(new SpinnerNumberModel(1, 0, 65535, 1));
		GridBagConstraints gbc_spinnerTrackBeforeDirectoryDecimal = new GridBagConstraints();
		gbc_spinnerTrackBeforeDirectoryDecimal.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerTrackBeforeDirectoryDecimal.gridx = 4;
		gbc_spinnerTrackBeforeDirectoryDecimal.gridy = 0;
		panelFileMetrics.add(spinnerTrackBeforeDirectoryDecimal, gbc_spinnerTrackBeforeDirectoryDecimal);

		JLabel lblLogicalBlockSize = new JLabel("Logical Block Size in Sectors");
		GridBagConstraints gbc_lblLogicalBlockSize = new GridBagConstraints();
		gbc_lblLogicalBlockSize.insets = new Insets(0, 0, 0, 5);
		gbc_lblLogicalBlockSize.gridx = 6;
		gbc_lblLogicalBlockSize.gridy = 0;
		panelFileMetrics.add(lblLogicalBlockSize, gbc_lblLogicalBlockSize);

		spinnerLogicalBlockSizeHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerLogicalBlockSizeHex = new GridBagConstraints();
		gbc_spinnerLogicalBlockSizeHex.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerLogicalBlockSizeHex.gridx = 8;
		gbc_spinnerLogicalBlockSizeHex.gridy = 0;
		panelFileMetrics.add(spinnerLogicalBlockSizeHex, gbc_spinnerLogicalBlockSizeHex);

		spinnerLogicalBlockSizeDecimal = new JSpinner();
		spinnerLogicalBlockSizeDecimal.setModel(new SpinnerNumberModel(4, 1, 65535, 1));
		GridBagConstraints gbc_spinnerLogicalBlockSizeDecimal = new GridBagConstraints();
		gbc_spinnerLogicalBlockSizeDecimal.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerLogicalBlockSizeDecimal.gridx = 9;
		gbc_spinnerLogicalBlockSizeDecimal.gridy = 0;
		panelFileMetrics.add(spinnerLogicalBlockSizeDecimal, gbc_spinnerLogicalBlockSizeDecimal);

		lblMaxDirectoryEntries = new JLabel("Max Directory Entries");
		GridBagConstraints gbc_lblMaxDirectoryEntries = new GridBagConstraints();
		gbc_lblMaxDirectoryEntries.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaxDirectoryEntries.gridx = 11;
		gbc_lblMaxDirectoryEntries.gridy = 0;
		panelFileMetrics.add(lblMaxDirectoryEntries, gbc_lblMaxDirectoryEntries);

		spinnerMaxDirectoryEntriesHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerMaxDirectoryEntriesHex = new GridBagConstraints();
		gbc_spinnerMaxDirectoryEntriesHex.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerMaxDirectoryEntriesHex.gridx = 13;
		gbc_spinnerMaxDirectoryEntriesHex.gridy = 0;
		panelFileMetrics.add(spinnerMaxDirectoryEntriesHex, gbc_spinnerMaxDirectoryEntriesHex);

		spinnerMaxDirectoryEntriesDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerMaxDirectoryEntriesDecimal = new GridBagConstraints();
		gbc_spinnerMaxDirectoryEntriesDecimal.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerMaxDirectoryEntriesDecimal.gridx = 14;
		gbc_spinnerMaxDirectoryEntriesDecimal.gridy = 0;
		panelFileMetrics.add(spinnerMaxDirectoryEntriesDecimal, gbc_spinnerMaxDirectoryEntriesDecimal);

		scrollDirectoryTable = new JScrollPane();
		GridBagConstraints gbc_scrollDirectoryTable = new GridBagConstraints();
		gbc_scrollDirectoryTable.insets = new Insets(0, 0, 0, 5);
		gbc_scrollDirectoryTable.fill = GridBagConstraints.BOTH;
		gbc_scrollDirectoryTable.gridx = 1;
		gbc_scrollDirectoryTable.gridy = 1;
		panelDirectory.add(scrollDirectoryTable, gbc_scrollDirectoryTable);

		panelDirRaw = new JPanel();
		GridBagConstraints gbc_panelDirRaw = new GridBagConstraints();
		gbc_panelDirRaw.fill = GridBagConstraints.BOTH;
		gbc_panelDirRaw.gridx = 4;
		gbc_panelDirRaw.gridy = 1;
		panelDirectory.add(panelDirRaw, gbc_panelDirRaw);
		GridBagLayout gbl_panelDirRaw = new GridBagLayout();
		gbl_panelDirRaw.columnWidths = new int[] { 0, 30, 46, 0 };
		gbl_panelDirRaw.rowHeights = new int[] { 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0 };
		gbl_panelDirRaw.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelDirRaw.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		panelDirRaw.setLayout(gbl_panelDirRaw);

		JLabel lblDR1 = new JLabel("User [0]");
		GridBagConstraints gbc_lblDR1 = new GridBagConstraints();
		gbc_lblDR1.anchor = GridBagConstraints.EAST;
		gbc_lblDR1.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR1.gridx = 0;
		gbc_lblDR1.gridy = 1;
		panelDirRaw.add(lblDR1, gbc_lblDR1);

		lblDirUser = new JLabel("00");
		lblDirUser.setForeground(Color.BLUE);
		lblDirUser.setFont(new Font("Courier New", Font.BOLD, 16));
		lblDirUser.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblDirUser = new GridBagConstraints();
		gbc_lblDirUser.anchor = GridBagConstraints.WEST;
		gbc_lblDirUser.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirUser.gridx = 2;
		gbc_lblDirUser.gridy = 1;
		panelDirRaw.add(lblDirUser, gbc_lblDirUser);

		JLabel lblDR2 = new JLabel("Name [1-8]");
		GridBagConstraints gbc_lblDR2 = new GridBagConstraints();
		gbc_lblDR2.anchor = GridBagConstraints.EAST;
		gbc_lblDR2.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR2.gridx = 0;
		gbc_lblDR2.gridy = 3;
		panelDirRaw.add(lblDR2, gbc_lblDR2);

		lblDirName = new JLabel("00 00 00 00 00 00 00 00 ");
		lblDirName.setForeground(Color.BLUE);
		lblDirName.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirName.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirName = new GridBagConstraints();
		gbc_lblDirName.anchor = GridBagConstraints.WEST;
		gbc_lblDirName.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirName.gridx = 2;
		gbc_lblDirName.gridy = 3;
		panelDirRaw.add(lblDirName, gbc_lblDirName);

		JLabel lblDR3 = new JLabel("Type[9-11)");
		GridBagConstraints gbc_lblDR3 = new GridBagConstraints();
		gbc_lblDR3.anchor = GridBagConstraints.EAST;
		gbc_lblDR3.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR3.gridx = 0;
		gbc_lblDR3.gridy = 4;
		panelDirRaw.add(lblDR3, gbc_lblDR3);

		lblDirType = new JLabel("00");
		lblDirType.setForeground(Color.BLUE);
		lblDirType.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirType.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirType = new GridBagConstraints();
		gbc_lblDirType.anchor = GridBagConstraints.WEST;
		gbc_lblDirType.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirType.gridx = 2;
		gbc_lblDirType.gridy = 4;
		panelDirRaw.add(lblDirType, gbc_lblDirType);

		JLabel lblDR4 = new JLabel("EX [12]");
		GridBagConstraints gbc_lblDR4 = new GridBagConstraints();
		gbc_lblDR4.anchor = GridBagConstraints.EAST;
		gbc_lblDR4.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR4.gridx = 0;
		gbc_lblDR4.gridy = 6;
		panelDirRaw.add(lblDR4, gbc_lblDR4);

		lblDirEX = new JLabel("00");
		lblDirEX.setForeground(Color.BLUE);
		lblDirEX.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirEX.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirEX = new GridBagConstraints();
		gbc_lblDirEX.anchor = GridBagConstraints.WEST;
		gbc_lblDirEX.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirEX.gridx = 2;
		gbc_lblDirEX.gridy = 6;
		panelDirRaw.add(lblDirEX, gbc_lblDirEX);

		JLabel lblDR5 = new JLabel("S1 [13]");
		GridBagConstraints gbc_lblDR5 = new GridBagConstraints();
		gbc_lblDR5.anchor = GridBagConstraints.EAST;
		gbc_lblDR5.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR5.gridx = 0;
		gbc_lblDR5.gridy = 7;
		panelDirRaw.add(lblDR5, gbc_lblDR5);

		lblDirS1 = new JLabel("00");
		lblDirS1.setForeground(Color.BLUE);
		lblDirS1.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirS1.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirS1 = new GridBagConstraints();
		gbc_lblDirS1.anchor = GridBagConstraints.WEST;
		gbc_lblDirS1.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirS1.gridx = 2;
		gbc_lblDirS1.gridy = 7;
		panelDirRaw.add(lblDirS1, gbc_lblDirS1);

		JLabel lblDR6 = new JLabel("S2 [14]");
		GridBagConstraints gbc_lblDR6 = new GridBagConstraints();
		gbc_lblDR6.anchor = GridBagConstraints.EAST;
		gbc_lblDR6.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR6.gridx = 0;
		gbc_lblDR6.gridy = 8;
		panelDirRaw.add(lblDR6, gbc_lblDR6);

		lblDirS2 = new JLabel("00");
		lblDirS2.setForeground(Color.BLUE);
		lblDirS2.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirS2.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirS2 = new GridBagConstraints();
		gbc_lblDirS2.anchor = GridBagConstraints.WEST;
		gbc_lblDirS2.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirS2.gridx = 2;
		gbc_lblDirS2.gridy = 8;
		panelDirRaw.add(lblDirS2, gbc_lblDirS2);

		JLabel lblDR7 = new JLabel("RC [15]");
		GridBagConstraints gbc_lblDR7 = new GridBagConstraints();
		gbc_lblDR7.anchor = GridBagConstraints.EAST;
		gbc_lblDR7.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR7.gridx = 0;
		gbc_lblDR7.gridy = 10;
		panelDirRaw.add(lblDR7, gbc_lblDR7);

		lblDirRC = new JLabel("00");
		lblDirRC.setForeground(Color.BLUE);
		lblDirRC.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirRC.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirRC = new GridBagConstraints();
		gbc_lblDirRC.anchor = GridBagConstraints.WEST;
		gbc_lblDirRC.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirRC.gridx = 2;
		gbc_lblDirRC.gridy = 10;
		panelDirRaw.add(lblDirRC, gbc_lblDirRC);

		JLabel lblDR8 = new JLabel("Allocation");
		GridBagConstraints gbc_lblDR8 = new GridBagConstraints();
		gbc_lblDR8.anchor = GridBagConstraints.EAST;
		gbc_lblDR8.insets = new Insets(0, 0, 5, 5);
		gbc_lblDR8.gridx = 0;
		gbc_lblDR8.gridy = 12;
		panelDirRaw.add(lblDR8, gbc_lblDR8);

		lblDirAllocation1 = new JLabel("00 00 00 00 00 00 00 00");
		lblDirAllocation1.setForeground(Color.BLUE);
		lblDirAllocation1.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirAllocation1.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirAllocation1 = new GridBagConstraints();
		gbc_lblDirAllocation1.anchor = GridBagConstraints.WEST;
		gbc_lblDirAllocation1.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirAllocation1.gridx = 2;
		gbc_lblDirAllocation1.gridy = 12;
		panelDirRaw.add(lblDirAllocation1, gbc_lblDirAllocation1);

		JLabel lblDR9 = new JLabel("[16-31]");
		GridBagConstraints gbc_lblDR9 = new GridBagConstraints();
		gbc_lblDR9.anchor = GridBagConstraints.EAST;
		gbc_lblDR9.insets = new Insets(0, 0, 0, 5);
		gbc_lblDR9.gridx = 0;
		gbc_lblDR9.gridy = 13;
		panelDirRaw.add(lblDR9, gbc_lblDR9);

		lblDirAllocation2 = new JLabel("00 00 00 00 00 00 00 00");
		lblDirAllocation2.setForeground(Color.BLUE);
		lblDirAllocation2.setHorizontalAlignment(SwingConstants.CENTER);
		lblDirAllocation2.setFont(new Font("Courier New", Font.BOLD, 16));
		GridBagConstraints gbc_lblDirAllocation2 = new GridBagConstraints();
		gbc_lblDirAllocation2.anchor = GridBagConstraints.WEST;
		gbc_lblDirAllocation2.gridx = 2;
		gbc_lblDirAllocation2.gridy = 13;
		panelDirRaw.add(lblDirAllocation2, gbc_lblDirAllocation2);

		JMenuBar menuBar = new JMenuBar();
		frmNativeDiskTool.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		mnuFileNewDisk = new JMenuItem("New");
		mnuFileNewDisk.addActionListener(this);
		mnuFileNewDisk.setActionCommand(AC_MNU_FILE_NEW);
		mnuFile.add(mnuFileNewDisk);

		mnuFileOpenDisk = new JMenuItem("Open File...");
		mnuFileOpenDisk.addActionListener(this);
		mnuFileOpenDisk.setActionCommand(AC_MNU_FILE_OPEN);
		mnuFile.add(mnuFileOpenDisk);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		mnuFileCloseDisk = new JMenuItem("Close");
		mnuFileCloseDisk.addActionListener(this);
		mnuFileCloseDisk.setActionCommand(AC_MNU_FILE_CLOSE);
		mnuFile.add(mnuFileCloseDisk);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);

		mnuFileSaveDisk = new JMenuItem("Save");
		mnuFileSaveDisk.addActionListener(this);
		mnuFileSaveDisk.setActionCommand(AC_MNU_FILE_SAVE);
		mnuFile.add(mnuFileSaveDisk);

		mnuFileSaveAsDisk = new JMenuItem("Save As...");
		mnuFileSaveAsDisk.addActionListener(this);
		mnuFileSaveAsDisk.setActionCommand(AC_MNU_FILE_SAVE_AS);
		mnuFile.add(mnuFileSaveAsDisk);

		JSeparator separator_2 = new JSeparator();
		mnuFile.add(separator_2);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.addActionListener(this);
		mnuFileExit.setActionCommand(AC_MNU_FILE_EXIT);
		mnuFile.add(mnuFileExit);
	}

}
