package disks;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Insets;
import myComponents.Hex64KSpinner;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;
import javax.swing.SpinnerNumberModel;
import myComponents.Hex64KSpinner16;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DiskParameterBlockTest {
	
	DiskGeometry dg;

	private static final String FMT_DECIMAL = "%,d";
	private static final String FMT_HEX = "%X";
	
	private JFrame frmTestDiskParameterblock;
	private Hex64KSpinner spinnerTracksHex;
	private Hex64KSpinner spinnerSectorsHex;
	private Hex64KSpinner spinnerBytesPerSectorHex;
	private Hex64KSpinner spinnerBlockSizeFactorHex;
	private JSpinner spinnerHeadsDecimal;
	private JSpinner spinnerTracksDecimal;
	private JSpinner spinnerSectorsDecimal;
	private JSpinner spinnerBytesPerSectorDecimal;
	private JPanel panelVariables;
	private JLabel lblHexPSC;
	private JLabel lblDecimalPSC;
	private JLabel lblHexCAP;
	private JLabel lblDecimalCAP;
	private JLabel lblHexLS;
	private JLabel lblDecimalLS;
	private JLabel lblDecimalLT;
	private JLabel lblHexBS;
	private JLabel lblDecimalBS;
	private JLabel lblHexEC;
	private JLabel lblDecimalEC;
	private JLabel lblHexLE;
	private JLabel lblDecimalLE;
	private JLabel lblHexPE;
	private JLabel lblDecimalPE;
	private JLabel lblHexSPT;
	private JLabel lblDecimalSPT;
	private JLabel lblHexBSH;
	private JLabel lblDecimalBSH;
	private JLabel lblHexBLM;
	private JLabel lblDecimalBLM;
	private JLabel lblHexEXM;
	private JLabel lblDecimalEXM;
	private JLabel lblHexDSM;
	private JLabel lblDecimalDSM;
	private JLabel lblHexDRM;
	private JLabel lblDecimalDRM;
	private JLabel lblHexAL0;
	private JLabel lblDecimalAL0;
	private JLabel lblHexAL1;
	private JLabel lblDecimalAL1;
	private JLabel lblHexCKS;
	private JLabel lblDecimalCKS;
	private JLabel lblHexOFS;
	private JLabel lblDecimalOFS;
	private JSpinner spinnerBlockSizeFactorDecimal;
	private Hex64KSpinner spinnerHeadsHex;
	private JLabel lblHexLT;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DiskParameterBlockTest window = new DiskParameterBlockTest();
					window.frmTestDiskParameterblock.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void showValue(){
		showVariables();
		
		lblDecimalPSC.setText(String.format(FMT_DECIMAL, dg.getPhysicalSectorCount()));
		lblHexPSC.setText(String.format(FMT_HEX, dg.getPhysicalSectorCount()));
		
		lblDecimalCAP.setText(String.format(FMT_DECIMAL, dg.getCapacity()));
		lblHexCAP.setText(String.format(FMT_HEX,  dg.getCapacity()));
		
		lblDecimalLS.setText(String.format(FMT_DECIMAL, dg.getLogicalSectorSize()));
		lblHexLS.setText(String.format(FMT_HEX,  dg.getLogicalSectorSize()));
		
		lblDecimalLT.setText(String.format(FMT_DECIMAL, dg.getLogicalTrack()));
		lblHexLT.setText(String.format(FMT_HEX,  dg.getLogicalTrack()));
		
		lblDecimalBS.setText(String.format(FMT_DECIMAL, dg.getBlockSize()));
		lblHexBS.setText(String.format(FMT_HEX,  dg.getBlockSize()));
		
		lblDecimalEC.setText(String.format(FMT_DECIMAL, dg.getExtentCapacity()));
		lblHexEC.setText(String.format(FMT_HEX,  dg.getExtentCapacity()));
		
		lblDecimalLE.setText(String.format(FMT_DECIMAL, dg.getLogicalExtent()));
		lblHexLE.setText(String.format(FMT_HEX,  dg.getLogicalExtent()));
		
		lblDecimalPE.setText(String.format(FMT_DECIMAL, dg.getPhysicalExtent()));
		lblHexPE.setText(String.format(FMT_HEX,  dg.getPhysicalExtent()));
		
	}
	private void showVariables(){
		spinnerHeadsHex.setValue(dg.getNumberOfHeads());
		 spinnerTracksHex.setValue(dg.getTracksPerHead());
		 spinnerSectorsHex.setValue(dg.getSectorsPerTrack());
		 spinnerBytesPerSectorHex.setValue(dg.getBytesPerSector());
		 spinnerBlockSizeFactorHex.setValue(dg.getBlockSizeFactor());

	}
	///-----------------------------------------------------------------------------------------
	
	private void appInit(){
		spinnerHeadsDecimal.setModel(spinnerHeadsHex.getModel());
		spinnerTracksDecimal.setModel(spinnerTracksHex.getModel());
		spinnerSectorsDecimal.setModel(spinnerSectorsHex.getModel());
		spinnerBytesPerSectorDecimal.setModel(spinnerBytesPerSectorHex.getModel());
		spinnerBlockSizeFactorDecimal.setModel(spinnerBlockSizeFactorHex.getModel());
		
		spinnerHeadsHex.setValue(2);
		spinnerTracksHex.setValue(40);
		spinnerSectorsHex.setValue(9);
		spinnerBytesPerSectorHex.setValue(512);
		spinnerBlockSizeFactorHex.setValue(4);
		dg = new DiskGeometry();
	}

	/**
	 * Create the application.
	 */
	public DiskParameterBlockTest() {
		initialize();
		appInit();
	}
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTestDiskParameterblock = new JFrame();
		frmTestDiskParameterblock.setTitle("Test Disk ParameterBlock Calculations");
		frmTestDiskParameterblock.setBounds(100, 100, 814, 773);
		frmTestDiskParameterblock.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{20, 0, 50, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		frmTestDiskParameterblock.getContentPane().setLayout(gridBagLayout);
		
		panelVariables = new JPanel();
		panelVariables.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 255), 1, true), "Variables", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panelVariables = new GridBagConstraints();
		gbc_panelVariables.gridheight = 3;
		gbc_panelVariables.insets = new Insets(0, 0, 5, 5);
		gbc_panelVariables.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelVariables.gridx = 1;
		gbc_panelVariables.gridy = 1;
		frmTestDiskParameterblock.getContentPane().add(panelVariables, gbc_panelVariables);
		GridBagLayout gbl_panelVariables = new GridBagLayout();
		gbl_panelVariables.columnWidths = new int[]{10, 0, 40, 0, 10, 0};
		gbl_panelVariables.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelVariables.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelVariables.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelVariables.setLayout(gbl_panelVariables);
		
		JLabel lblV1 = new JLabel("NOH");
		lblV1.setForeground(Color.RED);
		lblV1.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblV1 = new GridBagConstraints();
		gbc_lblV1.insets = new Insets(0, 0, 5, 5);
		gbc_lblV1.gridx = 1;
		gbc_lblV1.gridy = 0;
		panelVariables.add(lblV1, gbc_lblV1);
		
		JLabel lblV1a = new JLabel("Number of Heads");
		GridBagConstraints gbc_lblV1a = new GridBagConstraints();
		gbc_lblV1a.insets = new Insets(0, 0, 5, 5);
		gbc_lblV1a.gridx = 1;
		gbc_lblV1a.gridy = 1;
		panelVariables.add(lblV1a, gbc_lblV1a);
		
		spinnerHeadsHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerHeadsHex = new GridBagConstraints();
		gbc_spinnerHeadsHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerHeadsHex.gridx = 1;
		gbc_spinnerHeadsHex.gridy = 2;
		panelVariables.add(spinnerHeadsHex, gbc_spinnerHeadsHex);
		
		spinnerHeadsDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerHeadsDecimal = new GridBagConstraints();
		gbc_spinnerHeadsDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerHeadsDecimal.gridx = 1;
		gbc_spinnerHeadsDecimal.gridy = 3;
		panelVariables.add(spinnerHeadsDecimal, gbc_spinnerHeadsDecimal);
		
		JLabel lblV4 = new JLabel("PSS");
		lblV4.setForeground(Color.RED);
		lblV4.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblV4 = new GridBagConstraints();
		gbc_lblV4.insets = new Insets(0, 0, 5, 5);
		gbc_lblV4.gridx = 3;
		gbc_lblV4.gridy = 4;
		panelVariables.add(lblV4, gbc_lblV4);
		
		JLabel lblV2 = new JLabel("TPH");
		lblV2.setForeground(Color.RED);
		lblV2.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblV2 = new GridBagConstraints();
		gbc_lblV2.insets = new Insets(0, 0, 5, 5);
		gbc_lblV2.gridx = 1;
		gbc_lblV2.gridy = 5;
		panelVariables.add(lblV2, gbc_lblV2);
		
		JLabel lblV4a = new JLabel("Bytes per Sector");
		GridBagConstraints gbc_lblV4a = new GridBagConstraints();
		gbc_lblV4a.insets = new Insets(0, 0, 5, 5);
		gbc_lblV4a.gridx = 3;
		gbc_lblV4a.gridy = 5;
		panelVariables.add(lblV4a, gbc_lblV4a);
		
		JLabel lblV2a = new JLabel("Tracks per Head");
		GridBagConstraints gbc_lblV2a = new GridBagConstraints();
		gbc_lblV2a.insets = new Insets(0, 0, 5, 5);
		gbc_lblV2a.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblV2a.gridx = 1;
		gbc_lblV2a.gridy = 6;
		panelVariables.add(lblV2a, gbc_lblV2a);
		
		spinnerBytesPerSectorHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerBytesPerSectorHex = new GridBagConstraints();
		gbc_spinnerBytesPerSectorHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerBytesPerSectorHex.gridx = 3;
		gbc_spinnerBytesPerSectorHex.gridy = 6;
		panelVariables.add(spinnerBytesPerSectorHex, gbc_spinnerBytesPerSectorHex);
		
		spinnerTracksHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerTracksHex = new GridBagConstraints();
		gbc_spinnerTracksHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerTracksHex.gridx = 1;
		gbc_spinnerTracksHex.gridy = 7;
		panelVariables.add(spinnerTracksHex, gbc_spinnerTracksHex);
		
		spinnerBytesPerSectorDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerBytesPerSectorDecimal = new GridBagConstraints();
		gbc_spinnerBytesPerSectorDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerBytesPerSectorDecimal.gridx = 3;
		gbc_spinnerBytesPerSectorDecimal.gridy = 7;
		panelVariables.add(spinnerBytesPerSectorDecimal, gbc_spinnerBytesPerSectorDecimal);
		
		spinnerTracksDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerTracksDecimal = new GridBagConstraints();
		gbc_spinnerTracksDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerTracksDecimal.gridx = 1;
		gbc_spinnerTracksDecimal.gridy = 8;
		panelVariables.add(spinnerTracksDecimal, gbc_spinnerTracksDecimal);
		
		JLabel lblV5 = new JLabel("BSF");
		lblV5.setForeground(Color.RED);
		lblV5.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblV5 = new GridBagConstraints();
		gbc_lblV5.insets = new Insets(0, 0, 5, 5);
		gbc_lblV5.gridx = 3;
		gbc_lblV5.gridy = 9;
		panelVariables.add(lblV5, gbc_lblV5);
		
		JLabel lblV3 = new JLabel("SPT");
		lblV3.setForeground(Color.RED);
		lblV3.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblV3 = new GridBagConstraints();
		gbc_lblV3.insets = new Insets(0, 0, 5, 5);
		gbc_lblV3.gridx = 1;
		gbc_lblV3.gridy = 10;
		panelVariables.add(lblV3, gbc_lblV3);
		
		JLabel lblSizeFactor = new JLabel("Block Size Factor");
		GridBagConstraints gbc_lblSizeFactor = new GridBagConstraints();
		gbc_lblSizeFactor.anchor = GridBagConstraints.BELOW_BASELINE;
		gbc_lblSizeFactor.insets = new Insets(0, 0, 5, 5);
		gbc_lblSizeFactor.gridx = 3;
		gbc_lblSizeFactor.gridy = 10;
		panelVariables.add(lblSizeFactor, gbc_lblSizeFactor);
		
		JLabel lblV3a = new JLabel("Sectors/Track");
		GridBagConstraints gbc_lblV3a = new GridBagConstraints();
		gbc_lblV3a.insets = new Insets(0, 0, 5, 5);
		gbc_lblV3a.gridx = 1;
		gbc_lblV3a.gridy = 11;
		panelVariables.add(lblV3a, gbc_lblV3a);
		
		spinnerBlockSizeFactorHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerBlockSizeFactorHex = new GridBagConstraints();
		gbc_spinnerBlockSizeFactorHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerBlockSizeFactorHex.gridx = 3;
		gbc_spinnerBlockSizeFactorHex.gridy = 11;
		panelVariables.add(spinnerBlockSizeFactorHex, gbc_spinnerBlockSizeFactorHex);
		
		spinnerSectorsHex = new Hex64KSpinner();
		GridBagConstraints gbc_spinnerSectorsHex = new GridBagConstraints();
		gbc_spinnerSectorsHex.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerSectorsHex.gridx = 1;
		gbc_spinnerSectorsHex.gridy = 12;
		panelVariables.add(spinnerSectorsHex, gbc_spinnerSectorsHex);
		
		spinnerBlockSizeFactorDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerBlockSizeFactorDecimal = new GridBagConstraints();
		gbc_spinnerBlockSizeFactorDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerBlockSizeFactorDecimal.gridx = 3;
		gbc_spinnerBlockSizeFactorDecimal.gridy = 12;
		panelVariables.add(spinnerBlockSizeFactorDecimal, gbc_spinnerBlockSizeFactorDecimal);
		
		spinnerSectorsDecimal = new JSpinner();
		GridBagConstraints gbc_spinnerSectorsDecimal = new GridBagConstraints();
		gbc_spinnerSectorsDecimal.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerSectorsDecimal.gridx = 1;
		gbc_spinnerSectorsDecimal.gridy = 13;
		panelVariables.add(spinnerSectorsDecimal, gbc_spinnerSectorsDecimal);
		
		JPanel panelBios = new JPanel();
		panelBios.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 255), 1, true), "Misc BIOS values", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelBios = new GridBagConstraints();
		gbc_panelBios.insets = new Insets(0, 0, 5, 5);
		gbc_panelBios.fill = GridBagConstraints.VERTICAL;
		gbc_panelBios.gridx = 3;
		gbc_panelBios.gridy = 1;
		frmTestDiskParameterblock.getContentPane().add(panelBios, gbc_panelBios);
		GridBagLayout gbl_panelBios = new GridBagLayout();
		gbl_panelBios.columnWidths = new int[]{0, 0, 10, 0, 20, 10, 50, 50, 0};
		gbl_panelBios.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelBios.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelBios.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelBios.setLayout(gbl_panelBios);
		
		JLabel lblB1 = new JLabel("PSC");
		lblB1.setForeground(Color.RED);
		lblB1.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblB1 = new GridBagConstraints();
		gbc_lblB1.insets = new Insets(0, 0, 5, 5);
		gbc_lblB1.gridx = 1;
		gbc_lblB1.gridy = 0;
		panelBios.add(lblB1, gbc_lblB1);
		
		lblHexPSC = new JLabel("0800");
		lblHexPSC.setForeground(Color.BLUE);
		lblHexPSC.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexPSC = new GridBagConstraints();
		gbc_lblHexPSC.anchor = GridBagConstraints.EAST;
		gbc_lblHexPSC.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexPSC.gridx = 3;
		gbc_lblHexPSC.gridy = 0;
		panelBios.add(lblHexPSC, gbc_lblHexPSC);
		
		lblDecimalPSC = new JLabel("2,048");
		lblDecimalPSC.setForeground(Color.BLUE);
		lblDecimalPSC.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalPSC = new GridBagConstraints();
		gbc_lblDecimalPSC.anchor = GridBagConstraints.EAST;
		gbc_lblDecimalPSC.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalPSC.gridx = 5;
		gbc_lblDecimalPSC.gridy = 0;
		panelBios.add(lblDecimalPSC, gbc_lblDecimalPSC);
		
		JLabel lblPhysicalSectorCount = new JLabel("Physical Sector Count");
		GridBagConstraints gbc_lblPhysicalSectorCount = new GridBagConstraints();
		gbc_lblPhysicalSectorCount.insets = new Insets(0, 0, 5, 0);
		gbc_lblPhysicalSectorCount.gridx = 7;
		gbc_lblPhysicalSectorCount.gridy = 0;
		panelBios.add(lblPhysicalSectorCount, gbc_lblPhysicalSectorCount);
		
		JLabel lblB2 = new JLabel("CAP");
		lblB2.setForeground(Color.RED);
		lblB2.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblB2 = new GridBagConstraints();
		gbc_lblB2.insets = new Insets(0, 0, 5, 5);
		gbc_lblB2.gridx = 1;
		gbc_lblB2.gridy = 1;
		panelBios.add(lblB2, gbc_lblB2);
		
		lblHexCAP = new JLabel("0800");
		lblHexCAP.setForeground(Color.BLUE);
		lblHexCAP.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexCAP = new GridBagConstraints();
		gbc_lblHexCAP.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexCAP.gridx = 3;
		gbc_lblHexCAP.gridy = 1;
		panelBios.add(lblHexCAP, gbc_lblHexCAP);
		
		lblDecimalCAP = new JLabel("2,048");
		lblDecimalCAP.setForeground(Color.BLUE);
		lblDecimalCAP.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalCAP = new GridBagConstraints();
		gbc_lblDecimalCAP.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalCAP.gridx = 5;
		gbc_lblDecimalCAP.gridy = 1;
		panelBios.add(lblDecimalCAP, gbc_lblDecimalCAP);
		
		JLabel lblCapacityInBtyes = new JLabel("Capacity in Btyes");
		GridBagConstraints gbc_lblCapacityInBtyes = new GridBagConstraints();
		gbc_lblCapacityInBtyes.insets = new Insets(0, 0, 5, 0);
		gbc_lblCapacityInBtyes.gridx = 7;
		gbc_lblCapacityInBtyes.gridy = 1;
		panelBios.add(lblCapacityInBtyes, gbc_lblCapacityInBtyes);
		
		JLabel lblB3 = new JLabel("LS");
		lblB3.setForeground(Color.RED);
		lblB3.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblB3 = new GridBagConstraints();
		gbc_lblB3.insets = new Insets(0, 0, 5, 5);
		gbc_lblB3.gridx = 1;
		gbc_lblB3.gridy = 2;
		panelBios.add(lblB3, gbc_lblB3);
		
		lblHexLS = new JLabel("0800");
		lblHexLS.setForeground(Color.BLUE);
		lblHexLS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexLS = new GridBagConstraints();
		gbc_lblHexLS.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexLS.gridx = 3;
		gbc_lblHexLS.gridy = 2;
		panelBios.add(lblHexLS, gbc_lblHexLS);
		
		lblDecimalLS = new JLabel("2,048");
		lblDecimalLS.setForeground(Color.BLUE);
		lblDecimalLS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalLS = new GridBagConstraints();
		gbc_lblDecimalLS.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalLS.gridx = 5;
		gbc_lblDecimalLS.gridy = 2;
		panelBios.add(lblDecimalLS, gbc_lblDecimalLS);
		
		JLabel lblLogicalSector = new JLabel("Logical Sector");
		GridBagConstraints gbc_lblLogicalSector = new GridBagConstraints();
		gbc_lblLogicalSector.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogicalSector.gridx = 7;
		gbc_lblLogicalSector.gridy = 2;
		panelBios.add(lblLogicalSector, gbc_lblLogicalSector);
		
		JLabel label = new JLabel("");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 7;
		gbc_label.gridy = 3;
		panelBios.add(label, gbc_label);
		
		JLabel lblB4 = new JLabel("LT");
		lblB4.setForeground(Color.RED);
		lblB4.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblB4 = new GridBagConstraints();
		gbc_lblB4.insets = new Insets(0, 0, 5, 5);
		gbc_lblB4.gridx = 1;
		gbc_lblB4.gridy = 4;
		panelBios.add(lblB4, gbc_lblB4);
		
		lblHexLT = new JLabel("0800");
		lblHexLT.setForeground(Color.BLUE);
		lblHexLT.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexLT = new GridBagConstraints();
		gbc_lblHexLT.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexLT.gridx = 3;
		gbc_lblHexLT.gridy = 4;
		panelBios.add(lblHexLT, gbc_lblHexLT);
		
		lblDecimalLT = new JLabel("2,048");
		lblDecimalLT.setForeground(Color.BLUE);
		lblDecimalLT.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalLT = new GridBagConstraints();
		gbc_lblDecimalLT.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalLT.gridx = 5;
		gbc_lblDecimalLT.gridy = 4;
		panelBios.add(lblDecimalLT, gbc_lblDecimalLT);
		
		JLabel lblLogicalTrack = new JLabel("Logical Track");
		GridBagConstraints gbc_lblLogicalTrack = new GridBagConstraints();
		gbc_lblLogicalTrack.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogicalTrack.gridx = 7;
		gbc_lblLogicalTrack.gridy = 4;
		panelBios.add(lblLogicalTrack, gbc_lblLogicalTrack);
		
		JLabel lbl5 = new JLabel("BS");
		lbl5.setForeground(Color.RED);
		lbl5.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lbl5 = new GridBagConstraints();
		gbc_lbl5.insets = new Insets(0, 0, 5, 5);
		gbc_lbl5.gridx = 1;
		gbc_lbl5.gridy = 5;
		panelBios.add(lbl5, gbc_lbl5);
		
		lblHexBS = new JLabel("0800");
		lblHexBS.setForeground(Color.BLUE);
		lblHexBS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexBS = new GridBagConstraints();
		gbc_lblHexBS.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexBS.gridx = 3;
		gbc_lblHexBS.gridy = 5;
		panelBios.add(lblHexBS, gbc_lblHexBS);
		
		lblDecimalBS = new JLabel("2,048");
		lblDecimalBS.setForeground(Color.BLUE);
		lblDecimalBS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalBS = new GridBagConstraints();
		gbc_lblDecimalBS.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalBS.gridx = 5;
		gbc_lblDecimalBS.gridy = 5;
		panelBios.add(lblDecimalBS, gbc_lblDecimalBS);
		
		JLabel lblBlockSize = new JLabel("Block Size");
		GridBagConstraints gbc_lblBlockSize = new GridBagConstraints();
		gbc_lblBlockSize.insets = new Insets(0, 0, 5, 0);
		gbc_lblBlockSize.gridx = 7;
		gbc_lblBlockSize.gridy = 5;
		panelBios.add(lblBlockSize, gbc_lblBlockSize);
		
		JLabel lblEc = new JLabel("EC");
		lblEc.setForeground(Color.RED);
		lblEc.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblEc = new GridBagConstraints();
		gbc_lblEc.insets = new Insets(0, 0, 5, 5);
		gbc_lblEc.gridx = 1;
		gbc_lblEc.gridy = 7;
		panelBios.add(lblEc, gbc_lblEc);
		
		lblHexEC = new JLabel("0800");
		lblHexEC.setForeground(Color.BLUE);
		lblHexEC.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexEC = new GridBagConstraints();
		gbc_lblHexEC.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexEC.gridx = 3;
		gbc_lblHexEC.gridy = 7;
		panelBios.add(lblHexEC, gbc_lblHexEC);
		
		lblDecimalEC = new JLabel("2,048");
		lblDecimalEC.setForeground(Color.BLUE);
		lblDecimalEC.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalEC = new GridBagConstraints();
		gbc_lblDecimalEC.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalEC.gridx = 5;
		gbc_lblDecimalEC.gridy = 7;
		panelBios.add(lblDecimalEC, gbc_lblDecimalEC);
		
		JLabel lblExtentCapacity = new JLabel("Extent Capacity");
		GridBagConstraints gbc_lblExtentCapacity = new GridBagConstraints();
		gbc_lblExtentCapacity.insets = new Insets(0, 0, 5, 0);
		gbc_lblExtentCapacity.gridx = 7;
		gbc_lblExtentCapacity.gridy = 7;
		panelBios.add(lblExtentCapacity, gbc_lblExtentCapacity);
		
		JLabel lblLe = new JLabel("LE");
		lblLe.setForeground(Color.RED);
		lblLe.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblLe = new GridBagConstraints();
		gbc_lblLe.insets = new Insets(0, 0, 5, 5);
		gbc_lblLe.gridx = 1;
		gbc_lblLe.gridy = 8;
		panelBios.add(lblLe, gbc_lblLe);
		
		lblHexLE = new JLabel("0800");
		lblHexLE.setForeground(Color.BLUE);
		lblHexLE.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexLE = new GridBagConstraints();
		gbc_lblHexLE.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexLE.gridx = 3;
		gbc_lblHexLE.gridy = 8;
		panelBios.add(lblHexLE, gbc_lblHexLE);
		
		lblDecimalLE = new JLabel("2,048");
		lblDecimalLE.setForeground(Color.BLUE);
		lblDecimalLE.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalLE = new GridBagConstraints();
		gbc_lblDecimalLE.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalLE.gridx = 5;
		gbc_lblDecimalLE.gridy = 8;
		panelBios.add(lblDecimalLE, gbc_lblDecimalLE);
		
		JLabel lblLogicalExtent = new JLabel("Logical Extent");
		GridBagConstraints gbc_lblLogicalExtent = new GridBagConstraints();
		gbc_lblLogicalExtent.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogicalExtent.gridx = 7;
		gbc_lblLogicalExtent.gridy = 8;
		panelBios.add(lblLogicalExtent, gbc_lblLogicalExtent);
		
		JLabel lblPe = new JLabel("PE");
		lblPe.setForeground(Color.RED);
		lblPe.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblPe = new GridBagConstraints();
		gbc_lblPe.insets = new Insets(0, 0, 0, 5);
		gbc_lblPe.gridx = 1;
		gbc_lblPe.gridy = 9;
		panelBios.add(lblPe, gbc_lblPe);
		
		lblHexPE = new JLabel("0800");
		lblHexPE.setForeground(Color.BLUE);
		lblHexPE.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexPE = new GridBagConstraints();
		gbc_lblHexPE.insets = new Insets(0, 0, 0, 5);
		gbc_lblHexPE.gridx = 3;
		gbc_lblHexPE.gridy = 9;
		panelBios.add(lblHexPE, gbc_lblHexPE);
		
		lblDecimalPE = new JLabel("2,048");
		lblDecimalPE.setForeground(Color.BLUE);
		lblDecimalPE.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalPE = new GridBagConstraints();
		gbc_lblDecimalPE.insets = new Insets(0, 0, 0, 5);
		gbc_lblDecimalPE.gridx = 5;
		gbc_lblDecimalPE.gridy = 9;
		panelBios.add(lblDecimalPE, gbc_lblDecimalPE);
		
		JLabel lblPhysicalExtent = new JLabel("Physical Extent");
		GridBagConstraints gbc_lblPhysicalExtent = new GridBagConstraints();
		gbc_lblPhysicalExtent.gridx = 7;
		gbc_lblPhysicalExtent.gridy = 9;
		panelBios.add(lblPhysicalExtent, gbc_lblPhysicalExtent);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 255), 1, true), "Disk Parameter Block", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 3;
		gbc_panel.gridy = 3;
		frmTestDiskParameterblock.getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 10, 10, 20, 10, 50, 50, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblD1 = new JLabel("SPT");
		lblD1.setForeground(Color.RED);
		lblD1.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblD1 = new GridBagConstraints();
		gbc_lblD1.insets = new Insets(0, 0, 5, 5);
		gbc_lblD1.gridx = 1;
		gbc_lblD1.gridy = 1;
		panel.add(lblD1, gbc_lblD1);
		
		lblHexSPT = new JLabel("0800");
		lblHexSPT.setForeground(Color.BLUE);
		lblHexSPT.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexSPT = new GridBagConstraints();
		gbc_lblHexSPT.anchor = GridBagConstraints.EAST;
		gbc_lblHexSPT.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexSPT.gridx = 3;
		gbc_lblHexSPT.gridy = 1;
		panel.add(lblHexSPT, gbc_lblHexSPT);
		
		lblDecimalSPT = new JLabel("2,048");
		lblDecimalSPT.setForeground(Color.BLUE);
		lblDecimalSPT.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalSPT = new GridBagConstraints();
		gbc_lblDecimalSPT.anchor = GridBagConstraints.EAST;
		gbc_lblDecimalSPT.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalSPT.gridx = 5;
		gbc_lblDecimalSPT.gridy = 1;
		panel.add(lblDecimalSPT, gbc_lblDecimalSPT);
		
		JLabel lblSectorsPerLogical = new JLabel("Sectors per logical Track");
		GridBagConstraints gbc_lblSectorsPerLogical = new GridBagConstraints();
		gbc_lblSectorsPerLogical.insets = new Insets(0, 0, 5, 0);
		gbc_lblSectorsPerLogical.gridx = 7;
		gbc_lblSectorsPerLogical.gridy = 1;
		panel.add(lblSectorsPerLogical, gbc_lblSectorsPerLogical);
		
		JLabel lblBsh = new JLabel("BSH");
		lblBsh.setForeground(Color.RED);
		lblBsh.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblBsh = new GridBagConstraints();
		gbc_lblBsh.insets = new Insets(0, 0, 5, 5);
		gbc_lblBsh.gridx = 1;
		gbc_lblBsh.gridy = 2;
		panel.add(lblBsh, gbc_lblBsh);
		
		lblHexBSH = new JLabel("0800");
		lblHexBSH.setForeground(Color.BLUE);
		lblHexBSH.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexBSH = new GridBagConstraints();
		gbc_lblHexBSH.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexBSH.gridx = 3;
		gbc_lblHexBSH.gridy = 2;
		panel.add(lblHexBSH, gbc_lblHexBSH);
		
		lblDecimalBSH = new JLabel("2,048");
		lblDecimalBSH.setForeground(Color.BLUE);
		lblDecimalBSH.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalBSH = new GridBagConstraints();
		gbc_lblDecimalBSH.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalBSH.gridx = 5;
		gbc_lblDecimalBSH.gridy = 2;
		panel.add(lblDecimalBSH, gbc_lblDecimalBSH);
		
		JLabel lblBlockShift = new JLabel("Block Shift");
		GridBagConstraints gbc_lblBlockShift = new GridBagConstraints();
		gbc_lblBlockShift.insets = new Insets(0, 0, 5, 0);
		gbc_lblBlockShift.gridx = 7;
		gbc_lblBlockShift.gridy = 2;
		panel.add(lblBlockShift, gbc_lblBlockShift);
		
		JLabel lblBlm = new JLabel("BLM");
		lblBlm.setForeground(Color.RED);
		lblBlm.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblBlm = new GridBagConstraints();
		gbc_lblBlm.insets = new Insets(0, 0, 5, 5);
		gbc_lblBlm.gridx = 1;
		gbc_lblBlm.gridy = 3;
		panel.add(lblBlm, gbc_lblBlm);
		
		lblHexBLM = new JLabel("0800");
		lblHexBLM.setForeground(Color.BLUE);
		lblHexBLM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexBLM = new GridBagConstraints();
		gbc_lblHexBLM.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexBLM.gridx = 3;
		gbc_lblHexBLM.gridy = 3;
		panel.add(lblHexBLM, gbc_lblHexBLM);
		
		lblDecimalBLM = new JLabel("2,048");
		lblDecimalBLM.setForeground(Color.BLUE);
		lblDecimalBLM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalBLM = new GridBagConstraints();
		gbc_lblDecimalBLM.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalBLM.gridx = 5;
		gbc_lblDecimalBLM.gridy = 3;
		panel.add(lblDecimalBLM, gbc_lblDecimalBLM);
		
		JLabel lblBlockMask = new JLabel("Block Mask");
		GridBagConstraints gbc_lblBlockMask = new GridBagConstraints();
		gbc_lblBlockMask.insets = new Insets(0, 0, 5, 0);
		gbc_lblBlockMask.gridx = 7;
		gbc_lblBlockMask.gridy = 3;
		panel.add(lblBlockMask, gbc_lblBlockMask);
		
		JLabel lblExm = new JLabel("EXM");
		lblExm.setForeground(Color.RED);
		lblExm.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblExm = new GridBagConstraints();
		gbc_lblExm.insets = new Insets(0, 0, 5, 5);
		gbc_lblExm.gridx = 1;
		gbc_lblExm.gridy = 4;
		panel.add(lblExm, gbc_lblExm);
		
		lblHexEXM = new JLabel("0800");
		lblHexEXM.setForeground(Color.BLUE);
		lblHexEXM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexEXM = new GridBagConstraints();
		gbc_lblHexEXM.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexEXM.gridx = 3;
		gbc_lblHexEXM.gridy = 4;
		panel.add(lblHexEXM, gbc_lblHexEXM);
		
		lblDecimalEXM = new JLabel("2,048");
		lblDecimalEXM.setForeground(Color.BLUE);
		lblDecimalEXM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalEXM = new GridBagConstraints();
		gbc_lblDecimalEXM.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalEXM.gridx = 5;
		gbc_lblDecimalEXM.gridy = 4;
		panel.add(lblDecimalEXM, gbc_lblDecimalEXM);
		
		JLabel lblExtentMask = new JLabel("Extent Mask");
		GridBagConstraints gbc_lblExtentMask = new GridBagConstraints();
		gbc_lblExtentMask.insets = new Insets(0, 0, 5, 0);
		gbc_lblExtentMask.gridx = 7;
		gbc_lblExtentMask.gridy = 4;
		panel.add(lblExtentMask, gbc_lblExtentMask);
		
		JLabel lblDsm = new JLabel("DSM");
		lblDsm.setForeground(Color.RED);
		lblDsm.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblDsm = new GridBagConstraints();
		gbc_lblDsm.insets = new Insets(0, 0, 5, 5);
		gbc_lblDsm.gridx = 1;
		gbc_lblDsm.gridy = 5;
		panel.add(lblDsm, gbc_lblDsm);
		
		lblHexDSM = new JLabel("0800");
		lblHexDSM.setForeground(Color.BLUE);
		lblHexDSM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexDSM = new GridBagConstraints();
		gbc_lblHexDSM.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexDSM.gridx = 3;
		gbc_lblHexDSM.gridy = 5;
		panel.add(lblHexDSM, gbc_lblHexDSM);
		
		lblDecimalDSM = new JLabel("2,048");
		lblDecimalDSM.setForeground(Color.BLUE);
		lblDecimalDSM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalDSM = new GridBagConstraints();
		gbc_lblDecimalDSM.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalDSM.gridx = 5;
		gbc_lblDecimalDSM.gridy = 5;
		panel.add(lblDecimalDSM, gbc_lblDecimalDSM);
		
		JLabel lblHighestBlockNumber = new JLabel("Highest Block Number");
		GridBagConstraints gbc_lblHighestBlockNumber = new GridBagConstraints();
		gbc_lblHighestBlockNumber.insets = new Insets(0, 0, 5, 0);
		gbc_lblHighestBlockNumber.gridx = 7;
		gbc_lblHighestBlockNumber.gridy = 5;
		panel.add(lblHighestBlockNumber, gbc_lblHighestBlockNumber);
		
		JLabel lblDrm = new JLabel("DRM");
		lblDrm.setForeground(Color.RED);
		lblDrm.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblDrm = new GridBagConstraints();
		gbc_lblDrm.insets = new Insets(0, 0, 5, 5);
		gbc_lblDrm.gridx = 1;
		gbc_lblDrm.gridy = 6;
		panel.add(lblDrm, gbc_lblDrm);
		
		lblHexDRM = new JLabel("0800");
		lblHexDRM.setForeground(Color.BLUE);
		lblHexDRM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexDRM = new GridBagConstraints();
		gbc_lblHexDRM.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexDRM.gridx = 3;
		gbc_lblHexDRM.gridy = 6;
		panel.add(lblHexDRM, gbc_lblHexDRM);
		
		lblDecimalDRM = new JLabel("2,048");
		lblDecimalDRM.setForeground(Color.BLUE);
		lblDecimalDRM.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalDRM = new GridBagConstraints();
		gbc_lblDecimalDRM.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalDRM.gridx = 5;
		gbc_lblDecimalDRM.gridy = 6;
		panel.add(lblDecimalDRM, gbc_lblDecimalDRM);
		
		JLabel lblDirectoryEntries = new JLabel("Directory Entries -1");
		GridBagConstraints gbc_lblDirectoryEntries = new GridBagConstraints();
		gbc_lblDirectoryEntries.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirectoryEntries.gridx = 7;
		gbc_lblDirectoryEntries.gridy = 6;
		panel.add(lblDirectoryEntries, gbc_lblDirectoryEntries);
		
		JLabel lblAl = new JLabel("AL0");
		lblAl.setForeground(Color.RED);
		lblAl.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblAl = new GridBagConstraints();
		gbc_lblAl.insets = new Insets(0, 0, 5, 5);
		gbc_lblAl.gridx = 1;
		gbc_lblAl.gridy = 7;
		panel.add(lblAl, gbc_lblAl);
		
		lblHexAL0 = new JLabel("0800");
		lblHexAL0.setForeground(Color.BLUE);
		lblHexAL0.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexAL0 = new GridBagConstraints();
		gbc_lblHexAL0.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexAL0.gridx = 3;
		gbc_lblHexAL0.gridy = 7;
		panel.add(lblHexAL0, gbc_lblHexAL0);
		
		lblDecimalAL0 = new JLabel("2,048");
		lblDecimalAL0.setForeground(Color.BLUE);
		lblDecimalAL0.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalAL0 = new GridBagConstraints();
		gbc_lblDecimalAL0.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalAL0.gridx = 5;
		gbc_lblDecimalAL0.gridy = 7;
		panel.add(lblDecimalAL0, gbc_lblDecimalAL0);
		
		JLabel lblAllocaion = new JLabel("Allocaion 0");
		GridBagConstraints gbc_lblAllocaion = new GridBagConstraints();
		gbc_lblAllocaion.insets = new Insets(0, 0, 5, 0);
		gbc_lblAllocaion.gridx = 7;
		gbc_lblAllocaion.gridy = 7;
		panel.add(lblAllocaion, gbc_lblAllocaion);
		
		JLabel lblAl_1 = new JLabel("AL1");
		lblAl_1.setForeground(Color.RED);
		lblAl_1.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblAl_1 = new GridBagConstraints();
		gbc_lblAl_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblAl_1.gridx = 1;
		gbc_lblAl_1.gridy = 8;
		panel.add(lblAl_1, gbc_lblAl_1);
		
		lblHexAL1 = new JLabel("0800");
		lblHexAL1.setForeground(Color.BLUE);
		lblHexAL1.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexAL1 = new GridBagConstraints();
		gbc_lblHexAL1.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexAL1.gridx = 3;
		gbc_lblHexAL1.gridy = 8;
		panel.add(lblHexAL1, gbc_lblHexAL1);
		
		lblDecimalAL1 = new JLabel("2,048");
		lblDecimalAL1.setForeground(Color.BLUE);
		lblDecimalAL1.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalAL1 = new GridBagConstraints();
		gbc_lblDecimalAL1.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalAL1.gridx = 5;
		gbc_lblDecimalAL1.gridy = 8;
		panel.add(lblDecimalAL1, gbc_lblDecimalAL1);
		
		JLabel lblAllocation = new JLabel("Allocation 1");
		GridBagConstraints gbc_lblAllocation = new GridBagConstraints();
		gbc_lblAllocation.insets = new Insets(0, 0, 5, 0);
		gbc_lblAllocation.gridx = 7;
		gbc_lblAllocation.gridy = 8;
		panel.add(lblAllocation, gbc_lblAllocation);
		
		JLabel lblCks = new JLabel("CKS");
		lblCks.setForeground(Color.RED);
		lblCks.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblCks = new GridBagConstraints();
		gbc_lblCks.insets = new Insets(0, 0, 5, 5);
		gbc_lblCks.gridx = 1;
		gbc_lblCks.gridy = 9;
		panel.add(lblCks, gbc_lblCks);
		
		lblHexCKS = new JLabel("0800");
		lblHexCKS.setForeground(Color.BLUE);
		lblHexCKS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexCKS = new GridBagConstraints();
		gbc_lblHexCKS.insets = new Insets(0, 0, 5, 5);
		gbc_lblHexCKS.gridx = 3;
		gbc_lblHexCKS.gridy = 9;
		panel.add(lblHexCKS, gbc_lblHexCKS);
		
		lblDecimalCKS = new JLabel("2,048");
		lblDecimalCKS.setForeground(Color.BLUE);
		lblDecimalCKS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalCKS = new GridBagConstraints();
		gbc_lblDecimalCKS.insets = new Insets(0, 0, 5, 5);
		gbc_lblDecimalCKS.gridx = 5;
		gbc_lblDecimalCKS.gridy = 9;
		panel.add(lblDecimalCKS, gbc_lblDecimalCKS);
		
		JLabel lblChecksumVectorSize = new JLabel("Checksum Vector Size");
		GridBagConstraints gbc_lblChecksumVectorSize = new GridBagConstraints();
		gbc_lblChecksumVectorSize.insets = new Insets(0, 0, 5, 0);
		gbc_lblChecksumVectorSize.gridx = 7;
		gbc_lblChecksumVectorSize.gridy = 9;
		panel.add(lblChecksumVectorSize, gbc_lblChecksumVectorSize);
		
		JLabel lblOfs = new JLabel("OFS");
		lblOfs.setForeground(Color.RED);
		lblOfs.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblOfs = new GridBagConstraints();
		gbc_lblOfs.insets = new Insets(0, 0, 0, 5);
		gbc_lblOfs.fill = GridBagConstraints.VERTICAL;
		gbc_lblOfs.gridx = 1;
		gbc_lblOfs.gridy = 10;
		panel.add(lblOfs, gbc_lblOfs);
		
		lblHexOFS = new JLabel("0800");
		lblHexOFS.setForeground(Color.BLUE);
		lblHexOFS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblHexOFS = new GridBagConstraints();
		gbc_lblHexOFS.insets = new Insets(0, 0, 0, 5);
		gbc_lblHexOFS.gridx = 3;
		gbc_lblHexOFS.gridy = 10;
		panel.add(lblHexOFS, gbc_lblHexOFS);
		
		lblDecimalOFS = new JLabel("2,048");
		lblDecimalOFS.setForeground(Color.BLUE);
		lblDecimalOFS.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblDecimalOFS = new GridBagConstraints();
		gbc_lblDecimalOFS.insets = new Insets(0, 0, 0, 5);
		gbc_lblDecimalOFS.gridx = 5;
		gbc_lblDecimalOFS.gridy = 10;
		panel.add(lblDecimalOFS, gbc_lblDecimalOFS);
		
		JLabel lblCylinderOffset = new JLabel("Cylinder Offset");
		GridBagConstraints gbc_lblCylinderOffset = new GridBagConstraints();
		gbc_lblCylinderOffset.gridx = 7;
		gbc_lblCylinderOffset.gridy = 10;
		panel.add(lblCylinderOffset, gbc_lblCylinderOffset);
		
		JButton btnCalculate = new JButton("Calculate");
		btnCalculate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dg.setNumberOfHeads((int) spinnerHeadsHex.getValue());
				dg.setTracksPerHead((int) spinnerTracksHex.getValue());
				dg.setSectorsPerTrack((int) spinnerSectorsHex.getValue());
				dg.setBytesPerSector((int) spinnerBytesPerSectorHex.getValue());
				dg.setBlockSizeFactor((int) spinnerBlockSizeFactorHex.getValue());
				showValue();
			}
		});
		GridBagConstraints gbc_btnCalculate = new GridBagConstraints();
		gbc_btnCalculate.insets = new Insets(0, 0, 0, 5);
		gbc_btnCalculate.gridx = 1;
		gbc_btnCalculate.gridy = 5;
		frmTestDiskParameterblock.getContentPane().add(btnCalculate, gbc_btnCalculate);
	}

}
