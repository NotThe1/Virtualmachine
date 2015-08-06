package hardware;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JPanel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.border.BevelBorder;

import java.awt.BorderLayout;

import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;
import javax.swing.JCheckBox;

import java.awt.Font;
import java.awt.Dimension;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
//import javax.swing.JFormattedTextField$AbstractFormatter;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import codeSupport.Disassembler;
import memoryDisplay.MemorySaver;
import memoryDisplay.ShowCoreMemory;
import device.DeviceController;
import disks.DiskControlUnit;
import disks.DiskUserInterface;
import disks.MakeNewDisk;

import javax.swing.ScrollPaneConstants;
import javax.swing.BoxLayout;

import java.awt.FlowLayout;

public class Machine8080A implements PropertyChangeListener, MouseListener,
		FocusListener, ItemListener, ActionListener {

	private JFrame frame;

	private Core core;
	private MainMemory mm;
	private ConditionCodeRegister ccr;
	private WorkingRegisterSet wrs;
	private ArithmeticUnit au;
	private CentralProcessingUnit cpu;
	private DeviceController dc;

	private DiskControlUnit dcu;
	private ShowCoreMemory scm;
	private Disassembler disassembler;

	private JScrollPane scrollAssembler;
	private MaskFormatter format2HexDigits;
	private MaskFormatter format4HexDigits;

	private String currentMachineName = DEFAULT_STATE_FILE;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Machine8080A window = new Machine8080A();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private JFileChooser getFileChooser(String subDirectory, String filterDescription, String filterExtensions) {
		Path sourcePath = Paths.get(FILE_LOCATION, subDirectory);
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

	private void saveMachineState() {
		saveMachineState(getDefaultMachineStateFile());
		;
	}// saveMachineState

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

	private void restoreMachineState() {
		restoreMachineState(currentMachineName);// getDefaultMachineStateFile()
	}// restoreMachineState

	private void restoreMachineState(String fileName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName + FILE_SUFFIX_PERIOD));
			ccr = (ConditionCodeRegister) ois.readObject();
			wrs = (WorkingRegisterSet) ois.readObject();
			core = (Core) ois.readObject();
			currentMachineName = fileName;
			frame.setTitle(fileName);
			ois.close();
		} catch (Exception e) {
			System.err.printf(
					"Not able to completely restore machine state%n %s%n", e.getMessage());
			core = new Core(MEMORY_SIZE_BYTES);
			ccr = new ConditionCodeRegister();
			wrs = new WorkingRegisterSet();
		}// try
		mm = new MainMemory(core);
		// loadTheDisplay();
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
			value = mm.getByte(mLocation);
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
	}// updateConditionCode

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
				cpu.setProgramCounter((int) intValue);
				disassembler.run();
				txtAssemblerCode.setCaretPosition(0);
				scrollAssembler.getVerticalScrollBar().setValue(0);
			}
			break;

		default:
		}
	}// modifyRegister

	private void showRun(boolean runState) {
		btnRun.setVisible(runState);
		btnStop.setVisible(!runState);
	}// showRun

	private void loadMemoryImage() {
		JFileChooser chooserLMI = getFileChooser(MEMORY, "Memory files", MEMORY_SUFFIX);
		if (chooserLMI.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			System.out.printf("You cancelled the Load Memory...%n", "");
		} else {
			File sourceFile = chooserLMI.getSelectedFile();
			try {
				FileReader fileReader = new FileReader((sourceFile));
				BufferedReader reader = new BufferedReader(fileReader);
				String line;
				while ((line = reader.readLine()) != null) {
					parseAndLoadImage(line);
				}// while
				reader.close();
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
		}// if - returnValue

	}// loadMemoryImage

	private void parseAndLoadImage(String line) {
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
			return;
		}// if max memeory test

		byte value;
		byte[] values = new byte[SIXTEEN];
		for (int i = 0; i < SIXTEEN; i++) {
			values[i] = (byte) ((int) Integer.valueOf(scanner.next(), 16));
		}// for values

		core.writeDMA(address, values); // avoid setting off memory traps

	}// parseAndLoadImage

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Override
	public void actionPerformed(ActionEvent ae) {
		String actionCommand = ae.getActionCommand();
		switch (actionCommand) {
		case AC_BTN_RUN:
			showRun(false);
			cpu.startRunMode();
			showRun(true);
			loadTheDisplay();
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

		case AC_MNU_FILE_NEW:
			cpu = null;
			cpu = new CentralProcessingUnit();
			cpu.setProgramCounter((int) 0X0000);
			core = null;
			core = new Core(MEMORY_SIZE_BYTES);
			mm = null;
			mm = new MainMemory(core);
			wrs.initialize();
			disassembler.run();
			loadTheDisplay();
			break;

		case AC_MNU_FILE_OPEN:
			JFileChooser chooserOpen = getFileChooser(SETTINGS, "Saved Machine State", FILE_SUFFIX);
			if (chooserOpen.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				String absolutePath = chooserOpen.getSelectedFile().getAbsolutePath();
				// need to strip the file suffix off (will replace later)
				absolutePath = stripSuffix(absolutePath);
				restoreMachineState(absolutePath);
			} else {
				System.out.printf("You cancelled the Open...%n", "");
			}// if - returnValue
			break;
		case AC_MNU_FILE_SAVE:
			saveMachineState(getDefaultMachineStateFile());
			break;
		case AC_MNU_FILE_SAVEAS:
			JFileChooser chooserSaveAs = getFileChooser(SETTINGS, "Saved Machine State", FILE_SUFFIX);
			if (chooserSaveAs.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
				System.out.printf("You cancelled the Save as...%n", "");
			} else {
				String absolutePath = chooserSaveAs.getSelectedFile().getAbsolutePath();
				// need to strip the file suffix off (will replace later)
				absolutePath = stripSuffix(absolutePath);
				saveMachineState(absolutePath);
			}// if - returnValue
			break;
		case AC_MNU_FILE_CLOSE:
			saveMachineState();
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
			loadMemoryImage();
			if (scm != null) {
				scm.refresh();
			}
			disassembler.run();
			scrollAssembler.getVerticalScrollBar().setValue(0);
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

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public void appInit() {
		currentMachineName = getDefaultMachineStateFile();
		restoreMachineState();
		dc = new DeviceController();
		au = new ArithmeticUnit(ccr);
		cpu = new CentralProcessingUnit(mm, ccr, au, wrs, dc);
		cpu.setProgramCounter(wrs.getProgramCounter());
		dcu = new DiskControlUnit(core);
		disassembler = new Disassembler(core, txtAssemblerCode.getDocument(), cpu);
		loadTheDisplay();
	}

	public Machine8080A() {
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
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				saveMachineState(); // use DEFAULT_STATE_FILE
			}
		});
		frame.setTitle("Machine8080A");
		frame.setBounds(100, 100, 693, 774);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 20, 610, 20, 0 };
		gridBagLayout.rowHeights = new int[] { 20, 257, 96, 1, 105, 74, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

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
		frame.getContentPane().add(pnlRegistersAndStatus, gbc_pnlRegistersAndStatus);

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
		frame.getContentPane().add(pnlProgramCounter, gbc_pnlProgramCounter);

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
		frame.getContentPane().add(scrollAssembler, gbc_scrollAssembler);

		txtAssemblerCode = new JTextPane();
		// txtAssemblerCode.getDocument().putProperty(PlainDocument.tabSizeAttribute, 35);
		txtAssemblerCode.setFont(new Font("Courier New", Font.PLAIN, 16));
		scrollAssembler.setViewportView(txtAssemblerCode);

		JPanel panel = new JPanel();
		panel.setLayout(null);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 5;
		frame.getContentPane().add(panel, gbc_panel);

		btnRun = new JButton("RUN");
		btnRun.setActionCommand(AC_BTN_RUN);
		btnRun.addActionListener(this);
		btnRun.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnRun.setBounds(491, 17, 90, 42);
		panel.add(btnRun);

		JButton btnStep = new JButton("STEP");
		btnStep.setActionCommand(AC_BTN_STEP);
		btnStep.addActionListener(this);
		btnStep.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnStep.setBounds(145, 17, 90, 42);
		panel.add(btnStep);

		spinnerStepCount = new JSpinner();
		spinnerStepCount.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		spinnerStepCount.setFont(new Font("Tahoma", Font.PLAIN, 30));
		spinnerStepCount.setBounds(31, 17, 90, 42);
		panel.add(spinnerStepCount);

		btnStop = new JButton("STOP");
		btnStop.setActionCommand(AC_BTN_STOP);
		btnStop.addActionListener(this);
		btnStop.setForeground(Color.RED);
		btnStop.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnStop.setBounds(391, 17, 90, 42);
		panel.add(btnStop);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

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

		JMenuItem mnuDisksMount = new JMenuItem("Mount/Dismout disks...");
		mnuDisksMount.setActionCommand(AC_MNU_DISKS_MOUNT);
		mnuDisksMount.addActionListener(this);
		mnuDisks.add(mnuDisksMount);

		JMenuItem mnuDisksNew = new JMenuItem("Make a New Disk...");
		mnuDisksNew.setActionCommand(AC_MNU_DISKS_NEW);
		mnuDisksNew.addActionListener(this);
		mnuDisks.add(mnuDisksNew);

		JMenu mnuMemory = new JMenu("Memory");
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

		JMenuItem mnuMemoryLoad = new JMenuItem("Load Memory From File...");
		mnuMemoryLoad.setActionCommand(AC_MNU_MEMORY_LOAD);
		mnuMemoryLoad.addActionListener(this);
		mnuMemory.add(mnuMemoryLoad);
	}

	public static final int MEMORY_SIZE_K = 64; // in K
	private static final int MEMORY_SIZE_BYTES = MEMORY_SIZE_K * 1024;

	private final static int SIXTEEN = 16;

	private final static String AC_BTN_STEP = "btnStep";
	private final static String AC_BTN_RUN = "btnRun";
	private final static String AC_BTN_STOP = "btnStop";

	private final static String AC_MNU_FILE_NEW = "mnuFileNew";
	private final static String AC_MNU_FILE_RESET = "mnuFileReset";
	private final static String AC_MNU_FILE_OPEN = "mnuFileOpen";
	private final static String AC_MNU_FILE_SAVE = "mnuFileSave";
	private final static String AC_MNU_FILE_SAVEAS = "mnuFileSaveAs";
	private final static String AC_MNU_FILE_CLOSE = "mnuFileClose";

	private final static String AC_MNU_DISKS_MOUNT = "mnuDisksMount";
	private final static String AC_MNU_DISKS_NEW = "mnuDisksNew";

	private final static String AC_MNU_MEMORY_SHOW = "mnuMemoryShow";
	private final static String AC_MNU_MEMORY_SAVE = "mnuMemorySave";
	private final static String AC_MNU_MEMORY_LOAD = "mnuMemoryLoad";

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
	private final static String MEMORY = "Memory";
	private final static String DISKS = "Disks";

	public final static String MEMORY_SUFFIX = "mem";
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

	// end of classs
}
