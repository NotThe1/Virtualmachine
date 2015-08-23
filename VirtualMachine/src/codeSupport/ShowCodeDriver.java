package codeSupport;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JButton;

import java.awt.GridBagConstraints;

import javax.swing.JSpinner;

import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class ShowCodeDriver {
	
	private static ShowCode showCode;

	private JFrame frame;
	private JSpinner spinner;
	private JButton btnTxtLog;
	private JScrollPane scrollPane;
	private JTextArea txtLog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ShowCodeDriver window = new ShowCodeDriver();
					window.frame.setVisible(true);
					showCode = new ShowCode();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

	/**
	 * Create the application.
	 */
	public ShowCodeDriver() {
		

	/**
	 * Initialize the contents of the frame.
	 */
	
		frame = new JFrame();
		frame.setBounds(100, 100, 429, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 100, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showCode = new ShowCode();
			}
		});
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.insets = new Insets(0, 0, 5, 5);
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = 0;
		frame.getContentPane().add(btnStart, gbc_btnStart);
		
		JButton btnPC = new JButton("Send");
		btnPC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showCode.setProgramCounter((int)spinner.getValue());
			}
		});
		
		btnTxtLog = new JButton("Text Log");
		btnTxtLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			txtLog.setVisible(!txtLog.isVisible());
			}
		});
		GridBagConstraints gbc_btnTxtLog = new GridBagConstraints();
		gbc_btnTxtLog.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_btnTxtLog.insets = new Insets(0, 0, 5, 5);
		gbc_btnTxtLog.gridx = 1;
		gbc_btnTxtLog.gridy = 1;
		frame.getContentPane().add(btnTxtLog, gbc_btnTxtLog);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 4;
		gbc_scrollPane.gridy = 2;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);
		
		txtLog = new JTextArea();
		scrollPane.setViewportView(txtLog);
		GridBagConstraints gbc_btnPC = new GridBagConstraints();
		gbc_btnPC.insets = new Insets(0, 0, 0, 5);
		gbc_btnPC.gridx = 1;
		gbc_btnPC.gridy = 3;
		frame.getContentPane().add(btnPC, gbc_btnPC);
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(259), null, null, new Integer(1)));
		spinner.setPreferredSize(new Dimension(100, 20));
		spinner.setMinimumSize(new Dimension(100, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 2;
		gbc_spinner.gridy = 3;
		frame.getContentPane().add(spinner, gbc_spinner);
	}

}
