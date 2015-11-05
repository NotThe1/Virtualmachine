package disks;

import java.awt.EventQueue;

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
import java.awt.GridLayout;
import myComponents.Hex64KSpinner;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class NativeDiskTool {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NativeDiskTool window = new NativeDiskTool();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NativeDiskTool() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 930, 752);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{766, 0};
		gridBagLayout.rowHeights = new int[]{0, 79, 637, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JLabel lblFileName = new JLabel("File Name");
		lblFileName.setFont(new Font("Tahoma", Font.PLAIN, 18));
		GridBagConstraints gbc_lblFileName = new GridBagConstraints();
		gbc_lblFileName.insets = new Insets(0, 0, 5, 0);
		gbc_lblFileName.gridx = 0;
		gbc_lblFileName.gridy = 0;
		frame.getContentPane().add(lblFileName, gbc_lblFileName);
		
		JPanel panelGeomertry = new JPanel();
		panelGeomertry.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelGeomertry = new GridBagConstraints();
		gbc_panelGeomertry.insets = new Insets(0, 0, 5, 0);
		gbc_panelGeomertry.fill = GridBagConstraints.BOTH;
		gbc_panelGeomertry.gridx = 0;
		gbc_panelGeomertry.gridy = 1;
		frame.getContentPane().add(panelGeomertry, gbc_panelGeomertry);
		GridBagLayout gbl_panelGeomertry = new GridBagLayout();
		gbl_panelGeomertry.columnWidths = new int[]{0, 0, 0};
		gbl_panelGeomertry.rowHeights = new int[]{0, 0, 0};
		gbl_panelGeomertry.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panelGeomertry.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelGeomertry.setLayout(gbl_panelGeomertry);
		
		JPanel panelGeometry1 = new JPanel();
		panelGeometry1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Disk Geomertry", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelGeometry1 = new GridBagConstraints();
		gbc_panelGeometry1.insets = new Insets(0, 0, 5, 5);
		gbc_panelGeometry1.anchor = GridBagConstraints.NORTH;
		gbc_panelGeometry1.gridx = 0;
		gbc_panelGeometry1.gridy = 0;
		panelGeomertry.add(panelGeometry1, gbc_panelGeometry1);
		GridBagLayout gbl_panelGeometry1 = new GridBagLayout();
		gbl_panelGeometry1.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelGeometry1.rowHeights = new int[]{0, 0, 0};
		gbl_panelGeometry1.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelGeometry1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panelGeometry1.setLayout(gbl_panelGeometry1);
		
		JLabel lblTracks = new JLabel("Tracks");
		GridBagConstraints gbc_lblTracks = new GridBagConstraints();
		gbc_lblTracks.insets = new Insets(0, 0, 5, 5);
		gbc_lblTracks.gridx = 1;
		gbc_lblTracks.gridy = 0;
		panelGeometry1.add(lblTracks, gbc_lblTracks);
		
		JLabel lblTracksPerHead = new JLabel("Tracks/Head");
		GridBagConstraints gbc_lblTracksPerHead = new GridBagConstraints();
		gbc_lblTracksPerHead.insets = new Insets(0, 0, 5, 5);
		gbc_lblTracksPerHead.gridx = 3;
		gbc_lblTracksPerHead.gridy = 0;
		panelGeometry1.add(lblTracksPerHead, gbc_lblTracksPerHead);
		
		JLabel lblSectorsPerTrack = new JLabel("Sectors/Track");
		GridBagConstraints gbc_lblSectorsPerTrack = new GridBagConstraints();
		gbc_lblSectorsPerTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblSectorsPerTrack.gridx = 5;
		gbc_lblSectorsPerTrack.gridy = 0;
		panelGeometry1.add(lblSectorsPerTrack, gbc_lblSectorsPerTrack);
		
		JLabel lblSectorSize = new JLabel("Sector Size");
		GridBagConstraints gbc_lblSectorSize = new GridBagConstraints();
		gbc_lblSectorSize.insets = new Insets(0, 0, 5, 0);
		gbc_lblSectorSize.gridx = 7;
		gbc_lblSectorSize.gridy = 0;
		panelGeometry1.add(lblSectorSize, gbc_lblSectorSize);
		
		JLabel label = new JLabel("2");
		label.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 1;
		panelGeometry1.add(label, gbc_label);
		
		JLabel lblNewLabel = new JLabel("9");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 3;
		gbc_lblNewLabel.gridy = 1;
		panelGeometry1.add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("28");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 5;
		gbc_lblNewLabel_1.gridy = 1;
		panelGeometry1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel label_1 = new JLabel("512");
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.gridx = 7;
		gbc_label_1.gridy = 1;
		panelGeometry1.add(label_1, gbc_label_1);
		
		JPanel panelTotals = new JPanel();
		panelTotals.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Disk Capacities", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelTotals = new GridBagConstraints();
		gbc_panelTotals.insets = new Insets(0, 0, 5, 0);
		gbc_panelTotals.anchor = GridBagConstraints.NORTH;
		gbc_panelTotals.gridx = 1;
		gbc_panelTotals.gridy = 0;
		panelGeomertry.add(panelTotals, gbc_panelTotals);
		GridBagLayout gbl_panelTotals = new GridBagLayout();
		gbl_panelTotals.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panelTotals.rowHeights = new int[]{0, 0, 0};
		gbl_panelTotals.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelTotals.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panelTotals.setLayout(gbl_panelTotals);
		
		JLabel lblTotalTracks = new JLabel("Total Tracks");
		GridBagConstraints gbc_lblTotalTracks = new GridBagConstraints();
		gbc_lblTotalTracks.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalTracks.gridx = 1;
		gbc_lblTotalTracks.gridy = 0;
		panelTotals.add(lblTotalTracks, gbc_lblTotalTracks);
		
		JLabel lblTotalSectors = new JLabel("Total Sectors");
		GridBagConstraints gbc_lblTotalSectors = new GridBagConstraints();
		gbc_lblTotalSectors.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalSectors.gridx = 3;
		gbc_lblTotalSectors.gridy = 0;
		panelTotals.add(lblTotalSectors, gbc_lblTotalSectors);
		
		JLabel lblTotalBytes = new JLabel("Total Bytes");
		GridBagConstraints gbc_lblTotalBytes = new GridBagConstraints();
		gbc_lblTotalBytes.insets = new Insets(0, 0, 5, 0);
		gbc_lblTotalBytes.gridx = 5;
		gbc_lblTotalBytes.gridy = 0;
		panelTotals.add(lblTotalBytes, gbc_lblTotalBytes);
		
		JLabel label_2 = new JLabel("127");
		label_2.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.insets = new Insets(0, 0, 0, 5);
		gbc_label_2.gridx = 1;
		gbc_label_2.gridy = 1;
		panelTotals.add(label_2, gbc_label_2);
		
		JLabel label_3 = new JLabel("1234");
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_label_3 = new GridBagConstraints();
		gbc_label_3.insets = new Insets(0, 0, 0, 5);
		gbc_label_3.gridx = 3;
		gbc_label_3.gridy = 1;
		panelTotals.add(label_3, gbc_label_3);
		
		JLabel label_4 = new JLabel("1,234,567");
		label_4.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_label_4 = new GridBagConstraints();
		gbc_label_4.gridx = 5;
		gbc_label_4.gridy = 1;
		panelTotals.add(label_4, gbc_label_4);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 2;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		JPanel panelPhysical = new JPanel();
		tabbedPane.addTab("Physical View", null, panelPhysical, null);
		GridBagLayout gbl_panelPhysical = new GridBagLayout();
		gbl_panelPhysical.columnWidths = new int[]{0, 0, 5, 0, 0, 80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelPhysical.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panelPhysical.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelPhysical.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panelPhysical.setLayout(gbl_panelPhysical);
		
		JLabel lblHead = new JLabel("Head");
		lblHead.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblHead = new GridBagConstraints();
		gbc_lblHead.insets = new Insets(0, 0, 5, 5);
		gbc_lblHead.gridx = 1;
		gbc_lblHead.gridy = 1;
		panelPhysical.add(lblHead, gbc_lblHead);
		
		Hex64KSpinner hexHead = new Hex64KSpinner();
		hexHead.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_hexHead = new GridBagConstraints();
		gbc_hexHead.insets = new Insets(0, 0, 5, 5);
		gbc_hexHead.gridx = 3;
		gbc_hexHead.gridy = 1;
		panelPhysical.add(hexHead, gbc_hexHead);
		
		JSpinner DecimalHead = new JSpinner();
		DecimalHead.setPreferredSize(new Dimension(60, 20));
		DecimalHead.setMinimumSize(new Dimension(80, 20));
		DecimalHead.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
		GridBagConstraints gbc_DecimalHead = new GridBagConstraints();
		gbc_DecimalHead.insets = new Insets(0, 0, 5, 5);
		gbc_DecimalHead.gridx = 5;
		gbc_DecimalHead.gridy = 1;
		panelPhysical.add(DecimalHead, gbc_DecimalHead);
		
		JLabel lblTrack = new JLabel("Track");
		GridBagConstraints gbc_lblTrack = new GridBagConstraints();
		gbc_lblTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblTrack.gridx = 7;
		gbc_lblTrack.gridy = 1;
		panelPhysical.add(lblTrack, gbc_lblTrack);
		
		Hex64KSpinner hexTrack = new Hex64KSpinner();
		hexTrack.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_hexTrack = new GridBagConstraints();
		gbc_hexTrack.insets = new Insets(0, 0, 5, 5);
		gbc_hexTrack.gridx = 9;
		gbc_hexTrack.gridy = 1;
		panelPhysical.add(hexTrack, gbc_hexTrack);
		
		JSpinner spinner = new JSpinner();
		spinner.setPreferredSize(new Dimension(60, 20));
		spinner.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 11;
		gbc_spinner.gridy = 1;
		panelPhysical.add(spinner, gbc_spinner);
		
		JLabel lblSector = new JLabel("Sector");
		GridBagConstraints gbc_lblSector = new GridBagConstraints();
		gbc_lblSector.insets = new Insets(0, 0, 5, 5);
		gbc_lblSector.gridx = 13;
		gbc_lblSector.gridy = 1;
		panelPhysical.add(lblSector, gbc_lblSector);
		
		Hex64KSpinner hexSector = new Hex64KSpinner();
		hexSector.setModel(new SpinnerNumberModel(1, 1, 65535, 1));
		hexSector.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_hexSector = new GridBagConstraints();
		gbc_hexSector.insets = new Insets(0, 0, 5, 5);
		gbc_hexSector.gridx = 15;
		gbc_hexSector.gridy = 1;
		panelPhysical.add(hexSector, gbc_hexSector);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		spinner_1.setPreferredSize(new Dimension(60, 20));
		spinner_1.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_1.gridx = 17;
		gbc_spinner_1.gridy = 1;
		panelPhysical.add(spinner_1, gbc_spinner_1);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 17;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 3;
		panelPhysical.add(scrollPane, gbc_scrollPane);
		
		JTextArea textArea = new JTextArea();
		textArea.setText("         1         2         3         4         5         6         7         8\r\n1\r2345678901234567890123456789012345678901234567890123456789012345678901234567890\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10\r\n11\r\n12\r\n13\r\n14\r\n15\r\n16\r\n17\r\n18\r\n19\r\n20\r\n21\r\n22\r\n23\r\n24\r\n25\r\n26");
		textArea.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPane.setViewportView(textArea);
		
		JPanel panelDirectory = new JPanel();
		tabbedPane.addTab("Directory View", null, panelDirectory, null);
		panelDirectory.setLayout(new GridLayout(1, 0, 0, 0));
		
		JPanel panelFile = new JPanel();
		tabbedPane.addTab("File View", null, panelFile, null);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);
		
		JMenuItem mnuFileNew = new JMenuItem("New");
		mnuFile.add(mnuFileNew);
		
		JMenuItem mnuFileOpen = new JMenuItem("Open File...");
		mnuFile.add(mnuFileOpen);
		
		JSeparator separator = new JSeparator();
		mnuFile.add(separator);
		
		JMenuItem mnuFileClose = new JMenuItem("Close");
		mnuFile.add(mnuFileClose);
		
		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);
		
		JMenuItem mnuFileSave = new JMenuItem("Save");
		mnuFile.add(mnuFileSave);
		
		JMenuItem mnuFileSaveAs = new JMenuItem("Save As...");
		mnuFile.add(mnuFileSaveAs);
		
		JSeparator separator_2 = new JSeparator();
		mnuFile.add(separator_2);
		
		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFile.add(mnuFileExit);
	}

}
