package memoryDisplay;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;

import memory.Core;
import myComponents.Hex64KSpinner16;

import java.awt.Font;

import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MemorySaver extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private Hex64KSpinner16 firstAddress;
	private Hex64KSpinner16 lastAddress;

	private  Core core;

	public final static String FILE_LOCATION = ".";
	public final static String MEMORY = "Memory";
	public final static String MEMORY_SUFFIX = "mem";

	public final static int SIXTEEN = 16;

	/**
	 * Launch the application.
	 */
	
	public void show(Core core){
		this.core = core;
		setVisible(true);
	}//show
	

	private void saveMemory() {
		Path sourcePath = Paths.get(FILE_LOCATION, MEMORY);
		String fp = sourcePath.resolve(FILE_LOCATION).toString();
		JFileChooser chooser = new JFileChooser(fp);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Memory file", MEMORY_SUFFIX));
		chooser.setAcceptAllFileFilterUsed(false);

		int firstMemory = (int) firstAddress.getValue();
		int lastMemory = (int) lastAddress.getValue();

		if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			String destinationPath = chooser.getSelectedFile().getPath();
			destinationPath = stripSuffix(destinationPath) + "." + MEMORY_SUFFIX;

			try {
				FileWriter fileWriter = new FileWriter(destinationPath);
				BufferedWriter writer = new BufferedWriter(fileWriter);
				writer.write(generateMemoryDisplay(firstMemory, lastMemory));
				writer.close();
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}// try
		}// file was selected
	}// saveMemory

	private String stripSuffix(String fileName) {
		String result = fileName;
		int periodLocation = fileName.indexOf(".");
		if (periodLocation != -1) {// this selection has a suffix
			result = fileName.substring(0, periodLocation); // removed suffix
		}// inner if
		return result;
	}// stripSuffix

	private String generateMemoryDisplay(int memoryStart, int memoryEnd) {
		memoryStart = memoryStart & 0XFFF0; // start at xxx0
		memoryEnd = memoryEnd | 0X000F; // end at yyyF

		StringBuilder sb = new StringBuilder();
		char[] printables = new char[SIXTEEN];
		try {
			for (int currentMemory = memoryStart; currentMemory <= memoryEnd;) {
				for (int i = 0; i < SIXTEEN; i++) {
					printables[i] = ((core.read(currentMemory + i) >= 0X20) && core.read(currentMemory + i) <= 0X7F) ?
							(char) core.read(currentMemory + i) : '.';
				}// for - printables

				sb.append(String.format(
						"%04X: %02X %02X %02X %02X %02X %02X %02X %02X  %02X %02X %02X %02X %02X %02X %02X %02X ",
						currentMemory,
						core.read(currentMemory++), core.read(currentMemory++), core.read(currentMemory++),
						core.read(currentMemory++), core.read(currentMemory++), core.read(currentMemory++),
						core.read(currentMemory++), core.read(currentMemory++), core.read(currentMemory++),
						core.read(currentMemory++), core.read(currentMemory++), core.read(currentMemory++),
						core.read(currentMemory++), core.read(currentMemory++), core.read(currentMemory++),
						core.read(currentMemory++)
						));
				sb.append(printables);
				sb.append("\n");
			}// if - End of line

		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.printf("Bad memory location  in Memeory Save%n");
			// skip rest of display
		}// try

		return sb.toString();
	}// generateMemoryDisplay

	/**
	 * Create the dialog.
	 */
	public MemorySaver() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(DEFAULT_MODALITY_TYPE);

		setTitle("Save Memory To FIle");
		setBounds(100, 100, 426, 365);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblLastAddress = new JLabel("Last  Address");
			lblLastAddress.setHorizontalAlignment(SwingConstants.CENTER);
			lblLastAddress.setFont(new Font("Tahoma", Font.PLAIN, 20));
			lblLastAddress.setBounds(22, 144, 184, 50);
			contentPanel.add(lblLastAddress);
		}

		lastAddress = new Hex64KSpinner16();
		lastAddress.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lastAddress.setBounds(241, 143, 89, 50);
		contentPanel.add(lastAddress);

		firstAddress = new Hex64KSpinner16();
		firstAddress.setFont(new Font("Tahoma", Font.PLAIN, 25));
		firstAddress.setBounds(241, 58, 89, 50);
		contentPanel.add(firstAddress);

		JLabel lblFirstAddress = new JLabel("First Address");
		lblFirstAddress.setHorizontalAlignment(SwingConstants.CENTER);
		lblFirstAddress.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblFirstAddress.setBounds(22, 59, 184, 50);
		contentPanel.add(lblFirstAddress);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if ((int) firstAddress.getValue() >= (int) lastAddress.getValue()) {
							JOptionPane.showMessageDialog(null,
									"Last address needs to be greater than Starting address", "Save memory",
									JOptionPane.ERROR_MESSAGE);
							return;
						} else {
							saveMemory();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
