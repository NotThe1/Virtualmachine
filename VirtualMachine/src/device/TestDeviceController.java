package device;

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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TestDeviceController {

	private JFrame frmTestDeviceController;
	private JTextArea txtLog;
	DeviceController deviceController;
	private JFormattedTextField ftfByteToSend;
	private JTextField txtCharactersToSend;
	private JFormattedTextField ftfGetAllBytes;
	private JFormattedTextField ftfGetByte;
	private JFormattedTextField ftfStatusReceived;
	private JLabel lblTerminalSettings;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestDeviceController window = new TestDeviceController();
					window.frmTestDeviceController.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}// try
			}// run
		});
	}// main

	private void initApplication() {
		deviceController = new DeviceController();
		lblTerminalSettings.setText(deviceController.getConnectionString());

	}// initApplication
		// ----------------------------------------------------------------------------------------

	/**
	 * Create the application.
	 */
	public TestDeviceController() {
		initialize();
		initApplication();
	}// TestDeviceController

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws ParseException
	 */
	private void initialize() {
		frmTestDeviceController = new JFrame();
		frmTestDeviceController.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				deviceController.closeConnection();
			}
		});
		frmTestDeviceController.setTitle("Test Device Controller - Console");
		frmTestDeviceController.setBounds(100, 100, 783, 666);
		frmTestDeviceController.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTestDeviceController.getContentPane().setLayout(null);

		JPanel panelFromCPU = new JPanel();
		panelFromCPU.setBorder(new CompoundBorder(new TitledBorder(
				new LineBorder(new Color(0, 0, 0), 1, true),
				"From CPU to Device Controller", TitledBorder.CENTER,
				TitledBorder.TOP, null, null), null));
		panelFromCPU.setBounds(10, 23, 303, 224);
		frmTestDeviceController.getContentPane().add(panelFromCPU);
		panelFromCPU.setLayout(null);

		JButton btnSendByte = new JButton("Send Byte");
		btnSendByte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int thisValue = Integer.valueOf(
						(String) ftfByteToSend.getValue(), 16);
				deviceController.byteToDevice(DeviceController.CONSOLE_OUT,
						(byte) thisValue);
			}
		});
		btnSendByte.setBounds(10, 25, 129, 23);
		panelFromCPU.add(btnSendByte);

		JButton btnSendCharacters = new JButton("Send Characters");
		btnSendCharacters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				byte b;
				char[] charsToSend = txtCharactersToSend.getText()
						.toCharArray();
				for (Character c : charsToSend) {
					b = (byte) c.charValue();
					deviceController.byteToDevice(DeviceController.CONSOLE_OUT,
							b);
				}// for
			}// actionPerformed
		});
		btnSendCharacters.setBounds(10, 59, 129, 23);
		panelFromCPU.add(btnSendCharacters);

		try {
			ftfByteToSend = new JFormattedTextField(new MaskFormatter("HH"));
		} catch (ParseException pe) {

		}
		ftfByteToSend.setBounds(149, 26, 86, 20);
		panelFromCPU.add(ftfByteToSend);

		txtCharactersToSend = new JTextField();
		txtCharactersToSend.setBounds(149, 60, 86, 20);
		panelFromCPU.add(txtCharactersToSend);
		txtCharactersToSend.setColumns(10);

		JPanel panelFromDevice = new JPanel();
		panelFromDevice.setBorder(new CompoundBorder(new TitledBorder(
				new LineBorder(new Color(0, 0, 0), 1, true),
				"From Device Controller to CPU ", TitledBorder.CENTER,
				TitledBorder.TOP, null, null), null));
		panelFromDevice.setBounds(10, 325, 303, 224);
		frmTestDeviceController.getContentPane().add(panelFromDevice);
		panelFromDevice.setLayout(null);

		JButton btnGetStatus = new JButton("Get Status");
		btnGetStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Byte b = deviceController
						.byteFromDevice(DeviceController.CONSOLE_STATUS);
				ftfStatusReceived.setText(b.toString());
			}
		});
		btnGetStatus.setBounds(10, 33, 130, 23);
		panelFromDevice.add(btnGetStatus);

		ftfStatusReceived = new JFormattedTextField();
		ftfStatusReceived.setBounds(150, 34, 40, 20);
		panelFromDevice.add(ftfStatusReceived);

		JButton btnGetByte = new JButton("Get Byte");
		btnGetByte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Byte byteCount = deviceController
						.byteFromDevice(DeviceController.CONSOLE_STATUS);
				if (byteCount == 0) {
					//txtLog.append("There is nothing to read");
					ftfStatusReceived.setText("0");
				}else{
					ftfStatusReceived.setText(byteCount.toString());
					Byte byteToCPU = deviceController
							.byteFromDevice(DeviceController.CONSOLE_IN);
					String stringToCPU = new String(new byte[] { byteToCPU });
					ftfGetByte.setText(String.format("%02X-%s | ", byteToCPU,
							stringToCPU));
				}
			}
		});
		btnGetByte.setBounds(10, 67, 130, 23);
		panelFromDevice.add(btnGetByte);

		ftfGetByte = new JFormattedTextField();
		ftfGetByte.setBounds(150, 65, 115, 20);
		panelFromDevice.add(ftfGetByte);

		JButton btnGetAllBytes = new JButton("Get All Bytes");
		btnGetAllBytes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Byte byteCount = deviceController
						.byteFromDevice(DeviceController.CONSOLE_STATUS);
				if (byteCount == 0) {
					//txtLog.append("There is nothing to read");
					ftfStatusReceived.setText("0");
				} else {
					StringBuilder sb = new StringBuilder();
					Byte byteToCPU;
					String stringToCPU;
					while ((deviceController
							.byteFromDevice(DeviceController.CONSOLE_STATUS)) >= 1) {
						byteToCPU = deviceController
								.byteFromDevice(DeviceController.CONSOLE_IN);
						stringToCPU = new String(new byte[] { byteToCPU });
						sb.append(String.format("%02X-%s | ", byteToCPU,
								stringToCPU));
					}
					ftfGetAllBytes.setText(sb.toString());
					;
				}
			}
		});
		btnGetAllBytes.setBounds(69, 102, 147, 23);
		panelFromDevice.add(btnGetAllBytes);

		ftfGetAllBytes = new JFormattedTextField();
		ftfGetAllBytes.setBounds(10, 148, 269, 20);
		panelFromDevice.add(ftfGetAllBytes);

		JPanel panelLog = new JPanel();
		panelLog.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(
				new Color(0, 0, 0), 1, true), "Log", TitledBorder.CENTER,
				TitledBorder.TOP, null, null), null));
		panelLog.setBounds(407, 35, 303, 514);
		frmTestDeviceController.getContentPane().add(panelLog);
		panelLog.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 22, 294, 488);
		panelLog.add(scrollPane);

		JTextArea txtLog = new JTextArea();
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

		JButton btnSetConnectionValues = new JButton("Set Connection Values");
		btnSetConnectionValues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deviceController.setSerialConnection();
				lblTerminalSettings.setText(deviceController
						.getConnectionString());
			}
		});
		btnSetConnectionValues.setBounds(46, 258, 225, 23);
		frmTestDeviceController.getContentPane().add(btnSetConnectionValues);

		lblTerminalSettings = new JLabel("Not Connected");
		lblTerminalSettings.setHorizontalAlignment(SwingConstants.CENTER);
		lblTerminalSettings.setBounds(56, 292, 215, 23);
		frmTestDeviceController.getContentPane().add(lblTerminalSettings);
	}// initialize
}//
