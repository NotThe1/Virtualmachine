package disks;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JComboBox;
import java.awt.Dimension;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.border.LineBorder;

public class DiskDef {

	private JFrame frmDiskDef;
	private Preferences prefs;
	private JComboBox cbBlockSize;
	private JSpinner totalBytesDecimal;
	private JLabel lblBlockSize;
	private JLabel lblTotalBytes;

	private SpinnerNumberModel totalBytesModel;
	private SpinnerNumberModel headsModel;
	private SpinnerNumberModel tracksModel;
	private SpinnerNumberModel reservedTracksModel;
	private boolean blocking;

	static final int LOGICAL_SECTOR_SIZE = 128;
	static final int PHYSICAL_SECTOR_SIZE = 512;
	static final int LOGICAL_SECTORS_PER_PHYSICAL_SECTOR = PHYSICAL_SECTOR_SIZE / LOGICAL_SECTOR_SIZE;
	private JLabel lblNumberOfBlocks;
	private JLabel lblNumberOfLogicalSectors;
	private JLabel lblNumberOfPhysicalSectors;
	private JLabel lblWasted;
	private JLabel lblSectorsPerHeadPerTrack;
	private JLabel lblSPT;
	private JLabel lblBSH;
	private JLabel lblBLM;
	private JLabel lblEXM;
	private JLabel lblDSM;
	private JLabel lblDRM;
	private JLabel lblAL0;
	private JLabel lblAL1;
	private JLabel lblCKS;
	private JLabel lblOff;
	private JLabel lblBootable;
	private JLabel lblNetBlocksAvailable;

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
				}// if
			}// run
		});
	}// main
		// -----------------------------------------------

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
		int totalBytes = calcTotalBytes();
		int physicalBlocksPerSector = calcPhySectorsPerBlock();
		int blockSize = calcBlockSize();
		int numberOfBlocks = calcNumberOfBlocks(); // DSM
		int numberOfLogicalSectors = calcNumberOfLogicalSectors();
		int numberOfPhysicalSectors = calcNumberOfPhysicalSectors();
		int sectorsPerHeadPerTrack = calcSectorsPerHeadPerTrack();
		int logicalSectorsPerTrack = calcLogicalSectorsPerTrack(); // SPT
		int blockShiftFactor = calcBlockShiftFactor(); // BSH
		int dataBlockMask = calcDataBlockMask(); // BLM
		int numberOfAvailableBlocks = calcNetBlockCount(); // DRM
		int reservedTracks = calcReservedTracks(); // OFF
	}// calculateAllValues

	private int calcReservedTracks() { // OFF
		int ans = (int) reservedTracksModel.getValue();
		lblOff.setText(String.format("     %1$10d (0X%1$X)", ans ));
		return ans;
	}

	private int calcNetBlockCount() { // DRM
		int reserved = (int) reservedTracksModel.getValue();
		int heads = (int) headsModel.getValue();
		int tracks = (int) tracksModel.getValue();
		int sphpt = calcSectorsPerHeadPerTrack();
		int netPhysicalSectors = heads * (tracks - reserved) * sphpt;
		int sectorsPerBlock = calcPhySectorsPerBlock();
		int ans = (netPhysicalSectors / sectorsPerBlock);

		lblNetBlocksAvailable.setText(String.format("     %1$10d (0X%1$X)", ans));
		lblDSM.setText(String.format("     %1$10d (0X%1$X)", ans - 1));
		return ans;
	}

	private int calcDataBlockMask() { // BLM
		int ans = 2;
		for (int i = 0; i < calcBlockShiftFactor() - 1; i++) {
			ans *= 2;
		}
		ans--;
		lblBLM.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}

	private int calcBlockShiftFactor() { // BSH
		int ans = cbBlockSize.getSelectedIndex() + 2;
		lblBSH.setText(String.format("     %1$10d (0X%1$X)", ans));
		return ans;
	}

	private int calcLogicalSectorsPerTrack() { // SPT
		int logicalSecHeadTrk = calcSectorsPerHeadPerTrack() * LOGICAL_SECTORS_PER_PHYSICAL_SECTOR;
		int ans = logicalSecHeadTrk * (int) headsModel.getValue();
		lblSPT.setText(String.format("     %1$10d (0X%1$X)", ans));
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
		// int reserved = (int)reservedTracksModel.getValue();
		// int heads = (int)headsModel.getValue();
		// int tracks = (int) tracksModel.getValue();
		// int sphpt = calcSectorsPerHeadPerTrack();
		// int netPhysicalSectors = heads * ( tracks -reserved) * sphpt;
		// int sectorsPerBlock = calcPhySectorsPerBlock();
		// int netBlocks = (netPhysicalSectors / sectorsPerBlock );
		//
		// lblNetBlocksAvailable.setText(String.format("     %1$10d (0X%1$X)", netBlocks));
		// lblDSM.setText(String.format("     %1$10d (0X%1$X)", netBlocks-1));

		int ans = (int) totalBytesModel.getValue() / calcBlockSize();
		lblNumberOfBlocks.setText(String.format("     %1$10d (0X%1$X)", ans));

		// int ans = logicalSecHeadTrk * (int) headsModel.getValue();

		return ans;
	}

	private int calcPhySectorsPerBlock() {
		int blockSize = calcBlockSize();
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

	private int calcBlockSize() {
		int ans = 128;
		for (int i = 0; i < cbBlockSize.getSelectedIndex() + 2; i++) {
			ans = ans * 2;
		}// for
		return ans;

	}

	private int calcTotalBytes() {
		int ans = 0;
		return ans;
	}

	// -----------------------------------------------

	private void appClose() {
		prefs = Preferences.userRoot().node(this.getClass().getName());
		frmDiskDef.getX();
		prefs.putInt("DiskDef/X", frmDiskDef.getX());
		prefs.putInt("DiskDef/Y", frmDiskDef.getY());
		prefs.putInt("DiskDef/height", frmDiskDef.getHeight());
		prefs.putInt("DiskDef/width", frmDiskDef.getWidth());

		prefs.putInt("DiskDef/totalBytes", (int) totalBytesModel.getValue());
		prefs.putInt("DiskDef/blockSize", (int) cbBlockSize.getSelectedIndex());

		prefs.putInt("DiskDef/heads", (int) headsModel.getValue());
		prefs.putInt("DiskDef/tracks", (int) tracksModel.getValue());
		prefs = null;

	}

	private void appInit() {
		prefs = Preferences.userRoot().node(this.getClass().getName());
		frmDiskDef.setLocation(prefs.getInt("DiskDef/X", 00), prefs.getInt("DiskDef/Y", 00));
		frmDiskDef.setSize(prefs.getInt("DiskDef/width", 870), prefs.getInt("DiskDef/height", 802));

		totalBytesModel.setValue(prefs.getInt("DiskDef/totalBytes", PHYSICAL_SECTOR_SIZE));
		cbBlockSize.setSelectedIndex(prefs.getInt("DiskDef/blockSize", 0));
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
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		frmDiskDef.getContentPane().setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		frmDiskDef.getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Logical   Sector Size = 128  (0X080)");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Physical Sector Size = 512  (0X200)");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.gridheight = 6;
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 1;
		gbc_panel_4.gridy = 0;
		frmDiskDef.getContentPane().add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel_4.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_4.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.fill = GridBagConstraints.VERTICAL;
		gbc_panel_5.gridwidth = 2;
		gbc_panel_5.insets = new Insets(0, 0, 5, 0);
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 0;
		panel_4.add(panel_5, gbc_panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] { 99, 0 };
		gbl_panel_5.rowHeights = new int[] { 14, 0 };
		gbl_panel_5.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_5.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_5.setLayout(gbl_panel_5);

		JLabel lblDiskParameterBlock = new JLabel("Disk Parameter Block");
		GridBagConstraints gbc_lblDiskParameterBlock = new GridBagConstraints();
		gbc_lblDiskParameterBlock.gridx = 0;
		gbc_lblDiskParameterBlock.gridy = 0;
		panel_5.add(lblDiskParameterBlock, gbc_lblDiskParameterBlock);

		lblSPT = new JLabel("00");
		GridBagConstraints gbc_lblSPT = new GridBagConstraints();
		gbc_lblSPT.insets = new Insets(0, 0, 5, 5);
		gbc_lblSPT.anchor = GridBagConstraints.SOUTHEAST;
		gbc_lblSPT.gridx = 0;
		gbc_lblSPT.gridy = 1;
		panel_4.add(lblSPT, gbc_lblSPT);

		JLabel lblSptSectors = new JLabel("SPT - Sectors Per Track");
		GridBagConstraints gbc_lblSptSectors = new GridBagConstraints();
		gbc_lblSptSectors.anchor = GridBagConstraints.WEST;
		gbc_lblSptSectors.insets = new Insets(0, 0, 5, 0);
		gbc_lblSptSectors.gridx = 1;
		gbc_lblSptSectors.gridy = 1;
		panel_4.add(lblSptSectors, gbc_lblSptSectors);

		lblBSH = new JLabel("00");
		GridBagConstraints gbc_lblBSH = new GridBagConstraints();
		gbc_lblBSH.anchor = GridBagConstraints.EAST;
		gbc_lblBSH.insets = new Insets(0, 0, 5, 5);
		gbc_lblBSH.gridx = 0;
		gbc_lblBSH.gridy = 2;
		panel_4.add(lblBSH, gbc_lblBSH);

		JLabel lblBshBlock = new JLabel("BSH - Block Shift Factor");
		GridBagConstraints gbc_lblBshBlock = new GridBagConstraints();
		gbc_lblBshBlock.anchor = GridBagConstraints.WEST;
		gbc_lblBshBlock.insets = new Insets(0, 0, 5, 0);
		gbc_lblBshBlock.gridx = 1;
		gbc_lblBshBlock.gridy = 2;
		panel_4.add(lblBshBlock, gbc_lblBshBlock);

		lblBLM = new JLabel("00");
		GridBagConstraints gbc_lblBLM = new GridBagConstraints();
		gbc_lblBLM.anchor = GridBagConstraints.EAST;
		gbc_lblBLM.insets = new Insets(0, 0, 5, 5);
		gbc_lblBLM.gridx = 0;
		gbc_lblBLM.gridy = 3;
		panel_4.add(lblBLM, gbc_lblBLM);

		JLabel lblBlmData = new JLabel("BLM - Data Allocation Block Mask");
		GridBagConstraints gbc_lblBlmData = new GridBagConstraints();
		gbc_lblBlmData.anchor = GridBagConstraints.WEST;
		gbc_lblBlmData.insets = new Insets(0, 0, 5, 0);
		gbc_lblBlmData.gridx = 1;
		gbc_lblBlmData.gridy = 3;
		panel_4.add(lblBlmData, gbc_lblBlmData);

		lblEXM = new JLabel("00");
		GridBagConstraints gbc_lblEXM = new GridBagConstraints();
		gbc_lblEXM.anchor = GridBagConstraints.EAST;
		gbc_lblEXM.insets = new Insets(0, 0, 5, 5);
		gbc_lblEXM.gridx = 0;
		gbc_lblEXM.gridy = 4;
		panel_4.add(lblEXM, gbc_lblEXM);

		JLabel lblExmExtent = new JLabel("EXM - Extent Mask");
		GridBagConstraints gbc_lblExmExtent = new GridBagConstraints();
		gbc_lblExmExtent.anchor = GridBagConstraints.WEST;
		gbc_lblExmExtent.insets = new Insets(0, 0, 5, 0);
		gbc_lblExmExtent.gridx = 1;
		gbc_lblExmExtent.gridy = 4;
		panel_4.add(lblExmExtent, gbc_lblExmExtent);

		lblDSM = new JLabel("00");
		GridBagConstraints gbc_lblDSM = new GridBagConstraints();
		gbc_lblDSM.anchor = GridBagConstraints.SOUTHEAST;
		gbc_lblDSM.insets = new Insets(0, 0, 5, 5);
		gbc_lblDSM.gridx = 0;
		gbc_lblDSM.gridy = 5;
		panel_4.add(lblDSM, gbc_lblDSM);

		JLabel lblDiskStorageMax = new JLabel("DSM - Disk Storage Max");
		GridBagConstraints gbc_lblDiskStorageMax = new GridBagConstraints();
		gbc_lblDiskStorageMax.anchor = GridBagConstraints.WEST;
		gbc_lblDiskStorageMax.insets = new Insets(0, 0, 5, 0);
		gbc_lblDiskStorageMax.gridx = 1;
		gbc_lblDiskStorageMax.gridy = 5;
		panel_4.add(lblDiskStorageMax, gbc_lblDiskStorageMax);

		lblDRM = new JLabel("00");
		GridBagConstraints gbc_lblDRM = new GridBagConstraints();
		gbc_lblDRM.anchor = GridBagConstraints.EAST;
		gbc_lblDRM.insets = new Insets(0, 0, 5, 5);
		gbc_lblDRM.gridx = 0;
		gbc_lblDRM.gridy = 6;
		panel_4.add(lblDRM, gbc_lblDRM);

		JLabel lblNewLabel_5 = new JLabel("DRM - Directory Max");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_5.gridx = 1;
		gbc_lblNewLabel_5.gridy = 6;
		panel_4.add(lblNewLabel_5, gbc_lblNewLabel_5);

		lblAL0 = new JLabel("00");
		GridBagConstraints gbc_lblAL0 = new GridBagConstraints();
		gbc_lblAL0.anchor = GridBagConstraints.EAST;
		gbc_lblAL0.insets = new Insets(0, 0, 5, 5);
		gbc_lblAL0.gridx = 0;
		gbc_lblAL0.gridy = 7;
		panel_4.add(lblAL0, gbc_lblAL0);

		JLabel lblAlAllocation = new JLabel("AL0 - Allocation Mask 0");
		GridBagConstraints gbc_lblAlAllocation = new GridBagConstraints();
		gbc_lblAlAllocation.anchor = GridBagConstraints.WEST;
		gbc_lblAlAllocation.insets = new Insets(0, 0, 5, 0);
		gbc_lblAlAllocation.gridx = 1;
		gbc_lblAlAllocation.gridy = 7;
		panel_4.add(lblAlAllocation, gbc_lblAlAllocation);

		lblAL1 = new JLabel("00");
		GridBagConstraints gbc_lblAL1 = new GridBagConstraints();
		gbc_lblAL1.anchor = GridBagConstraints.EAST;
		gbc_lblAL1.insets = new Insets(0, 0, 5, 5);
		gbc_lblAL1.gridx = 0;
		gbc_lblAL1.gridy = 8;
		panel_4.add(lblAL1, gbc_lblAL1);

		JLabel lblNewLabel_6 = new JLabel("AL1 - Allocation Mask 1");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_6.gridx = 1;
		gbc_lblNewLabel_6.gridy = 8;
		panel_4.add(lblNewLabel_6, gbc_lblNewLabel_6);

		lblCKS = new JLabel("00");
		GridBagConstraints gbc_lblCKS = new GridBagConstraints();
		gbc_lblCKS.anchor = GridBagConstraints.EAST;
		gbc_lblCKS.insets = new Insets(0, 0, 5, 5);
		gbc_lblCKS.gridx = 0;
		gbc_lblCKS.gridy = 9;
		panel_4.add(lblCKS, gbc_lblCKS);

		JLabel lblCksCheck = new JLabel("CKS - Check Size Area");
		GridBagConstraints gbc_lblCksCheck = new GridBagConstraints();
		gbc_lblCksCheck.anchor = GridBagConstraints.WEST;
		gbc_lblCksCheck.insets = new Insets(0, 0, 5, 0);
		gbc_lblCksCheck.gridx = 1;
		gbc_lblCksCheck.gridy = 9;
		panel_4.add(lblCksCheck, gbc_lblCksCheck);

		lblOff = new JLabel("00");
		GridBagConstraints gbc_lblOff = new GridBagConstraints();
		gbc_lblOff.anchor = GridBagConstraints.EAST;
		gbc_lblOff.insets = new Insets(0, 0, 0, 5);
		gbc_lblOff.gridx = 0;
		gbc_lblOff.gridy = 10;
		panel_4.add(lblOff, gbc_lblOff);

		JLabel lblOffReservedTracks = new JLabel("OFF Reserved Tracks");
		GridBagConstraints gbc_lblOffReservedTracks = new GridBagConstraints();
		gbc_lblOffReservedTracks.anchor = GridBagConstraints.WEST;
		gbc_lblOffReservedTracks.gridx = 1;
		gbc_lblOffReservedTracks.gridy = 10;
		panel_4.add(lblOffReservedTracks, gbc_lblOffReservedTracks);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		frmDiskDef.getContentPane().add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 100, 100, 120, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);
		// -----------
		JLabel lblNewLabel_2 = new JLabel("Total Bytes");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);

		totalBytesModel =
				new SpinnerNumberModel(PHYSICAL_SECTOR_SIZE, PHYSICAL_SECTOR_SIZE, Integer.MAX_VALUE,
						PHYSICAL_SECTOR_SIZE);

		totalBytesDecimal = new JSpinner(totalBytesModel);
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

		totalBytesDecimal.setPreferredSize(new Dimension(120, 20));
		GridBagConstraints gbc_totalBytesDecimal = new GridBagConstraints();
		gbc_totalBytesDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_totalBytesDecimal.gridx = 1;
		gbc_totalBytesDecimal.gridy = 0;
		panel_1.add(totalBytesDecimal, gbc_totalBytesDecimal);

		HexSpinner totalBytesHex = new HexSpinner(totalBytesModel);
		totalBytesHex.setPreferredSize(new Dimension(120, 20));
		GridBagConstraints gbc_totalBytesHex = new GridBagConstraints();
		gbc_totalBytesHex.insets = new Insets(0, 0, 5, 5);
		gbc_totalBytesHex.gridx = 2;
		gbc_totalBytesHex.gridy = 0;
		panel_1.add(totalBytesHex, gbc_totalBytesHex);

		lblTotalBytes = new JLabel("Total Bytes");
		GridBagConstraints gbc_lblTotalBytes = new GridBagConstraints();
		gbc_lblTotalBytes.insets = new Insets(0, 0, 5, 0);
		gbc_lblTotalBytes.gridx = 3;
		gbc_lblTotalBytes.gridy = 0;
		panel_1.add(lblTotalBytes, gbc_lblTotalBytes);
		// -----------
		JLabel lblNewLabel_3 = new JLabel("Block Size");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 1;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);

		cbBlockSize = new JComboBox();
		cbBlockSize.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				calculateAllValues();
			}
		});
		cbBlockSize.setModel(new DefaultComboBoxModel(new String[]
		{ "   512         0X0200", " 1024         0X0400",
				" 2048         0X0800", " 4096         0X1000",
				" 8192         0X2000", "16384        0X4000" }));
		cbBlockSize.setEditable(false);
		GridBagConstraints gbc_cbBlockSize = new GridBagConstraints();
		gbc_cbBlockSize.insets = new Insets(0, 0, 0, 5);
		gbc_cbBlockSize.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbBlockSize.gridx = 1;
		gbc_cbBlockSize.gridy = 1;
		panel_1.add(cbBlockSize, gbc_cbBlockSize);

		lblBlockSize = new JLabel("Block Size");
		GridBagConstraints gbc_lblBlockSize = new GridBagConstraints();
		gbc_lblBlockSize.anchor = GridBagConstraints.BELOW_BASELINE_LEADING;
		gbc_lblBlockSize.gridx = 3;
		gbc_lblBlockSize.gridy = 1;
		panel_1.add(lblBlockSize, gbc_lblBlockSize);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		frmDiskDef.getContentPane().add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 100, 50, 50, 20, 20, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 22, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);
		// -----------
		JLabel lblHeads0 = new JLabel("Heads");
		GridBagConstraints gbc_lblHeads = new GridBagConstraints();
		gbc_lblHeads.anchor = GridBagConstraints.EAST;
		gbc_lblHeads.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeads.gridx = 0;
		gbc_lblHeads.gridy = 1;
		panel_2.add(lblHeads0, gbc_lblHeads);

		headsModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);

		JSpinner headsDecimal = new JSpinner(headsModel);
		headsDecimal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				calculateAllValues();
			}
		});
		headsDecimal.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_headsDecimal = new GridBagConstraints();
		gbc_headsDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_headsDecimal.gridx = 1;
		gbc_headsDecimal.gridy = 1;
		panel_2.add(headsDecimal, gbc_headsDecimal);

		HexSpinner headsHex = new HexSpinner(headsModel);
		headsHex.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_headsHex = new GridBagConstraints();
		gbc_headsHex.insets = new Insets(0, 0, 5, 0);
		gbc_headsHex.gridx = 2;
		gbc_headsHex.gridy = 1;
		panel_2.add(headsHex, gbc_headsHex);

		lblSectorsPerHeadPerTrack = new JLabel("New label");
		GridBagConstraints gbc_lblSectorsPerHeadPerTrack = new GridBagConstraints();
		gbc_lblSectorsPerHeadPerTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblSectorsPerHeadPerTrack.gridx = 4;
		gbc_lblSectorsPerHeadPerTrack.gridy = 1;
		panel_2.add(lblSectorsPerHeadPerTrack, gbc_lblSectorsPerHeadPerTrack);

		JLabel lblSectorsPerHeadPerTrack0 = new JLabel("Sectors/Track/Head");
		GridBagConstraints gbc_lblSectorsPerHeadPerTrack0 = new GridBagConstraints();
		gbc_lblSectorsPerHeadPerTrack0.insets = new Insets(0, 0, 5, 0);
		gbc_lblSectorsPerHeadPerTrack0.gridx = 5;
		gbc_lblSectorsPerHeadPerTrack0.gridy = 1;
		panel_2.add(lblSectorsPerHeadPerTrack0, gbc_lblSectorsPerHeadPerTrack0);
		// -----------
		JLabel lblTracks0 = new JLabel("Tracks");
		GridBagConstraints gbc_lblTracks = new GridBagConstraints();
		gbc_lblTracks.insets = new Insets(0, 0, 5, 5);
		gbc_lblTracks.anchor = GridBagConstraints.EAST;
		gbc_lblTracks.gridx = 0;
		gbc_lblTracks.gridy = 2;
		panel_2.add(lblTracks0, gbc_lblTracks);

		tracksModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);

		JSpinner tracksDecimal = new JSpinner(tracksModel);
		tracksDecimal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				calculateAllValues();
			}
		});
		tracksDecimal.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_reservedTracksDecimal = new GridBagConstraints();
		gbc_reservedTracksDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_reservedTracksDecimal.gridx = 1;
		gbc_reservedTracksDecimal.gridy = 2;
		panel_2.add(tracksDecimal, gbc_reservedTracksDecimal);

		HexSpinner tracksHex = new HexSpinner(tracksModel);
		tracksHex.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_tracksHex = new GridBagConstraints();
		gbc_tracksHex.insets = new Insets(0, 0, 5, 0);
		gbc_tracksHex.gridx = 2;
		gbc_tracksHex.gridy = 2;
		panel_2.add(tracksHex, gbc_tracksHex);

		lblWasted = new JLabel(".");
		lblWasted.setForeground(Color.RED);
		GridBagConstraints gbc_lblWasted = new GridBagConstraints();
		gbc_lblWasted.gridwidth = 2;
		gbc_lblWasted.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblWasted.insets = new Insets(0, 0, 5, 5);
		gbc_lblWasted.gridx = 4;
		gbc_lblWasted.gridy = 2;
		panel_2.add(lblWasted, gbc_lblWasted);

		JPanel panel_6 = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.insets = new Insets(0, 0, 5, 5);
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 3;
		frmDiskDef.getContentPane().add(panel_6, gbc_panel_6);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[] { 100, 50, 50, 0, 0, 0 };
		gbl_panel_6.rowHeights = new int[] { 0, 0 };
		gbl_panel_6.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_6.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_6.setLayout(gbl_panel_6);

		JLabel lblNewLabel_7 = new JLabel("Reserved Tracks");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = 0;
		panel_6.add(lblNewLabel_7, gbc_lblNewLabel_7);

		reservedTracksModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);

		JSpinner reservedTracksDecimal = new JSpinner(reservedTracksModel);
		reservedTracksDecimal.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				calculateAllValues();
			}
		});

		reservedTracksDecimal.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_reservedTracksDecimal1 = new GridBagConstraints();
		gbc_reservedTracksDecimal1.insets = new Insets(0, 0, 0, 5);
		gbc_reservedTracksDecimal1.gridx = 1;
		gbc_reservedTracksDecimal1.gridy = 0;
		panel_6.add(reservedTracksDecimal, gbc_reservedTracksDecimal1);

		HexSpinner reservedTracksHex = new HexSpinner(reservedTracksModel);
		reservedTracksHex.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_reservedTracksHex = new GridBagConstraints();
		gbc_reservedTracksHex.insets = new Insets(0, 0, 0, 5);
		gbc_reservedTracksHex.gridx = 2;
		gbc_reservedTracksHex.gridy = 0;
		panel_6.add(reservedTracksHex, gbc_reservedTracksHex);

		lblBootable = new JLabel("Not Bootable");
		GridBagConstraints gbc_lblBootable = new GridBagConstraints();
		gbc_lblBootable.gridx = 4;
		gbc_lblBootable.gridy = 0;
		panel_6.add(lblBootable, gbc_lblBootable);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 4;
		frmDiskDef.getContentPane().add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 150, 20, 0 };
		gbl_panel_3.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_3.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		JLabel lblNumberOfPhysicalSectors0 = new JLabel("Number Of PhysicalSectors");
		GridBagConstraints gbc_lblNumberOfPhysicalSectors0 = new GridBagConstraints();
		gbc_lblNumberOfPhysicalSectors0.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfPhysicalSectors0.gridx = 0;
		gbc_lblNumberOfPhysicalSectors0.gridy = 2;
		panel_3.add(lblNumberOfPhysicalSectors0, gbc_lblNumberOfPhysicalSectors0);

		lblNumberOfPhysicalSectors = new JLabel("New label");
		GridBagConstraints gbc_lblNumberOfPhysicalSectors = new GridBagConstraints();
		gbc_lblNumberOfPhysicalSectors.insets = new Insets(0, 0, 5, 0);
		gbc_lblNumberOfPhysicalSectors.gridx = 1;
		gbc_lblNumberOfPhysicalSectors.gridy = 2;
		panel_3.add(lblNumberOfPhysicalSectors, gbc_lblNumberOfPhysicalSectors);

		JLabel lblNewLabel_4 = new JLabel("Number Of Logical Sectors");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 3;
		panel_3.add(lblNewLabel_4, gbc_lblNewLabel_4);

		lblNumberOfLogicalSectors = new JLabel("New label");
		GridBagConstraints gbc_lblNumberOfLogicalSectors = new GridBagConstraints();
		gbc_lblNumberOfLogicalSectors.insets = new Insets(0, 0, 5, 0);
		gbc_lblNumberOfLogicalSectors.gridx = 1;
		gbc_lblNumberOfLogicalSectors.gridy = 3;
		panel_3.add(lblNumberOfLogicalSectors, gbc_lblNumberOfLogicalSectors);

		JLabel lblNumberOfBlocks0 = new JLabel("Raw Blocks on Disk");
		GridBagConstraints gbc_lblNumberOfBlocks0 = new GridBagConstraints();
		gbc_lblNumberOfBlocks0.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfBlocks0.gridx = 0;
		gbc_lblNumberOfBlocks0.gridy = 4;
		panel_3.add(lblNumberOfBlocks0, gbc_lblNumberOfBlocks0);

		lblNumberOfBlocks = new JLabel("New label");
		GridBagConstraints gbc_lblNumberOfBlocks = new GridBagConstraints();
		gbc_lblNumberOfBlocks.insets = new Insets(0, 0, 5, 0);
		gbc_lblNumberOfBlocks.gridx = 1;
		gbc_lblNumberOfBlocks.gridy = 4;
		panel_3.add(lblNumberOfBlocks, gbc_lblNumberOfBlocks);

		JLabel lblNewLabel_8 = new JLabel("Net Blocks Available");
		lblNewLabel_8.setToolTipText("");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = 5;
		panel_3.add(lblNewLabel_8, gbc_lblNewLabel_8);

		lblNetBlocksAvailable = new JLabel("New label");
		GridBagConstraints gbc_lblNetBlocksAvailable = new GridBagConstraints();
		gbc_lblNetBlocksAvailable.gridx = 1;
		gbc_lblNetBlocksAvailable.gridy = 5;
		panel_3.add(lblNetBlocksAvailable, gbc_lblNetBlocksAvailable);
	}// initialize
		// ---------------------------------------------------------------------
		// ---------------------------------------------------------------------

	private class HexSpinner extends JSpinner {
		/**
	 * 
	 */
		private static final long serialVersionUID = 1L;

		HexSpinner(SpinnerNumberModel numberModel) {
			super(numberModel);
			JSpinner.DefaultEditor editor = (DefaultEditor) this.getEditor();
			JFormattedTextField ftf = editor.getTextField();
			ftf.setFormatterFactory(new MyFormatterFactory());
		}// constructor

		class HexFormatter extends DefaultFormatter {

			/**
		 * 
		 */
			private static final long serialVersionUID = 1L;

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
