package console;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import terminal.TerminalSettings;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TestConsole {

	private JFrame frmTestConsole;
	JTextArea txtLog;
	Console console;
	TerminalSettings terminalSettings;
	private JFormattedTextField ftfByteToSend;
	private JLabel lblCurrentSettings;
	private JTextField txtStringToSend;
	private JFormattedTextField ftfStatusReceived;
	private JFormattedTextField ftfByteReceived;
	private JFormattedTextField ftfAllBytesReceived;

	private void showConnectionString() {
		lblCurrentSettings.setText(getConnectionString());
	}// showConnectionString
	
	public String getConnectionString(){
		return console.getConnectionString();
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestConsole window = new TestConsole();
					window.frmTestConsole.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}// try
			}// run
		});
	}// main

	private void initApplication() {
		console = new Console((byte) 01, (byte) 01, (byte) 02);
		TerminalSettings termininalSettings = console.getTerminalSettings();
		showConnectionString();

	}// initApplication
		// ----------------------------------------------------------------------------------------

	/**
	 * Create the application.
	 */
	public TestConsole() {
		initialize();
		initApplication();
	}// TestDeviceController

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws ParseException
	 */
	private void initialize() {
		frmTestConsole = new JFrame();
		frmTestConsole.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				if (console != null) {
					console.closeConnection();
				}
			}
		});
		frmTestConsole.setTitle("Test Console");
		frmTestConsole.setBounds(100, 100, 783, 666);
		frmTestConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTestConsole.getContentPane().setLayout(null);

		JPanel panelFromCPU = new JPanel();
		panelFromCPU.setBorder(new CompoundBorder(new TitledBorder(
				new LineBorder(new Color(0, 0, 0), 1, true),
				"From CPU to Device Controller", TitledBorder.CENTER,
				TitledBorder.TOP, null, null), null));
		panelFromCPU.setBounds(10, 23, 303, 224);
		frmTestConsole.getContentPane().add(panelFromCPU);
		panelFromCPU.setLayout(null);

		JButton btnSendByte = new JButton("Send Byte");
		btnSendByte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int thisValue = Integer.valueOf(
						(String) ftfByteToSend.getValue(), 16);
				console.byteFromCPU((byte) 01, (byte) thisValue);
			}
		});
		btnSendByte.setBounds(10, 25, 133, 23);
		panelFromCPU.add(btnSendByte);

		JButton btnSendString = new JButton("Send a Character");
		btnSendString.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				byte b;
				char[] charsToSend = txtStringToSend.getText().toCharArray();
				for (Character c : charsToSend) {
					b = (byte) c.charValue();
					console.byteFromCPU((byte) 01, b);
				}
			}
		});
		btnSendString.setBounds(10, 59, 133, 23);
		panelFromCPU.add(btnSendString);

		try {
			ftfByteToSend = new JFormattedTextField(new MaskFormatter("HH"));
		} catch (ParseException pe) {

		}
		ftfByteToSend.setBounds(188, 25, 86, 20);
		panelFromCPU.add(ftfByteToSend);

		txtStringToSend = new JTextField();
		txtStringToSend.setBounds(188, 59, 86, 20);
		panelFromCPU.add(txtStringToSend);
		txtStringToSend.setColumns(10);

		JPanel panelFromDevice = new JPanel();
		panelFromDevice.setBorder(new CompoundBorder(new TitledBorder(
				new LineBorder(new Color(0, 0, 0), 1, true),
				"From Device Controller to CPU ", TitledBorder.CENTER,
				TitledBorder.TOP, null, null), null));
		panelFromDevice.setBounds(10, 325, 303, 224);
		frmTestConsole.getContentPane().add(panelFromDevice);
		panelFromDevice.setLayout(null);

		JButton btnGetByte = new JButton("Get Byte");
		btnGetByte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Byte b = console.byteToCPU((byte) 02);
				if ((b & Console.CONSOLE_INPUT_STATUS_MASK) == 0) {
					// txtLog.append("There is nothing to read");
					ftfStatusReceived.setText(String.format("%02X", b));

				} else {
					ftfStatusReceived.setText(String.format("%02X", b));
					Byte byteToCPU = console.byteToCPU((byte) 01);
					String stringFromCPU = new String(new byte[] { byteToCPU });
					ftfByteReceived.setText(String.format("%02X - %02d - %s",
							byteToCPU, byteToCPU, stringFromCPU));
				}
			}
		});
		btnGetByte.setBounds(10, 76, 135, 23);
		panelFromDevice.add(btnGetByte);

		ftfByteReceived = new JFormattedTextField();
		ftfByteReceived.setBounds(187, 76, 86, 20);
		panelFromDevice.add(ftfByteReceived);

		ftfStatusReceived = new JFormattedTextField();
		ftfStatusReceived.setBounds(187, 25, 86, 20);
		panelFromDevice.add(ftfStatusReceived);

		JButton btnGetStatus = new JButton("Get Status");
		btnGetStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Byte b = console.byteToCPU((byte) 02);
				ftfStatusReceived.setText(String.format("%02X", b));
			}
		});
		btnGetStatus.setBounds(10, 25, 135, 23);
		panelFromDevice.add(btnGetStatus);

		JButton btnGetAllBytes = new JButton("Get All Byte");
		btnGetAllBytes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Byte b = console.byteToCPU((byte) 02);
				if ((b & Console.CONSOLE_OUTPUT_STATUS_MASK) == 0) {
					// txtLog.append("There is nothing to read");
					ftfStatusReceived.setText(String.format("%02X", b));

				} else {
					StringBuilder sb = new StringBuilder();
					while ((console.byteToCPU((byte) 02) & Console.CONSOLE_INPUT_STATUS_MASK) >= 1) {
						Byte byteToCPU = console.byteToCPU((byte) 01);
						String stringFromCPU = new String(
								new byte[] { byteToCPU });
						sb.append(String.format("%02X-%s | ", byteToCPU,stringFromCPU));
					}
					ftfAllBytesReceived.setText(sb.toString());
				}
			}
		});
		btnGetAllBytes.setBounds(70, 110, 135, 23);
		panelFromDevice.add(btnGetAllBytes);

		ftfAllBytesReceived = new JFormattedTextField();
		ftfAllBytesReceived.setHorizontalAlignment(SwingConstants.LEFT);
		ftfAllBytesReceived.setBounds(10, 144, 263, 20);
		panelFromDevice.add(ftfAllBytesReceived);

		JPanel panelLog = new JPanel();
		panelLog.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(
				new Color(0, 0, 0), 1, true), "Log", TitledBorder.CENTER,
				TitledBorder.TOP, null, null), null));
		panelLog.setBounds(407, 35, 303, 514);
		frmTestConsole.getContentPane().add(panelLog);
		panelLog.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 22, 294, 488);
		panelLog.add(scrollPane);

		JTextArea txtLog = new JTextArea();
		txtLog.setText("line one - ");
		txtLog.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() >= 2) {
					JTextArea source = (JTextArea) me.getSource();
					source.setText("");
				}// if - double click
			}// mouseClicked
		});
		scrollPane.setViewportView(txtLog);

		JButton btnSetConnectionValues = new JButton("Set Connection values");
		btnSetConnectionValues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setSerialConnection();
				TerminalSettings termininalSettings = console
						.getTerminalSettings();
				showConnectionString();
			}
		});
		btnSetConnectionValues.setBounds(65, 258, 191, 23);
		frmTestConsole.getContentPane().add(btnSetConnectionValues);

		lblCurrentSettings = new JLabel("New label");
		lblCurrentSettings.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentSettings.setBounds(10, 287, 303, 23);
		frmTestConsole.getContentPane().add(lblCurrentSettings);
	}// initialize
}//
