package console;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import device.Device8080;
import terminal.TerminalSettings;
import terminal.PortSetupDetails;
import terminal.Terminal.SerialPortReader;

public class Console extends Device8080 {
	private TerminalSettings terminalSettings;
	public SerialPort serialPort;
	Queue<Byte> keyBoardBuffer;
	Queue<Byte> inputBuffer;
	public static final byte CONSOLE_OUTPUT_STATUS_MASK = (byte) 0X80; // ready for output
	public static final byte CONSOLE_INPUT_STATUS_MASK = (byte) 0X7F;  // Bytes in input buffer
	

	/*
	 * @param name device name
	 * 
	 * @param type type of device ie storage
	 * 
	 * @param input is this an input device
	 * 
	 * @param addressIn address of the device for input to CPU
	 * 
	 * @param output is this an output device
	 * 
	 * @param addressOut address of the device for output from CPU
	 * 
	 * @param addressStatus id of status port if different from i or out
	 */
	public Console(String name, String type, boolean input, Byte addressIn,
			boolean output, Byte addressOut, Byte addressStatus) {
		super(name, type, input, addressIn, output, addressOut, addressStatus);
		loadSettings();
	}// Constructor -

	public Console(Byte addressIn, Byte addressOut, Byte addressStatus) {
		super("tty", "Serial", true, addressIn, true, addressOut, addressStatus);
//		System.runFinalizersOnExit(true);
		loadSettings();
		openConnection();
		inputBuffer = new LinkedList<Byte>();
	}// Constructor -
	
//	protected void finalize() throws Throwable{
//		closeConnection();
//		System.out.println("In finalize\n");
//		super.finalize();
//	}

	@Override
	public void byteFromCPU(Byte address, Byte value) {
		if (serialPort == null) {
			String msg = String.format("Serial Port %s is not opened",
					terminalSettings.getPortName());
			JOptionPane.showMessageDialog(null, "Keyboard In", msg,
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				serialPort.writeByte(value);
			} catch (SerialPortException e) {
				String msg = String
						.format("Failed to write byte %02d to port %s with exception %s",
								value, terminalSettings.getPortName(),
								e.getExceptionType());
				JOptionPane.showMessageDialog(null, msg, "Keyboard In",
						JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}// try
		}// if
	}// byteFromCPU

	@Override
	public byte byteToCPU(Byte address) { // this is a blocking read
		Byte byteToCPU = null;
		if (address == getAddressIn()) {
			while (byteToCPU == null) {
				byteToCPU = inputBuffer.poll();
			}// while
		} else if (address == getAddressStatus()) {
			byteToCPU =  (byte) (CONSOLE_OUTPUT_STATUS_MASK |(byte)( inputBuffer.size())) ; //Set ready for output
//			byteToCPU = (byte) inputBuffer.size(); // tell how many bytes in the
													// buffer
		} else {
			byteToCPU = 0;
		}
		return byteToCPU;
	}// byteToCPU
	
	public void setSerialConnection(){
		closeConnection();
		PortSetupDetails psd = new PortSetupDetails(terminalSettings);
		psd.setVisible(true);
		openConnection();
	}

	private void openConnection() {
		
		if (serialPort != null) {
//			String msg = String.format("Serial Port %s is already opened%nClosing Port....",
//					terminalSettings.getPortName());
//			JOptionPane.showMessageDialog(null, msg);
			//closeConnection();
			serialPort = null;
		}
		serialPort = new SerialPort(terminalSettings.getPortName());

		try {
			serialPort.openPort();// Open serial port
			serialPort.setParams(terminalSettings.getBaudRate(),
					terminalSettings.getDataBits(),
					terminalSettings.getStopBits(),
					terminalSettings.getParity());
			serialPort.addEventListener(new SerialPortReader());
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}// try
		saveSettings();
	}// openConnection

	public void closeConnection() {
		if (serialPort != null) {
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				e.printStackTrace();
			}// try
			serialPort = null;
		}// if
	}// closeConnection

//	private void showConnectionString(TerminalSettings terminalSettings) {
	public String getConnectionString(){
		String strStopBits = "0";

		switch (terminalSettings.getStopBits()) {
		case 1:
			strStopBits = "1";
			break;
		case 2:
			strStopBits = "2";
			break;
		case 3:
			strStopBits = "1.5";
			break;
		}// switch - stopBits
		String[] strParity = new String[] { "None", "Odd", "Even", "Mark",
				"Space" };
		String getConnectionString = String.format("%s-%d-%d-%s-%s",
				terminalSettings.getPortName(), terminalSettings.getBaudRate(),
				terminalSettings.getDataBits(), strStopBits,
				strParity[terminalSettings.getParity()]);

		return getConnectionString;
	}// showConnectionString
	

	public TerminalSettings getTerminalSettings() {
		return terminalSettings;
	}

	public void loadSettings() {
		loadSettings(DEFAULT_STATE_FILE);
	}// loadSettings()

	private void loadSettings(String fileName) {
		terminalSettings = new TerminalSettings();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				fileName + FILE_SUFFIX_PERIOD))) {
			terminalSettings = (TerminalSettings) ois.readObject();
		} catch (ClassNotFoundException | IOException cnfe) {
			String msg = String.format(
					"Could not find: %s, will proceed with default settings",
					fileName);
			JOptionPane.showMessageDialog(null, msg);
			if (terminalSettings != null) {
				terminalSettings = null;
			}//
			terminalSettings = new TerminalSettings();
			terminalSettings.setDefaultSettings();
			terminalSettings.setPortName("COM2");
		}// try

	}// loadSettings(fileName)

	public void saveSettings() {
		saveSettings(DEFAULT_STATE_FILE);
	}

	private void saveSettings(String fileName) {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(fileName + FILE_SUFFIX_PERIOD))) {
			oos.writeObject(terminalSettings);
		} catch (Exception e) {
			String msg = String.format("Could not save to : %s%S. ", fileName,
					FILE_SUFFIX_PERIOD);
			JOptionPane.showMessageDialog(null, msg);
		}
	}

	private final static String DEFAULT_STATE_FILE = "defaultConsoleSettings";
	private final static String FILE_SUFFIX = "ser";
	private final static String FILE_SUFFIX_PERIOD = "." + FILE_SUFFIX;

	// private void readInputBuffer() {
	// Byte inByte = inputBuffer.poll();
	// while (inByte != null) {
	// keyReceived(inByte);
	// inByte = inputBuffer.poll();
	// }// while
	// }//readInputBuffer

	private void sendOutput(Byte value) {
		if (serialPort == null) {
			String msg = String.format("Serial Port %s is not opened",
					terminalSettings.getPortName());
			JOptionPane.showMessageDialog(null, "Keyboard In", msg,
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				serialPort.writeByte(value);
			} catch (SerialPortException e) {
				String msg = String
						.format("Failed to write byte %02d to port %s with exception %s",
								value, terminalSettings.getPortName(),
								e.getExceptionType());
				JOptionPane.showMessageDialog(null, msg, "Keyboard In",
						JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		}
	}// sendOutput

	public class SerialPortReader implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent spe) {
			if (spe.isRXCHAR()) {
				// System.out.printf(" spe.getEventValue() = %d%n",
				// spe.getEventValue());
				if (spe.getEventValue() > 0) {// data available
					try {
						byte[] buffer = serialPort.readBytes();
						for (Byte b : buffer) {
							inputBuffer.add(b);
						}
						// ******readInputBuffer();
						// System.out.println(Arrays.toString(buffer));

					} catch (SerialPortException speRead) {
						System.out.println(speRead);
					}// try
				}// inner if

			} else if (spe.isCTS()) { // CTS line has changed state
				String msg = (spe.getEventValue() == 1) ? "CTS - On"
						: "CTS - Off";
				System.out.println(msg);
			} else if (spe.isDSR()) { // DSR line has changed state
				String msg = (spe.getEventValue() == 1) ? "DSR - On"
						: "DSR - Off";
				System.out.println(msg);
			} else {
				System.out.printf("Unhandled event : %s%n", spe.toString());
			}

		}// serialEvent

	}// class SerialPortReader
}// class console
