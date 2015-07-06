package disks;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import java.awt.Color;

import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

//import disks.DiskControlUnit.DriveLetter;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DiskUserInterface extends JDialog implements ActionListener {

	private final JPanel contentPanel = new JPanel();
	private DiskControlUnit dcu;
	private JLabel lblFileNameA;
	private JButton btnMountA;
	private JLabel lblSizeA;
	private JLabel lblFileNameB;
	private JButton btnMountB;
	private JLabel lblSizeB;
	private JLabel lblFileNameC;
	private JButton btnMountC;
	private JLabel lblSizeC;
	private JLabel lblFileNameD;
	private JButton btnMountD;
	private JLabel lblSizeD;

	private JLabel[] lblFileNames;
	private JLabel[] lblSizes;
	private JButton[] btnMounts;

	private final static String MOUNT = "Mount";
	private final static String DISMOUNT = "Dismount";
	private final static String NO_FILENAME = "<slot empty>";
	private final static String NO_SIZE = "<0 KB>";


	private JButton btnMakeNewDisk;

	/**
	 * Launch the application.
	 */
	// public static void main(String[] args) {
	// try {
	// DiskUserInterfaceDialog dialog = new DiskUserInterfaceDialog();
	// dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	// dialog.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	@Override
	public void actionPerformed(ActionEvent ae) {
//		DriveLetter selectedDriveLetter;
		switch (ae.getActionCommand()) {
		case "btnMountA":
			if (btnMountA.getText().equals(MOUNT)) {
				mountDisk(0);
			} else {
				dismountDisk(0);
			}// if
			break;
		case "btnMountB":
			if (btnMountB.getText().equals(MOUNT)) {
				mountDisk(1);
			} else {
				dismountDisk(1);
			}// if
			break;
		case "btnMountC":
			if (btnMountC.getText().equals(MOUNT)) {
				mountDisk(2);
			} else {
				dismountDisk(2);
			}// if
			break;
		case "btnMountD":
			if (btnMountD.getText().equals(MOUNT)) {
				mountDisk(3);
			} else {
				dismountDisk(3);
			}// if
			break;
		case "btnClose":
			this.dispose();
			break;
		default:
			break;
		}// switch

	}// actionPerformed

	private void dismountDisk(int index) {
		DiskDrive[] drives = dcu.getDrives();
		if (drives[index] == null) {
			JOptionPane.showMessageDialog(null, "No Disk to unmounted", "Unmount disk",
					JOptionPane.WARNING_MESSAGE);
			return;
		}// if

	}// dismountDisk

	private void mountDisk(int index) {
		DiskDrive[] drives = dcu.getDrives();
		if (drives[index] != null) {
			JOptionPane.showMessageDialog(null, "Already Mounted", "Mount disk",
					JOptionPane.WARNING_MESSAGE);
			return;
		}// if
		String diskType = (index < 2) ? "F5" : "F8";
		String diskAbsoluteName = getDisk(diskType);
		if (diskAbsoluteName == null) {
			return;
		}
		dcu.addDiskDrive(index, diskAbsoluteName);
		showTheDisks();
	}// mountDisk

	private String getDisk(String diskType) {
		String fileLocation = ".";
		Path sourcePath = Paths.get(fileLocation, "Disks");
		JFileChooser chooser = new JFileChooser(sourcePath.resolve(fileLocation).toString());
		chooser.setMultiSelectionEnabled(false);
		//
		for (DiskLayout diskLayout : DiskLayout.values()) {
			if (diskLayout.fileExtension.startsWith(diskType)) {
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

	private void showTheDisks() {
		DiskDrive[] drives = dcu.getDrives();
		boolean diskHere = false;
		for (int i = 0; i < dcu.getMaxNumberOfDrives(); i++) {
			diskHere = (drives[i] != null) ? true : false;
			lblFileNames[i].setText((diskHere) ? drives[i].getFileLocalName() : NO_FILENAME);
			lblFileNames[i].setToolTipText((diskHere) ? drives[i].getFileAbsoluteName() : NO_FILENAME);
			if (diskHere) {
				long size = drives[i].getTotalBytes();
				if (size > 1024000) {
					lblSizes[i].setText(String.format("%1.2f MB", (float) (size / 1024000)));
				} else {
					lblSizes[i].setText(String.format("%.0f KB", (float) (size / 1024)));
				}// if - KB v MB
			} else {
				lblSizes[i].setText(NO_SIZE);
			}// lblSize

			btnMounts[i].setText((diskHere) ? DISMOUNT : MOUNT);
		}// for - all disks

	}// showTheDisks

	private void appInit(DiskControlUnit dcu) {
		this.dcu = dcu;
		lblFileNames = new JLabel[] { lblFileNameA, lblFileNameB, lblFileNameC, lblFileNameD };
		lblSizes = new JLabel[] { lblSizeA, lblSizeB, lblSizeC, lblSizeD };
		btnMounts = new JButton[] { btnMountA, btnMountB, btnMountC, btnMountD };

		showTheDisks();

	}// appInit

	// ---------------------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	public DiskUserInterface(DiskControlUnit dcu) {
		super(null, "Disk User Interface", JDialog.DEFAULT_MODALITY_TYPE.MODELESS);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Font activeFont = new Font("Courier New", Font.BOLD, 20);
		Font inActiveFont = new Font("Courier New", Font.PLAIN, 20);

		setBounds(100, 100, 757, 426);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 740, 345);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);

		JPanel pnlAB = new JPanel();
		pnlAB.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 2, true), "5.25 \" Drives",
				TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlAB.setBounds(36, 32, 673, 121);
		contentPanel.add(pnlAB);
		pnlAB.setLayout(null);

		JLabel lblDriveA = new JLabel("A:");
		lblDriveA.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblDriveA.setHorizontalAlignment(SwingConstants.CENTER);
		lblDriveA.setBounds(10, 23, 30, 30);
		pnlAB.add(lblDriveA);

		lblFileNameA = new JLabel("New label");
		lblFileNameA.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lblFileNameA.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameA.setFont(new Font("Courier New", Font.BOLD, 20));
		lblFileNameA.setBounds(80, 23, 300, 30);
		pnlAB.add(lblFileNameA);

		lblSizeA = new JLabel("1.44MB");
		lblSizeA.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeA.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblSizeA.setBounds(550, 23, 95, 30);
		pnlAB.add(lblSizeA);

		btnMountA = new JButton(MOUNT);
		btnMountA.setActionCommand("btnMountA");
		btnMountA.addActionListener(this);
		btnMountA.setBounds(420, 23, 90, 30);
		pnlAB.add(btnMountA);

		JLabel lblDriveB = new JLabel("B:");
		lblDriveB.setHorizontalAlignment(SwingConstants.CENTER);
		lblDriveB.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblDriveB.setBounds(10, 69, 30, 30);
		pnlAB.add(lblDriveB);

		lblFileNameB = new JLabel("New label");
		lblFileNameB.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameB.setFont(new Font("Courier New", Font.BOLD, 20));
		lblFileNameB.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lblFileNameB.setBounds(80, 69, 300, 30);
		pnlAB.add(lblFileNameB);

		btnMountB = new JButton(MOUNT);
		btnMountB.setActionCommand("btnMountB");
		btnMountB.addActionListener(this);
		btnMountB.setBounds(420, 69, 90, 30);
		pnlAB.add(btnMountB);

		lblSizeB = new JLabel("1.44MB");
		lblSizeB.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeB.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblSizeB.setBounds(550, 69, 95, 30);
		pnlAB.add(lblSizeB);

		JPanel pnlCD = new JPanel();
		pnlCD.setLayout(null);
		pnlCD.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 2, true), "8 \" Drives", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlCD.setBounds(36, 187, 673, 121);
		contentPanel.add(pnlCD);

		JLabel lblDriveC = new JLabel("C:");
		lblDriveC.setHorizontalAlignment(SwingConstants.CENTER);
		lblDriveC.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblDriveC.setBounds(10, 23, 30, 30);
		pnlCD.add(lblDriveC);

		lblFileNameC = new JLabel("New label");
		lblFileNameC.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameC.setFont(new Font("Courier New", Font.BOLD, 20));
		lblFileNameC.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lblFileNameC.setBounds(80, 23, 300, 30);
		pnlCD.add(lblFileNameC);

		lblSizeC = new JLabel("1.44MB");
		lblSizeC.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeC.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblSizeC.setBounds(550, 23, 95, 30);
		pnlCD.add(lblSizeC);

		btnMountC = new JButton(MOUNT);
		btnMountC.setActionCommand("btnMountC");
		btnMountC.addActionListener(this);
		btnMountC.setBounds(420, 23, 90, 30);
		pnlCD.add(btnMountC);

		JLabel lblDriveD = new JLabel("D:");
		lblDriveD.setHorizontalAlignment(SwingConstants.CENTER);
		lblDriveD.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblDriveD.setBounds(10, 69, 30, 30);
		pnlCD.add(lblDriveD);

		lblFileNameD = new JLabel("New label");
		lblFileNameD.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameD.setFont(new Font("Courier New", Font.BOLD, 20));
		lblFileNameD.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lblFileNameD.setBounds(80, 69, 300, 30);
		pnlCD.add(lblFileNameD);

		btnMountD = new JButton(MOUNT);
		btnMountD.setActionCommand("btnMountD");
		btnMountD.addActionListener(this);
		btnMountD.setBounds(420, 69, 90, 30);
		pnlCD.add(btnMountD);

		lblSizeD = new JLabel("1.44MB");
		lblSizeD.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeD.setFont(new Font("Courier New", Font.PLAIN, 20));
		lblSizeD.setBounds(550, 69, 95, 30);
		pnlCD.add(lblSizeD);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(0, 350, 740, 33);
			getContentPane().add(buttonPane);
			buttonPane.setLayout(null);
			
			btnMakeNewDisk = new JButton("Make a new Disk");
			btnMakeNewDisk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					MakeNewDisk.makeNewDisk();
				}
			});
			btnMakeNewDisk.setBounds(10, 5, 135, 23);
			btnMakeNewDisk.setHorizontalAlignment(SwingConstants.LEADING);
			btnMakeNewDisk.setActionCommand("btnClose");
			buttonPane.add(btnMakeNewDisk);
			{
				JButton btnClose = new JButton("Close");
				btnClose.setBounds(654, 5, 81, 23);
				buttonPane.add(btnClose);
				btnClose.setActionCommand("btnClose");
				btnClose.addActionListener(this);
				getRootPane().setDefaultButton(btnClose);
			}
		}
		appInit(dcu); // initialize with the DiskControlUnit

	}// class

}
