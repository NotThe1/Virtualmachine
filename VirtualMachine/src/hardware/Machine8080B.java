package hardware;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Scanner;
//import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
//import javax.swing.JTextArea;
import javax.swing.JButton;
//import javax.swing.text.PlainDocument;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
//import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;

//import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
//import javax.swing.JFormattedTextField$AbstractFormatter;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

import codeSupport.DebugManager;
import codeSupport.Disassembler;
import codeSupport.ShowCode;
import device.DeviceController;
import disks.DiskControlUnit;
import disks.DiskDrive;
//import disks.DiskLayout;
import disks.DiskMetrics;
import disks.DiskUserInterface;
import disks.MakeNewDisk;
import memory.Core;
import memory.MainMemory;
import memory.MemoryLimitVerifier;
import memoryDisplay.MemorySaver;
import memoryDisplay.ShowCoreMemory;

//import javax.swing.ScrollPaneConstants;
//import javax.swing.BoxLayout;

//import java.awt.FlowLayout;

public class Machine8080B implements PropertyChangeListener, MouseListener,
		FocusListener, ItemListener, ActionListener {

	private JFrame frmMachineb;
	private Preferences prefs;

	private Core core;
	private ConditionCodeRegister ccr;
	private WorkingRegisterSet wrs;
	private MainMemory mm; // (core)
	private DeviceController dc;
	private ArithmeticUnit au; // (ccr)
	private CentralProcessingUnit cpu; // (mm,ccr,au,wrs,dc)
	private DiskControlUnit dcu; // (core)

	private ShowCode showCode;
	private Disassembler disassembler; // (core,txtBox,cpu)
	private ShowCoreMemory scm;
	private DebugManager debugManager;

	private JScrollPane scrollAssembler;
	private MaskFormatter format2HexDigits;
	private MaskFormatter format4HexDigits;

	private String currentMachineName = DEFAULT_STATE_FILE;

	private String cmdPutty = "\"C:\\Program Files (x86)\\PuTTY\\putty.exe\" -load \"COM1\" ";
	private Process putty;

	private String memoryDirectory = MEMORY;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Machine8080B window = new Machine8080B();
					window.frmMachineb.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private JFileChooser getFileChooser(String subDirectory, String filterDescription, String... filterExtensions) {
		Path sourcePath;
		if (subDirectory.contains("\\")) {
			sourcePath = Paths.get(subDirectory);
		} else {
			sourcePath = Paths.get(FILE_LOCATION, subDirectory);
		}
		String fp = sourcePath.resolve(FILE_LOCATION).toString();

		JFileChooser chooser = new JFileChooser(fp);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDescription, filterExtensions));
		chooser.setAcceptAllFileFilterUsed(false);
		return chooser;
	}// getFileChooser

	private String stripSuffix(String fileName) {
		String result = fileName;
		int periodLocation = fileName.indexOf(".");
		if (periodLocation != -1) {// this selection has a suffix
			result = fileName.substring(0, periodLocation); // removed suffix
		}// inner if
		return result;
	}// stripSuffix

	private String getDefaultMachineStateFile() {
		Path sourcePath = Paths.get(FILE_LOCATION, SETTINGS, DEFAULT_STATE_FILE).toAbsolutePath().normalize();
		return sourcePath.toString();
	}// getDefaultMachineStateFile

	// private void saveMachineState() {
	// saveMachineState(getDefaultMachineStateFile());
	// ;
	// }// saveMachineState

	private void saveMachineState(String fileName) {
		wrs.setProgramCounter(cpu.getProgramCounter()); // save PC
		dcu.close();
		core.resetListeners();
		// mm.close();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName + FILE_SUFFIX_PERIOD));
			oos.writeObject(ccr);
			oos.writeObject(wrs);
			oos.writeObject(core);
			oos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}// try - write objects
	}// saveMachineState(String fileName)

	private void makeNewMachine() {
		closeAllObjects();
		core = new Core(MEMORY_SIZE_BYTES);
		ccr = new ConditionCodeRegister();
		wrs = new WorkingRegisterSet();

		mm = new MainMemory(core);

		dc = new DeviceController();
		au = new ArithmeticUnit(ccr);
		cpu = new CentralProcessingUnit(mm, ccr, au, wrs, dc);
		cpu.setProgramCounter(wrs.getProgramCounter());
		dcu = new DiskControlUnit(core);
		// disassembler = new Disassembler(core, txtAssemblerCode.getDocument(), cpu);
		disassembler = new Disassembler(core, txtAssemblerCode.getDocument());
		disassembler.upDateDisplay(wrs.getProgramCounter());
		loadTheDisplay();
		setupDisks();
	}

	// private void restoreMachineState() {
	// restoreMachineState(currentMachineName);// getDefaultMachineStateFile()
	// }// restoreMachineState

	private void restoreMachineState(String fileName) {
		makeNewMachine();
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName + FILE_SUFFIX_PERIOD));
			ccr = (ConditionCodeRegister) ois.readObject();
			wrs = (WorkingRegisterSet) ois.readObject();
			core = (Core) ois.readObject();
			currentMachineName = fileName;
			frmMachineb.setTitle(fileName);
			ois.close();
		} catch (Exception e) {
			System.err.printf(
					"Not able to completely restore machine state%n %s%n", e.getMessage());
			core = new Core(MEMORY_SIZE_BYTES);
			ccr = new ConditionCodeRegister();
			wrs = new WorkingRegisterSet();
		}// try

		mm = new MainMemory(core);

		// dc = new DeviceController();
		au = new ArithmeticUnit(ccr);
		cpu = new CentralProcessingUnit(mm, ccr, au, wrs, dc);
		cpu.setProgramCounter(wrs.getProgramCounter());
		dcu = new DiskControlUnit(core);
		disassembler = new Disassembler(core, txtAssemblerCode.getDocument());

	}// restoreMachineState

	private void loadTheDisplay() {
		ftfReg_A.setValue(getByteDisplayValue(wrs.getReg(Reg.A)));
		ftfReg_B.setValue(getByteDisplayValue(wrs.getReg(Reg.B)));
		ftfReg_C.setValue(getByteDisplayValue(wrs.getReg(Reg.C)));
		ftfReg_D.setValue(getByteDisplayValue(wrs.getReg(Reg.D)));
		ftfReg_E.setValue(getByteDisplayValue(wrs.getReg(Reg.E)));
		ftfReg_H.setValue(getByteDisplayValue(wrs.getReg(Reg.H)));
		ftfReg_L.setValue(getByteDisplayValue(wrs.getReg(Reg.L)));
		int sp = wrs.getStackPointer();
		String spDisplay = getWordDisplayValue(sp);
		ftfReg_SP.setValue(spDisplay);
		showRegM();
		wrs.setProgramCounter(cpu.getProgramCounter());
		ftfReg_PC.setValue(getWordDisplayValue(wrs.getProgramCounter()));
		// disassembler.run();

		// if (cpu != null) { // use the current value, not the initial value from the restore file
		// wrs.setProgramCounter(cpu.getProgramCounter());
		// Disassembler d = new Disassembler(core, txtAssemblerCode.getDocument(), cpu);
		// d.run();
		// }//
		ckbSign.setSelected(ccr.isSignFlagSet());
		ckbZero.setSelected(ccr.isZeroFlagSet());
		ckbAuxCarry.setSelected(ccr.isAuxilaryCarryFlagSet());
		ckbParity.setSelected(ccr.isParityFlagSet());
		ckbCarry.setSelected(ccr.isCarryFlagSet());

		if (scm != null) {
			scm.refresh();
		}// if

	}// loadTheDisplay

	private void showRegM() {
		String Reg_HL = (String) ftfReg_H.getValue()
				+ (String) ftfReg_L.getValue();
		if (Reg_HL.contains("null")) {
			return;
		}
		Integer mLocation = Integer.valueOf(Reg_HL, 16);
		byte value = 0X00;
		try {
			value = core.readForIO(mLocation);
			ftfReg_M.setForeground(Color.BLACK);
		} catch (ArrayIndexOutOfBoundsException e) {
			ftfReg_M.setForeground(Color.lightGray);
		}// try
		ftfReg_M.setValue(getByteDisplayValue(value));
	}// showRegM

	private String getByteDisplayValue(int value) {
		return String.format("%02X", value & 0XFF);
	}// getByteDisplayValue

	private String getWordDisplayValue(int value) {
		return String.format("%04X", value & 0XFFFF);
	}// getWordDisplayValue

	private void updateConditionCode(String flagName, boolean state) {
		switch (flagName) {
		case NAME_FLAG_SIGN:
			ccr.setSignFlag(state);
			break;
		case NAME_FLAG_ZERO:
			ccr.setZeroFlag(state);
			break;
		case NAME_FLAG_AUXCARRY:
			ccr.setAuxilaryCarryFlag(state);
			break;
		case NAME_FLAG_PARITY:
			ccr.setParityFlag(state);
			break;
		case NAME_FLAG_CARRY:
			ccr.setCarryFlag(state);
			break;
		}// switch
		setCheckBoxFont(ckbSign);
		setCheckBoxFont(ckbZero);
		setCheckBoxFont(ckbAuxCarry);
		setCheckBoxFont(ckbParity);
		setCheckBoxFont(ckbCarry);

	}// updateConditionCode

	private void setCheckBoxFont(JCheckBox ckb) {
		if (ckb.isSelected()) {
			ckb.setForeground(Color.RED);
		} else {
			ckb.setForeground(Color.GRAY);
		}// if
	}// setCheckBoxFont

	private void modifyRegister(String regName, String strValue) {
		int intValue = Integer.valueOf(strValue, 16);

		switch (regName) {
		case NAME_REG_A:
			wrs.setReg(Reg.A, (byte) intValue);

			break;
		case NAME_REG_B:
			wrs.setReg(Reg.B, (byte) intValue);

			break;
		case NAME_REG_C:
			wrs.setReg(Reg.C, (byte) intValue);

			break;
		case NAME_REG_D:
			wrs.setReg(Reg.D, (byte) intValue);

			break;
		case NAME_REG_E:
			wrs.setReg(Reg.E, (byte) intValue);

			break;
		case NAME_REG_H:
			wrs.setReg(Reg.H, (byte) intValue);
			showRegM();

			break;
		case NAME_REG_L:
			wrs.setReg(Reg.L, (byte) intValue);
			showRegM();

			break;
		case NAME_REG_M:
			// not yet implemented
			break;
		case NAME_REG_SP:
			wrs.setStackPointer((int) intValue);
			break;
		case NAME_REG_PC:
			if (cpu != null) {
				int thisIntValue = ((int) intValue) & 0XFFFF;
				cpu.setProgramCounter(thisIntValue);
				wrs.setProgramCounter(thisIntValue);
				// disassembler.run();
				disassembler.upDateDisplay(thisIntValue);
				txtAssemblerCode.setCaretPosition(0);
				// scrollAssembler.getVerticalScrollBar().setValue(0);
				if (showCode != null) {
					showCode.setProgramCounter(thisIntValue);
				}// if
			}// if - cpu!=null
			break;

		default:
		}
	}// modifyRegister

	private void showRun(boolean runState) {
		btnRun.setVisible(runState);
		btnStop.setVisible(!runState);
	}// showRun

	private void removeSelectedFromList() {
		int itemCount = mnuMemory.getItemCount();
		for (int i = itemCount - 1; i > 0; i--) {
			if (mnuMemory.getItem(i) instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem mi = (JCheckBoxMenuItem) mnuMemory.getItem(i);
				if (mi.getState()) {
					mnuMemory.remove(mi);
				}
			}// if
		}// for
	}// removeSelectedFromList
	private void saveSelectedToList(){
		JFileChooser fc = getFileChooser("Lists", "Listing Set Files", "ListSet");
		if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
			System.out.printf("You cancelled theSave Selected as List...%n", "");
			return;
		}// if
		String destinationPath = fc.getSelectedFile().getAbsolutePath();
		try {
			FileWriter fileWriter = new FileWriter(destinationPath + ".ListSet");
			BufferedWriter writer = new BufferedWriter(fileWriter);

			int itemCount = mnuMemory.getItemCount();
			for (int i = itemCount - 1; i > 0; i--) {

				if (mnuMemory.getItem(i) instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem mi = (JCheckBoxMenuItem) mnuMemory.getItem(i);
					if (mi.getState()) {
						writer.write(mi.getName() + "\n");
					}// inner if - selected = true
				}// outer if
			}// for
			writer.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}// try
	}

	private void getMemoryImageFiles() {
//		JFileChooser fc = getFileChooser(memoryDirectory, "Memory file lists", "ListSet");
		JFileChooser fc = getFileChooser("Lists", "Memory file lists", "ListSet");
		if (fc.showOpenDialog(frmMachineb) != JFileChooser.APPROVE_OPTION) {
			System.out.printf("You cancelled the Load Memory from File List...%n", "");
		} else {
			FileReader fileReader;
			try {
				fileReader = new FileReader((fc.getSelectedFile().getAbsolutePath()));
				BufferedReader reader = new BufferedReader(fileReader);
				String rawFilePathName = null;
				String filePathName = null;
				File currentFile;
				while ((rawFilePathName = reader.readLine()) != null) {
					filePathName = rawFilePathName.replaceAll("list\\z", MEMORY_SUFFIX);
					currentFile = new File(filePathName);
					loadMemoryImage(currentFile);
				}// while
			} catch (IOException e1) {
				System.out.printf(e1.getMessage() + "%n", "");
			}// try
		}// if

		return;
	}// getMemoryImageFiles

	private File getMemoryImageFile() {
		File sourceFile = null;
		JFileChooser chooserLMI = getFileChooser("", "Memory files", MEMORY_SUFFIX, MEMORY_SUFFIX1);
		if (chooserLMI.showOpenDialog(frmMachineb) != JFileChooser.APPROVE_OPTION) {
			System.out.printf("You cancelled the Load Memory...%n", "");
		} else {
			sourceFile = chooserLMI.getSelectedFile();
			memoryDirectory = sourceFile.getParent();
		}
		return sourceFile;
	}

	private void loadMemoryImage(File sourceFile) {
		// JFileChooser chooserLMI = getFileChooser(memoryDirectory, "Memory files", MEMORY_SUFFIX, MEMORY_SUFFIX1);
		// if (chooserLMI.showOpenDialog(frmMachineb) != JFileChooser.APPROVE_OPTION) {
		// System.out.printf("You cancelled the Load Memory...%n", "");
		// } else {

		// File sourceFile = chooserLMI.getSelectedFile();
		// memoryDirectory = sourceFile.getParent();
		String memoryFileType = (sourceFile.getName().endsWith(MEMORY_SUFFIX)) ? MEMORY_SUFFIX : MEMORY_SUFFIX1;
		try {
			FileReader fileReader = new FileReader((sourceFile));
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			while ((line = reader.readLine()) != null) {
				switch (memoryFileType) {
				case MEMORY_SUFFIX:
					parseAndLoadImageMem(line);
					break;
				case MEMORY_SUFFIX1:
					parseAndLoadImageHex(line);
					break;
				default:
				}
			}// while
			reader.close();
			String thisFileName = sourceFile.getName();
			mnuNew = new JCheckBoxMenuItem(thisFileName);
			mnuNew.setName(sourceFile.getAbsolutePath());
			mnuNew.setActionCommand(thisFileName);
			mnuMemory.add(mnuNew);

		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(null, sourceFile.getAbsolutePath()
					+ "not found", "unable to locate",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		} catch (IOException ie) {
			JOptionPane.showMessageDialog(null, sourceFile.getAbsolutePath()
					+ ie.getMessage(), "IO error",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		}// try
			// }// if - returnValue

	}// loadMemoryImage

	private void parseAndLoadImageMem(String line) {
		if (line.length() == 0) {
			return; // skip the line
		}// if

		Scanner scanner = new Scanner(line);
		String strAddress = scanner.next();
		strAddress = strAddress.replace(":", "");
		int address = Integer.valueOf(strAddress, 16);

		if ((address + 0X0F) >= MEMORY_SIZE_BYTES) {
			JOptionPane.showMessageDialog(null,
					"Address out of current memory on address line: "
							+ strAddress, "Out of bounds",
					JOptionPane.ERROR_MESSAGE);
			scanner.close();
			return;
		}// if max memeory test

		// byte value;
		byte[] values = new byte[SIXTEEN];
		for (int i = 0; i < SIXTEEN; i++) {
			values[i] = (byte) ((int) Integer.valueOf(scanner.next(), 16));
		}// for values

		core.writeDMA(address, values); // avoid setting off memory traps
		scanner.close();
	}// parseAndLoadImage

	private void parseAndLoadImageHex(String line) {
		line = line.replace(" ", ""); // remove any spaces
		if (line.length() == 0) {
			return; // skip the line
		}// if
		if (line.startsWith(":") == false) {
			return; // skip the line
		}// if
			// first value beyond the colon
		int byteCount = Integer.valueOf(line.substring(1, 3), 16);
		int address = Integer.valueOf(line.substring(3, 7), 16);
		if ((address + byteCount - 1) >= MEMORY_SIZE_BYTES) {
			String msg = String.format(
					"Address out of current memory on address line: %s.", line.substring(3, 7));
			JOptionPane.showMessageDialog(null, msg, "Out of bounds", JOptionPane.ERROR_MESSAGE);
			return;
		}// if max memory test
		byte recordType = (byte) ((int) (Integer.valueOf(line.substring(7, 9), 16)));
		switch (recordType) {
		case DATA_RECORD:
			byte[] values = new byte[byteCount];
			byte value;
			int checksum = byteCount + recordType +
					Integer.valueOf(line.substring(3, 5), 16) +
					Integer.valueOf(line.substring(5, 7), 16);
			for (int i = 0; i < byteCount; i++) {
				value = (byte) ((int) Integer.valueOf(line.substring((i * 2) + 9, (i * 2) + 11), 16));
				values[i] = value;
				checksum += value;
			}// for
			int checksumValue = Integer.valueOf(line.substring((byteCount * 2) + 9, (byteCount * 2) + 11), 16);
			checksum = checksum + checksumValue;

			if ((checksum & 0xFF) != 0) {
				String msg = String.format(
						"checksum error on address line: %s.", line.substring(3, 7));
				JOptionPane.showMessageDialog(null, msg, "CheckSum error", JOptionPane.ERROR_MESSAGE);
				return;
			}// if - checksum test

			core.writeDMA(address, values); // avoid setting off memory traps

			break;
		case END_OF_FILE_RECORD:
			String msg = "End of File Record found!";
			JOptionPane.showMessageDialog(null, msg, "Hex memory loader", JOptionPane.INFORMATION_MESSAGE);
			break;
		case EXTENDED_SEGMENT_ADDRESS_RECORD:
			break;
		case START_SEGMENT_ADDRESS_RECORD:
			break;
		case EXTENDED_LINEAR_ADDRESS_RECORD:
			break;
		case START_LINEAR_ADDRESS_RECORD:
			break;
		default:
		}// switch

	}

	static final byte DATA_RECORD = (byte) 0x00;
	static final byte END_OF_FILE_RECORD = (byte) 0x01;
	static final byte EXTENDED_SEGMENT_ADDRESS_RECORD = (byte) 0x02;
	static final byte START_SEGMENT_ADDRESS_RECORD = (byte) 0x03;
	static final byte EXTENDED_LINEAR_ADDRESS_RECORD = (byte) 0x04;
	static final byte START_LINEAR_ADDRESS_RECORD = (byte) 0x05;

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Override
	public void actionPerformed(ActionEvent ae) {

		String actionCommand = ae.getActionCommand();
		switch (actionCommand) {
		case AC_BTN_RUN:
//			showRun(false);
//			cpu.startRunMode();
//			showRun(true);
//			loadTheDisplay();
			doRun();
			break;
		case AC_BTN_STOP:
			// scrollAssembler.getVerticalScrollBar().setValue(0);
			// System.out.printf("Scroll Bar value: %d%n",scrollAssembler.getVerticalScrollBar().getValue());

			showRun(true);
			cpu.setRunning(false);
			// loadTheDisplay();
			break;
		case AC_BTN_STEP:
			cpu.startStepMode((int) spinnerStepCount.getValue());
			loadTheDisplay();
			break;

		case AC_BTN_MOUNT_A:
			addRmoveDisk(0, ((AbstractButton) ae.getSource()).getText());
			break;
		case AC_BTN_MOUNT_B:
			addRmoveDisk(1, ((AbstractButton) ae.getSource()).getText());
			break;
		case AC_BTN_MOUNT_C:
			addRmoveDisk(2, ((AbstractButton) ae.getSource()).getText());
			break;
		case AC_BTN_MOUNT_D:
			addRmoveDisk(3, ((AbstractButton) ae.getSource()).getText());
			break;
		case AC_MNU_FILE_NEW:
			// makeNewMachine();
			appInit();
			// disassembler.upDateDisplay(wrs.getProgramCounter());
			break;

		case AC_MNU_FILE_OPEN:
			JFileChooser chooserOpen = getFileChooser(SETTINGS, "Saved Machine State", FILE_SUFFIX);
			if (chooserOpen.showOpenDialog(frmMachineb) == JFileChooser.APPROVE_OPTION) {
				String absolutePath = chooserOpen.getSelectedFile().getAbsolutePath();
				// need to strip the file suffix off (will replace later)
				absolutePath = stripSuffix(absolutePath);
				restoreMachineState(absolutePath);
				disassembler.resetDisplay();
				loadTheDisplay();
				disassembler.upDateDisplay(wrs.getProgramCounter());
			} else {
				System.out.printf("You cancelled the Open...%n", "");
			}// if - returnValue
			break;
		case AC_MNU_FILE_SAVE:
			saveMachineState(getDefaultMachineStateFile());
			break;
		case AC_MNU_FILE_SAVEAS:
			JFileChooser chooserSaveAs = getFileChooser(SETTINGS, "Saved Machine State", FILE_SUFFIX);
			if (chooserSaveAs.showSaveDialog(frmMachineb) != JFileChooser.APPROVE_OPTION) {
				System.out.printf("You cancelled the Save as...%n", "");
			} else {
				String absolutePath = chooserSaveAs.getSelectedFile().getAbsolutePath();
				// need to strip the file suffix off (will replace later)
				absolutePath = stripSuffix(absolutePath);
				saveMachineState(absolutePath);
			}// if - returnValue
			break;
		case AC_MNU_FILE_CLOSE:
			appClose();
			System.exit(-1);
			break;

		case AC_MNU_DISKS_MOUNT:
			DiskUserInterface duid = new DiskUserInterface(dcu);
			duid.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			duid.setVisible(true);
			break;
		case AC_MNU_DISKS_NEW:
			MakeNewDisk.makeNewDisk();
			break;

		case AC_MNU_MEMORY_SHOW:
			scm = new ShowCoreMemory(core);
			scm.run();
			break;
		case AC_MNU_MEMORY_SAVE:
			new MemorySaver().show(core);
			break;
		case AC_MNU_MEMORY_LOAD:
			File sourceFile = getMemoryImageFile();
			if (sourceFile == null) {
				break;
			}
			loadMemoryImage(sourceFile);
			if (scm != null) {
				scm.refresh();
			}
			disassembler.resetDisplay();
			disassembler.upDateDisplay(wrs.getProgramCounter());
			txtAssemblerCode.setCaretPosition(0);
			break;

		case AC_MNU_MEMORY_LOAD_LIST:
			getMemoryImageFiles();
			break;
		case AC_MNU_MEMORY_SAVE_LIST:
			saveSelectedToList();
			break;
		case AC_MNU_MEMORY_REMOVE_SELECTED:
			removeSelectedFromList();
			break;
		case AC_MNU_TOOLS_PUTTY:
			loadPutty();
			break;
		case AC_MNU_TOOLS_DEBUG_SUITE:
			if (showCode == null) {
				showCode = new ShowCode();
			}// if
			showCode.setVisible(true);
			if (debugManager == null) {
				debugManager = new DebugManager(core);
			}// if
//			debugManager.setLocation(1, 100);
			debugManager.setVisible(true);
			

		case AC_MNU_TOOLS_SHOW_CODE:
			if (showCode == null) {
				showCode = new ShowCode();
			}// if
			showCode.setVisible(true);
			break;
		case AC_MNU_TOOLS_DEBUG_MANAGER:
			if (debugManager == null) {
				debugManager = new DebugManager(core);
			}// if
			debugManager.setVisible(true);
			break;
		case AC_MNU_TOOLS_REBOOT:
			loadROM();
			wrs.setProgramCounter(0X0000);
			wrs.setStackPointer(0X0080);
			doRun();
			break;
		}// switch - actionCommand

	}// actionPerformed

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		if ("value".equals(pce.getPropertyName())) {
			if (pce.getNewValue() != pce.getOldValue()) {
				String ftfName = ((JFormattedTextField) pce.getSource()).getName();
				modifyRegister(ftfName, (String) pce.getNewValue());
			}// inner if - really changed
		}// outer if - "value"
	}// propertyChange

	@Override
	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() == 2) {
			Object source = me.getSource();
			if (source instanceof JFormattedTextField) {
				JFormattedTextField ftf = (JFormattedTextField) me.getSource();
				ftf.setEditable(true);
			}// if instanceOf
		}// if mouse click
	}// mouseClicked

	@Override
	public void focusLost(FocusEvent fe) {
		JFormattedTextField ftf = (JFormattedTextField) fe.getSource();
		ftf.setEditable(false);
		if (ftf.getName().equals(NAME_REG_PC)) {
			disassembler.resetDisplay();
		}

	}// focusLost

	@Override
	public void itemStateChanged(ItemEvent ie) {
		String flagName = ((JCheckBox) ie.getSource()).getName();
		boolean state = (ie.getStateChange() == ItemEvent.SELECTED) ? true
				: false;
		updateConditionCode(flagName, state);
	}// itemStateChanged

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

	private void loadPutty() {
		if (putty != null) {
			if (putty.isAlive()) {
				putty.destroy();
			}// if alive
		}// if not null
		closeIt(putty);
		try {
			putty = Runtime.getRuntime().exec(cmdPutty);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, cmdPutty
					+ "failed execution ", "Load Putty",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		}// try

	}
	
	private void loadROM(){
		byte[] romMemory = new byte[0xff];
		byte[] bootLoader = new byte[]{(byte)0X21, (byte)0X19, (byte)0X00, (byte)0X22, (byte)0X46, (byte)0X00, 
				(byte)0X21, (byte)0X45, (byte)0X00, (byte)0X36, (byte)0X80, (byte)0X7E, (byte)0XB7, (byte)0XC2, 
				(byte)0X0B, (byte)0X00, (byte)0X3A, (byte)0X43, (byte)0X00, (byte)0XFE, (byte)0X80, (byte)0XC3,
				(byte)0X00, (byte)0X01, (byte)0X76, (byte)0X01, (byte)0X00, (byte)0X00, (byte)0X00, (byte)0X01,
				(byte)0X00, (byte)0X02, (byte)0X00, (byte)0X01, (byte)0X43, (byte)0X00, (byte)0X40};
		romMemory = prefs.getByteArray("Machine8080/BootLoader", bootLoader);
		
			core.writeDMA(0X0000, romMemory);
		
	}
	private void doRun(){
		showRun(false);
		cpu.startRunMode();
		showRun(true);
		loadTheDisplay();
	}//do run

//	private void loadCPM() {
//		File sourceFile = new File("C:\\Users\\admin\\git\\assembler8080\\assembler8080\\Code\\System\\CPM22.mem");
//		loadNewSystem(sourceFile);
//		sourceFile = new File("C:\\Users\\admin\\git\\assembler8080\\assembler8080\\Code\\System\\BIOS.mem");
//		loadNewSystem(sourceFile);
//	}//loadCPM

	private void addRmoveDisk(int diskNumber, String action) {
		if (action.equals(MOUNT)) {
			mountDisk(diskNumber);
		} else {
			dismountDisk(diskNumber);
		}// if
	}

	private void dismountDisk(int index) {
		DiskDrive[] drives = dcu.getDrives();
		if (drives[index] == null) {
			JOptionPane.showMessageDialog(null, "No Disk to unmounted", "Unmount disk",
					JOptionPane.WARNING_MESSAGE);
			return;
		} else {
			dcu.removeDiskDrive(index);
			showTheDisks();
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

		String selectedAbsolutePath = chooser.getSelectedFile().getAbsolutePath().toString();
		DiskDrive[] drives = dcu.getDrives();
		for (int i = 0; i < drives.length; i++) {
			if (drives[i] != null) {
				if (drives[i].getFileAbsoluteName().equals(selectedAbsolutePath)) {
					JOptionPane.showMessageDialog(null, "Disk Already Mounted!", "Adding disk",
							JOptionPane.WARNING_MESSAGE);
					return null;
				}// if
			}// outer if
		}// for

		return selectedAbsolutePath;

	}// getDisk

	private void showTheDisks() {
		DiskDrive[] drives = dcu.getDrives();
		boolean diskHere = false;
		for (int i = 0; i < dcu.getMaxNumberOfDrives(); i++) {
			diskHere = (drives[i] != null) ? true : false;
			lblFileNames[i].setText((diskHere) ? drives[i].getFileLocalName() : NO_FILENAME);
			lblFileNames[i].setForeground((diskHere) ? Color.BLUE : Color.BLACK);

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

	private void setupDisks() {
		lblFileNames = new JLabel[] { lblFileNameA, lblFileNameB, lblFileNameC, lblFileNameD };
		lblSizes = new JLabel[] { lblSizeA, lblSizeB, lblSizeC, lblSizeD };
		btnMounts = new JButton[] { btnMountA, btnMountB, btnMountC, btnMountD };

		showTheDisks();
	}// setupDisks

//	private void loadNewSystem(File sourceFile) {
//		try {
//			FileReader fileReader = new FileReader((sourceFile));
//			BufferedReader reader = new BufferedReader(fileReader);
//			String line;
//			while ((line = reader.readLine()) != null) {
//				parseAndLoadImageMem(line);
//			}// while
//			reader.close();
//		} catch (FileNotFoundException fnfe) {
//			JOptionPane.showMessageDialog(null, sourceFile.getAbsolutePath()
//					+ "not found", "unable to locate",
//					JOptionPane.ERROR_MESSAGE);
//			return; // exit gracefully
//		} catch (IOException ie) {
//			JOptionPane.showMessageDialog(null, sourceFile.getAbsolutePath()
//					+ ie.getMessage(), "IO error",
//					JOptionPane.ERROR_MESSAGE);
//			return; // exit gracefully
//		}// try
//	}// if - loadNewSystem

	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	private void appInit() {
		prefs = Preferences.userNodeForPackage(Machine8080B.class).node(this.getClass().getName());
		frmMachineb.setLocation(prefs.getInt("Machine8080/X", 00), prefs.getInt("Machine8080/Y", 00));
		makeNewMachine();
//		loadCPM();
		loadROM();
		loadPutty();
		// loadTheDisplay();
		disassembler.upDateDisplay(wrs.getProgramCounter());
		// setupDisks();
	}

	private void appClose() {
		
		prefs.putInt("Machine8080/X",frmMachineb.getX());
		prefs.putInt("Machine8080/Y",frmMachineb.getY());

		closeAllObjects();
	}

	private void closeAllObjects() {
		if (putty != null) {
			if (putty.isAlive()) {
				putty.destroy();
			}// if alive
		}// if not null
		closeIt(putty);
		closeIt(debugManager);
		closeIt(showCode);
		closeIt(disassembler);
		closeIt(dcu);
		closeIt(cpu);
		closeIt(au);
		if (dc != null) {
			dc.close();
			dc = null;
		}
		closeIt(mm);
		closeIt(wrs);
		closeIt(ccr);
		closeIt(core);
	}

	private void closeIt(Object obj) {
		if (obj != null) {
			obj = null;
		}
	}

	public Machine8080B() {
		try {
			format2HexDigits = new MaskFormatter("HH");
			format4HexDigits = new MaskFormatter("HHHH");
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		}// try

		initialize();
		appInit();
	}

	// -----------------------------------------------------------------------------------------------------------
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMachineb = new JFrame();
		frmMachineb.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose(); // clean up
			}
		});
		frmMachineb.setTitle("Machine8080B");
		frmMachineb.setBounds(80, 120, 693, 850);
		frmMachineb.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 20, 610, 20, 0 };
		gridBagLayout.rowHeights = new int[] { 185, 257, 96, 1, 105, 74, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frmMachineb.getContentPane().setLayout(gridBagLayout);

		JPanel pnlDisks = new JPanel();
		pnlDisks.setMinimumSize(new Dimension(610, 290));
		pnlDisks.setPreferredSize(new Dimension(0, 0));
		pnlDisks.setMaximumSize(new Dimension(610, 290));
		pnlDisks.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		pnlDisks.setLayout(null);
		GridBagConstraints gbc_pnlDisks = new GridBagConstraints();
		gbc_pnlDisks.insets = new Insets(0, 0, 5, 5);
		gbc_pnlDisks.fill = GridBagConstraints.BOTH;
		gbc_pnlDisks.gridx = 1;
		gbc_pnlDisks.gridy = 0;
		frmMachineb.getContentPane().add(pnlDisks, gbc_pnlDisks);

		JPanel pnlAB = new JPanel();
		pnlAB.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "5.25\" Disketts",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, Color.RED));
		pnlAB.setMinimumSize(new Dimension(673, 121));
		pnlAB.setMaximumSize(new Dimension(673, 121));
		pnlAB.setBounds(24, 10, 550, 75);
		pnlDisks.add(pnlAB);
		pnlAB.setLayout(null);

		JLabel lblA = new JLabel("A:");
		lblA.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblA.setHorizontalAlignment(SwingConstants.CENTER);
		lblA.setBounds(10, 21, 20, 14);
		pnlAB.add(lblA);

		lblFileNameA = new JLabel("Empty Disk Slot");
		lblFileNameA.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFileNameA.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameA.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblFileNameA.setBounds(35, 21, 340, 14);
		pnlAB.add(lblFileNameA);

		lblSizeA = new JLabel("1.44 MB");
		lblSizeA.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeA.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblSizeA.setBounds(478, 21, 62, 14);
		pnlAB.add(lblSizeA);

		btnMountA = new JButton(MOUNT);
		btnMountA.setActionCommand(AC_BTN_MOUNT_A);
		btnMountA.addActionListener(this);
		btnMountA.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnMountA.setBounds(387, 20, 90, 14);
		pnlAB.add(btnMountA);

		JLabel lblB = new JLabel("B:");
		lblB.setHorizontalAlignment(SwingConstants.CENTER);
		lblB.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblB.setBounds(10, 46, 20, 14);
		pnlAB.add(lblB);

		lblFileNameB = new JLabel("Empty Disk Slot");
		lblFileNameB.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameB.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblFileNameB.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFileNameB.setBounds(35, 45, 340, 14);
		pnlAB.add(lblFileNameB);

		btnMountB = new JButton(MOUNT);
		btnMountB.setActionCommand(AC_BTN_MOUNT_B);
		btnMountB.addActionListener(this);
		btnMountB.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnMountB.setBounds(387, 41, 90, 14);
		pnlAB.add(btnMountB);

		lblSizeB = new JLabel("1.44 MB");
		lblSizeB.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeB.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblSizeB.setBounds(478, 41, 62, 14);
		pnlAB.add(lblSizeB);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "8\" Floppies",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, Color.RED));
		panel_1.setBounds(24, 85, 550, 75);
		pnlDisks.add(panel_1);
		panel_1.setLayout(null);

		JLabel lblC = new JLabel("C:");
		lblC.setHorizontalAlignment(SwingConstants.CENTER);
		lblC.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblC.setBounds(10, 20, 20, 14);
		panel_1.add(lblC);

		lblFileNameC = new JLabel("Empty Disk Slot");
		lblFileNameC.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameC.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblFileNameC.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFileNameC.setBounds(35, 21, 340, 14);
		panel_1.add(lblFileNameC);

		btnMountC = new JButton(MOUNT);
		btnMountC.setActionCommand(AC_BTN_MOUNT_C);
		btnMountC.addActionListener(this);
		btnMountC.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnMountC.setBounds(387, 20, 90, 14);
		panel_1.add(btnMountC);

		lblSizeC = new JLabel("1.44 MB");
		lblSizeC.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeC.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblSizeC.setBounds(478, 21, 62, 14);
		panel_1.add(lblSizeC);

		JLabel lblD = new JLabel("D:");
		lblD.setHorizontalAlignment(SwingConstants.CENTER);
		lblD.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblD.setBounds(10, 46, 20, 14);
		panel_1.add(lblD);

		lblFileNameD = new JLabel("Empty Disk Slot");
		lblFileNameD.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileNameD.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblFileNameD.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFileNameD.setBounds(35, 45, 340, 14);
		panel_1.add(lblFileNameD);

		btnMountD = new JButton(MOUNT);
		btnMountD.setActionCommand(AC_BTN_MOUNT_D);
		btnMountD.addActionListener(this);
		btnMountD.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnMountD.setBounds(387, 41, 90, 14);
		panel_1.add(btnMountD);

		lblSizeD = new JLabel("1.44 MB");
		lblSizeD.setHorizontalAlignment(SwingConstants.CENTER);
		lblSizeD.setFont(new Font("Courier New", Font.PLAIN, 12));
		lblSizeD.setBounds(478, 41, 62, 14);
		panel_1.add(lblSizeD);

		JPanel pnlRegistersAndStatus = new JPanel();
		pnlRegistersAndStatus.setPreferredSize(new Dimension(610, 257));
		pnlRegistersAndStatus.setMaximumSize(new Dimension(610, 257));
		pnlRegistersAndStatus.setMinimumSize(new Dimension(400, 400));
		pnlRegistersAndStatus.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		pnlRegistersAndStatus.setLayout(null);
		GridBagConstraints gbc_pnlRegistersAndStatus = new GridBagConstraints();
		gbc_pnlRegistersAndStatus.insets = new Insets(0, 0, 5, 5);
		gbc_pnlRegistersAndStatus.fill = GridBagConstraints.BOTH;
		gbc_pnlRegistersAndStatus.gridx = 1;
		gbc_pnlRegistersAndStatus.gridy = 1;
		frmMachineb.getContentPane().add(pnlRegistersAndStatus, gbc_pnlRegistersAndStatus);

		JPanel pnlRegisters = new JPanel();
		pnlRegisters.setBounds(24, 11, 555, 130);
		pnlRegisters.setLayout(null);
		pnlRegisters.setBorder(new TitledBorder(new LineBorder(new Color(0, 0,

				0), 1, true), "Registers", TitledBorder.LEADING,

				TitledBorder.TOP, null, Color.RED));
		pnlRegistersAndStatus.add(pnlRegisters);

		JPanel pnlReg_A = new JPanel();
		pnlReg_A.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_A.setBounds(8, 20, 60, 80);
		pnlRegisters.add(pnlReg_A);
		pnlReg_A.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_A = new JLabel("A");
		lblReg_A.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_A.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_A.add(lblReg_A, BorderLayout.NORTH);

		ftfReg_A = new JFormattedTextField(format2HexDigits);
		ftfReg_A.setForeground(Color.BLACK);
		ftfReg_A.setText("");
		// ftfReg_A.setText("");
		ftfReg_A.setName(NAME_REG_A);
		ftfReg_A.addPropertyChangeListener(this);
		ftfReg_A.addMouseListener(this);
		ftfReg_A.addFocusListener(this);

		ftfReg_A.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_A.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_A.setEditable(false);
		ftfReg_A.setColumns(2);
		pnlReg_A.add(ftfReg_A, BorderLayout.CENTER);

		JPanel pnlReg_B = new JPanel();
		pnlReg_B.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_B.setBounds(70, 20, 60, 80);
		pnlRegisters.add(pnlReg_B);
		pnlReg_B.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_B = new JLabel("B");
		lblReg_B.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_B.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_B.add(lblReg_B, BorderLayout.NORTH);

		ftfReg_B = new JFormattedTextField(format2HexDigits);
		ftfReg_B.setName(NAME_REG_B);
		ftfReg_B.addPropertyChangeListener(this);
		ftfReg_B.addMouseListener(this);
		ftfReg_B.addFocusListener(this);
		ftfReg_B.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_B.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_B.setEditable(false);
		ftfReg_B.setColumns(2);
		pnlReg_B.add(ftfReg_B, BorderLayout.CENTER);

		JPanel pnlReg_C = new JPanel();
		pnlReg_C.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_C.setBounds(132, 20, 60, 80);
		pnlRegisters.add(pnlReg_C);
		pnlReg_C.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_C = new JLabel("C");
		lblReg_C.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_C.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_C.add(lblReg_C, BorderLayout.NORTH);

		ftfReg_C = new JFormattedTextField(format2HexDigits);
		ftfReg_C.setName(NAME_REG_C);
		ftfReg_C.addPropertyChangeListener(this);
		ftfReg_C.addMouseListener(this);
		ftfReg_C.addFocusListener(this);
		ftfReg_C.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_C.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_C.setEditable(false);
		ftfReg_C.setColumns(2);
		pnlReg_C.add(ftfReg_C, BorderLayout.CENTER);

		JPanel pnlReg_D = new JPanel();
		pnlReg_D.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_D.setBounds(194, 20, 60, 80);
		pnlRegisters.add(pnlReg_D);
		pnlReg_D.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_D = new JLabel("D");
		lblReg_D.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_D.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_D.add(lblReg_D, BorderLayout.NORTH);

		ftfReg_D = new JFormattedTextField(format2HexDigits);
		ftfReg_D.setName(NAME_REG_D);
		ftfReg_D.addPropertyChangeListener(this);
		ftfReg_D.addMouseListener(this);
		ftfReg_D.addFocusListener(this);
		ftfReg_D.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_D.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_D.setEditable(false);
		ftfReg_D.setColumns(2);
		pnlReg_D.add(ftfReg_D, BorderLayout.CENTER);

		JPanel pnlReg_E = new JPanel();
		pnlReg_E.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_E.setBounds(256, 20, 60, 80);
		pnlRegisters.add(pnlReg_E);
		pnlReg_E.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_E = new JLabel("E");
		lblReg_E.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_E.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_E.add(lblReg_E, BorderLayout.NORTH);

		ftfReg_E = new JFormattedTextField(format2HexDigits);
		ftfReg_E.setName(NAME_REG_E);
		ftfReg_E.addPropertyChangeListener(this);
		ftfReg_E.addMouseListener(this);
		ftfReg_E.addFocusListener(this);
		ftfReg_E.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_E.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_E.setEditable(false);
		ftfReg_E.setColumns(2);
		pnlReg_E.add(ftfReg_E, BorderLayout.CENTER);

		JPanel pnlReg_H = new JPanel();
		pnlReg_H.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_H.setBounds(318, 20, 60, 80);
		pnlRegisters.add(pnlReg_H);
		pnlReg_H.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_H = new JLabel("H");
		lblReg_H.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_H.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_H.add(lblReg_H, BorderLayout.NORTH);

		ftfReg_H = new JFormattedTextField(format2HexDigits);
		ftfReg_H.setName(NAME_REG_H);
		ftfReg_H.addPropertyChangeListener(this);
		ftfReg_H.addMouseListener(this);
		ftfReg_H.addFocusListener(this);
		ftfReg_H.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_H.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_H.setEditable(false);
		ftfReg_H.setColumns(2);
		pnlReg_H.add(ftfReg_H, BorderLayout.CENTER);

		JPanel pnlReg_L = new JPanel();
		pnlReg_L.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_L.setBounds(380, 20, 60, 80);
		pnlRegisters.add(pnlReg_L);
		pnlReg_L.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_L = new JLabel("L");
		lblReg_L.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_L.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_L.add(lblReg_L, BorderLayout.NORTH);

		ftfReg_L = new JFormattedTextField(format2HexDigits);
		ftfReg_L.setEditable(false);
		ftfReg_L.setName(NAME_REG_L);
		ftfReg_L.addPropertyChangeListener(this);
		ftfReg_L.addMouseListener(this);
		ftfReg_L.addFocusListener(this);
		ftfReg_L.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_L.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_L.setColumns(2);
		pnlReg_L.add(ftfReg_L, BorderLayout.CENTER);

		JPanel pnlReg_M = new JPanel();
		pnlReg_M.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,

				null, null));
		pnlReg_M.setBounds(475, 20, 60, 80);
		pnlRegisters.add(pnlReg_M);
		pnlReg_M.setLayout(new BorderLayout(0, 0));

		JLabel lblReg_M = new JLabel("M");
		lblReg_M.setHorizontalAlignment(SwingConstants.CENTER);
		lblReg_M.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pnlReg_M.add(lblReg_M, BorderLayout.NORTH);

		ftfReg_M = new JFormattedTextField(format2HexDigits);
		ftfReg_M.setEditable(false);
		ftfReg_M.setBackground(Color.LIGHT_GRAY);
		ftfReg_M.setName(NAME_REG_M);
		ftfReg_M.addPropertyChangeListener(this);
		ftfReg_M.addMouseListener(this);
		ftfReg_M.addFocusListener(this);
		ftfReg_M.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_M.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_M.setColumns(2);
		pnlReg_M.add(ftfReg_M, BorderLayout.CENTER);

		JPanel pnlStatus = new JPanel();
		pnlStatus.setBounds(24, 152, 555, 79);
		pnlStatus.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Condition Codes",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(255, 0, 0)), null));
		pnlRegistersAndStatus.add(pnlStatus);
		pnlStatus.setLayout(null);

		ckbSign = new JCheckBox("Sign");
		ckbSign.addItemListener(this);
		ckbSign.setName(NAME_FLAG_SIGN);
		ckbSign.setFont(new Font("Tahoma", Font.PLAIN, 16));
		ckbSign.setBounds(27, 26, 63, 23);
		pnlStatus.add(ckbSign);

		ckbZero = new JCheckBox("Zero");
		ckbZero.addItemListener(this);
		ckbZero.setName(NAME_FLAG_ZERO);
		ckbZero.setFont(new Font("Tahoma", Font.PLAIN, 16));
		ckbZero.setBounds(117, 26, 63, 23);
		pnlStatus.add(ckbZero);

		ckbAuxCarry = new JCheckBox("Aux Carry");
		ckbAuxCarry.addItemListener(this);
		ckbAuxCarry.setName(NAME_FLAG_AUXCARRY);
		ckbAuxCarry.setFont(new Font("Tahoma", Font.PLAIN, 16));
		ckbAuxCarry.setBounds(207, 26, 109, 23);
		pnlStatus.add(ckbAuxCarry);

		ckbParity = new JCheckBox("Parity");
		ckbParity.addItemListener(this);
		ckbParity.setName(NAME_FLAG_PARITY);
		ckbParity.setFont(new Font("Tahoma", Font.PLAIN, 16));
		ckbParity.setBounds(343, 26, 92, 23);
		pnlStatus.add(ckbParity);

		ckbCarry = new JCheckBox("Carry");
		ckbCarry.addItemListener(this);
		ckbCarry.setName(NAME_FLAG_CARRY);
		ckbCarry.setFont(new Font("Tahoma", Font.PLAIN, 16));
		ckbCarry.setBounds(462, 26, 63, 23);
		pnlStatus.add(ckbCarry);

		JPanel pnlProgramCounter = new JPanel();
		pnlProgramCounter.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		pnlProgramCounter.setLayout(null);
		GridBagConstraints gbc_pnlProgramCounter = new GridBagConstraints();
		gbc_pnlProgramCounter.insets = new Insets(0, 0, 5, 5);
		gbc_pnlProgramCounter.fill = GridBagConstraints.BOTH;
		gbc_pnlProgramCounter.gridx = 1;
		gbc_pnlProgramCounter.gridy = 2;
		frmMachineb.getContentPane().add(pnlProgramCounter, gbc_pnlProgramCounter);

		JLabel lblReg_SP = new JLabel("SP");
		lblReg_SP.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblReg_SP.setBounds(363, 19, 25, 52);
		pnlProgramCounter.add(lblReg_SP);

		JLabel lblReg_PC = new JLabel("PC");
		lblReg_PC.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblReg_PC.setBounds(86, 19, 25, 52);
		pnlProgramCounter.add(lblReg_PC);

		ftfReg_PC = new JFormattedTextField(format4HexDigits);
		ftfReg_PC.setInputVerifier(new MemoryLimitVerifier(MEMORY_SIZE_K));
		ftfReg_PC.setName(NAME_REG_PC);
		ftfReg_PC.addPropertyChangeListener(this);
		ftfReg_PC.addMouseListener(this);
		ftfReg_PC.addFocusListener(this);
		ftfReg_PC.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_PC.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_PC.setColumns(4);
		ftfReg_PC.setBounds(149, 25, 110, 40);
		pnlProgramCounter.add(ftfReg_PC);

		ftfReg_SP = new JFormattedTextField(format4HexDigits);
		ftfReg_SP.setInputVerifier(new MemoryLimitVerifier(MEMORY_SIZE_K));
		ftfReg_SP.setName(NAME_REG_SP);
		ftfReg_SP.addPropertyChangeListener(this);
		ftfReg_SP.addMouseListener(this);
		ftfReg_SP.addFocusListener(this);
		ftfReg_SP.setHorizontalAlignment(SwingConstants.CENTER);
		ftfReg_SP.setFont(new Font("Tahoma", Font.PLAIN, 38));
		ftfReg_SP.setColumns(4);
		ftfReg_SP.setBounds(422, 25, 110, 40);
		pnlProgramCounter.add(ftfReg_SP);

		scrollAssembler = new JScrollPane();
		scrollAssembler.setPreferredSize(new Dimension(0, 0));
		scrollAssembler.setBorder(new LineBorder(Color.BLUE, 2, true));

		GridBagConstraints gbc_scrollAssembler = new GridBagConstraints();
		gbc_scrollAssembler.insets = new Insets(0, 0, 5, 5);
		gbc_scrollAssembler.fill = GridBagConstraints.BOTH;
		gbc_scrollAssembler.gridx = 1;
		gbc_scrollAssembler.gridy = 4;
		frmMachineb.getContentPane().add(scrollAssembler, gbc_scrollAssembler);

		txtAssemblerCode = new JTextPane();
		txtAssemblerCode.setEditable(false);
		// txtAssemblerCode.getDocument().putProperty(PlainDocument.tabSizeAttribute, 35);
		txtAssemblerCode.setFont(new Font("Courier New", Font.PLAIN, 16));
		scrollAssembler.setViewportView(txtAssemblerCode);

		JPanel pnlRun = new JPanel();
		pnlRun.setLayout(null);
		GridBagConstraints gbc_pnlRun = new GridBagConstraints();
		gbc_pnlRun.insets = new Insets(0, 0, 0, 5);
		gbc_pnlRun.fill = GridBagConstraints.BOTH;
		gbc_pnlRun.gridx = 1;
		gbc_pnlRun.gridy = 5;
		frmMachineb.getContentPane().add(pnlRun, gbc_pnlRun);

		btnRun = new JButton("RUN");
		btnRun.setActionCommand(AC_BTN_RUN);
		btnRun.addActionListener(this);
		btnRun.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnRun.setBounds(491, 17, 90, 42);
		pnlRun.add(btnRun);

		JButton btnStep = new JButton("STEP");
		btnStep.setActionCommand(AC_BTN_STEP);
		btnStep.addActionListener(this);
		btnStep.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnStep.setBounds(145, 17, 90, 42);
		pnlRun.add(btnStep);

		spinnerStepCount = new JSpinner();
		spinnerStepCount.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		spinnerStepCount.setFont(new Font("Tahoma", Font.PLAIN, 30));
		spinnerStepCount.setBounds(31, 17, 90, 42);
		pnlRun.add(spinnerStepCount);

		btnStop = new JButton("STOP");
		btnStop.setActionCommand(AC_BTN_STOP);
		btnStop.addActionListener(this);
		btnStop.setForeground(Color.RED);
		btnStop.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnStop.setBounds(391, 17, 90, 42);
		pnlRun.add(btnStop);

		JMenuBar menuBar = new JMenuBar();
		frmMachineb.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		JMenuItem mnuFileNew = new JMenuItem("New");
		mnuFileNew.setActionCommand(AC_MNU_FILE_NEW);
		mnuFileNew.addActionListener(this);
		mnuFile.add(mnuFileNew);

		JMenuItem mnuFileOpen = new JMenuItem("Open");
		mnuFileOpen.setActionCommand(AC_MNU_FILE_OPEN);
		mnuFileOpen.addActionListener(this);
		mnuFile.add(mnuFileOpen);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		JMenuItem mnuFileSave = new JMenuItem("Save");
		mnuFileSave.setActionCommand(AC_MNU_FILE_SAVE);
		mnuFileSave.addActionListener(this);
		mnuFile.add(mnuFileSave);

		JMenuItem mnuFileSaveAs = new JMenuItem("Save As...");
		mnuFileSaveAs.setActionCommand(AC_MNU_FILE_SAVEAS);
		mnuFileSaveAs.addActionListener(this);
		mnuFile.add(mnuFileSaveAs);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);

		JMenuItem mnuFileClose = new JMenuItem("Close");
		mnuFileClose.setActionCommand(AC_MNU_FILE_CLOSE);
		mnuFileClose.addActionListener(this);
		mnuFile.add(mnuFileClose);

		JMenu mnuDisks = new JMenu("Disks");
		menuBar.add(mnuDisks);

		JMenuItem mnuDisksNew = new JMenuItem("Make a New Disk...");
		mnuDisksNew.setActionCommand(AC_MNU_DISKS_NEW);
		mnuDisksNew.addActionListener(this);
		mnuDisks.add(mnuDisksNew);

		mnuMemory = new JMenu("Memory");
		menuBar.add(mnuMemory);

		JMenuItem mnuMemoryShow = new JMenuItem("Show Memory ...");
		mnuMemoryShow.setActionCommand(AC_MNU_MEMORY_SHOW);
		mnuMemoryShow.addActionListener(this);
		mnuMemory.add(mnuMemoryShow);

		JSeparator separator_2 = new JSeparator();
		mnuMemory.add(separator_2);

		JMenuItem mnuMemorySave = new JMenuItem("Save Memory to File...");
		mnuMemorySave.setActionCommand(AC_MNU_MEMORY_SAVE);
		mnuMemorySave.addActionListener(this);
		mnuMemory.add(mnuMemorySave);

		JMenuItem mnuMemoryLoad = new JMenuItem("Load Memory From A File...");
		mnuMemoryLoad.setActionCommand(AC_MNU_MEMORY_LOAD);
		mnuMemoryLoad.addActionListener(this);

		JSeparator separator_5 = new JSeparator();
		mnuMemory.add(separator_5);
		mnuMemory.add(mnuMemoryLoad);

		JSeparator separator_6 = new JSeparator();
		mnuMemory.add(separator_6);

		JMenuItem mnuMemoryLoadFromList = new JMenuItem("Load Memory from FIle List...");
		mnuMemoryLoadFromList.setName(AC_MNU_MEMORY_LOAD_LIST);
		mnuMemoryLoadFromList.setActionCommand(AC_MNU_MEMORY_LOAD_LIST);
		mnuMemoryLoadFromList.addActionListener(this);
		mnuMemory.add(mnuMemoryLoadFromList);

		JMenuItem mnuMemorySaveToList = new JMenuItem("Save Selected to List...");
		mnuMemorySaveToList.setName(AC_MNU_MEMORY_SAVE_LIST);
		mnuMemorySaveToList.setActionCommand(AC_MNU_MEMORY_SAVE_LIST);
		mnuMemorySaveToList.addActionListener(this);
		mnuMemory.add(mnuMemorySaveToList);

		JMenuItem mnuMemoryRemoveSelected = new JMenuItem("Remove Selected  from List");
		mnuMemoryRemoveSelected.setName(AC_MNU_MEMORY_REMOVE_SELECTED);
		mnuMemoryRemoveSelected.setActionCommand(AC_MNU_MEMORY_REMOVE_SELECTED);
		mnuMemoryRemoveSelected.addActionListener(this);
		mnuMemory.add(mnuMemoryRemoveSelected);

		JSeparator separator_7 = new JSeparator();
		mnuMemory.add(separator_7);

		JMenu mnuTools = new JMenu("Tools");
		menuBar.add(mnuTools);

		JMenuItem mnuToolsShowCode = new JMenuItem("Show Code ...");
		mnuToolsShowCode.setName(AC_MNU_TOOLS_SHOW_CODE);
		mnuToolsShowCode.setActionCommand(AC_MNU_TOOLS_SHOW_CODE);
		mnuToolsShowCode.addActionListener(this);

		JMenuItem mnuToolsPutty = new JMenuItem("Open PuTTy");
		mnuToolsPutty.setName(AC_MNU_TOOLS_PUTTY);
		mnuToolsPutty.setActionCommand(AC_MNU_TOOLS_PUTTY);
		mnuToolsPutty.addActionListener(this);
		mnuTools.add(mnuToolsPutty);

		JSeparator separator_4 = new JSeparator();
		mnuTools.add(separator_4);

		JMenuItem mnuToolsDebugSuite = new JMenuItem("Debug Suite");
		mnuToolsDebugSuite.setActionCommand(AC_MNU_TOOLS_DEBUG_SUITE);
		mnuToolsDebugSuite.addActionListener(this);
		mnuTools.add(mnuToolsDebugSuite);
		mnuTools.add(mnuToolsShowCode);

		JMenuItem mnuToolsDebugManager = new JMenuItem("Debug Manager ...");
		mnuToolsDebugManager.setActionCommand(AC_MNU_TOOLS_DEBUG_MANAGER);
		mnuToolsDebugManager.addActionListener(this);
		mnuTools.add(mnuToolsDebugManager);

		JSeparator separator_3 = new JSeparator();
		mnuTools.add(separator_3);

		JMenuItem mnuToolsHardBoot = new JMenuItem("Hard Boot");
		mnuToolsHardBoot.setName(AC_MNU_TOOLS_REBOOT);
		mnuToolsHardBoot.setActionCommand("mnuToolsHardBoot");
		mnuToolsHardBoot.addActionListener(this);
		mnuToolsHardBoot.setToolTipText("Load Fresh BIOS.MEM and CPM22.MEM");
		mnuTools.add(mnuToolsHardBoot);
	}

	public static final int MEMORY_SIZE_K = 64; // in K
	private static final int MEMORY_SIZE_BYTES = MEMORY_SIZE_K * 1024;

	private final static int SIXTEEN = 16;

	private final static String AC_BTN_STEP = "btnStep";
	private final static String AC_BTN_RUN = "btnRun";
	private final static String AC_BTN_STOP = "btnStop";

	private final static String AC_BTN_MOUNT_A = "btnMountA";
	private final static String AC_BTN_MOUNT_B = "btnMountB";
	private final static String AC_BTN_MOUNT_C = "btnMountC";
	private final static String AC_BTN_MOUNT_D = "btnMountD";

	private final static String AC_MNU_FILE_NEW = "mnuFileNew";
	// private final static String AC_MNU_FILE_RESET = "mnuFileReset";
	private final static String AC_MNU_FILE_OPEN = "mnuFileOpen";
	private final static String AC_MNU_FILE_SAVE = "mnuFileSave";
	private final static String AC_MNU_FILE_SAVEAS = "mnuFileSaveAs";
	private final static String AC_MNU_FILE_CLOSE = "mnuFileClose";

	private final static String AC_MNU_DISKS_MOUNT = "mnuDisksMount";
	private final static String AC_MNU_DISKS_NEW = "mnuDisksNew";

	private final static String AC_MNU_MEMORY_SHOW = "mnuMemoryShow";
	private final static String AC_MNU_MEMORY_SAVE = "mnuMemorySave";
	private final static String AC_MNU_MEMORY_LOAD = "mnuMemoryLoad";
	private final static String AC_MNU_MEMORY_LOAD_LIST = "mnuMemoryLoadFromList";
	private final static String AC_MNU_MEMORY_SAVE_LIST = "mnuMemorySaveToList";
	private final static String AC_MNU_MEMORY_REMOVE_SELECTED = "mnuMemoryRemoveSelected";

	private final static String AC_MNU_TOOLS_PUTTY = "mnuToolsPutty";
	private final static String AC_MNU_TOOLS_REBOOT = "mnuToolsHardBoot";
	private final static String AC_MNU_TOOLS_SHOW_CODE = "mnuToolsShowCode";
	private final static String AC_MNU_TOOLS_DEBUG_MANAGER = "mnuToolsDebugManager";
	private final static String AC_MNU_TOOLS_DEBUG_SUITE = "mnuToolsDebugSuite";
	private final static String NAME_REG_A = "Reg_A";
	private final static String NAME_REG_B = "Reg_B";
	private final static String NAME_REG_C = "Reg_C";
	private final static String NAME_REG_D = "Reg_D";
	private final static String NAME_REG_E = "Reg_E";
	private final static String NAME_REG_H = "Reg_H";
	private final static String NAME_REG_L = "Reg_L";
	private final static String NAME_REG_M = "Reg_M";
	private final static String NAME_REG_SP = "Reg_SP";
	private final static String NAME_REG_PC = "Reg_PC";

	private final static String NAME_FLAG_SIGN = "signFlag";
	private final static String NAME_FLAG_ZERO = "zeroFlag";
	private final static String NAME_FLAG_AUXCARRY = "auxCarryFlag";
	private final static String NAME_FLAG_PARITY = "parityFlag";
	private final static String NAME_FLAG_CARRY = "carryFlag";

	private final static String DEFAULT_STATE_FILE = "defaultMachineState";
	public final static String FILE_LOCATION = ".";
	private final static String SETTINGS = "Settings";
	public final static String MEMORY = "Memory";
	// private final static String DISKS = "Disks";

	public final static String LIST_SUFFIX1 = "list";
	public final static String MEMORY_SUFFIX1 = "hex";
	public final static String MEMORY_SUFFIX = "mem";
	public final static String DISK_SUFFIX = "mem";
	private final static String FILE_SUFFIX = "sms";
	private final static String FILE_SUFFIX_PERIOD = "." + FILE_SUFFIX;
	private JFormattedTextField ftfReg_A;
	private JFormattedTextField ftfReg_B;
	private JFormattedTextField ftfReg_C;
	private JFormattedTextField ftfReg_D;
	private JFormattedTextField ftfReg_E;
	private JFormattedTextField ftfReg_H;
	private JFormattedTextField ftfReg_L;
	private JFormattedTextField ftfReg_M;
	private JFormattedTextField ftfReg_PC;
	private JFormattedTextField ftfReg_SP;
	private JSpinner spinnerStepCount;
	private JTextPane txtAssemblerCode;
	private JCheckBox ckbSign;
	private JCheckBox ckbZero;
	private JCheckBox ckbAuxCarry;
	private JCheckBox ckbParity;
	private JCheckBox ckbCarry;
	private JButton btnRun;
	private JButton btnStop;
	private JCheckBoxMenuItem mnuNew;

	private JLabel lblFileNameA;
	private JLabel lblFileNameB;
	private JLabel lblFileNameC;
	private JLabel lblFileNameD;
	private JButton btnMountA;
	private JButton btnMountB;
	private JButton btnMountC;
	private JButton btnMountD;
	private JLabel lblSizeA;
	private JLabel lblSizeB;
	private JLabel lblSizeC;
	private JLabel lblSizeD;

	private JLabel[] lblFileNames;
	private JLabel[] lblSizes;
	private JButton[] btnMounts;

	private final static String MOUNT = "Mount";
	private final static String DISMOUNT = "Dismount";
	private final static String NO_FILENAME = "<slot empty>";
	private final static String NO_SIZE = "<0 KB>";
	private JMenu mnuMemory;

	// -----------------------------------------------------------

	@Override
	public void focusGained(FocusEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
