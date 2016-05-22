package disks;

import java.awt.EventQueue;
import java.text.ParseException;
import java.util.prefs.Preferences;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JMenuBar;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

import java.awt.Insets;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Color;
import javax.swing.SpinnerListModel;

public class DiskDef {

	private JFrame frmDiskDef;
	private Preferences prefs;
	private JLabel lblTotalBytes;
	private JLabel lblBlockSize;

	private SpinnerNumberModel totalBytesModel;
	private SpinnerNumberModel blockSizeModel;
	// private SpinnerNumberModel numberOfBlocksModel;
	// private SpinnerNumberModel logicalSectorsModel;
	// private SpinnerNumberModel physicalSectorsModel;
	private SpinnerNumberModel headsModel;
	private SpinnerNumberModel tracksModel;

	static final int LOGICAL_SECTOR_SIZE = 128;
	static final int PHYSICAL_SECTOR_SIZE = 512;
	static final int LOGICAL_SECTORS_PER_PHYSICAL_SECTOR = PHYSICAL_SECTOR_SIZE / LOGICAL_SECTOR_SIZE;

	private boolean blocking;
	private JLabel lblNumberOfBlocks;
	private JLabel lblNumberOfPhysicalSectors;
	private JLabel lblNumberOfLogicalSectors;
	private JLabel lblSectorsPerHeadPerTrack;
	private JLabel lblWasted;
	private JLabel lblLogicalSectorsPerTrack;
	private JLabel lblBlockShiftFactor;
	private JLabel lblDataBlockMask;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DiskDef window = new DiskDef();
					window.frmDiskDef.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}// try
			}// run
		});
	}// main

	private String makeValueDisplay(int value) {
		String ans = "";
		if (value < 1024) {
			ans = String.format("%,d Bytes", value);
		} else if (value < 1024000) {
			ans = String.format("%,.2f KB", (float) value / 1024);
		} else {
			ans = String.format("%,.2f MB", (float) value / 1024000);
		}
		return ans;
	}// makeValueDisplay

	private void calculateAllValues() {
		int physicalBlocksPerSector = calcPhySectorsPerBlock();
		int numberOfBlocks = calcNumberOfBlocks();
		int numberOfLogicalSectors = calcNumberOfLogicalSectors();
		int numberOfPhysicalSectors = calcNumberOfPhysicalSectors();
		int sectorsPerHeadPerTrack = calcSectorsPerHeadPerTrack();
		int logicalSectorsPerTrack = calcLogicalSectorsPerTrack(); // SPT
		 int blockShiftValue = calcBlockShiftValue(); // BSH
		 int dataBlockMask = calcDataBlockMask(); // BSH
	}// calculateAllValues 

	private int calcDataBlockMask(){
	blockSizeModel.getValue();
		int ans;
		switch ((int)blockSizeModel.getValue()) {
		case 512:
			ans = 0;
			break;
		case 1024:
			ans = 7;
			break;
		case 2048:
			ans = 15;
			break;
		case 4096:
			ans = 31;
			break;
		case 8192:
			ans = 63;
			break;
		case 16384:
			ans = 127;
			break;
		 default:
				ans = 0;			// not a good value reset
		}// switch
		lblDataBlockMask.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}
	private int calcBlockShiftValue(){
	blockSizeModel.getValue();
		int ans;
		switch ((int)blockSizeModel.getValue()) {
		case 512:
			ans = 0;
			break;
		case 1024:
			ans = 3;
			break;
		case 2048:
			ans = 4;
			break;
		case 4096:
			ans = 5;
			break;
		case 8192:
			ans = 6;
			break;
		case 16384:
			ans = 7;
			break;
		 default:
				ans = 0;			// not a good value reset
		}// switch
		lblBlockShiftFactor.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}

	private int calcLogicalSectorsPerTrack() { // SPT
		int logicalSecHeadTrk = calcSectorsPerHeadPerTrack() * LOGICAL_SECTORS_PER_PHYSICAL_SECTOR;
		int ans = logicalSecHeadTrk * (int) headsModel.getValue();
		lblLogicalSectorsPerTrack.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}

	private int calcSectorsPerHeadPerTrack() {
		int trackHeads = (int) tracksModel.getValue() * (int) headsModel.getValue();
		int ans = calcNumberOfPhysicalSectors() / trackHeads;
		int usedSectors = ans * trackHeads;
		lblSectorsPerHeadPerTrack.setText(String.format("     %1$10d (0X%1$X)", ans));
		int wasted = calcNumberOfPhysicalSectors() - usedSectors;
		if (wasted == 0) {
			lblWasted.setText("");
		} else {
			lblWasted.setText(String.format("%1$d (0X%1$X) sectors are being Wasted! ", wasted));
		}
		return ans;

	}

	private int calcNumberOfPhysicalSectors() {
		int ans = (int) totalBytesModel.getValue() / PHYSICAL_SECTOR_SIZE;
		lblNumberOfPhysicalSectors.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}

	private int calcNumberOfLogicalSectors() {
		int ans = (int) totalBytesModel.getValue() / LOGICAL_SECTOR_SIZE;
		lblNumberOfLogicalSectors.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}

	private int calcNumberOfBlocks() {
		int ans = (int) totalBytesModel.getValue() / (int) blockSizeModel.getValue();
		lblNumberOfBlocks.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}

	private int calcPhySectorsPerBlock() {
		int blockSize = (int) blockSizeModel.getValue();
		int ans = 1;
		if (blockSize == PHYSICAL_SECTOR_SIZE) {
			blocking = false;
			lblBlockSize.setText("No blocks");
		} else {
			blocking = true;
			ans = blockSize / PHYSICAL_SECTOR_SIZE;
			lblBlockSize.setText(String.format("%d Sectors per Block", ans));
		}// if
		return ans;
	}// calcPhySectorsPerBlock
		// -----------------------------------------------

	private void appClose() {
		prefs = Preferences.userRoot().node(this.getClass().getName());
		frmDiskDef.getX();
		prefs.putInt("DiskDef/X", frmDiskDef.getX());
		prefs.putInt("DiskDef/Y", frmDiskDef.getY());
		prefs.putInt("DiskDef/height", frmDiskDef.getHeight());
		prefs.putInt("DiskDef/width", frmDiskDef.getWidth());

		prefs.putInt("DiskDef/totalBytes", (int) totalBytesModel.getValue());
		prefs.putInt("DiskDef/blockSize", (int) blockSizeModel.getValue());

		prefs.putInt("DiskDef/heads", (int) headsModel.getValue());
		prefs.putInt("DiskDef/tracks", (int) tracksModel.getValue());
		prefs = null;

	}

	private void appInit() {
		prefs = Preferences.userRoot().node(this.getClass().getName());
		frmDiskDef.setLocation(prefs.getInt("DiskDef/X", 00), prefs.getInt("DiskDef/Y", 00));
		frmDiskDef.setSize(prefs.getInt("DiskDef/width", 520),
				prefs.getInt("DiskDef/height", 742));
		totalBytesModel.setValue(prefs.getInt("DiskDef/totalBytes", PHYSICAL_SECTOR_SIZE));
		blockSizeModel.setValue(prefs.getInt("DiskDef/blockSize", PHYSICAL_SECTOR_SIZE));
		headsModel.setValue(prefs.getInt("DiskDef/heads", 1));
		tracksModel.setValue(prefs.getInt("DiskDef/tracks", 1));
		prefs = null;
		blocking = false;
		calculateAllValues();
	}// initApp

	/**
	 * Create the application.
	 */
	public DiskDef() {
		initialize();
		appInit();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDiskDef = new JFrame();
		frmDiskDef.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		frmDiskDef.setTitle("Disk Definition Calculator");
		frmDiskDef.setBounds(100, 100, 517, 742);
		frmDiskDef.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmDiskDef.setJMenuBar(menuBar);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 120, 40, 30, 80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 70, 0, 0, 50, 0, 0, 0, 0, 50, 0, 0, 0, 0, 20, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, Double.MIN_VALUE };
		frmDiskDef.getContentPane().setLayout(gridBagLayout);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 0;
		frmDiskDef.getContentPane().add(verticalStrut, gbc_verticalStrut);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 4;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		frmDiskDef.getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		Component horizontalStrut_1 = Box.createHorizontalStrut(50);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalStrut_1.gridx = 1;
		gbc_horizontalStrut_1.gridy = 0;
		panel.add(horizontalStrut_1, gbc_horizontalStrut_1);

		JLabel lblNewLabel = new JLabel("Physical Sector Size = 512  (0X200)");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Logical   Sector Size = 128  (0X080)");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.gridx = 1;
		gbc_horizontalStrut.gridy = 3;
		panel.add(horizontalStrut, gbc_horizontalStrut);

		JLabel label1 = new JLabel("Total Bytes");
		GridBagConstraints gbc_label1 = new GridBagConstraints();
		gbc_label1.anchor = GridBagConstraints.EAST;
		gbc_label1.insets = new Insets(0, 0, 5, 5);
		gbc_label1.gridx = 1;
		gbc_label1.gridy = 1;
		frmDiskDef.getContentPane().add(label1, gbc_label1);

		totalBytesModel =
				new SpinnerNumberModel(PHYSICAL_SECTOR_SIZE, PHYSICAL_SECTOR_SIZE, Integer.MAX_VALUE,
						PHYSICAL_SECTOR_SIZE);

		JSpinner totalBytesDecimal = new JSpinner(totalBytesModel);
		totalBytesDecimal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner src = (JSpinner) e.getSource();
				int totalBytes = (int) src.getValue();
				if ((totalBytes % PHYSICAL_SECTOR_SIZE) != 0) {
					totalBytes = Math.max(PHYSICAL_SECTOR_SIZE, (totalBytes / PHYSICAL_SECTOR_SIZE)
							* PHYSICAL_SECTOR_SIZE);
					src.setValue(totalBytes);
				}// if
				lblTotalBytes.setText(makeValueDisplay(totalBytes));
				calculateAllValues();
			}
		});

		totalBytesDecimal.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinnerListTest = new GridBagConstraints();
		gbc_spinnerListTest.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerListTest.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerListTest.gridx = 2;
		gbc_spinnerListTest.gridy = 1;
		frmDiskDef.getContentPane().add(totalBytesDecimal, gbc_spinnerListTest);

		HexSpinner totalBytesHex = new HexSpinner(totalBytesModel);
		totalBytesHex.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.gridx = 3;
		gbc_spinner_1.gridy = 1;
		frmDiskDef.getContentPane().add(totalBytesHex, gbc_spinner_1);

		lblTotalBytes = new JLabel("Minium");
		GridBagConstraints gbc_lblTotalBytes = new GridBagConstraints();
		gbc_lblTotalBytes.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalBytes.gridx = 4;
		gbc_lblTotalBytes.gridy = 1;
		frmDiskDef.getContentPane().add(lblTotalBytes, gbc_lblTotalBytes);

		// //-------------------------------

		JLabel label2 = new JLabel("Block Size");
		GridBagConstraints gbc_label2 = new GridBagConstraints();
		gbc_label2.anchor = GridBagConstraints.EAST;
		gbc_label2.insets = new Insets(0, 0, 5, 5);
		gbc_label2.gridx = 1;
		gbc_label2.gridy = 2;
		frmDiskDef.getContentPane().add(label2, gbc_label2);

		blockSizeModel =
				new SpinnerNumberModel(PHYSICAL_SECTOR_SIZE, PHYSICAL_SECTOR_SIZE, Integer.MAX_VALUE,
						PHYSICAL_SECTOR_SIZE);

		JSpinner blockSizeDecimal = new JSpinner(blockSizeModel);
		blockSizeDecimal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner src = (JSpinner) e.getSource();
				int blockSize = (int) src.getValue();
				int targetValue = PHYSICAL_SECTOR_SIZE;
				switch (blockSize) {
				case 512:
				case 1024:
				case 2048:
				case 4096:
				case 8192:
				case 16384:
					// all goo values
					break;
				default:
					src.setValue(PHYSICAL_SECTOR_SIZE); // not a good value reset
				}// switch
					// if ((blockSize % PHYSICAL_SECTOR_SIZE) != 0) {
				// blockSize = Math.max(PHYSICAL_SECTOR_SIZE, (blockSize / PHYSICAL_SECTOR_SIZE)
				// * PHYSICAL_SECTOR_SIZE);
				// src.setValue(blockSize);
				// }// if
				calculateAllValues();
			}// stateChanged
		});
		blockSizeDecimal.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_blockSize = new GridBagConstraints();
		gbc_blockSize.insets = new Insets(0, 0, 5, 5);
		gbc_blockSize.fill = GridBagConstraints.HORIZONTAL;
		gbc_blockSize.gridx = 2;
		gbc_blockSize.gridy = 2;
		frmDiskDef.getContentPane().add(blockSizeDecimal, gbc_blockSize);

		HexSpinner blockSizeHex = new HexSpinner(blockSizeModel);
		blockSizeHex.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_blockSizeH = new GridBagConstraints();
		gbc_blockSizeH.insets = new Insets(0, 0, 5, 5);
		gbc_blockSizeH.fill = GridBagConstraints.HORIZONTAL;
		gbc_blockSizeH.gridx = 3;
		gbc_blockSizeH.gridy = 2;
		frmDiskDef.getContentPane().add(blockSizeHex, gbc_blockSizeH);

		lblBlockSize = new JLabel("No blocks");
		GridBagConstraints gbc_lblBlockSize = new GridBagConstraints();
		gbc_lblBlockSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblBlockSize.gridx = 4;
		gbc_lblBlockSize.gridy = 2;
		frmDiskDef.getContentPane().add(lblBlockSize, gbc_lblBlockSize);

		// //-------------------------------

		JLabel label6 = new JLabel("Heads");
		GridBagConstraints gbc_label6 = new GridBagConstraints();
		gbc_label6.anchor = GridBagConstraints.EAST;
		gbc_label6.fill = GridBagConstraints.VERTICAL;
		gbc_label6.insets = new Insets(0, 0, 5, 5);
		gbc_label6.gridx = 1;
		gbc_label6.gridy = 6;
		frmDiskDef.getContentPane().add(label6, gbc_label6);

		headsModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);

		JSpinner headsDecimal = new JSpinner(headsModel);
		headsDecimal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				calculateAllValues();
			}
		});
		headsDecimal.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_heads = new GridBagConstraints();
		gbc_heads.insets = new Insets(0, 0, 5, 5);
		gbc_heads.fill = GridBagConstraints.HORIZONTAL;
		gbc_heads.gridx = 2;
		gbc_heads.gridy = 6;
		frmDiskDef.getContentPane().add(headsDecimal, gbc_heads);

		HexSpinner headsHex = new HexSpinner(headsModel);
		headsHex.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_headsH = new GridBagConstraints();
		gbc_headsH.insets = new Insets(0, 0, 5, 5);
		gbc_headsH.fill = GridBagConstraints.HORIZONTAL;
		gbc_headsH.gridx = 3;
		gbc_headsH.gridy = 6;
		frmDiskDef.getContentPane().add(headsHex, gbc_headsH);

		// //-------------------------------

		JLabel label7 = new JLabel("Tracks");
		GridBagConstraints gbc_label7 = new GridBagConstraints();
		gbc_label7.anchor = GridBagConstraints.EAST;
		gbc_label7.insets = new Insets(0, 0, 5, 5);
		gbc_label7.gridx = 1;
		gbc_label7.gridy = 7;
		frmDiskDef.getContentPane().add(label7, gbc_label7);

		tracksModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);

		JSpinner tracksDecimal = new JSpinner(tracksModel);
		tracksDecimal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner src = (JSpinner) e.getSource();
				calculateAllValues();
			}
		});
		tracksDecimal.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_tracks = new GridBagConstraints();
		gbc_tracks.insets = new Insets(0, 0, 5, 5);
		gbc_tracks.fill = GridBagConstraints.HORIZONTAL;
		gbc_tracks.gridx = 2;
		gbc_tracks.gridy = 7;
		frmDiskDef.getContentPane().add(tracksDecimal, gbc_tracks);

		HexSpinner tracksHex = new HexSpinner(tracksModel);
		tracksHex.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_tracksH = new GridBagConstraints();
		gbc_tracksH.insets = new Insets(0, 0, 5, 5);
		gbc_tracksH.fill = GridBagConstraints.HORIZONTAL;
		gbc_tracksH.gridx = 3;
		gbc_tracksH.gridy = 7;
		frmDiskDef.getContentPane().add(tracksHex, gbc_tracksH);

		JLabel lblNewLabel_2 = new JLabel("Sectors per Track per Head: ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 9;
		frmDiskDef.getContentPane().add(lblNewLabel_2, gbc_lblNewLabel_2);

		// //-------------------------------

		lblSectorsPerHeadPerTrack = new JLabel("New label");
		GridBagConstraints gbc_lblPhysicalSectorsPerTrack = new GridBagConstraints();
		gbc_lblPhysicalSectorsPerTrack.anchor = GridBagConstraints.WEST;
		gbc_lblPhysicalSectorsPerTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblPhysicalSectorsPerTrack.gridx = 2;
		gbc_lblPhysicalSectorsPerTrack.gridy = 9;
		frmDiskDef.getContentPane().add(lblSectorsPerHeadPerTrack, gbc_lblPhysicalSectorsPerTrack);
		
				lblWasted = new JLabel(" full");
				lblWasted.setForeground(Color.RED);
				GridBagConstraints gbc_lblWasted = new GridBagConstraints();
				gbc_lblWasted.gridwidth = 3;
				gbc_lblWasted.anchor = GridBagConstraints.WEST;
				gbc_lblWasted.insets = new Insets(0, 0, 5, 5);
				gbc_lblWasted.gridx = 1;
				gbc_lblWasted.gridy = 12;
				frmDiskDef.getContentPane().add(lblWasted, gbc_lblWasted);
		
				JLabel lblNewLabel_3 = new JLabel("SPT - Logical Records per Track");
				GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
				gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
				gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel_3.gridx = 1;
				gbc_lblNewLabel_3.gridy = 13;
				frmDiskDef.getContentPane().add(lblNewLabel_3, gbc_lblNewLabel_3);
		
				lblLogicalSectorsPerTrack = new JLabel("New label");
				GridBagConstraints gbc_lblLogicalSectorsPerTrack = new GridBagConstraints();
				gbc_lblLogicalSectorsPerTrack.anchor = GridBagConstraints.WEST;
				gbc_lblLogicalSectorsPerTrack.insets = new Insets(0, 0, 5, 5);
				gbc_lblLogicalSectorsPerTrack.gridx = 2;
				gbc_lblLogicalSectorsPerTrack.gridy = 13;
				frmDiskDef.getContentPane().add(lblLogicalSectorsPerTrack, gbc_lblLogicalSectorsPerTrack);
		
		JLabel label3 = new JLabel("BSH Block Shift Factor");
		GridBagConstraints gbc_label3 = new GridBagConstraints();
		gbc_label3.anchor = GridBagConstraints.EAST;
		gbc_label3.fill = GridBagConstraints.VERTICAL;
		gbc_label3.insets = new Insets(0, 0, 5, 5);
		gbc_label3.gridx = 1;
		gbc_label3.gridy = 14;
		frmDiskDef.getContentPane().add(label3, gbc_label3);
		
		lblBlockShiftFactor = new JLabel("New label");
		GridBagConstraints gbc_lblBlockShiftFactor = new GridBagConstraints();
		gbc_lblBlockShiftFactor.anchor = GridBagConstraints.WEST;
		gbc_lblBlockShiftFactor.insets = new Insets(0, 0, 5, 5);
		gbc_lblBlockShiftFactor.gridx = 2;
		gbc_lblBlockShiftFactor.gridy = 14;
		frmDiskDef.getContentPane().add(lblBlockShiftFactor, gbc_lblBlockShiftFactor);
		
		JLabel lblNewLabel_7 = new JLabel("BLM - Data Block Mask");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_7.gridx = 1;
		gbc_lblNewLabel_7.gridy = 16;
		frmDiskDef.getContentPane().add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		lblDataBlockMask = new JLabel("New label");
		GridBagConstraints gbc_lblDataBlockMask = new GridBagConstraints();
		gbc_lblDataBlockMask.anchor = GridBagConstraints.WEST;
		gbc_lblDataBlockMask.insets = new Insets(0, 0, 5, 5);
		gbc_lblDataBlockMask.gridx = 2;
		gbc_lblDataBlockMask.gridy = 16;
		frmDiskDef.getContentPane().add(lblDataBlockMask, gbc_lblDataBlockMask);

		JLabel label10 = new JLabel("New label");
		GridBagConstraints gbc_label10 = new GridBagConstraints();
		gbc_label10.anchor = GridBagConstraints.EAST;
		gbc_label10.insets = new Insets(0, 0, 5, 5);
		gbc_label10.gridx = 1;
		gbc_label10.gridy = 21;
		frmDiskDef.getContentPane().add(label10, gbc_label10);

		JLabel lblNewLabel_4 = new JLabel("Number of Blocks :");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 1;
		gbc_lblNewLabel_4.gridy = 22;
		frmDiskDef.getContentPane().add(lblNewLabel_4, gbc_lblNewLabel_4);

		lblNumberOfBlocks = new JLabel("... Bytes");
		GridBagConstraints gbc_lblNumberOfBlocks = new GridBagConstraints();
		gbc_lblNumberOfBlocks.anchor = GridBagConstraints.WEST;
		gbc_lblNumberOfBlocks.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfBlocks.gridx = 2;
		gbc_lblNumberOfBlocks.gridy = 22;
		frmDiskDef.getContentPane().add(lblNumberOfBlocks, gbc_lblNumberOfBlocks);

		JLabel lblNewLabel_5 = new JLabel("Number Of Physical Sectors :");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 1;
		gbc_lblNewLabel_5.gridy = 23;
		frmDiskDef.getContentPane().add(lblNewLabel_5, gbc_lblNewLabel_5);

		lblNumberOfPhysicalSectors = new JLabel("... Bytes");
		GridBagConstraints gbc_lblNumberOfPhysicalSectors = new GridBagConstraints();
		gbc_lblNumberOfPhysicalSectors.anchor = GridBagConstraints.WEST;
		gbc_lblNumberOfPhysicalSectors.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfPhysicalSectors.gridx = 2;
		gbc_lblNumberOfPhysicalSectors.gridy = 23;
		frmDiskDef.getContentPane().add(lblNumberOfPhysicalSectors, gbc_lblNumberOfPhysicalSectors);

		JLabel lblNewLabel_6 = new JLabel("Number Of Logical Sectors :");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 1;
		gbc_lblNewLabel_6.gridy = 24;
		frmDiskDef.getContentPane().add(lblNewLabel_6, gbc_lblNewLabel_6);

		lblNumberOfLogicalSectors = new JLabel("... Bytes");
		GridBagConstraints gbc_lblNumberOfLogicalSectors = new GridBagConstraints();
		gbc_lblNumberOfLogicalSectors.anchor = GridBagConstraints.WEST;
		gbc_lblNumberOfLogicalSectors.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfLogicalSectors.gridx = 2;
		gbc_lblNumberOfLogicalSectors.gridy = 24;
		frmDiskDef.getContentPane().add(lblNumberOfLogicalSectors, gbc_lblNumberOfLogicalSectors);

		JLabel label14 = new JLabel("New label");
		GridBagConstraints gbc_label14 = new GridBagConstraints();
		gbc_label14.anchor = GridBagConstraints.EAST;
		gbc_label14.insets = new Insets(0, 0, 5, 5);
		gbc_label14.gridx = 1;
		gbc_label14.gridy = 25;
		frmDiskDef.getContentPane().add(label14, gbc_label14);

		JLabel label15 = new JLabel("New label");
		GridBagConstraints gbc_label15 = new GridBagConstraints();
		gbc_label15.anchor = GridBagConstraints.EAST;
		gbc_label15.insets = new Insets(0, 0, 5, 5);
		gbc_label15.gridx = 1;
		gbc_label15.gridy = 26;
		frmDiskDef.getContentPane().add(label15, gbc_label15);

		JLabel label16 = new JLabel("New label");
		GridBagConstraints gbc_label16 = new GridBagConstraints();
		gbc_label16.anchor = GridBagConstraints.EAST;
		gbc_label16.insets = new Insets(0, 0, 0, 5);
		gbc_label16.gridx = 1;
		gbc_label16.gridy = 27;
		frmDiskDef.getContentPane().add(label16, gbc_label16);
	}// initialize
		// ---------------------------------------------------------------------
		// ---------------------------------------------------------------------

	private class HexSpinner extends JSpinner {
		HexSpinner(SpinnerNumberModel numberModel) {
			super(numberModel);
			JSpinner.DefaultEditor editor = (DefaultEditor) this.getEditor();
			JFormattedTextField ftf = editor.getTextField();
			ftf.setFormatterFactory(new MyFormatterFactory());
		}// constructor

		class HexFormatter extends DefaultFormatter {

			public Object stringToValue(String text) throws ParseException {
				try {
					return Integer.valueOf(text, 16);
				} catch (NumberFormatException nfe) {
					throw new ParseException(text, 0);
				}// try
			}// stringToValue

			public String valueToString(Object value) throws ParseException {
				return String.format("%04X", value);
			}// valueToString
		}// class HexFormatter
			// -----------------------------------------------------------------------------------------------

		class MyFormatterFactory extends DefaultFormatterFactory {
			private static final long serialVersionUID = 1L;

			public AbstractFormatter getDefaultFormatter() {
				return new HexFormatter();
			}// getDefaultFormatter
		}// class MyFormatterFactory
			// -----------------------------------------------------------------------------------------------
	} // class HexSpinner
		// -----------------------------------------------------------------------------------------------

}// class DiskDef
