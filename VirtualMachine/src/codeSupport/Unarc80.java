package codeSupport;

import java.awt.EventQueue;
import java.awt.FontMetrics;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JTable;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JTabbedPane;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import java.awt.Color;

import javax.swing.JScrollPane;

public class Unarc80 implements ActionListener {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Unarc80 window = new Unarc80();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String actionCommand = actionEvent.getActionCommand();

		switch (actionCommand) {
		case AC_MNU_FILE_OPEN:
			sourceFile = getNativeFile();
			openSourceFile(sourceFile);
			fileIsOpen(sourceFile != null);
			break;
		case AC_MNU_FILE_CLOSE:
			sourceFile = null;
			fileIsOpen(sourceFile != null);
			break;
		case AC_MNU_FILE_EXIT:
			appClose();
			break;
		}// switch

	}// actionPerformed

	private void openSourceFile(File sourceFile) {
		if (sourceFile == null) {
			JOptionPane.showMessageDialog(null, " Need to select a source file", "Open Source File",
					JOptionPane.WARNING_MESSAGE);
			return;
		}// if

		setTocTable();
		modelToc = (DefaultTableModel) tocTable.getModel();
		arcHeaders.clear();
		FileChannel fcIn = null;
		FileInputStream fIn = null;
		byte[] arcHeaderRaw;
		MappedByteBuffer mbb;
		int arcHeaderBase = 0;
		try {
			fIn = new FileInputStream(sourceFile);
			fcIn = fIn.getChannel();
			arcHeaderRaw = new byte[ARC_HEADER_SIZE];
			mbb = fcIn.map(FileChannel.MapMode.READ_ONLY, 0, sourceSize);
			int index = 0;
			ArrayList<String> testArryList = new ArrayList<String>();
			ArcHeader arcHeader;
			while (isHeader(mbb, arcHeaderBase)) {
				mbb.position(arcHeaderBase);
				mbb.get(arcHeaderRaw, 0, ARC_HEADER_SIZE);
				arcHeader = new ArcHeader(arcHeaderBase, arcHeaderRaw);
				arcHeaders.add(arcHeader);
				testArryList.add(arcHeader.getRawString());
				String name = arcHeader.getName();
				int stored = arcHeader.getStored();
				int length = arcHeader.getLength();
				int saved = arcHeader.getSaved();

				modelToc.insertRow(index, new Object[] { index, name, arcHeaderBase, stored, length, 0 });
				System.out.printf("File name is : %s, base is : %06X Length is : %,7d, Stored is :%,7d%n",
						arcHeader.getNameFixed(), arcHeader.getBaseAddress(), length, stored);
				arcHeaderBase += stored + ARC_HEADER_SIZE;
				int a = 0;
				index++;

			}// while
			tocTable.setRowSelectionInterval(0, 0);
			fcIn.close();
			fIn.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}// try
	}// openSourceFile

	boolean isHeader(MappedByteBuffer mbb, int arcHeaderStart) {

		if (arcHeaderStart >= (sourceSize - 2)) {
			return false; // end of file
		}
		mbb.position(arcHeaderStart);
		byte[] type = new byte[2];
		mbb.get(type, 0, 2);
		if (Arrays.equals(type, HEADER_FLAG)) {
			return true;
		} else {
			return false;
		}// if

	}

	private void fileIsOpen(boolean state) {
		mnuFileOpen.setEnabled(!state);
		mnuFileClose.setEnabled(state);

		if (state) {
			lblSourceFileName.setText(sourceFile.getName());
			lblSourceFileName.setToolTipText(sourceFile.getAbsolutePath());

		} else {
			lblSourceFileName.setText(NO_FILE_SELECTED);
			lblSourceFileName.setToolTipText(NO_FILE_SELECTED);
		}// if

	}// fileIsOpen

	private File getNativeFile() {
		sourceFile = pickNativeFile();

		if (sourceFile != null) {
			sourceSize = sourceFile.length();
		}// if
		return sourceFile;
	}// getNativeFile

	private File pickNativeFile() {
		return pickNativeFile(false);
	}// pickNativeFile

	private File pickNativeFile(boolean write) {
		String fileLocation = ".";
		Path sourcePath = Paths.get(fileLocation);

		JFileChooser nativeChooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		nativeChooser.setMultiSelectionEnabled(false);
		if (nativeChooser.showDialog(null, "Select the file") != JFileChooser.APPROVE_OPTION) {
			return null;
		}// if
		return nativeChooser.getSelectedFile();
	}// pickNativeFile

	private JTable setTocTable() {
		if (tocTable != null) {
			tocTable = null;
		}
		tocTable = new JTable(new DefaultTableModel(columnNames, 0)) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tocTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tocTable.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tocTable.getSelectionModel().addListSelectionListener(new RowListener());
		// dirTable.setName(AC_DIRECTORY_TABLE);
		adjustTableLook(tocTable);

		scrollPaneTOC.setViewportView(tocTable);
		// fillDirectoryTable(tocTable);
		// tocTable.setRowSelectionInterval(0, 0);

		return tocTable;
	}

	private void adjustTableLook(JTable table) {
		Font realColumnFont = table.getFont();
		FontMetrics fontMetrics = table.getFontMetrics(realColumnFont);

		int charWidth = fontMetrics.stringWidth("W");

		TableColumnModel tableColumn = table.getColumnModel();
		tableColumn.getColumn(0).setPreferredWidth(charWidth * 5); // Index
		tableColumn.getColumn(1).setPreferredWidth(charWidth * 15); // Name
		tableColumn.getColumn(2).setPreferredWidth(charWidth * 9); // Base
		tableColumn.getColumn(3).setPreferredWidth(charWidth * 8); // Stored
		tableColumn.getColumn(4).setPreferredWidth(charWidth * 9); // Length
		tableColumn.getColumn(5).setPreferredWidth(charWidth * 4); // CRC

		DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
		rightAlign.setHorizontalAlignment(JLabel.RIGHT);
		tableColumn.getColumn(0).setCellRenderer(rightAlign); // Index
		tableColumn.getColumn(2).setCellRenderer(rightAlign); // Base
		tableColumn.getColumn(3).setCellRenderer(rightAlign); // Stored
		tableColumn.getColumn(4).setCellRenderer(rightAlign); // Length
		tableColumn.getColumn(5).setCellRenderer(rightAlign); // CRC

		DefaultTableCellRenderer leftAlign = new DefaultTableCellRenderer();
		leftAlign.setHorizontalAlignment(JLabel.LEFT);
		tableColumn.getColumn(1).setCellRenderer(leftAlign); // Name

		// DefaultTableCellRenderer centerAlign = new DefaultTableCellRenderer();
		// centerAlign.setHorizontalAlignment(JLabel.CENTER);
		// tableColumn.getColumn(4).setCellRenderer(centerAlign);
		// tableColumn.getColumn(5).setCellRenderer(centerAlign);
	}// adjustTableLook

	// ---------------------------------------------------------------------------
	public void appClose() {
		System.exit(0);
	}

	public void appInit() {
		arcHeaders = new ArrayList<ArcHeader>();

		columnNames = new Object[] { "Index", "Name", "Base", "Stored", "Length", "CRC" };
	}

	/**
	 * Create the application.
	 */
	public Unarc80() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		frame.setBounds(100, 100, 770, 696);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		JPanel panelSourceFileName = new JPanel();
		GridBagConstraints gbc_panelSourceFileName = new GridBagConstraints();
		gbc_panelSourceFileName.insets = new Insets(0, 0, 5, 0);
		gbc_panelSourceFileName.fill = GridBagConstraints.VERTICAL;
		gbc_panelSourceFileName.gridx = 0;
		gbc_panelSourceFileName.gridy = 0;
		frame.getContentPane().add(panelSourceFileName, gbc_panelSourceFileName);
		GridBagLayout gbl_panelSourceFileName = new GridBagLayout();
		gbl_panelSourceFileName.columnWidths = new int[] { 0, 0 };
		gbl_panelSourceFileName.rowHeights = new int[] { 0, 0 };
		gbl_panelSourceFileName.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelSourceFileName.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelSourceFileName.setLayout(gbl_panelSourceFileName);

		lblSourceFileName = new JLabel("<No File Selected>");
		lblSourceFileName.setFont(new Font("Tahoma", Font.BOLD, 15));
		GridBagConstraints gbc_lblSourceFileName = new GridBagConstraints();
		gbc_lblSourceFileName.gridx = 0;
		gbc_lblSourceFileName.gridy = 0;
		panelSourceFileName.add(lblSourceFileName, gbc_lblSourceFileName);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);

		JPanel tabTOC = new JPanel();
		tabbedPane.addTab("TOC", null, tabTOC, null);
		GridBagLayout gbl_tabTOC = new GridBagLayout();
		gbl_tabTOC.columnWidths = new int[] { 0, 0 };
		gbl_tabTOC.rowHeights = new int[] { 0, 0, 0 };
		gbl_tabTOC.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabTOC.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		tabTOC.setLayout(gbl_tabTOC);

		JPanel pnlHeaderInfo = new JPanel();
		pnlHeaderInfo.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		GridBagConstraints gbc_pnlHeaderInfo = new GridBagConstraints();
		gbc_pnlHeaderInfo.insets = new Insets(0, 0, 5, 0);
		gbc_pnlHeaderInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlHeaderInfo.gridx = 0;
		gbc_pnlHeaderInfo.gridy = 0;
		tabTOC.add(pnlHeaderInfo, gbc_pnlHeaderInfo);
		GridBagLayout gbl_pnlHeaderInfo = new GridBagLayout();
		gbl_pnlHeaderInfo.columnWidths = new int[] { 0, 0 };
		gbl_pnlHeaderInfo.rowHeights = new int[] { 0, 0, 0 };
		gbl_pnlHeaderInfo.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlHeaderInfo.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		pnlHeaderInfo.setLayout(gbl_pnlHeaderInfo);

		JPanel pnlHeaderLabel = new JPanel();
		pnlHeaderLabel.setBorder(null);
		GridBagConstraints gbc_pnlHeaderLabel = new GridBagConstraints();
		gbc_pnlHeaderLabel.insets = new Insets(0, 0, 5, 0);
		gbc_pnlHeaderLabel.fill = GridBagConstraints.BOTH;
		gbc_pnlHeaderLabel.gridx = 0;
		gbc_pnlHeaderLabel.gridy = 0;
		pnlHeaderInfo.add(pnlHeaderLabel, gbc_pnlHeaderLabel);
		GridBagLayout gbl_pnlHeaderLabel = new GridBagLayout();
		gbl_pnlHeaderLabel.columnWidths = new int[] { 0, 0 };
		gbl_pnlHeaderLabel.rowHeights = new int[] { 0, 0 };
		gbl_pnlHeaderLabel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlHeaderLabel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlHeaderLabel.setLayout(gbl_pnlHeaderLabel);

		JLabel label1 = new JLabel(
				"00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C");
		label1.setFont(new Font("Courier New", Font.PLAIN, 12));
		GridBagConstraints gbc_label1 = new GridBagConstraints();
		gbc_label1.gridx = 0;
		gbc_label1.gridy = 0;
		pnlHeaderLabel.add(label1, gbc_label1);

		JPanel pnlHeaderValues = new JPanel();
		pnlHeaderValues.setBorder(null);
		GridBagConstraints gbc_pnlHeaderValues = new GridBagConstraints();
		gbc_pnlHeaderValues.fill = GridBagConstraints.BOTH;
		gbc_pnlHeaderValues.gridx = 0;
		gbc_pnlHeaderValues.gridy = 1;
		pnlHeaderInfo.add(pnlHeaderValues, gbc_pnlHeaderValues);
		GridBagLayout gbl_pnlHeaderValues = new GridBagLayout();
		gbl_pnlHeaderValues.columnWidths = new int[] { 0, 0 };
		gbl_pnlHeaderValues.rowHeights = new int[] { 0, 0 };
		gbl_pnlHeaderValues.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlHeaderValues.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlHeaderValues.setLayout(gbl_pnlHeaderValues);

		lblHeaderValues = new JLabel(
				"00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D");
		lblHeaderValues.setFont(new Font("Courier New", Font.PLAIN, 12));
		GridBagConstraints gbc_lblHeaderValues = new GridBagConstraints();
		gbc_lblHeaderValues.gridx = 0;
		gbc_lblHeaderValues.gridy = 0;
		pnlHeaderValues.add(lblHeaderValues, gbc_lblHeaderValues);

		scrollPaneTOC = new JScrollPane();
		GridBagConstraints gbc_scrollPaneTOC = new GridBagConstraints();
		gbc_scrollPaneTOC.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneTOC.gridx = 0;
		gbc_scrollPaneTOC.gridy = 1;
		tabTOC.add(scrollPaneTOC, gbc_scrollPaneTOC);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		mnuFileOpen = new JMenuItem("Open...");
		mnuFileOpen.setActionCommand(AC_MNU_FILE_OPEN);
		mnuFileOpen.addActionListener(this);
		mnuFile.add(mnuFileOpen);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		mnuFileClose = new JMenuItem("Close");
		mnuFileClose.setActionCommand(AC_MNU_FILE_CLOSE);
		mnuFileClose.addActionListener(this);
		mnuFile.add(mnuFileClose);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setActionCommand(AC_MNU_FILE_EXIT);
		mnuFileExit.addActionListener(this);
		mnuFile.add(mnuFileExit);

		appInit();
	}

	// --------------------------------------------------------------------------------

	private final static String AC_MNU_FILE_OPEN = "MenuFileOpen";
	private final static String AC_MNU_FILE_CLOSE = "MenuFileClose";
	private final static String AC_MNU_FILE_EXIT = "MenuFileExit";

	private final static String NO_FILE_SELECTED = "<No File Selected>";

	private final static int ARC_HEADER_SIZE = 0x1D;

	private final static byte[] HEADER_FLAG = new byte[] { 0x1A, 0x08 };

	// ------------------------------------------------------------
	// private String selectedAbsolutePath;
	// private FileChannel fcIn;
	private File sourceFile = null;
	// private String sourceFileAbsoluteName = null;
	private long sourceSize;
	private JLabel lblSourceFileName;
	private JMenuItem mnuFileOpen;
	private JMenuItem mnuFileClose;
	private JScrollPane scrollPaneTOC;

	private JTable tocTable;
	private DefaultTableModel modelToc;

	Object[] columnNames;
	ArrayList<ArcHeader> arcHeaders;
	String[] rawString;
	private JLabel lblHeaderValues;

	// ------------------------------------------------------------

	public class ArcHeader {
		private Byte[] rawData = new Byte[ARC_HEADER_SIZE];

		private int baseAddress;
		public boolean isValid;

		private ArcHeader(int baseAddress, byte[] rawDataIn) {
			if (rawData.length != (ARC_HEADER_SIZE)) {
				this.isValid = false;
				return;
			}//
			this.isValid = true;
			this.baseAddress = baseAddress;
			for (int i = 0; i < ARC_HEADER_SIZE; i++) {
				this.rawData[i] = rawDataIn[i];
			}
			// this.rawData = rawDataIn;
		}// constructor

		public String toString() {
			return getNameFixed();
		}// toString

		public int getBaseAddress() {
			return this.baseAddress;
		}// getBaseAddress

		public String getName() {
			int index = 2;
			StringBuilder sb = new StringBuilder();
			while (rawData[index] != 00) {
				sb.append((char) ((byte) rawData[index++]));
			}// while
			return sb.toString();

		}// getName

		public String getNameFixed() {
			String[] nameParts = getName().split("\\.");
			String left = nameParts[0];
			String right = (nameParts.length > 1) ? nameParts[1] : "";
			return String.format("%-8s.%-3s", left, right);
		}// getNameFixed

		public int getLength() {
			return calculateValue(0x19);
		}// getLength

		public int getStored() {
			return calculateValue(0x0F);
		}// getStored

		private int calculateValue(int index) {
			int ans = rawData[index] & 0xFF;
			ans |= (rawData[index + 1] << 8) & 0xFFFF;
			ans |= (rawData[index + 2] << 16) & 0xFFFFFF;
			ans |= (rawData[index + 3] << 24) & 0xFFFFFFFF;
			return ans;
		}// calculateValue

		public int getSaved() {
			float l = (float) getLength();
			float s = (float) getStored();
			float ans = s / l;
			ans = (1.00f - ans) * 100;
			return (int) ans;
		}// getSaved

		public String getDisk() {
			// TODO getDisk
			return "?K";
		}// getDisk

		public String getMethod() {
			String ans = "   Error";
			switch (rawData[1]) {
			case (byte) 0x00:
				ans = " InValid";
				break;
			case (byte) 0x01:
			case (byte) 0x02:
				ans = "Unpacked";
				break;
			case (byte) 0x04:
				ans = "Squeezed";
				break;
			case (byte) 0x03:
			case (byte) 0x05:
			case (byte) 0x06:
			case (byte) 0x07:
			case (byte) 0x08:
				ans = "Crunched";
				break;
			default:
				ans = " Unknown";
				break;
			}// switch
			return ans;
		}// getMethod

		public String getVer() {
			return Integer.valueOf(rawData[1]).toString();
		}// getVer

		public String getDate() {
			// TODO getDate
			return "00 abc 00";
		}// getDate

		public String getTime() {
			// TODO getTime
			return "0:00p";
		}// getTime

		public int getCRC() {
			// TODO getCRC
			return 0;
		}// getCRC

		public String getRawString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < ARC_HEADER_SIZE; i++) {
				sb.append(String.format("%02X ", rawData[i]));
			}// for
			return sb.toString();
		}

	}// class ArcHeader
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	private class RowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}// if
			int selectedRow = tocTable.getSelectedRow();
			ArcHeader ah = arcHeaders.get(selectedRow);
			String s = ah.getRawString();
			lblHeaderValues.setText(arcHeaders.get(tocTable.getSelectedRow()).getRawString());
			// showDirectoryDetail(tocTable.getSelectedRow());

		}// valueChanged
	}// class RowListener

}// class Unarc80
