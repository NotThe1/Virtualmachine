package memoryDisplay;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import java.awt.Font;

import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;

import java.awt.Color;

import javax.swing.JScrollPane;

import java.awt.Dimension;

import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import memory.Core;
import myComponents.Hex64KSpinner16;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ShowCoreMemory extends JDialog implements PropertyChangeListener, Runnable {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JTextArea txtTop;
	private JTextArea txtBottom;
	private int lineHeight;
	private Document doc;
	private StringBuilder lineBuilder;
//	private int maxMemory;
	private HashMap<Integer, Byte> changeStagingArea;

	private MaskFormatter format16HexDigits;
	private Core core;

	@Override
	public void run() {
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);

	}// run

	private String getDisplayForValues(int firstLocation) {
		int startLocation = firstLocation & 0XFFF0;
		lineBuilder.setLength(0);

		lineBuilder.append(String.format(
				"%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X",
				core.readForIO(startLocation++), core.readForIO(startLocation++), core.readForIO(startLocation++),
				core.readForIO(startLocation++), core.readForIO(startLocation++), core.readForIO(startLocation++),
				core.readForIO(startLocation++), core.readForIO(startLocation++), core.readForIO(startLocation++),
				core.readForIO(startLocation++), core.readForIO(startLocation++), core.readForIO(startLocation++),
				core.readForIO(startLocation++), core.readForIO(startLocation++), core.readForIO(startLocation++),
				core.readForIO(startLocation++)
				));
		return lineBuilder.toString();
	}// getDisplayForValues

//	private int lineNumberToStartingLocation(int lineNumber) {
//		return lineNumber * SIXTEEN;
//	}// lineNumberToStartingLocation

//	private int locationToLineNumber(int location) {
//		int loc = location & 0XFFF0;
//		return loc / SIXTEEN;
//	}// locationToLineNumber

//	private int startDocIndex(int lineNumber) {
//		return lineNumber * LINE_SIZE;
//	}// startDocIndex

	public void refresh() {
		MemoryDocumentLoader mdl = new MemoryDocumentLoader(core, doc, 0, 4095);
		Thread mdlThread0 = new Thread(mdl);
		mdlThread0.start();
	}// refresh

	private void openEditPane(JTextArea txtArea) {
		// Object source = me.getSource();
		int displayLine = 0;
		try {
			displayLine = txtArea.getLineOfOffset(txtArea.getCaretPosition());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}// try
		int targetAddress = displayLine * SIXTEEN;
		spinnerEdit.setValue(targetAddress);
		ftfEdit.setText(getDisplayForValues(targetAddress));
		panelEdit.setVisible(true);
	}// openEditPane

	private void displayEditValues() {
		ftfEdit.setText(getDisplayForValues((int) spinnerEdit.getValue()));
		changeStagingArea.clear();
		btnCommit.setEnabled(false);
		btnCommit.setForeground(Color.BLACK);

	}// displayEditValues

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		if (pce.getOldValue() == pce.getNewValue()) {
			return; // nothing going on
		}//
		String sourceName = ((Component) pce.getSource()).getName();

		switch (sourceName) {
		case "ftfEdit":
			changeStagingArea.clear();
			Scanner scanner = new Scanner((String) ftfEdit.getText()); // ftfEdit.getValue
			int baseAddress = (int) spinnerEdit.getValue();

			int value;
			for (int i = 0; i < SIXTEEN; i++) {
				value = Integer.valueOf(scanner.next(), 16);
				changeStagingArea.put(baseAddress + i, (byte) value);
			}// for

			scanner.close();
			btnCommit.setEnabled(true);
			btnCommit.setForeground(Color.RED);
			break;
		default:
		}// switch

	}// propertyChange

	private void appInit() {
		changeStagingArea = new HashMap<Integer, Byte>();

//		maxMemory = core.getSize() - 1;
//		maxLineNumber = locationToLineNumber(maxMemory);
		doc = new PlainDocument();

		txtTop.setDocument(doc);
		txtBottom.setDocument(doc);
		FontMetrics fontMetrics = getFontMetrics(txtTop.getFont());
		lineHeight = fontMetrics.getHeight();
		lineBuilder = new StringBuilder();
		refresh();

	}// appInit
		// -----------------------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	public ShowCoreMemory(Core core) {
		setTitle("Main Memory");
		this.core = core;
		try {
			format16HexDigits = new MaskFormatter("HH HH HH HH HH HH HH HH  HH HH HH HH HH HH HH HH");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		setBounds(100, 100, 703, 777);
		getContentPane().setLayout(new BorderLayout(0, 0));
		contentPanel.setBorder(new LineBorder(Color.ORANGE, 3));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setDividerSize(10);
			splitPane.setOneTouchExpandable(true);
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			contentPanel.add(splitPane, BorderLayout.CENTER);
			{
				JPanel panelTop = new JPanel();
				splitPane.setLeftComponent(panelTop);
				panelTop.setLayout(new BorderLayout(0, 0));

				JPanel panelTop1 = new JPanel();
				panelTop1.setBorder(new LineBorder(Color.BLUE, 4, true));
				panelTop.add(panelTop1, BorderLayout.CENTER);
				panelTop1.setLayout(new BorderLayout(0, 0));

				JPanel panelAddressTop = new JPanel();
				panelTop1.add(panelAddressTop, BorderLayout.NORTH);

				Hex64KSpinner16 spinnerTop = new Hex64KSpinner16();
				spinnerTop.setName("spinnerTop");
				spinnerTop.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent ce) {
						String name = ((Component) ce.getSource()).getName();
						if ("spinnerTop".equals(name)) {
							scrollPaneTop.getVerticalScrollBar().setValue(
									((int) spinnerTop.getValue() / SIXTEEN) * lineHeight);
						}// if
					}// stateChanged
				});
				spinnerTop.setFont(new Font("Courier New", Font.PLAIN, 20));
				panelAddressTop.add(spinnerTop);

				scrollPaneTop = new JScrollPane();
				panelTop1.add(scrollPaneTop);

				txtTop = new JTextArea();
				txtTop.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent me) {
						if (me.getClickCount() >= 2) {
							openEditPane(txtTop);
							panelEdit.setVisible(true);
						}// if
					}// mouseClicked
				});
				txtTop.setEditable(false);
				txtTop.setFont(new Font("Courier New", Font.PLAIN, 15));
				txtTop.setRows(10);
				txtTop.setColumns(45);
				scrollPaneTop.setViewportView(txtTop);
			}
			{
				JPanel panelBottom = new JPanel();
				splitPane.setRightComponent(panelBottom);
				panelBottom.setLayout(new BorderLayout(0, 0));

				JPanel panelBottom1 = new JPanel();
				panelBottom1.setBorder(new LineBorder(Color.BLUE, 4, true));
				panelBottom.add(panelBottom1, BorderLayout.CENTER);
				panelBottom1.setLayout(new BorderLayout(0, 0));

				JPanel panelAddressBottom = new JPanel();
				panelBottom1.add(panelAddressBottom, BorderLayout.NORTH);

				Hex64KSpinner16 spinnerBottom = new Hex64KSpinner16();
				spinnerBottom.setName("spinnerBottom");
				spinnerBottom.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent ce) {
						String name = ((Component) ce.getSource()).getName();
						if ("spinnerBottom".equals(name)) {
							scrollPaneBottom.getVerticalScrollBar().setValue(
									((int) spinnerBottom.getValue() / SIXTEEN) * lineHeight);
						}// if
					}// stateChanged
				});

				spinnerBottom.setFont(new Font("Courier New", Font.PLAIN, 20));
				panelAddressBottom.add(spinnerBottom);

				scrollPaneBottom = new JScrollPane();
				panelBottom1.add(scrollPaneBottom);

				txtBottom = new JTextArea();
				txtBottom.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent me) {
						if (me.getClickCount() >= 2) {
							openEditPane(txtBottom);
							panelEdit.setVisible(true);
						}// if
					}// mouseClicked
				});

				txtBottom.setEditable(false);
				txtBottom.setFont(new Font("Courier New", Font.PLAIN, 15));
				// panelBottom1.add(txtBottom, BorderLayout.CENTER);
				txtBottom.setRows(10);
				txtBottom.setColumns(45);
				scrollPaneBottom.setViewportView(txtBottom);

			}
			splitPane.setDividerLocation(300);
		}

		panelEdit = new JPanel();
		panelEdit.setVisible(false);
		panelEdit.setMaximumSize(new Dimension(200, 60));
		panelEdit.setBorder(new LineBorder(Color.GREEN, 4, true));
		contentPanel.add(panelEdit, BorderLayout.SOUTH);
		GridBagLayout gbl_panelEdit = new GridBagLayout();
		gbl_panelEdit.columnWidths = new int[] { 23, 75, 492, 60, 59 };
		gbl_panelEdit.rowHeights = new int[] { 23, 31, 30 };
		gbl_panelEdit.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelEdit.rowWeights = new double[] { 0.0, 1.0, 0.0 };
		panelEdit.setLayout(gbl_panelEdit);

		JLabel label = new JLabel("00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F");
		label.setFont(new Font("Courier New", Font.PLAIN, 16));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.NORTHWEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 2;
		gbc_label.gridy = 0;
		panelEdit.add(label, gbc_label);

		spinnerEdit = new Hex64KSpinner16();
		spinnerEdit.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent ce) {
				String name = ((Component) ce.getSource()).getName();
				if ("spinnerEdit".equals(name) & panelEdit.isVisible()) {
					displayEditValues();
				}// if
			}

		});
		spinnerEdit.setName("spinnerEdit");
		spinnerEdit.setFont(new Font("Courier New", Font.PLAIN, 16));
		GridBagConstraints gbc_spinnerEdit = new GridBagConstraints();
		gbc_spinnerEdit.anchor = GridBagConstraints.WEST;
		gbc_spinnerEdit.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerEdit.gridx = 1;
		gbc_spinnerEdit.gridy = 1;
		panelEdit.add(spinnerEdit, gbc_spinnerEdit);

		ftfEdit = new JFormattedTextField(format16HexDigits);
		ftfEdit.setText("");
		ftfEdit.setName("ftfEdit");
		ftfEdit.setActionCommand("ftfEdit");
		ftfEdit.addPropertyChangeListener("value", this);
		// ftfEdit.setText("01 23 45 67 89 AB CD EF  FE DC BA 98 76 54 32 10");
		ftfEdit.setHorizontalAlignment(SwingConstants.LEFT);
		ftfEdit.setFont(new Font("Courier New", Font.BOLD, 16));
		ftfEdit.setColumns(49);
		label.setLabelFor(ftfEdit);
		GridBagConstraints gbc_ftfEdit = new GridBagConstraints();
		gbc_ftfEdit.fill = GridBagConstraints.BOTH;
		gbc_ftfEdit.anchor = GridBagConstraints.NORTHWEST;
		gbc_ftfEdit.gridwidth = 2;
		gbc_ftfEdit.insets = new Insets(0, 0, 5, 0);
		gbc_ftfEdit.gridx = 2;
		gbc_ftfEdit.gridy = 1;
		panelEdit.add(ftfEdit, gbc_ftfEdit);

		btnCommit = new JButton("Commit Edit");
		btnCommit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Set<Integer> locations = changeStagingArea.keySet();
				if (locations.isEmpty()) {
					return; // nothing to update
				}// if

				for (int location : locations) {
					byte value = changeStagingArea.get(location);
					// mm[location] = value;
					core.write(location, value);
				}//

				int lineNumber = (int) spinnerEdit.getValue() / SIXTEEN;
				MemoryDocumentLoader mdl = new MemoryDocumentLoader(core, doc, lineNumber, 1);
				Thread mdlThread0 = new Thread(mdl);
				mdlThread0.start();

				btnCommit.setEnabled(false);
				changeStagingArea.clear();
				btnCommit.setForeground(Color.BLACK);

			}
		});
		btnCommit.setEnabled(false);
		btnCommit.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_btnCommit = new GridBagConstraints();
		gbc_btnCommit.insets = new Insets(0, 0, 0, 5);
		gbc_btnCommit.gridx = 2;
		gbc_btnCommit.gridy = 2;
		panelEdit.add(btnCommit, gbc_btnCommit);
		btnCommit.setActionCommand("OK");

		JButton btnCloseEdit = new JButton("Close");
		btnCloseEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnCommit.setEnabled(false);
				changeStagingArea.clear();
				btnCommit.setForeground(Color.BLACK);
				panelEdit.setVisible(false);
			}
		});
		btnCloseEdit.setHorizontalAlignment(SwingConstants.LEFT);
		btnCloseEdit.setActionCommand("OK");
		GridBagConstraints gbc_btnCloseEdit = new GridBagConstraints();
		gbc_btnCloseEdit.gridx = 3;
		gbc_btnCloseEdit.gridy = 2;
		panelEdit.add(btnCloseEdit, gbc_btnCloseEdit);
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			{
				JButton btnOk = new JButton("OK");
				btnOk.setVisible(false);
				btnOk.setHorizontalAlignment(SwingConstants.RIGHT);
				btnOk.setActionCommand("OK");
				buttonPane.add(btnOk);
				getRootPane().setDefaultButton(btnOk);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.setVisible(false);
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						// TODO Cancel
					}
				});
				btnCancel.setHorizontalAlignment(SwingConstants.RIGHT);
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}
		appInit();
	}

	private static final int SIXTEEN = 16; // or 0X10;
//	private static final int LINE_SIZE = 67;
	private JPanel panelEdit;
	private Hex64KSpinner16 spinnerEdit;
	private JButton btnCommit;
	private JFormattedTextField ftfEdit;
	private JScrollPane scrollPaneTop;
	private JScrollPane scrollPaneBottom;

}
